package analyses;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathArrays.OrderDirection;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;
import org.moeaframework.util.tree.For;

import com.sun.org.apache.xpath.internal.operations.Bool;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.geometry.LSGeometryCalc;
import calculators.performance.ClimbCalc;
import calculators.performance.DescentCalc;
import calculators.performance.FlightManeuveringEnvelopeCalc;
import calculators.performance.LandingCalc;
import calculators.performance.LandingNoiseTrajectoryCalc;
import calculators.performance.MissionProfileCalc;
import calculators.performance.PayloadRangeCalcMissionProfile;
import calculators.performance.PerformanceCalcUtils;
import calculators.performance.SpecificRangeCalc;
import calculators.performance.TakeOffCalc;
import calculators.performance.TakeOffNoiseTrajectoryCalc;
import calculators.performance.ThrustCalc;
import calculators.performance.customdata.CeilingMap;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.DragThrustIntersectionMap;
import calculators.performance.customdata.FlightEnvelopeMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.SpecificRangeMap;
import calculators.performance.customdata.ThrustMap;
import configuration.MyConfiguration;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import junit.framework.AssertionFailedError;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import sun.rmi.server.UnicastRef;
import writers.JPADStaticWriteUtils;

public class ACPerformanceManager {

	/*
	 *******************************************************************************
	 * @author Vittorio Trifari
	 *******************************************************************************
	 */
	
	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private IACPerformanceManager _thePerformanceInterface;

	//------------------------------------------------------------------------------
	// OUTPUT DATA
	//..............................................................................
	// Take-Off
	private Map<Double, TakeOffCalc> _theTakeOffCalculatorMap;
	private Map<Double, Amount<Length>> _takeOffDistanceAEOMap;
	private Map<Double, Amount<Length>> _takeOffDistanceFAR25Map;
	private Map<Double, Amount<Length>> _balancedFieldLengthMap;
	private Map<Double, Amount<Length>> _groundRollDistanceTakeOffMap;
	private Map<Double, Amount<Length>> _rotationDistanceTakeOffMap;
	private Map<Double, Amount<Length>> _airborneDistanceTakeOffMap;
	private Map<Double, Amount<Velocity>> _vStallTakeOffMap;
	private Map<Double, Amount<Velocity>> _vMCMap;
	private Map<Double, Amount<Velocity>>_vRotationMap;
	private Map<Double, Amount<Velocity>> _vLiftOffMap;
	private Map<Double, Amount<Velocity>>_v1Map;
	private Map<Double, Amount<Velocity>> _v2Map;
	private Map<Double, Amount<Duration>> _takeOffDurationMap;
	private Map<Double, double[]> _thrustMomentOEIMap;
	private Map<Double, double[]> _yawingMomentOEIMap;
	private Map<Double, Amount<Mass>>_takeOffFuelMap;
	private Map<Double, Amount<Mass>>_takeOffNOxEmissionsMap;
	private Map<Double, Amount<Mass>>_takeOffCOEmissionsMap;
	private Map<Double, Amount<Mass>>_takeOffHCEmissionsMap;
	private Map<Double, Amount<Mass>>_takeOffSootEmissionsMap;
	private Map<Double, Amount<Mass>>_takeOffCO2EmissionsMap;
	private Map<Double, Amount<Mass>>_takeOffSOxEmissionsMap;
	private Map<Double, Amount<Mass>>_takeOffH2OEmissionsMap;
	//..............................................................................
	// Climb
	private Map<Double, ClimbCalc> _theClimbCalculatorMap;
	private Map<Double, List<RCMap>> _rcAEOMap;
	private Map<Double, List<RCMap>> _rcOEIMap;
	private Map<Double, CeilingMap> _ceilingAEOMap;
	private Map<Double, CeilingMap> _ceilingOEIMap;
	private Map<Double, List<DragMap>> _dragListAEOMap;
	private Map<Double, List<ThrustMap>> _thrustListAEOMap;
	private Map<Double, List<DragMap>> _dragListOEIMap;
	private Map<Double, List<ThrustMap>> _thrustListOEIMap;
	private Map<Double, Map<String, List<Double>>> _efficiencyAltitudeAEOMap;
	private Map<Double, Map<String, List<Double>>> _efficiencyAltitudeOEIMap;
	
	private Map<Double, Amount<Length>> _absoluteCeilingAEOMap;
	private Map<Double, Amount<Length>> _serviceCeilingAEOMap;
	private Map<Double, Amount<Duration>> _minimumClimbTimeAEOMap;
	private Map<Double, Amount<Duration>> _climbTimeAtSpecificClimbSpeedAEOMap;
	private Map<Double, Amount<Mass>> _fuelUsedDuringClimbMap;
	private Map<Double, Amount<Mass>> _emissionsNOxDuringClimbMap;
	private Map<Double, Amount<Mass>> _emissionsCODuringClimbMap;
	private Map<Double, Amount<Mass>> _emissionsHCDuringClimbMap;
	private Map<Double, Amount<Mass>> _emissionsSootDuringClimbMap;
	private Map<Double, Amount<Mass>> _emissionsCO2DuringClimbMap;
	private Map<Double, Amount<Mass>> _emissionsSOxDuringClimbMap;
	private Map<Double, Amount<Mass>> _emissionsH2ODuringClimbMap;
	
	private Map<Double, Amount<Length>> _absoluteCeilingOEIMap;
	private Map<Double, Amount<Length>> _serviceCeilingOEIMap;
	//..............................................................................
	// Cruise
	private Map<Double, List<DragMap>> _dragListAltitudeParameterizationMap;
	private Map<Double, List<ThrustMap>> _thrustListAltitudeParameterizationMap;
	private Map<Double, List<DragMap>> _dragListWeightParameterizationMap;
	private Map<Double, List<ThrustMap>> _thrustListWeightParameterizationMap;
	private Map<Double, List<Amount<Force>>> _weightListCruiseMap;
	
	private Map<Double, List<DragThrustIntersectionMap>> _intersectionListMap;
	private Map<Double, List<FlightEnvelopeMap>> _cruiseEnvelopeListMap;
	
	private Map<Double, Map<String, List<Double>>> _efficiencyAltitudeMap;
	private Map<Double, Map<String, List<Double>>> _efficiencyWeightMap;
	
	private Map<Double, List<SpecificRangeMap>> _specificRangeMap;
	
	private Map<Double, Amount<Velocity>> _maxSpeesTASAtCruiseAltitudeMap;
	private Map<Double, Amount<Velocity>> _minSpeesTASAtCruiseAltitudeMap;
	private Map<Double, Amount<Velocity>> _maxSpeesCASAtCruiseAltitudeMap;
	private Map<Double, Amount<Velocity>> _minSpeesCASAtCruiseAltitudeMap;
	private Map<Double, Double> _maxMachAtCruiseAltitudeMap;
	private Map<Double, Double> _minMachAtCruiseAltitudeMap;
	private Map<Double, Double> _efficiencyAtCruiseAltitudeAndMachMap;
	private Map<Double, Amount<Force>> _thrustAtCruiseAltitudeAndMachMap;
	private Map<Double, Amount<Force>> _dragAtCruiseAltitudeAndMachMap;
	private Map<Double, Amount<Power>> _powerAvailableAtCruiseAltitudeAndMachMap;
	private Map<Double, Amount<Power>> _powerNeededAtCruiseAltitudeAndMachMap;
	//..............................................................................
	// Descent
	private Map<Double, DescentCalc> _theDescentCalculatorMap;
	private Map<Double, List<Amount<Length>>> _descentLengthsMap;
	private Map<Double, List<Amount<Duration>>> _descentTimesMap;
	private Map<Double, List<Amount<Angle>>> _descentAnglesMap;
	private Map<Double, Amount<Length>> _totalDescentLengthMap;
	private Map<Double, Amount<Duration>> _totalDescentTimeMap;
	private Map<Double, Amount<Mass>> _totalDescentFuelUsedMap;
	private Map<Double, Amount<Mass>> _totalDescentEmissionsNOxMap;
	private Map<Double, Amount<Mass>> _totalDescentEmissionsCOMap;
	private Map<Double, Amount<Mass>> _totalDescentEmissionsHCMap;
	private Map<Double, Amount<Mass>> _totalDescentEmissionsSootMap;
	private Map<Double, Amount<Mass>> _totalDescentEmissionsCO2Map;
	private Map<Double, Amount<Mass>> _totalDescentEmissionsSOxMap;
	private Map<Double, Amount<Mass>> _totalDescentEmissionsH2OMap;
	//..............................................................................
	// Landing
	private Map<Double, LandingCalc> _theLandingCalculatorMap;
	private Map<Double, Amount<Length>> _landingDistanceMap;
	private Map<Double, Amount<Length>> _landingDistanceFAR25Map;
	private Map<Double, Amount<Length>> _totalDistanceMap;
	private Map<Double, Amount<Length>> _groundRollDistanceLandingMap;
	private Map<Double, Amount<Length>> _flareDistanceLandingMap;
	private Map<Double, Amount<Length>> _airborneDistanceLandingMap;
	private Map<Double, Amount<Velocity>> _vStallLandingMap;
	private Map<Double, Amount<Velocity>> _vApproachMap;
	private Map<Double, Amount<Velocity>> _vFlareMap;
	private Map<Double, Amount<Velocity>> _vTouchDownMap;
	private Map<Double, Amount<Duration>> _landingDurationMap;
	private Map<Double, Amount<Duration>> _totalDurationMap;
	//..............................................................................
	// Payload-Range
	private Map<Double, PayloadRangeCalcMissionProfile> _thePayloadRangeCalculatorMap;
	
	private Map<Double, Amount<Length>> _rangeAtMaxPayloadMap;
	private Map<Double, Amount<Length>> _rangeAtDesignPayloadMap;
	private Map<Double, Amount<Length>> _rangeAtMaxFuelMap;	
	private Map<Double, Amount<Length>> _rangeAtZeroPayloadMap;
	private Map<Double, Amount<Mass>> _takeOffMassAtZeroPayloadMap;
	private Map<Double, Amount<Mass>> _maxPayloadMap;
	private Map<Double, Amount<Mass>> _designPayloadMap;
	private Map<Double, Amount<Mass>> _payloadAtMaxFuelMap;
	private Map<Double, Integer> _passengersNumberAtMaxPayloadMap;
	private Map<Double, Integer> _passengersNumberAtDesignPayloadMap;
	private Map<Double, Integer> _passengersNumberAtMaxFuelMap;
	private Map<Double, Amount<Mass>> _requiredMassAtMaxPayloadMap;
	private Map<Double, Amount<Mass>> _requiredMassAtDesignPayloadMap;
	
	private Map<Double, List<Amount<Length>>> _rangeArrayMap;
	private Map<Double, List<Double>> _payloadArrayMap;
	
	private Map<Double, double[][]> _rangeMatrixMap;
	private Map<Double, double[][]> _payloadMatrixMap;
	//..............................................................................
	// Maneuvering and Gust Flight Envelope 
	private Map<Double, FlightManeuveringEnvelopeCalc> _theEnvelopeCalculatorMap;
	private Map<Double, Amount<Velocity>> _stallSpeedFullFlapMap;
	private Map<Double, Amount<Velocity>> _stallSpeedCleanMap;
	private Map<Double, Amount<Velocity>> _stallSpeedInvertedMap;
	private Map<Double, Amount<Velocity>> _maneuveringSpeedMap;
	private Map<Double, Amount<Velocity>> _maneuveringFlapSpeedMap;
	private Map<Double, Amount<Velocity>> _maneuveringSpeedInvertedMap;
	private Map<Double, Amount<Velocity>> _designFlapSpeedMap;
	private Map<Double, Double> _positiveLoadFactorManeuveringSpeedMap;
	private Map<Double, Double> _positiveLoadFactorCruisingSpeedMap;
	private Map<Double, Double> _positiveLoadFactorDiveSpeedMap;
	private Map<Double, Double> _positiveLoadFactorDesignFlapSpeedMap;
	private Map<Double, Double> _negativeLoadFactorManeuveringSpeedInvertedMap;
	private Map<Double, Double> _negativeLoadFactorCruisingSpeedMap;
	private Map<Double, Double> _negativeLoadFactorDiveSpeedMap;
	private Map<Double, Double> _positiveLoadFactorManeuveringSpeedWithGustMap;
	private Map<Double, Double> _positiveLoadFactorCruisingSpeedWithGustMap;
	private Map<Double, Double> _positiveLoadFactorDiveSpeedWithGustMap;
	private Map<Double, Double> _positiveLoadFactorDesignFlapSpeedWithGustMap;
	private Map<Double, Double> _negativeLoadFactorManeuveringSpeedInvertedWithGustMap;
	private Map<Double, Double> _negativeLoadFactorCruisingSpeedWithGustMap;
	private Map<Double, Double> _negativeLoadFactorDiveSpeedWithGustMap;
	private Map<Double, Double> _negativeLoadFactorDesignFlapSpeedWithGustMap;
	
	//..............................................................................
	// Mission profile
	private Map<Double, MissionProfileCalc> _theMissionProfileCalculatorMap;
	private Map<Double, List<Amount<Length>>> _altitudeListMap;
	private Map<Double, List<Amount<Length>>> _rangeListMap;
	private Map<Double, List<Amount<Duration>>> _timeListMap;
	private Map<Double, List<Amount<Mass>>> _fuelUsedListMap;
	private Map<Double, List<Amount<Mass>>> _massListMap;
	private Map<Double, List<Amount<Velocity>>> _speedTASMissionListMap;
	private Map<Double, List<Amount<Velocity>>> _speedCASMissionListMap;
	private Map<Double, List<Double>> _machMissionListMap;
	private Map<Double, List<Double>> _liftingCoefficientMissionListMap;
	private Map<Double, List<Double>> _dragCoefficientMissionListMap;
	private Map<Double, List<Double>> _efficiencyMissionListMap;
	private Map<Double, List<Amount<Force>>> _thrustMissionListMap;
	private Map<Double, List<Amount<Force>>> _dragMissionListMap;
	private Map<Double, List<Amount<Velocity>>> _rateOfClimbMissionListMap;
	private Map<Double, List<Amount<Angle>>> _climbAngleMissionListMap;
	private Map<Double, List<Double>> _fuelFlowMissionListMap;
	private Map<Double, List<Double>>_sfcMissionListMap;
	private Map<Double, List<Double>>_throttleMissionListMap;
	
	private Map<Double, Amount<Mass>> _initialFuelMassMap;
	private Map<Double, Amount<Mass>> _totalFuelUsedMap;
	private Map<Double, Amount<Mass>> _blockFuelMap;
	private Map<Double, Amount<Duration>> _totalMissionTimeMap;
	private Map<Double, Amount<Duration>> _blockTimeMap;
	private Map<Double, Amount<Mass>> _initialMissionMassMap;
	private Map<Double, Amount<Mass>> _endMissionMassMap;
	private Map<Double, Amount<Length>> _totalMissionRangeMap;
	
	//..............................................................................
	// Noise trajectories
	private Map<Double, TakeOffNoiseTrajectoryCalc> _theTakeOffNoiseTrajectoryCalculatorMap;
	private Map<Double, LandingNoiseTrajectoryCalc> _theLandingNoiseTrajectoryCalculatorMap;
	private Map<Double, List<Amount<Length>>> _certificationPointsLongitudinalDistanceMap;
	private Map<Double, List<Amount<Length>>> _certificationPointsAltitudeMap;
	private Map<Double, List<Amount<Velocity>>> _certificationPointsSpeedTASMap;
	private Map<Double, List<Amount<Velocity>>> _certificationPointsSpeedCASMap;
	private Map<Double, List<Amount<Angle>>> _certificationPointsGammaMap;
	private Map<Double, List<Amount<Angle>>> _certificationPointsAlphaMap;
	private Map<Double, List<Amount<Angle>>> _certificationPointsThetaMap;
	private Map<Double, List<Amount<Force>>> _certificationPointsThrustMap;
	
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		//..............................................................................
		// Take-Off
		this._theTakeOffCalculatorMap = new HashMap<>();
		this._takeOffDistanceAEOMap = new HashMap<>();
		this._balancedFieldLengthMap = new HashMap<>();
		this._takeOffDistanceFAR25Map = new HashMap<>();
		this._groundRollDistanceTakeOffMap = new HashMap<>();
		this._rotationDistanceTakeOffMap = new HashMap<>();
		this._airborneDistanceTakeOffMap = new HashMap<>();
		this._vStallTakeOffMap = new HashMap<>();
		this._vMCMap = new HashMap<>();
		this._vRotationMap = new HashMap<>();
		this._vLiftOffMap = new HashMap<>();
		this._v1Map = new HashMap<>();
		this._v2Map = new HashMap<>();
		this._takeOffDurationMap = new HashMap<>();
		this._thrustMomentOEIMap = new HashMap<>();
		this._yawingMomentOEIMap = new HashMap<>();
		this._takeOffFuelMap = new HashMap<>();
		this._takeOffNOxEmissionsMap = new HashMap<>();
		this._takeOffCOEmissionsMap= new HashMap<>();
		this._takeOffHCEmissionsMap = new HashMap<>();
		this._takeOffSootEmissionsMap = new HashMap<>();
		this._takeOffCO2EmissionsMap = new HashMap<>();
		this._takeOffSOxEmissionsMap = new HashMap<>();
		this._takeOffH2OEmissionsMap = new HashMap<>();
		//..............................................................................
		// Climb
		this._theClimbCalculatorMap = new HashMap<>();
		this._rcAEOMap = new HashMap<>();
		this._rcOEIMap = new HashMap<>();
		this._ceilingAEOMap = new HashMap<>();
		this._ceilingOEIMap = new HashMap<>();
		this._dragListAEOMap = new HashMap<>();
		this._thrustListAEOMap = new HashMap<>();
		this._dragListOEIMap = new HashMap<>();
		this._thrustListOEIMap = new HashMap<>();
		this._efficiencyAltitudeAEOMap = new HashMap<>();
		this._efficiencyAltitudeOEIMap = new HashMap<>();
		
		this._absoluteCeilingAEOMap = new HashMap<>();
		this._serviceCeilingAEOMap = new HashMap<>();
		this._minimumClimbTimeAEOMap = new HashMap<>();
		this._climbTimeAtSpecificClimbSpeedAEOMap = new HashMap<>();
		this._fuelUsedDuringClimbMap = new HashMap<>();
		this._emissionsNOxDuringClimbMap = new HashMap<>();
		this._emissionsCODuringClimbMap = new HashMap<>();
		this._emissionsHCDuringClimbMap = new HashMap<>();
		this._emissionsSootDuringClimbMap = new HashMap<>();
		this._emissionsCO2DuringClimbMap = new HashMap<>();
		this._emissionsSOxDuringClimbMap = new HashMap<>();
		this._emissionsH2ODuringClimbMap = new HashMap<>();
		
		this._absoluteCeilingOEIMap = new HashMap<>();
		this._serviceCeilingOEIMap = new HashMap<>();
		//..............................................................................
		// Cruise
		this._dragListAltitudeParameterizationMap = new HashMap<>();
		this._thrustListAltitudeParameterizationMap = new HashMap<>();
		this._dragListWeightParameterizationMap = new HashMap<>();
		this._thrustListWeightParameterizationMap = new HashMap<>();
		this._weightListCruiseMap = new HashMap<>();
		
		this._intersectionListMap = new HashMap<>();
		this._cruiseEnvelopeListMap = new HashMap<>();
		
		this._efficiencyAltitudeMap = new HashMap<>();
		this._efficiencyWeightMap = new HashMap<>();
		
		this._specificRangeMap = new HashMap<>();
		
		this._maxSpeesTASAtCruiseAltitudeMap = new HashMap<>();
		this._minSpeesTASAtCruiseAltitudeMap = new HashMap<>();
		this._maxSpeesCASAtCruiseAltitudeMap = new HashMap<>();
		this._minSpeesCASAtCruiseAltitudeMap = new HashMap<>();
		this._maxMachAtCruiseAltitudeMap = new HashMap<>();
		this._minMachAtCruiseAltitudeMap = new HashMap<>();
		this._efficiencyAtCruiseAltitudeAndMachMap = new HashMap<>();
		this._thrustAtCruiseAltitudeAndMachMap = new HashMap<>();
		this._dragAtCruiseAltitudeAndMachMap = new HashMap<>();
		this._powerAvailableAtCruiseAltitudeAndMachMap = new HashMap<>();
		this._powerNeededAtCruiseAltitudeAndMachMap = new HashMap<>();
		//.............................................................................
		// Descent
		this._theDescentCalculatorMap = new HashMap<>();
		this._descentLengthsMap = new HashMap<>();
		this._descentTimesMap = new HashMap<>();
		this._descentAnglesMap = new HashMap<>();
		this._totalDescentLengthMap = new HashMap<>();
		this._totalDescentTimeMap = new HashMap<>();
		this._totalDescentFuelUsedMap = new HashMap<>();
		this._totalDescentEmissionsNOxMap = new HashMap<>();
		this._totalDescentEmissionsCOMap = new HashMap<>();
		this._totalDescentEmissionsHCMap = new HashMap<>();
		this._totalDescentEmissionsSootMap = new HashMap<>();
		this._totalDescentEmissionsCO2Map = new HashMap<>();
		this._totalDescentEmissionsSOxMap = new HashMap<>();
		this._totalDescentEmissionsH2OMap = new HashMap<>();
		//..............................................................................
		// Landing
		this._theLandingCalculatorMap = new HashMap<>();
		this._landingDistanceMap = new HashMap<>();
		this._landingDistanceFAR25Map = new HashMap<>();
		this._totalDistanceMap = new HashMap<>();
		this._groundRollDistanceLandingMap = new HashMap<>();
		this._flareDistanceLandingMap = new HashMap<>();
		this._airborneDistanceLandingMap = new HashMap<>();
		this._vStallLandingMap = new HashMap<>();
		this._vApproachMap = new HashMap<>();
		this._vFlareMap = new HashMap<>();
		this._vTouchDownMap = new HashMap<>();
		this._landingDurationMap = new HashMap<>();
		this._totalDurationMap = new HashMap<>();
		//..............................................................................
		// Payload-Range
		this._thePayloadRangeCalculatorMap = new HashMap<>();
		
		this._rangeAtMaxPayloadMap = new HashMap<>();
		this._rangeAtDesignPayloadMap = new HashMap<>();
		this._rangeAtMaxFuelMap = new HashMap<>();	
		this._rangeAtZeroPayloadMap = new HashMap<>();
		this._takeOffMassAtZeroPayloadMap = new HashMap<>();
		this._maxPayloadMap = new HashMap<>();
		this._designPayloadMap = new HashMap<>();
		this._payloadAtMaxFuelMap = new HashMap<>();
		this._passengersNumberAtMaxPayloadMap = new HashMap<>();
		this._passengersNumberAtDesignPayloadMap = new HashMap<>();
		this._passengersNumberAtMaxFuelMap = new HashMap<>();
		this._requiredMassAtMaxPayloadMap = new HashMap<>();
		this._requiredMassAtDesignPayloadMap = new HashMap<>();
		
		this._rangeArrayMap = new HashMap<>();
		this._payloadArrayMap = new HashMap<>();
		
		this._rangeMatrixMap = new HashMap<>();
		this._payloadMatrixMap = new HashMap<>();
		//..............................................................................
		// Maneuvering and Gust Flight Envelope 
		this._theEnvelopeCalculatorMap = new HashMap<>();
		this._stallSpeedFullFlapMap = new HashMap<>();
		this._stallSpeedCleanMap = new HashMap<>();
		this._stallSpeedInvertedMap = new HashMap<>();
		this._maneuveringSpeedMap = new HashMap<>();
		this._maneuveringFlapSpeedMap = new HashMap<>();
		this._maneuveringSpeedInvertedMap = new HashMap<>();
		this._designFlapSpeedMap = new HashMap<>();
		this._positiveLoadFactorManeuveringSpeedMap = new HashMap<>();
		this._positiveLoadFactorCruisingSpeedMap = new HashMap<>();
		this._positiveLoadFactorDiveSpeedMap = new HashMap<>();
		this._positiveLoadFactorDesignFlapSpeedMap = new HashMap<>();
		this._negativeLoadFactorManeuveringSpeedInvertedMap = new HashMap<>();
		this._negativeLoadFactorCruisingSpeedMap = new HashMap<>();
		this._negativeLoadFactorDiveSpeedMap = new HashMap<>();
		this._positiveLoadFactorManeuveringSpeedWithGustMap = new HashMap<>();
		this._positiveLoadFactorCruisingSpeedWithGustMap = new HashMap<>();
		this._positiveLoadFactorDiveSpeedWithGustMap = new HashMap<>();
		this._positiveLoadFactorDesignFlapSpeedWithGustMap = new HashMap<>();
		this._negativeLoadFactorManeuveringSpeedInvertedWithGustMap = new HashMap<>();
		this._negativeLoadFactorCruisingSpeedWithGustMap = new HashMap<>();
		this._negativeLoadFactorDiveSpeedWithGustMap = new HashMap<>();
		this._negativeLoadFactorDesignFlapSpeedWithGustMap = new HashMap<>();
		//..............................................................................
		// Mission profile
		this._theMissionProfileCalculatorMap= new HashMap<>();
		this._altitudeListMap= new HashMap<>();
		this._rangeListMap= new HashMap<>();
		this._timeListMap= new HashMap<>();
		this._fuelUsedListMap= new HashMap<>();
		this._massListMap= new HashMap<>();
		this._speedTASMissionListMap= new HashMap<>();
		this._speedCASMissionListMap= new HashMap<>();
		this._machMissionListMap= new HashMap<>();
		this._liftingCoefficientMissionListMap= new HashMap<>();
		this._dragCoefficientMissionListMap= new HashMap<>();
		this._efficiencyMissionListMap= new HashMap<>();
		this._thrustMissionListMap= new HashMap<>();
		this._dragMissionListMap= new HashMap<>();
		this._rateOfClimbMissionListMap= new HashMap<>();
		this._climbAngleMissionListMap= new HashMap<>();
		this._fuelFlowMissionListMap= new HashMap<>();
		this._sfcMissionListMap= new HashMap<>();
		this._throttleMissionListMap = new HashMap<>();
		
		this._initialFuelMassMap= new HashMap<>();
		this._totalFuelUsedMap= new HashMap<>();
		this._blockFuelMap= new HashMap<>();
		this._totalMissionTimeMap= new HashMap<>();
		this._blockTimeMap= new HashMap<>();
		this._initialMissionMassMap= new HashMap<>();
		this._endMissionMassMap= new HashMap<>();
		this._totalMissionRangeMap= new HashMap<>();
		
		//..............................................................................
		// Mission profile
		this._theTakeOffNoiseTrajectoryCalculatorMap = new HashMap<>();
		this._theLandingNoiseTrajectoryCalculatorMap = new HashMap<>();
		this._certificationPointsLongitudinalDistanceMap = new HashMap<>();
		this._certificationPointsAltitudeMap = new HashMap<>();
		this._certificationPointsSpeedTASMap = new HashMap<>();
		this._certificationPointsSpeedCASMap = new HashMap<>();
		this._certificationPointsGammaMap = new HashMap<>();
		this._certificationPointsAlphaMap = new HashMap<>();
		this._certificationPointsThetaMap = new HashMap<>();
		this._certificationPointsThrustMap = new HashMap<>();
		
	}
	
	@SuppressWarnings({ "unchecked" })
	public static ACPerformanceManager importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions
			) throws IOException {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading performance analysis data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");

		//---------------------------------------------------------------------------------------
		// WEIGHTS FROM FILE INSTRUCTION
		Boolean readWeightsFromPreviousAnalysisFlag;
		String readWeightsFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@weights_from_previous_file");
		if(readWeightsFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readWeightsFromPreviousAnalysisFlag = Boolean.TRUE;
		else {
			readWeightsFromPreviousAnalysisFlag = Boolean.FALSE;
			if(theAircraft.getTheAnalysisManager().getTheAnalysisManagerInterface().isIterativeLoop() == true) {
				System.err.println("WARNING (IMPORT PERFORMANCE DATA): IF THE ITERATIVE LOOP FLAG IS 'TRUE', THE 'weights_from_previous_file' FLAG MUST BE TRUE. TERMINATING ...");
				System.exit(1);
			}
		}
		
		//---------------------------------------------------------------------------------------
		// AERODYNAMICS FROM FILE INSTRUCTION
		Boolean readAerodynamicsFromPreviousAnalysisFlag;
		String readAerodynamicsFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@aerodynamics_from_previous_file");
		if(readAerodynamicsFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readAerodynamicsFromPreviousAnalysisFlag = Boolean.TRUE;
		else {
			readAerodynamicsFromPreviousAnalysisFlag = Boolean.FALSE;
			if(theAircraft.getTheAnalysisManager().getTheAnalysisManagerInterface().isIterativeLoop() == true) {
				System.err.println("WARNING (IMPORT PERFORMANCE DATA): IF THE ITERATIVE LOOP FLAG IS 'TRUE', THE 'aerodynamics_from_previous_file' FLAG MUST BE TRUE. TERMINATING ...");
				System.exit(1);
			}
		}

		//===========================================================================================
		// READING WEIGHTS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the ACWeightsManager and ignores the assigned
		 * data inside the xml file.
		 * Otherwise it ignores the manager file and reads the input data from the xml.
		 */
		Amount<Mass> maximumTakeOffMass = null;
		Amount<Mass> operatingEmptyMass = null;
		Amount<Mass> maximumFuelMass = null;
		Amount<Mass> singlePassengerMass = null;
		
		if(readWeightsFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheWeights() != null) {

					//...............................................................
					// MAXIMUM TAKE-OFF MASS
					maximumTakeOffMass = theAircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(SI.KILOGRAM);

					//...............................................................
					// OPERATING EMPTY MASS
					operatingEmptyMass = theAircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().to(SI.KILOGRAM);

					//...............................................................
					// MAXIMUM FUEL MASS
					if(theAircraft.getFuelTank() != null) 
						maximumFuelMass = theAircraft.getFuelTank().getFuelMass().to(SI.KILOGRAM);
					else {
						System.err.println("WARNING!! THE FUEL TANK DOES NOT EXIST ... TERMINATING");
						System.exit(1);
					}
					
					//...............................................................
					// SINGLE PASSENGER MASS
					singlePassengerMass = theAircraft.getTheAnalysisManager().getTheWeights().getTheWeightsManagerInterface().getSinglePassengerMass().to(SI.KILOGRAM);
						
				}
				else {
					System.err.println("WARNING!! THE WEIGHTS ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
			}
		}
		else {
			//...............................................................
			// MAXIMUM TAKE-OFF MASS
			String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_take_off_mass");
			if(maximumTakeOffMassProperty != null)
				maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_take_off_mass");

			//...............................................................
			// OPERATING EMPTY MASS
			String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//performance/weights/operating_empty_mass");
			if(operatingEmptyMassProperty != null)
				operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/operating_empty_mass");

			//...............................................................
			// MAXIMUM FUEL MASS
			String maximumFuelMassProperty = reader.getXMLPropertyByPath("//performance/weights/maximum_fuel_mass");
			if(maximumFuelMassProperty != null)
				maximumFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/maximum_fuel_mass");

			//...............................................................
			// SINGLE PASSENGER MASS
			String singlePassengerMassProperty = reader.getXMLPropertyByPath("//performance/weights/single_passenger_mass");
			if(singlePassengerMassProperty != null)
				singlePassengerMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//performance/weights/single_passenger_mass");
		}

		//===========================================================================================
		// READING AERODYNAMICS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the ACAerodynamicAndStabilityManager and ignores the assigned
		 * data inside the xml file.
		 * Otherwise it ignores the manager file and reads the input data from the xml.
		 */
		Map<Double, MyInterpolatingFunction> tauRudderMap = new HashMap<>();

		List<Double> centerOfGravityList = new ArrayList<>();

		Map<Double,Double> cLmaxClean  = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaClean  = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaTakeOff  = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaLanding  = new HashMap<>();
		Map<Double,Double> cLmaxTakeOff  = new HashMap<>();
		Map<Double,Double> cLZeroTakeOff  = new HashMap<>();
		Map<Double,Double> cLmaxLanding  = new HashMap<>();
		Map<Double,Double> cLZeroLanding  = new HashMap<>();
		Map<Double,Double> deltaCD0TakeOff  = new HashMap<>();
		Map<Double,Double> deltaCD0Landing  = new HashMap<>();
		Map<Double,Double> deltaCD0LandingGears  = new HashMap<>();

		Map<Double, Double> cD0 = new HashMap<>();
		Map<Double,Double> oswaldCruise = new HashMap<>();
		Map<Double,Double> oswaldClimb = new HashMap<>();
		Map<Double,Double> oswaldTakeOff = new HashMap<>();
		Map<Double,Double> oswaldLanding = new HashMap<>();

		Map<Double,double[]> polarCLCruise  = new HashMap<>();
		Map<Double,double[]> polarCDCruise  = new HashMap<>();
		Map<Double,double[]> polarCLClimb  = new HashMap<>();
		Map<Double,double[]> polarCDClimb  = new HashMap<>();
		Map<Double,double[]> polarCLTakeOff  = new HashMap<>();
		Map<Double,double[]> polarCDTakeOff  = new HashMap<>();
		Map<Double,double[]> polarCLLanding  = new HashMap<>();
		Map<Double,double[]> polarCDLanding  = new HashMap<>();

		if(readAerodynamicsFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability() != null) {

					List<MyInterpolatingFunction> tauRudderFunctionList = new ArrayList<>();
					MyInterpolatingFunction tauRudderFunctionTakeOff = new MyInterpolatingFunction();
					MyInterpolatingFunction tauRudderFunctionClimb = new MyInterpolatingFunction();
					MyInterpolatingFunction tauRudderFunctionCruise = new MyInterpolatingFunction();
					MyInterpolatingFunction tauRudderFunctionLanding = new MyInterpolatingFunction();
					List<Double> xcgTakeOff = new ArrayList<>();
					List<Double> xcgClimb = new ArrayList<>();
					List<Double> xcgCruise = new ArrayList<>();
					List<Double> xcgLanding = new ArrayList<>();

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF) != null) { 
						xcgTakeOff = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.TAKE_OFF).getTheAerodynamicBuilderInterface().getXCGAircraft();
						tauRudderFunctionTakeOff = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.TAKE_OFF).getTheAerodynamicBuilderInterface().getTauRudderFunction();
					}
					else 
						System.err.println("WARNING!! THE TAKE-OFF AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB) != null) {
						xcgClimb = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CLIMB).getTheAerodynamicBuilderInterface().getXCGAircraft();
						tauRudderFunctionClimb = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CLIMB).getTheAerodynamicBuilderInterface().getTauRudderFunction();
					}
					else 
						System.err.println("WARNING!! THE CLIMB AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE) != null) {
						xcgCruise = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CRUISE).getTheAerodynamicBuilderInterface().getXCGAircraft();
						tauRudderFunctionCruise = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CRUISE).getTheAerodynamicBuilderInterface().getTauRudderFunction();
					}
					else 
						System.err.println("WARNING!! THE CRUISE AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					//.........................................................................................................
					if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING) != null) {
						xcgLanding = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.LANDING).getTheAerodynamicBuilderInterface().getXCGAircraft();
						tauRudderFunctionClimb = theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.LANDING).getTheAerodynamicBuilderInterface().getTauRudderFunction();
					}
					else 
						System.err.println("WARNING!! THE LANDING AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT!!");

					tauRudderFunctionList.add(tauRudderFunctionTakeOff);
					tauRudderFunctionList.add(tauRudderFunctionClimb);
					tauRudderFunctionList.add(tauRudderFunctionCruise);
					tauRudderFunctionList.add(tauRudderFunctionLanding);

					//---------------------------------------------------------------------------------------------------------
					// TEST: SAME Xcg FOR EACH CONDITION... 
					//---------------------------------------------------------------------------------------------------------
					try {
						assertEquals("WARNING! THE TAKE-OFF AND CLIMB XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgTakeOff, xcgClimb);
						assertEquals("WARNING! THE TAKE-OFF AND CRUISE XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgTakeOff, xcgCruise);
						assertEquals("WARNING! THE TAKE-OFF AND LANDING XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgTakeOff, xcgLanding);
						assertEquals("WARNING! THE CLIMB AND CRUISE XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgClimb, xcgCruise);
						assertEquals("WARNING! THE CLIMB AND LANDING XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgClimb, xcgLanding);
						assertEquals("WARNING! THE CRUISE AND LANDING XCG LISTS DO NOT CONTAINS THE SAME VALUES. IMPOSSIBLE TO CONTINUE WITH THE PERFORMANCE EVALUATION ...", xcgCruise, xcgLanding);
					}
					catch (AssertionFailedError e) {
						System.err.println("TERMINATING ...");
						System.exit(1);
					}

					centerOfGravityList.add(
							theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
							.get(ConditionEnum.TAKE_OFF).getTheAerodynamicBuilderInterface().getXCGAircraft().get(0)
							);

					centerOfGravityList.stream().forEach(xcg -> tauRudderMap.put(xcg, tauRudderFunctionList.get(centerOfGravityList.indexOf(xcg))));

					//---------------------------------------------------------------------------------------------------------
					// TAKE-OFF 
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionTakeOffMap = new HashMap<>();

					List<Integer> indexOfMaximumCLTrimmed = new ArrayList<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {

						liftCurveFunctionTakeOffMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						//.......................................
						// CUTTTING ALL CURVES UNTIL MAXIMUM CL
						//.......................................
						indexOfMaximumCLTrimmed.add(i, theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size());

						double [] firstDerivativeArray = 
								MyMathUtils.calculateArrayFirstDerivative2Point(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.TAKE_OFF).getAlphaBodyList()),
										MyArrayUtils.convertToDoublePrimitive(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
												.get(centerOfGravityList.get(i)))
										);

						for(int ii=0; ii<theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size()-1; 
								ii++) {
							if((theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii+1)
									<
									theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii)) ||
									firstDerivativeArray[ii] <0.01){
								indexOfMaximumCLTrimmed.set(i, ii);
								break;
							}
						}

						//-------------------------------------------------------------				
						liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).getAlphaBodyList().subList(0, indexOfMaximumCLTrimmed.get(i))
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						cLAlphaTakeOff.put(
								centerOfGravityList.get(i),
								Amount.valueOf(
										Math.atan(
												liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).value(1.0)
												- liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).value(0.0)
												),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						cLmaxTakeOff.put(
								centerOfGravityList.get(i), 
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF).
								getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cL -> cL).max().getAsDouble()
								);

						cLZeroTakeOff.put(
								centerOfGravityList.get(i), 
								liftCurveFunctionTakeOffMap.get(centerOfGravityList.get(i)).value(0.0)
								);

						polarCLTakeOff.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF)
										.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										.stream().mapToDouble(cL -> cL).toArray()
								);

						polarCDTakeOff.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF)
								.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cD -> cD).toArray()
								);
						
						deltaCD0LandingGears.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF)
								.get_deltaCDZeroLandingGear()
								);
						
						deltaCD0TakeOff.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.TAKE_OFF)
								.get_deltaCDZeroFlap()
								);
						
					}
					

					//---------------------------------------------------------------------------------------------------------
					// CLIMB 
					//---------------------------------------------------------------------------------------------------------
					indexOfMaximumCLTrimmed = new ArrayList<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {


						indexOfMaximumCLTrimmed.add(i, theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size());

						double [] firstDerivativeArray = 
								MyMathUtils.calculateArrayFirstDerivative2Point(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.CLIMB).getAlphaBodyList()),
										MyArrayUtils.convertToDoublePrimitive(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
												.get(centerOfGravityList.get(i)))
										);

						for(int ii=0; ii<theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size()-1; 
								ii++) {
							if((theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii+1)
									<
									theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.CLIMB).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii)) ||
									firstDerivativeArray[ii] <0.01){
								indexOfMaximumCLTrimmed.set(i, ii);
								break;
							}
						}

						polarCLClimb.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB)
								.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cL -> cL).toArray()
								);

						polarCDClimb.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CLIMB)
								.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cD -> cD).toArray()
								);
					}

					//---------------------------------------------------------------------------------------------------------
					// CRUISE 
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionCruiseMap = new HashMap<>();
					indexOfMaximumCLTrimmed = new ArrayList<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {


						indexOfMaximumCLTrimmed.add(i, theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size());

						double [] firstDerivativeArray = 
								MyMathUtils.calculateArrayFirstDerivative2Point(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.CRUISE).getAlphaBodyList()),
										MyArrayUtils.convertToDoublePrimitive(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
												.get(centerOfGravityList.get(i)))
										);

						for(int ii=0; ii<theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size()-1; 
								ii++) {
							if((theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii+1)
									<
									theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii)) ||
									firstDerivativeArray[ii] <0.01){
								indexOfMaximumCLTrimmed.set(i, ii);
								break;
							}
						}

						liftCurveFunctionCruiseMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						liftCurveFunctionCruiseMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).getAlphaBodyList().subList(0, indexOfMaximumCLTrimmed.get(i))
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						cLAlphaClean.put(
								centerOfGravityList.get(i),
								Amount.valueOf(
										Math.atan(
												liftCurveFunctionCruiseMap.get(centerOfGravityList.get(i)).value(1.0)
												- liftCurveFunctionCruiseMap.get(centerOfGravityList.get(i)).value(0.0)
												),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						cLmaxClean.put(
								centerOfGravityList.get(i), 
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE).
								getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cL -> cL).max().getAsDouble()
								);

						polarCLCruise.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE)
								.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cL -> cL).toArray()
								);

						polarCDCruise.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.CRUISE)
								.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cD -> cD).toArray()
								);
					}
					//---------------------------------------------------------------------------------------------------------
					// LANDING
					//---------------------------------------------------------------------------------------------------------
					Map<Double, MyInterpolatingFunction> liftCurveFunctionLandingMap = new HashMap<>();
					indexOfMaximumCLTrimmed = new ArrayList<>();

					for(int i=0; i<centerOfGravityList.size(); i++) {

						indexOfMaximumCLTrimmed.add(i, theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size());

						double [] firstDerivativeArray = 
								MyMathUtils.calculateArrayFirstDerivative2Point(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.LANDING).getAlphaBodyList()),
										MyArrayUtils.convertToDoublePrimitive(
												theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
												.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
												.get(centerOfGravityList.get(i)))
										);

						for(int ii=0; ii<theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
								.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
								.get(centerOfGravityList.get(i)).size()-1; 
								ii++) {
							if((theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii+1)
									<
									theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability()
									.get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
									.get(centerOfGravityList.get(i)).get(ii)) ||
									firstDerivativeArray[ii] <0.01){
								indexOfMaximumCLTrimmed.set(i, ii);
								break;
							}
						}

						liftCurveFunctionLandingMap.put(
								centerOfGravityList.get(i),
								new MyInterpolatingFunction()
								);

						liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).getAlphaBodyList().subList(0, indexOfMaximumCLTrimmed.get(i))
										),
								MyArrayUtils.convertToDoublePrimitive(
										theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).getTotalEquilibriumLiftCoefficient()
										.get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
										)
								);

						cLAlphaLanding.put(
								centerOfGravityList.get(i),
								Amount.valueOf(
										Math.atan(
												liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).value(1.0)
												- liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).value(0.0)
												),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						cLmaxLanding.put(
								centerOfGravityList.get(i), 
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING).
								getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i))
								.stream().mapToDouble(cL -> cL).max().getAsDouble()
								);

						cLZeroLanding.put(
								centerOfGravityList.get(i), 
								liftCurveFunctionLandingMap.get(centerOfGravityList.get(i)).value(0.0)
								);

						polarCLLanding.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING)
								.getTotalEquilibriumLiftCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cL -> cL).toArray()
								);

						polarCDLanding.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING)
								.getTotalEquilibriumDragCoefficient().get(centerOfGravityList.get(i)).subList(0, indexOfMaximumCLTrimmed.get(i))
								.stream().mapToDouble(cD -> cD).toArray()
								);
						
						deltaCD0Landing.put(
								centerOfGravityList.get(i),
								theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability().get(ConditionEnum.LANDING)
								.get_deltaCDZeroFlap()
								);
						
					}
				}
				else {
					System.err.println("WARNING!! THE AERODYNAMIC AND STABILITY ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
			}
		}
		else {

			Boolean parabolicDragPolarFlag = Boolean.FALSE;
			String paraboliDragPolarProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//@parabolicDragPolar");
			if(paraboliDragPolarProperty.equalsIgnoreCase("TRUE"))
				parabolicDragPolarFlag = Boolean.TRUE;
			else if(paraboliDragPolarProperty.equalsIgnoreCase("FALSE"))
				parabolicDragPolarFlag = Boolean.FALSE;
			else {
				System.err.println("ERROR : parabolicDragPolar HAS TO BE TRUE/FALSE !");
				System.exit(1);
			}

			//...............................................................
			// Xcg LIST
			List<String> xCGListProperty = MyXMLReaderUtils.getXMLPropertiesByPath(reader.getXmlDoc(), reader.getXpath(),"//xcg/@value");
			if(!xCGListProperty.isEmpty())
				xCGListProperty.stream().forEach(xcg -> centerOfGravityList.add(Double.valueOf(xcg)));

			//---------------------------------------------------------------
			// TAU RUDDER
			List<String> calculateTauRudderListProperty = MyXMLReaderUtils
					.getXMLPropertiesByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//tau_rudder_function/@calculate");

			if(!calculateTauRudderListProperty.isEmpty()) {

				List<MyInterpolatingFunction> tauRudderFunctionList = new ArrayList<>();

				for(int i=0; i<calculateTauRudderListProperty.size(); i++) {

					MyInterpolatingFunction tauRudderInterpolatingFunction = new MyInterpolatingFunction();

					if(calculateTauRudderListProperty.get(i).equalsIgnoreCase("TRUE")) {

						List<Double> tauRudderArray = new ArrayList<>();

						double[] deltaRudderArray = MyArrayUtils.linspace(-25, 25, 51);
						for(int j=0; j<deltaRudderArray.length; j++)
							tauRudderArray.add(
									LiftCalc.calculateTauIndexElevator(
											theAircraft.getVTail().getSymmetricFlaps().get(0).getMeanChordRatio(), 
											theAircraft.getVTail().getAspectRatio(), 
											theAircraft.getVTail().getHighLiftDatabaseReader(), 
											theAircraft.getVTail().getAeroDatabaseReader(), 
											Amount.valueOf(deltaRudderArray[j], NonSI.DEGREE_ANGLE)
											)
									);

						tauRudderInterpolatingFunction.interpolateLinear(
								deltaRudderArray, MyArrayUtils.convertToDoublePrimitive(tauRudderArray)
								);

						tauRudderFunctionList.add(tauRudderInterpolatingFunction);

					}
					else {

						List<Double> tauRudderFunction = new ArrayList<>();
						List<Amount<Angle>> tauRudderFunctionDeltaRudder = new ArrayList<>();

						String tauRudderFunctionProperty = reader.getXMLPropertyByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/tau_rudder_function/tau");
						if(tauRudderFunctionProperty != null)
							tauRudderFunction = reader.readArrayDoubleFromXML("//xcg[@value='" + centerOfGravityList.get(i) + "']/tau_rudder_function/tau"); 
						String tauRudderFunctionDeltaRudderProperty = reader.getXMLPropertyByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/tau_rudder_function/delta_rudder");
						if(tauRudderFunctionDeltaRudderProperty != null)
							tauRudderFunctionDeltaRudder = reader.readArrayofAmountFromXML("//xcg[@value='" + centerOfGravityList.get(i) + "']/tau_rudder_function/delta_rudder");

						if(tauRudderFunction.size() > 1)
							if(tauRudderFunction.size() != tauRudderFunctionDeltaRudder.size()) {
								System.err.println("TAU RUDDER ARRAY AND THE RELATED DELTA RUDDER ARRAY MUST HAVE THE SAME LENGTH !");
								System.exit(1);
							}
						if(tauRudderFunction.size() == 1) {
							tauRudderFunction.add(tauRudderFunction.get(0));
							tauRudderFunctionDeltaRudder.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
							tauRudderFunctionDeltaRudder.add(Amount.valueOf(360.0, NonSI.DEGREE_ANGLE));
						}

						tauRudderInterpolatingFunction.interpolateLinear(
								MyArrayUtils.convertListOfAmountTodoubleArray(
										tauRudderFunctionDeltaRudder.stream()
										.map(f -> f.to(NonSI.DEGREE_ANGLE))
										.collect(Collectors.toList())
										),
								MyArrayUtils.convertToDoublePrimitive(tauRudderFunction)
								);

						tauRudderFunctionList.add(tauRudderInterpolatingFunction);
					};

					tauRudderMap.put(centerOfGravityList.get(i), tauRudderFunctionList.get(i));
				}
			}

			//...............................................................
			// CLmax CLEAN
			List<String> cLmaxCleanProperty = reader.getXMLPropertiesByPath("//cLmax_clean_configuration");
			if(!cLmaxCleanProperty.isEmpty())
				for(int i=0; i<cLmaxCleanProperty.size(); i++)
					cLmaxClean.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxCleanProperty.get(i))
							);
			//...............................................................
			// CLalpha CLEAN
			List<String> cLAlphaCleanProperty = reader.getXMLPropertiesByPath("//cL_alpha_clean_configuration");
			List<Amount<?>> cLAlphaCleanList = new ArrayList<>();
			if(!cLAlphaCleanProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaCleanList.add(
							reader.getXMLAmountWithUnitByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_clean_configuration")
							);
			}
			for(int i=0; i<cLAlphaCleanList.size(); i++)
				cLAlphaClean.put(
						centerOfGravityList.get(i), 
						cLAlphaCleanList.get(i)
						);
				
			//...............................................................
			// CLalpha TAKE-OFF
			List<String> cLAlphaTakeOffProperty = reader.getXMLPropertiesByPath("//cL_alpha_take_off");
			List<Amount<?>> cLAlphaTakeOffList = new ArrayList<>();
			if(!cLAlphaTakeOffProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaTakeOffList.add(
							reader.getXMLAmountWithUnitByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_take_off")
							);
			}
			for(int i=0; i<cLAlphaTakeOffList.size(); i++)
				cLAlphaTakeOff.put(
						centerOfGravityList.get(i), 
						cLAlphaTakeOffList.get(i)
						);
			//...............................................................
			// CLalpha LANDING
			List<String> cLAlphaLandingProperty = reader.getXMLPropertiesByPath("//cL_alpha_landing");
			List<Amount<?>> cLAlphaLandingList = new ArrayList<>();
			if(!cLAlphaLandingProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaLandingList.add(
							reader.getXMLAmountWithUnitByPath("//xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_landing")
							);
			}
			for(int i=0; i<cLAlphaLandingList.size(); i++)
				cLAlphaLanding.put(
						centerOfGravityList.get(i), 
						cLAlphaLandingList.get(i)
						);
			//...............................................................
			// CLmax TAKE-OFF
			List<String> cLmaxTakeOffProperty = reader.getXMLPropertiesByPath("//cLmax_take_off_configuration");
			if(!cLmaxTakeOffProperty.isEmpty())
				for(int i=0; i<cLmaxTakeOffProperty.size(); i++)
					cLmaxTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxTakeOffProperty.get(i))
							);
			//...............................................................
			// CL0 TAKE-OFF
			List<String> cL0TakeOffProperty = reader.getXMLPropertiesByPath("//cL0_take_off_configuration");
			if(!cL0TakeOffProperty.isEmpty())
				for(int i=0; i<cL0TakeOffProperty.size(); i++)
					cLZeroTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(cL0TakeOffProperty.get(i))
							);
			//...............................................................
			// CLmax LANDING
			List<String> cLmaxLandingProperty = reader.getXMLPropertiesByPath("//cLmax_landing_configuration");
			if(!cLmaxLandingProperty.isEmpty())
				for(int i=0; i<cLmaxLandingProperty.size(); i++)
					cLmaxLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxLandingProperty.get(i))
							);
			//...............................................................
			// CL0 LANDING
			List<String> cL0LandingProperty = reader.getXMLPropertiesByPath("//cL0_landing_configuration");
			if(!cL0LandingProperty.isEmpty())
				for(int i=0; i<cL0LandingProperty.size(); i++)
					cLZeroLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(cL0LandingProperty.get(i))
							);
			
			//...............................................................
			// DeltaCD0 TAKE-OFF
			List<String> deltaCD0TakeOffProperty = reader.getXMLPropertiesByPath("//delta_CD0_flap_take_off");
			if(!deltaCD0TakeOffProperty.isEmpty())
				for(int i=0; i<deltaCD0TakeOffProperty.size(); i++)
					deltaCD0TakeOff.put(
						centerOfGravityList.get(i),
						Double.valueOf(deltaCD0TakeOffProperty.get(i))
						);
			//...............................................................
			// DeltaCD0 LANDING
			List<String> deltaCD0LandingProperty = reader.getXMLPropertiesByPath("//delta_CD0_flap_landing");
			if(!deltaCD0LandingProperty.isEmpty())
				for(int i=0; i<deltaCD0LandingProperty.size(); i++)
					deltaCD0Landing.put(
						centerOfGravityList.get(i),
						Double.valueOf(deltaCD0LandingProperty.get(i))
						);
			//...............................................................
			// DeltaCD0 LANDING GEARS
			List<String> deltaCD0LandingGearsProperty = reader.getXMLPropertiesByPath("//delta_CD0_landing_gears");
			if(!deltaCD0LandingGearsProperty.isEmpty())
				for(int i=0; i<deltaCD0LandingGearsProperty.size(); i++)
					deltaCD0LandingGears.put(
						centerOfGravityList.get(i),
						Double.valueOf(deltaCD0LandingGearsProperty.get(i))
						);
			
			if(parabolicDragPolarFlag == Boolean.FALSE) {
				
				//...............................................................
				// POLAR CURVE CRUISE
				Map<Double, List<String>> polarCLCruiseProperty = new HashMap<>();
				Map<Double, List<String>> polarCDCruiseProperty = new HashMap<>();
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							polarCLCruiseProperty.put(
								xcg,
								reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/cruise/polar_curve_CL")
								);
							polarCDCruiseProperty.put(
									xcg,
									reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/cruise/polar_curve_CD")
									);
						});
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							if(!polarCLCruiseProperty.get(xcg).isEmpty())
								polarCLCruise.put(
										xcg, 
										reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/cruise/polar_curve_CL")
										.stream().mapToDouble(cL -> cL).toArray()
										);
							if(!polarCDCruiseProperty.get(xcg).isEmpty())
								polarCDCruise.put(
										xcg, 
										reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/cruise/polar_curve_CD")
										.stream().mapToDouble(cD -> cD).toArray()
										);
						});

				//...............................................................
				// POLAR CURVE CLIMB
				Map<Double, List<String>> polarCLClimbProperty = new HashMap<>();
				Map<Double, List<String>> polarCDClimbProperty = new HashMap<>();
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							polarCLClimbProperty.put(
								xcg,
								reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/climb/polar_curve_CL")
								);
							polarCDClimbProperty.put(
									xcg,
									reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/climb/polar_curve_CD")
									);
						});
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							if(!polarCLClimbProperty.get(xcg).isEmpty())
								polarCLClimb.put(
										xcg, 
										reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/climb/polar_curve_CL")
										.stream().mapToDouble(cL -> cL).toArray()
										);
							if(!polarCDClimbProperty.get(xcg).isEmpty())
								polarCDClimb.put(
										xcg, 
										reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/climb/polar_curve_CD")
										.stream().mapToDouble(cD -> cD).toArray()
										);
						});
				
				//...............................................................
				// POLAR CURVE TAKE-OFF
				Map<Double, List<String>> polarCLTakeOffProperty = new HashMap<>();
				Map<Double, List<String>> polarCDTakeOffProperty = new HashMap<>();
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							polarCLTakeOffProperty.put(
								xcg,
								reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/take_off/polar_curve_CL")
								);
							polarCDTakeOffProperty.put(
									xcg,
									reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/take_off/polar_curve_CD")
									);
						});
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							if(!polarCLTakeOffProperty.get(xcg).isEmpty())
								polarCLTakeOff.put(
										xcg, 
										reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/take_off/polar_curve_CL")
										.stream().mapToDouble(cL -> cL).toArray()
										);
							if(!polarCDTakeOffProperty.get(xcg).isEmpty())
								polarCDTakeOff.put(
										xcg, 
										reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/take_off/polar_curve_CD")
										.stream().mapToDouble(cD -> cD).toArray()
										);
						});
				
				//...............................................................
				// POLAR CURVE LANDING
				Map<Double, List<String>> polarCLLandingProperty = new HashMap<>();
				Map<Double, List<String>> polarCDLandingProperty = new HashMap<>();
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							polarCLLandingProperty.put(
								xcg,
								reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/landing/polar_curve_CL")
								);
							polarCDLandingProperty.put(
									xcg,
									reader.getXMLPropertiesByPath("//xcg[@value='" + xcg + "']/polar_curves/landing/polar_curve_CD")
									);
						});
				
				centerOfGravityList.stream().forEach(
						xcg -> {
							if(!polarCLLandingProperty.get(xcg).isEmpty())
								polarCLLanding.put(
										xcg, 
										reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/landing/polar_curve_CL")
										.stream().mapToDouble(cL -> cL).toArray()
										);
							if(!polarCDLandingProperty.get(xcg).isEmpty())
								polarCDLanding.put(
										xcg, 
										reader.readArrayDoubleFromXML("//xcg[@value='" + xcg + "']/polar_curves/landing/polar_curve_CD")
										.stream().mapToDouble(cD -> cD).toArray()
										);
						});
				
			}
			else {
				
				//...............................................................
				// CD0
				List<String> cD0Property = reader.getXMLPropertiesByPath("//cD0");
				if(!cD0Property.isEmpty())
					for(int i=0; i<cD0Property.size(); i++)
						cD0.put(
							centerOfGravityList.get(i),
							Double.valueOf(cD0Property.get(i))
							);
				//...............................................................
				// OSWALD CRUISE
				List<String> oswladCruiseProperty = reader.getXMLPropertiesByPath("//oswald_cruise");
				if(!oswladCruiseProperty.isEmpty())
					for(int i=0; i<oswladCruiseProperty.size(); i++)
						oswaldCruise.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladCruiseProperty.get(i))
							);
				//...............................................................
				// OSWALD CLIMB
				List<String> oswladClimbProperty = reader.getXMLPropertiesByPath("//oswald_climb");
				if(!oswladClimbProperty.isEmpty())
					for(int i=0; i<oswladClimbProperty.size(); i++)
						oswaldClimb.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladClimbProperty.get(i))
							);
				//...............................................................
				// OSWALD TO
				List<String> oswladTOProperty = reader.getXMLPropertiesByPath("//oswald_take_off");
				if(!oswladTOProperty.isEmpty())
					for(int i=0; i<oswladTOProperty.size(); i++)
						oswaldTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladTOProperty.get(i))
							);
				//...............................................................
				// OSWALD LND
				List<String> oswladLNDProperty = reader.getXMLPropertiesByPath("//oswald_landing");
				if(!oswladLNDProperty.isEmpty())
					for(int i=0; i<oswladLNDProperty.size(); i++)
						oswaldLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladLNDProperty.get(i))
							);
					
				int numberOfPolarPoints = 50;

				centerOfGravityList.stream().forEach(xcg -> {

					//...............................................................
					// POLAR CURVE CRUISE
					polarCLCruise.put(
							xcg, 
							MyArrayUtils.linspace(-0.2, cLmaxClean.get(xcg), numberOfPolarPoints)
							);				
					polarCDCruise.put(
							xcg,
							new double[polarCLCruise.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE CLIMB
					polarCLClimb.put(
							xcg, 
							MyArrayUtils.linspace(-0.2, cLmaxClean.get(xcg), numberOfPolarPoints)
							);				
					polarCDClimb.put(
							xcg,
							new double[polarCLClimb.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE TAKE-OFF
					polarCLTakeOff.put(
							xcg, 
							MyArrayUtils.linspace(-0.2, cLmaxTakeOff.get(xcg), numberOfPolarPoints)
							);				
					polarCDTakeOff.put(
							xcg,
							new double[polarCLTakeOff.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE LANDING
					polarCLLanding.put(
							xcg, 
							MyArrayUtils.linspace(-0.2, cLmaxLanding.get(xcg), numberOfPolarPoints)
							);				
					polarCDLanding.put(
							xcg,
							new double[polarCLLanding.get(xcg).length]
							);

					//...............................................................
					// building the CD arrays...
					for(int i=0; i<numberOfPolarPoints; i++) {
						polarCDClimb.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg),
								polarCLClimb.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldClimb.get(xcg), 
								theOperatingConditions.getMachClimb(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLCruise.get(xcg)[i],
										theOperatingConditions.getMachClimb(),
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										theAircraft.getWing().getAirfoilList().get(0).getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getType()
										)
								);
						polarCDCruise.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg),
								polarCLCruise.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldCruise.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLCruise.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										theAircraft.getWing().getAirfoilList().get(0).getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getType()
										)
								);
						polarCDTakeOff.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg) + deltaCD0TakeOff.get(xcg) + deltaCD0LandingGears.get(xcg),
								polarCLTakeOff.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldTakeOff.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLTakeOff.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										theAircraft.getWing().getAirfoilList().get(0).getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getType()
										)
								);
						polarCDLanding.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg) + deltaCD0Landing.get(xcg) + deltaCD0LandingGears.get(xcg),
								polarCLLanding.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldLanding.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLLanding.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										theAircraft.getWing().getAirfoilList().get(0).getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getType()
										)
								);
					}
				});
			}
		}
		
		//===========================================================================================
		// READING TAKE-OFF AND LANDING DATA ...	

		// default values
		Amount<Velocity> windSpeed = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
		List<Double> muFunction = new ArrayList<>();
		List<Amount<Velocity>> muFunctionSpeed = new ArrayList<>();
		List<Double> muBrakeFunction = new ArrayList<>();
		List<Amount<Velocity>> muBrakeFunctionSpeed = new ArrayList<>();
		
		Amount<Duration> dtHold = Amount.valueOf(0.5, SI.SECOND);
		Amount<Angle> alphaGround = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Length> obstacleTakeOff = Amount.valueOf(35, NonSI.FOOT).to(SI.METER);
		double kRotation = 1.05;
		double alphaDotRotation = 3.0;
		double kCLmaxTakeOff = 0.9;
		double dragDueToEngineFailure = 0.0;
		double kAlphaDot = 0.04;
		
		double kLandingWeight = 0.97;
		Amount<Length> initialAltitudeLanding = Amount.valueOf(1500, NonSI.FOOT).to(SI.METER);
		Amount<Length> obstacleLanding = Amount.valueOf(50, NonSI.FOOT).to(SI.METER);
		Amount<Angle> approachAngle = Amount.valueOf(-3.0, NonSI.DEGREE_ANGLE);
		double kCLmaxLanding = 0.9;
		double kApproach = 1.3;
		double kFlare = 1.2;
		double kTouchDown = 1.15;
		Amount<Duration> freeRollDuration = Amount.valueOf(2.0, SI.SECOND);
		
		//...............................................................
		// dt HOLD
		String dtHoldProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/dt_hold");
		if(dtHoldProperty != null)
			dtHold = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/dt_hold");				
		
		//...............................................................
		// ALPHA GROUND
		String alphaGroundProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_ground");
		if(alphaGroundProperty != null)
			alphaGround = (Amount<Angle>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/alpha_ground");
		
		//...............................................................
		// WIND SPEED
		String windSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wind_speed_along_runway");
		if(windSpeedProperty != null)
			windSpeed = (Amount<Velocity>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/wind_speed_along_runway");
		
		//...............................................................
		// OBSTACLE TAKE-OFF
		String obstacleTakeOffProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_take_off");
		if(obstacleTakeOffProperty != null)
			obstacleTakeOff = reader.getXMLAmountLengthByPath("//performance/takeoff_landing/obstacle_take_off");
		
		//...............................................................
		// WHEELS FRICTION COEFFICIENT FUNCTION
		String wheelsFrictionCoefficientFunctionProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_function/friction_coefficient");
		if(wheelsFrictionCoefficientFunctionProperty != null)
			muFunction = reader.readArrayDoubleFromXML("//performance/takeoff_landing/wheels_friction_coefficient_function/friction_coefficient"); 
		String wheelsFrictionCoefficientFunctionSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_function/speed");
		if(wheelsFrictionCoefficientFunctionSpeedProperty != null)
			muFunctionSpeed = reader.readArrayofAmountFromXML("//performance/takeoff_landing/wheels_friction_coefficient_function/speed");
		
		if(muFunction.size() > 1)
			if(muFunction.size() != muFunctionSpeed.size())
			{
				System.err.println("FRICTION COEFFICIENT ARRAY AND THE RELATED SPEED ARRAY MUST HAVE THE SAME LENGTH !");
				System.exit(1);
			}
		if(muFunction.size() == 1) {
			muFunction.add(muFunction.get(0));
			muFunctionSpeed.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
			muFunctionSpeed.add(Amount.valueOf(10000.0, SI.METERS_PER_SECOND));
		}
		
		MyInterpolatingFunction muInterpolatingFunction = new MyInterpolatingFunction();
		muInterpolatingFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						muFunctionSpeed.stream()
						.map(f -> f.to(SI.METERS_PER_SECOND))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertToDoublePrimitive(muFunction)
				);
			
		//...............................................................
		// WHEELS FRICTION COEFFICIENT (WITH BRAKES) FUNCTION
		String wheelsFrictionCoefficientBrakesFunctionProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes_function/friction_coefficient_with_brakes");
		if(wheelsFrictionCoefficientBrakesFunctionProperty != null)
			muBrakeFunction = reader.readArrayDoubleFromXML("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes_function/friction_coefficient_with_brakes"); 
		String wheelsFrictionCoefficientBrakesFunctionSpeedProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes_function/speed");
		if(wheelsFrictionCoefficientBrakesFunctionSpeedProperty != null)
			muBrakeFunctionSpeed = reader.readArrayofAmountFromXML("//performance/takeoff_landing/wheels_friction_coefficient_with_brakes_function/speed");
		
		if(muBrakeFunction.size() > 1)
			if(muBrakeFunction.size() != muBrakeFunctionSpeed.size())
			{
				System.err.println("FRICTION COEFFICIENT (WITH BRAKES) ARRAY AND THE RELATED SPEED ARRAY MUST HAVE THE SAME LENGTH !");
				System.exit(1);
			}
		if(muBrakeFunction.size() == 1) {
			muBrakeFunction.add(muBrakeFunction.get(0));
			muBrakeFunctionSpeed.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
			muBrakeFunctionSpeed.add(Amount.valueOf(10000.0, SI.METERS_PER_SECOND));
		}
		
		MyInterpolatingFunction muBrakeInterpolatingFunction = new MyInterpolatingFunction();
		muBrakeInterpolatingFunction.interpolateLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						muBrakeFunctionSpeed.stream()
						.map(f -> f.to(SI.METERS_PER_SECOND))
						.collect(Collectors.toList())
						),
				MyArrayUtils.convertToDoublePrimitive(muBrakeFunction)
				);
		
		//...............................................................
		// K ROTATION
		String kRotationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_rotation");
		if(kRotationProperty != null)
			kRotation = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_rotation"));
		
		//...............................................................
		// ALPHA DOT ROTATION
		String alphaDotRotationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_dot_rotation");
		if(alphaDotRotationProperty != null)
			alphaDotRotation = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/alpha_dot_rotation"));
		
		//...............................................................
		// K CLmax TAKE-OFF
		String kCLmaxTakeOffProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_cLmax_take_off");
		if(kCLmaxTakeOffProperty != null)
			kCLmaxTakeOff = Double.valueOf(kCLmaxTakeOffProperty);
		
		//...............................................................
		// DRAG DUE TO ENGINE FAILURE
		String dragDueToEngineFailureProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/drag_due_to_engine_failure");
		if(dragDueToEngineFailureProperty != null)
			dragDueToEngineFailure = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/drag_due_to_engine_failure"));

		//...............................................................
		// K ALPHA DOT
		String kAlphaDotProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_alpha_dot");
		if(kAlphaDotProperty != null)
			kAlphaDot = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_alpha_dot"));

		//...............................................................
		// K LANDING WEIGHT
		String kLandingWeightProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_landing_weight");
		if(kLandingWeightProperty != null)
			kLandingWeight = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_landing_weight"));
		
		//...............................................................
		// OBSTACLE LANDING
		String obstacleLandingProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/obstacle_landing");
		if(obstacleLandingProperty != null)
			obstacleLanding = reader.getXMLAmountLengthByPath("//performance/takeoff_landing/obstacle_landing");		
		
		//...............................................................
		// INITIAL ALTITUDE LANDING
		String initialAltitudeLandingProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/initial_altitude_landing");
		if(initialAltitudeLandingProperty != null)
			initialAltitudeLanding = reader.getXMLAmountLengthByPath("//performance/takeoff_landing/initial_altitude_landing");
		
		//...............................................................
		// APPROACH ANGLE
		String approachAngleProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/approach_angle");
		if(approachAngleProperty != null)
			approachAngle = (Amount<Angle>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/approach_angle");		
		
		//...............................................................
		// K CLmax LANDING
		String kCLmaxLandingProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_cLmax_landing");
		if(kCLmaxLandingProperty != null)
			kCLmaxLanding = Double.valueOf(kCLmaxLandingProperty);
		
		//...............................................................
		// K APPROACH
		String kApproachProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_approach");
		if(kApproachProperty != null)
			kApproach = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_approach"));
		
		//...............................................................
		// K FLARE
		String kFlareProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_flare");
		if(kFlareProperty != null)
			kFlare = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_flare"));
		
		//...............................................................
		// K TOUCH DOWN
		String kTouchDownProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/k_touch_down");
		if(kTouchDownProperty != null)
			kTouchDown = Double.valueOf(reader.getXMLPropertyByPath("//performance/takeoff_landing/k_touch_down"));
		
		//...............................................................
		// FREE ROLL DURATION
		String freeRollDurationProperty = reader.getXMLPropertyByPath("//performance/takeoff_landing/free_roll_duration");
		if(freeRollDurationProperty != null)
			freeRollDuration = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/takeoff_landing/free_roll_duration");
		
		
		//===========================================================================================
		// READING CLIMB DATA ...	
		double kClimbWeightAEO = 1.0;
		double kClimbWeightOEI = 0.97;
		Amount<Velocity> climbSpeed = null;
		Amount<Length> initialClimbAltitude = null;
		Amount<Length> finalClimbAltitude = null;
		
		//...............................................................
		// K CLIMB WEIGHT AEO
		String kClimbWeightAEOProperty = reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_AEO");
		if(kClimbWeightAEOProperty != null)
			kClimbWeightAEO = Double.valueOf(reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_AEO"));
		
		//...............................................................
		// K CLIMB WEIGHT OEI
		String kClimbWeightOEIProperty = reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_OEI");
		if(kClimbWeightOEIProperty != null)
			kClimbWeightOEI = Double.valueOf(reader.getXMLPropertyByPath("//performance/climb/k_climb_weight_OEI"));
		
		//...............................................................
		// CLIMB SPEED
		String climbSpeedProperty = reader.getXMLPropertyByPath("//performance/climb/climb_speed_CAS");
		if(climbSpeedProperty != null)
			climbSpeed = reader.getXMLAmountWithUnitByPath("//performance/climb/climb_speed_CAS").to(SI.METERS_PER_SECOND);
		
		//...............................................................
		// INITIAL CLIMB ALTITUDE
		String initialClimbAltitudeProperty = reader.getXMLPropertyByPath("//performance/climb/initial_climb_altitude");
		if(initialClimbAltitudeProperty != null)
			initialClimbAltitude = reader.getXMLAmountLengthByPath("//performance/climb/initial_climb_altitude");
		
		//...............................................................
		// FINAL CLIMB ALTITUDE
		String finalClimbAltitudeProperty = reader.getXMLPropertyByPath("//performance/climb/final_climb_altitude");
		if(finalClimbAltitudeProperty != null)
			finalClimbAltitude = reader.getXMLAmountLengthByPath("//performance/climb/final_climb_altitude");
		
		//===========================================================================================
		// READING CRUISE DATA ...	
		List<Amount<Length>> altitudeListCruise = null;
		double kCruiseWeight = 0.97;
		
		//...............................................................
		// ALTITUDE LIST CRUISE
		List<String> altitudeListCruiseProperty = reader.getXMLPropertiesByPath("//performance/cruise/altitudes_array");
		if(!altitudeListCruiseProperty.isEmpty()) {
			altitudeListCruise = reader.readArrayofAmountFromXML("//performance/cruise/altitudes_array"); 
			if(altitudeListCruise.size() == 1) {
				System.err.println("WARNING!! THERE ARE TOO FEW CRUISE ALTITUDE (MIN: 2) ... ADDING CRUISE ALTITUDES TO THE LIST (THE SAME CHOSEN ALTITUDE WILL BE ADDED)");
				Amount<Length> selectedAltitude = altitudeListCruise.get(0).times(1.001);
				altitudeListCruise.add(selectedAltitude);
			}
		}
		//...............................................................
		// K CRUISE WEIGHT 
		String kCruiseWeightProperty = reader.getXMLPropertyByPath("//performance/cruise/k_cruise_weight");
		if(kCruiseWeightProperty != null)
			kCruiseWeight = Double.valueOf(reader.getXMLPropertyByPath("//performance/cruise/k_cruise_weight"));
		
		//===========================================================================================
		// READING DESCENT DATA ...
		Amount<Velocity> rateOfDescent = null;
		Amount<Velocity> speedDescentCAS = null;
		double kDescentWeight = 0.9;
		Amount<Length> initialDescentAltitude = null;
		Amount<Length> finalDescentAltitude = null;
		//...............................................................
		// RATE OF DESCENT 
		String rateOfDescentProperty = reader.getXMLPropertyByPath("//performance/descent/rate_of_descent");
		if(rateOfDescentProperty != null)
			rateOfDescent = (Amount<Velocity>) reader.getXMLAmountWithUnitByPath("//performance/descent/rate_of_descent");		
		//...............................................................
		// SPEED DESCENT CAS
		String speedDescentCASProperty = reader.getXMLPropertyByPath("//performance/descent/descent_speed_CAS");
		if(speedDescentCASProperty != null)
			speedDescentCAS = (Amount<Velocity>) reader.getXMLAmountWithUnitByPath("//performance/descent/descent_speed_CAS");
		//...............................................................
		// K DESCENT WEIGHT
		String kDescentWeightProperty = reader.getXMLPropertyByPath("//performance/descent/k_descent_weight");
		if(kDescentWeightProperty != null)
			kDescentWeight = Double.valueOf(reader.getXMLPropertyByPath("//performance/descent/k_descent_weight"));
		//...............................................................
		// INITIAL DESCENT ALTITUDE
		String initialDescentAltitudeProperty = reader.getXMLPropertyByPath("//performance/descent/initial_descent_altitude");
		if(initialDescentAltitudeProperty != null)
			initialDescentAltitude = reader.getXMLAmountLengthByPath("//performance/descent/initial_descent_altitude");
		
		//...............................................................
		// FINAL DESCENT ALTITUDE
		String finalDescentAltitudeProperty = reader.getXMLPropertyByPath("//performance/descent/final_descent_altitude");
		if(finalDescentAltitudeProperty != null)
			finalDescentAltitude = reader.getXMLAmountLengthByPath("//performance/descent/final_descent_altitude");
		
		//===========================================================================================
		// READING FLIGHT MANEUVERING AND GUST ENVELOPE DATA ...
		double cLmaxInverted = -1.0;
		//...............................................................
		// CL MAX INVERTED
		String cLmaxInvertedProperty = reader.getXMLPropertyByPath("//performance/flight_maneuvering_and_gust_envelope/cLmax_inverted");
		if(cLmaxInvertedProperty != null)
			cLmaxInverted = Double.valueOf(reader.getXMLPropertyByPath("//performance/flight_maneuvering_and_gust_envelope/cLmax_inverted"));
		
		//===========================================================================================
		// READING NOISE TRAJECTORIES DATA ...
		Amount<Length> takeOffNoiseTrajectoryXEndSimulation = Amount.valueOf(8000.0, SI.METER);
		Amount<Length> takeOffNoiseTrajectoryCutbackAltitude = Amount.valueOf(984.0, NonSI.FOOT);
		int takeOffNoiseTrajectoryNumberOfThrustSettingCutback = 3;
		Amount<Duration> takeOffNoiseTrajectoryLandingGearRetractionTimeInterval = Amount.valueOf(12.0, SI.SECOND);
		Amount<Duration> takeOffNoiseTrajectoryThrustReductionCutbackTimeInterval = Amount.valueOf(4.0, SI.SECOND);
		Amount<Length> landingNoiseTrajectoryInitialAltitude = Amount.valueOf(4000.0, NonSI.FOOT);
		Amount<Angle> landingNoiseTrajectoryTrajectoryAngle = Amount.valueOf(-3.0, NonSI.DEGREE_ANGLE);
		//...............................................................
		// TAKE-OFF GROUND DISTANCE END SIMULATION
		String takeOffNoiseTrajectoryXEndSimulationProperty = reader.getXMLPropertyByPath("//performance/noise_trajectories/take_off/ground_distance_end_simulation");
		if(takeOffNoiseTrajectoryXEndSimulationProperty != null)
			takeOffNoiseTrajectoryXEndSimulation = reader.getXMLAmountLengthByPath("//performance/noise_trajectories/take_off/ground_distance_end_simulation");
		//...............................................................
		// TAKE-OFF CUTBACK ALTITUDE
		String takeOffNoiseTrajectoryCutbackAltitudeProperty = reader.getXMLPropertyByPath("//performance/noise_trajectories/take_off/cutback_altitude");
		if(takeOffNoiseTrajectoryCutbackAltitudeProperty != null)
			takeOffNoiseTrajectoryCutbackAltitude = reader.getXMLAmountLengthByPath("//performance/noise_trajectories/take_off/cutback_altitude");
		//...............................................................
		// TAKE-OFF NUMBER OF THRUST SETTINGS CUTBACK
		String takeOffNoiseTrajectoryNumberOfThrustSettingCutbackProperty = reader.getXMLPropertyByPath("//performance/noise_trajectories/take_off/number_of_thrust_settings_cutback");
		if(takeOffNoiseTrajectoryNumberOfThrustSettingCutbackProperty != null)
			takeOffNoiseTrajectoryNumberOfThrustSettingCutback = Integer.valueOf(takeOffNoiseTrajectoryNumberOfThrustSettingCutbackProperty);
		//...............................................................
		// TAKE-OFF LANDING GEAR RETRACTION TIME INTERVAL
		String takeOffNoiseTrajectoryLandingGearRetractionTimeIntervalProperty = reader.getXMLPropertyByPath("//performance/noise_trajectories/take_off/dt_landing_gear_retraction");
		if(takeOffNoiseTrajectoryLandingGearRetractionTimeIntervalProperty != null)
			takeOffNoiseTrajectoryLandingGearRetractionTimeInterval = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/noise_trajectories/take_off/dt_landing_gear_retraction");
		//...............................................................
		// TAKE-OFF THRUST REDUCTION CUTBACK TIME INTERVAL
		String takeOffNoiseTrajectoryThrustReductionCutbackTimeIntervalProperty = reader.getXMLPropertyByPath("//performance/noise_trajectories/take_off/dt_thrust_cutback");
		if(takeOffNoiseTrajectoryThrustReductionCutbackTimeIntervalProperty != null)
			takeOffNoiseTrajectoryThrustReductionCutbackTimeInterval = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/noise_trajectories/take_off/dt_thrust_cutback");
		//...............................................................
		// LANDING INITIAL ALTITUDE
		String landingNoiseTrajectoryInitialAltitudeProperty = reader.getXMLPropertyByPath("//performance/noise_trajectories/landing/initial_altitude");
		if(landingNoiseTrajectoryInitialAltitudeProperty != null)
			landingNoiseTrajectoryInitialAltitude = reader.getXMLAmountLengthByPath("//performance/noise_trajectories/landing/initial_altitude");
		//...............................................................
		// LANDING TRAJECTORY ANGLE
		String landingNoiseTrajectoryTrajectoryAngleProperty = reader.getXMLPropertyByPath("//performance/noise_trajectories/landing/trajectory_angle");
		if(landingNoiseTrajectoryTrajectoryAngleProperty != null)
			landingNoiseTrajectoryTrajectoryAngle = reader.getXMLAmountAngleByPath("//performance/noise_trajectories/landing/trajectory_angle");
		
		//===========================================================================================
		// READING MISSION PROFILE DATA ...
		Amount<Length> missionRange = null;
		Amount<Length> alternateCruiseLength = Amount.valueOf(0.0, SI.METER);
		Amount<Length> alternateCruiseAltitude = Amount.valueOf(15.24, SI.METER);
		Amount<Duration> holdingDuration = Amount.valueOf(0.0, SI.SECOND);
		Amount<Length> holdingAltitude = Amount.valueOf(15.24, SI.METER);
		double holdingMachNumber = 0.01; // default value but != 0.0
		double fuelReserve = 0.0;
		
		//...............................................................
		// MISSION RANGE
		List<String> missionRangeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/mission_range");
		if(!missionRangeProperty.isEmpty()) {
			missionRange = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/mission_range"); 
		}
		//...............................................................
		// ALTERNATE CRUISE LENGTH
		List<String> alternateCruiseLengthProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/alternate_cruise_length");
		if(!alternateCruiseLengthProperty.isEmpty()) {
			alternateCruiseLength = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/alternate_cruise_length"); 
		}
		//...............................................................
		// ALTERNATE CRUISE ALTITUDE
		List<String> alternateCruiseAltitudeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/alternate_cruise_altitude");
		if(!alternateCruiseAltitudeProperty.isEmpty()) {
			alternateCruiseAltitude = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/alternate_cruise_altitude"); 
		}
		//...............................................................
		// HOLDING DURATION
		List<String> holdingDurationProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/holding_duration");
		if(!holdingDurationProperty.isEmpty()) {
			holdingDuration = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//performance/mission_profile_and_payload_range/holding_duration"); 
		}
		//...............................................................
		// HOLDING ALTITUDE
		List<String> holdingAltitudeProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/holding_altitude");
		if(!holdingAltitudeProperty.isEmpty()) {
			holdingAltitude = reader.getXMLAmountLengthByPath("//performance/mission_profile_and_payload_range/holding_altitude"); 
		}
		//...............................................................
		// HOLDING MACH NUMBER
		List<String> holdingMachNumberProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/holding_mach_number");
		if(!holdingMachNumberProperty.isEmpty()) {
			holdingMachNumber = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/holding_mach_number")); 
		}
		//...............................................................
		// FUEL RESERVE
		List<String> fuelReserveProperty = reader.getXMLPropertiesByPath("//performance/mission_profile_and_payload_range/fuel_reserve");
		if(!fuelReserveProperty.isEmpty()) {
			fuelReserve = Double.valueOf(reader.getXMLPropertyByPath("//performance/mission_profile_and_payload_range/fuel_reserve")); 
		}
		
		//---------------------------------------------------------------
		// READING CALIBRATION FACTORS ...
		//...............................................................
		// Thrust
		double takeOffCalibrationFactorThrust = 1.0;
		double aprCalibrationFactorThrust = 1.0;
		double climbCalibrationFactorThrust = 1.0;
		double continuousCalibrationFactorThrust = 1.0;
		double cruiseCalibrationFactorThrust = 1.0;
		double flightIdleCalibrationFactorThrust = 1.0;
		double groundIdleCalibrationFactorThrust = 1.0;
		
		String takeOffCalibrationFactorThrustProperty = reader.getXMLPropertyByPath("//calibrations/thrust/take_off_calibration_factor");
		if(takeOffCalibrationFactorThrustProperty != null) {
			takeOffCalibrationFactorThrust = Double.valueOf(takeOffCalibrationFactorThrustProperty); 
		}
		String aprCalibrationFactorThrustProperty = reader.getXMLPropertyByPath("//calibrations/thrust/apr_calibration_factor");
		if(aprCalibrationFactorThrustProperty != null) {
			aprCalibrationFactorThrust = Double.valueOf(aprCalibrationFactorThrustProperty); 
		}
		String climbCalibrationFactorThrustProperty = reader.getXMLPropertyByPath("//calibrations/thrust/climb_calibration_factor");
		if(climbCalibrationFactorThrustProperty != null) {
			climbCalibrationFactorThrust = Double.valueOf(climbCalibrationFactorThrustProperty); 
		}
		String continuousCalibrationFactorThrustProperty = reader.getXMLPropertyByPath("//calibrations/thrust/continuous_calibration_factor");
		if(continuousCalibrationFactorThrustProperty != null) {
			continuousCalibrationFactorThrust = Double.valueOf(continuousCalibrationFactorThrustProperty); 
		}
		String cruiseCalibrationFactorThrustProperty = reader.getXMLPropertyByPath("//calibrations/thrust/cruise_calibration_factor");
		if(cruiseCalibrationFactorThrustProperty != null) {
			cruiseCalibrationFactorThrust = Double.valueOf(cruiseCalibrationFactorThrustProperty); 
		}
		String flightIdleCalibrationFactorThrustProperty = reader.getXMLPropertyByPath("//calibrations/thrust/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorThrustProperty != null) {
			flightIdleCalibrationFactorThrust = Double.valueOf(flightIdleCalibrationFactorThrustProperty); 
		}
		String groundIdleCalibrationFactorThrustProperty = reader.getXMLPropertyByPath("//calibrations/thrust/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorThrustProperty != null) {
			groundIdleCalibrationFactorThrust = Double.valueOf(groundIdleCalibrationFactorThrustProperty); 
		}
		//...............................................................
		// SFC
		double takeOffCalibrationFactorSFC = 1.0;
		double aprCalibrationFactorSFC = 1.0;
		double climbCalibrationFactorSFC = 1.0;
		double continuousCalibrationFactorSFC = 1.0;
		double cruiseCalibrationFactorSFC = 1.0;
		double flightIdleCalibrationFactorSFC = 1.0;
		double groundIdleCalibrationFactorSFC = 1.0;
		
		String takeOffCalibrationFactorSFCProperty = reader.getXMLPropertyByPath("//calibrations/sfc/take_off_calibration_factor");
		if(takeOffCalibrationFactorSFCProperty != null) {
			takeOffCalibrationFactorSFC = Double.valueOf(takeOffCalibrationFactorSFCProperty); 
		}
		String aprCalibrationFactorSFCProperty = reader.getXMLPropertyByPath("//calibrations/sfc/apr_calibration_factor");
		if(aprCalibrationFactorSFCProperty != null) {
			aprCalibrationFactorSFC = Double.valueOf(aprCalibrationFactorSFCProperty); 
		}
		String climbCalibrationFactorSFCProperty = reader.getXMLPropertyByPath("//calibrations/sfc/climb_calibration_factor");
		if(climbCalibrationFactorSFCProperty != null) {
			climbCalibrationFactorSFC = Double.valueOf(climbCalibrationFactorSFCProperty); 
		}
		String continuousCalibrationFactorSFCProperty = reader.getXMLPropertyByPath("//calibrations/sfc/continuous_calibration_factor");
		if(continuousCalibrationFactorSFCProperty != null) {
			continuousCalibrationFactorSFC = Double.valueOf(continuousCalibrationFactorSFCProperty); 
		}
		String cruiseCalibrationFactorSFCProperty = reader.getXMLPropertyByPath("//calibrations/sfc/cruise_calibration_factor");
		if(cruiseCalibrationFactorSFCProperty != null) {
			cruiseCalibrationFactorSFC = Double.valueOf(cruiseCalibrationFactorSFCProperty); 
		}
		String flightIdleCalibrationFactorSFCProperty = reader.getXMLPropertyByPath("//calibrations/sfc/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorSFCProperty != null) {
			flightIdleCalibrationFactorSFC = Double.valueOf(flightIdleCalibrationFactorSFCProperty); 
		}
		String groundIdleCalibrationFactorSFCProperty = reader.getXMLPropertyByPath("//calibrations/sfc/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorSFCProperty != null) {
			groundIdleCalibrationFactorSFC = Double.valueOf(groundIdleCalibrationFactorSFCProperty); 
		}
		
		//...............................................................
		// EmissionIndexNOx
		double takeOffCalibrationFactorEmissionIndexNOx = 1.0;
		double aprCalibrationFactorEmissionIndexNOx = 1.0;
		double climbCalibrationFactorEmissionIndexNOx = 1.0;
		double continuousCalibrationFactorEmissionIndexNOx = 1.0;
		double cruiseCalibrationFactorEmissionIndexNOx = 1.0;
		double flightIdleCalibrationFactorEmissionIndexNOx = 1.0;
		double groundIdleCalibrationFactorEmissionIndexNOx = 1.0;
		
		String takeOffCalibrationFactorEmissionIndexNOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_NOx/take_off_calibration_factor");
		if(takeOffCalibrationFactorEmissionIndexNOxProperty != null) {
			takeOffCalibrationFactorEmissionIndexNOx = Double.valueOf(takeOffCalibrationFactorEmissionIndexNOxProperty); 
		}
		String aprCalibrationFactorEmissionIndexNOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_NOx/apr_calibration_factor");
		if(aprCalibrationFactorEmissionIndexNOxProperty != null) {
			aprCalibrationFactorEmissionIndexNOx = Double.valueOf(aprCalibrationFactorEmissionIndexNOxProperty); 
		}
		String climbCalibrationFactorEmissionIndexNOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_NOx/climb_calibration_factor");
		if(climbCalibrationFactorEmissionIndexNOxProperty != null) {
			climbCalibrationFactorEmissionIndexNOx = Double.valueOf(climbCalibrationFactorEmissionIndexNOxProperty); 
		}
		String continuousCalibrationFactorEmissionIndexNOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_NOx/continuous_calibration_factor");
		if(continuousCalibrationFactorEmissionIndexNOxProperty != null) {
			continuousCalibrationFactorEmissionIndexNOx = Double.valueOf(continuousCalibrationFactorEmissionIndexNOxProperty); 
		}
		String cruiseCalibrationFactorEmissionIndexNOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_NOx/cruise_calibration_factor");
		if(cruiseCalibrationFactorEmissionIndexNOxProperty != null) {
			cruiseCalibrationFactorEmissionIndexNOx = Double.valueOf(cruiseCalibrationFactorEmissionIndexNOxProperty); 
		}
		String flightIdleCalibrationFactorEmissionIndexNOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_NOx/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorEmissionIndexNOxProperty != null) {
			flightIdleCalibrationFactorEmissionIndexNOx = Double.valueOf(flightIdleCalibrationFactorEmissionIndexNOxProperty); 
		}
		String groundIdleCalibrationFactorEmissionIndexNOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_NOx/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorEmissionIndexNOxProperty != null) {
			groundIdleCalibrationFactorEmissionIndexNOx = Double.valueOf(groundIdleCalibrationFactorEmissionIndexNOxProperty); 
		}
		
		//...............................................................
		// EmissionIndexCO
		double takeOffCalibrationFactorEmissionIndexCO = 1.0;
		double aprCalibrationFactorEmissionIndexCO = 1.0;
		double climbCalibrationFactorEmissionIndexCO = 1.0;
		double continuousCalibrationFactorEmissionIndexCO = 1.0;
		double cruiseCalibrationFactorEmissionIndexCO = 1.0;
		double flightIdleCalibrationFactorEmissionIndexCO = 1.0;
		double groundIdleCalibrationFactorEmissionIndexCO = 1.0;
		
		String takeOffCalibrationFactorEmissionIndexCOProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_CO/take_off_calibration_factor");
		if(takeOffCalibrationFactorEmissionIndexCOProperty != null) {
			takeOffCalibrationFactorEmissionIndexCO = Double.valueOf(takeOffCalibrationFactorEmissionIndexCOProperty); 
		}
		String aprCalibrationFactorEmissionIndexCOProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_CO/apr_calibration_factor");
		if(aprCalibrationFactorEmissionIndexCOProperty != null) {
			aprCalibrationFactorEmissionIndexCO = Double.valueOf(aprCalibrationFactorEmissionIndexCOProperty); 
		}
		String climbCalibrationFactorEmissionIndexCOProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_CO/climb_calibration_factor");
		if(climbCalibrationFactorEmissionIndexCOProperty != null) {
			climbCalibrationFactorEmissionIndexCO = Double.valueOf(climbCalibrationFactorEmissionIndexCOProperty); 
		}
		String continuousCalibrationFactorEmissionIndexCOProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_CO/continuous_calibration_factor");
		if(continuousCalibrationFactorEmissionIndexCOProperty != null) {
			continuousCalibrationFactorEmissionIndexCO = Double.valueOf(continuousCalibrationFactorEmissionIndexCOProperty); 
		}
		String cruiseCalibrationFactorEmissionIndexCOProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_CO/cruise_calibration_factor");
		if(cruiseCalibrationFactorEmissionIndexCOProperty != null) {
			cruiseCalibrationFactorEmissionIndexCO = Double.valueOf(cruiseCalibrationFactorEmissionIndexCOProperty); 
		}
		String flightIdleCalibrationFactorEmissionIndexCOProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_CO/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorEmissionIndexCOProperty != null) {
			flightIdleCalibrationFactorEmissionIndexCO = Double.valueOf(flightIdleCalibrationFactorEmissionIndexCOProperty); 
		}
		String groundIdleCalibrationFactorEmissionIndexCOProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_CO/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorEmissionIndexCOProperty != null) {
			groundIdleCalibrationFactorEmissionIndexCO = Double.valueOf(groundIdleCalibrationFactorEmissionIndexCOProperty); 
		}
		
		//...............................................................
		// EmissionIndexHC
		double takeOffCalibrationFactorEmissionIndexHC = 1.0;
		double aprCalibrationFactorEmissionIndexHC = 1.0;
		double climbCalibrationFactorEmissionIndexHC = 1.0;
		double continuousCalibrationFactorEmissionIndexHC = 1.0;
		double cruiseCalibrationFactorEmissionIndexHC = 1.0;
		double flightIdleCalibrationFactorEmissionIndexHC = 1.0;
		double groundIdleCalibrationFactorEmissionIndexHC = 1.0;
		
		String takeOffCalibrationFactorEmissionIndexHCProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_HC/take_off_calibration_factor");
		if(takeOffCalibrationFactorEmissionIndexHCProperty != null) {
			takeOffCalibrationFactorEmissionIndexHC = Double.valueOf(takeOffCalibrationFactorEmissionIndexHCProperty); 
		}
		String aprCalibrationFactorEmissionIndexHCProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_HC/apr_calibration_factor");
		if(aprCalibrationFactorEmissionIndexHCProperty != null) {
			aprCalibrationFactorEmissionIndexHC = Double.valueOf(aprCalibrationFactorEmissionIndexHCProperty); 
		}
		String climbCalibrationFactorEmissionIndexHCProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_HC/climb_calibration_factor");
		if(climbCalibrationFactorEmissionIndexHCProperty != null) {
			climbCalibrationFactorEmissionIndexHC = Double.valueOf(climbCalibrationFactorEmissionIndexHCProperty); 
		}
		String continuousCalibrationFactorEmissionIndexHCProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_HC/continuous_calibration_factor");
		if(continuousCalibrationFactorEmissionIndexHCProperty != null) {
			continuousCalibrationFactorEmissionIndexHC = Double.valueOf(continuousCalibrationFactorEmissionIndexHCProperty); 
		}
		String cruiseCalibrationFactorEmissionIndexHCProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_HC/cruise_calibration_factor");
		if(cruiseCalibrationFactorEmissionIndexHCProperty != null) {
			cruiseCalibrationFactorEmissionIndexHC = Double.valueOf(cruiseCalibrationFactorEmissionIndexHCProperty); 
		}
		String flightIdleCalibrationFactorEmissionIndexHCProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_HC/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorEmissionIndexHCProperty != null) {
			flightIdleCalibrationFactorEmissionIndexHC = Double.valueOf(flightIdleCalibrationFactorEmissionIndexHCProperty); 
		}
		String groundIdleCalibrationFactorEmissionIndexHCProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_HC/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorEmissionIndexHCProperty != null) {
			groundIdleCalibrationFactorEmissionIndexHC = Double.valueOf(groundIdleCalibrationFactorEmissionIndexHCProperty); 
		}
		
		//...............................................................
		// EmissionIndexSoot
		double takeOffCalibrationFactorEmissionIndexSoot = 1.0;
		double aprCalibrationFactorEmissionIndexSoot = 1.0;
		double climbCalibrationFactorEmissionIndexSoot = 1.0;
		double continuousCalibrationFactorEmissionIndexSoot = 1.0;
		double cruiseCalibrationFactorEmissionIndexSoot = 1.0;
		double flightIdleCalibrationFactorEmissionIndexSoot = 1.0;
		double groundIdleCalibrationFactorEmissionIndexSoot = 1.0;
		
		String takeOffCalibrationFactorEmissionIndexSootProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_Soot/take_off_calibration_factor");
		if(takeOffCalibrationFactorEmissionIndexSootProperty != null) {
			takeOffCalibrationFactorEmissionIndexSoot = Double.valueOf(takeOffCalibrationFactorEmissionIndexSootProperty); 
		}
		String aprCalibrationFactorEmissionIndexSootProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_Soot/apr_calibration_factor");
		if(aprCalibrationFactorEmissionIndexSootProperty != null) {
			aprCalibrationFactorEmissionIndexSoot = Double.valueOf(aprCalibrationFactorEmissionIndexSootProperty); 
		}
		String climbCalibrationFactorEmissionIndexSootProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_Soot/climb_calibration_factor");
		if(climbCalibrationFactorEmissionIndexSootProperty != null) {
			climbCalibrationFactorEmissionIndexSoot = Double.valueOf(climbCalibrationFactorEmissionIndexSootProperty); 
		}
		String continuousCalibrationFactorEmissionIndexSootProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_Soot/continuous_calibration_factor");
		if(continuousCalibrationFactorEmissionIndexSootProperty != null) {
			continuousCalibrationFactorEmissionIndexSoot = Double.valueOf(continuousCalibrationFactorEmissionIndexSootProperty); 
		}
		String cruiseCalibrationFactorEmissionIndexSootProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_Soot/cruise_calibration_factor");
		if(cruiseCalibrationFactorEmissionIndexSootProperty != null) {
			cruiseCalibrationFactorEmissionIndexSoot = Double.valueOf(cruiseCalibrationFactorEmissionIndexSootProperty); 
		}
		String flightIdleCalibrationFactorEmissionIndexSootProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_Soot/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorEmissionIndexSootProperty != null) {
			flightIdleCalibrationFactorEmissionIndexSoot = Double.valueOf(flightIdleCalibrationFactorEmissionIndexSootProperty); 
		}
		String groundIdleCalibrationFactorEmissionIndexSootProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_Soot/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorEmissionIndexSootProperty != null) {
			groundIdleCalibrationFactorEmissionIndexSoot = Double.valueOf(groundIdleCalibrationFactorEmissionIndexSootProperty); 
		}
		
		//...............................................................
		// EmissionIndexCO2
		double takeOffCalibrationFactorEmissionIndexCO2 = 1.0;
		double aprCalibrationFactorEmissionIndexCO2 = 1.0;
		double climbCalibrationFactorEmissionIndexCO2 = 1.0;
		double continuousCalibrationFactorEmissionIndexCO2 = 1.0;
		double cruiseCalibrationFactorEmissionIndexCO2 = 1.0;
		double flightIdleCalibrationFactorEmissionIndexCO2 = 1.0;
		double groundIdleCalibrationFactorEmissionIndexCO2 = 1.0;
		
		String takeOffCalibrationFactorEmissionIndexCO2Property = reader.getXMLPropertyByPath("//calibrations/emission_index_CO2/take_off_calibration_factor");
		if(takeOffCalibrationFactorEmissionIndexCO2Property != null) {
			takeOffCalibrationFactorEmissionIndexCO2 = Double.valueOf(takeOffCalibrationFactorEmissionIndexCO2Property); 
		}
		String aprCalibrationFactorEmissionIndexCO2Property = reader.getXMLPropertyByPath("//calibrations/emission_index_CO2/apr_calibration_factor");
		if(aprCalibrationFactorEmissionIndexCO2Property != null) {
			aprCalibrationFactorEmissionIndexCO2 = Double.valueOf(aprCalibrationFactorEmissionIndexCO2Property); 
		}
		String climbCalibrationFactorEmissionIndexCO2Property = reader.getXMLPropertyByPath("//calibrations/emission_index_CO2/climb_calibration_factor");
		if(climbCalibrationFactorEmissionIndexCO2Property != null) {
			climbCalibrationFactorEmissionIndexCO2 = Double.valueOf(climbCalibrationFactorEmissionIndexCO2Property); 
		}
		String continuousCalibrationFactorEmissionIndexCO2Property = reader.getXMLPropertyByPath("//calibrations/emission_index_CO2/continuous_calibration_factor");
		if(continuousCalibrationFactorEmissionIndexCO2Property != null) {
			continuousCalibrationFactorEmissionIndexCO2 = Double.valueOf(continuousCalibrationFactorEmissionIndexCO2Property); 
		}
		String cruiseCalibrationFactorEmissionIndexCO2Property = reader.getXMLPropertyByPath("//calibrations/emission_index_CO2/cruise_calibration_factor");
		if(cruiseCalibrationFactorEmissionIndexCO2Property != null) {
			cruiseCalibrationFactorEmissionIndexCO2 = Double.valueOf(cruiseCalibrationFactorEmissionIndexCO2Property); 
		}
		String flightIdleCalibrationFactorEmissionIndexCO2Property = reader.getXMLPropertyByPath("//calibrations/emission_index_CO2/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorEmissionIndexCO2Property != null) {
			flightIdleCalibrationFactorEmissionIndexCO2 = Double.valueOf(flightIdleCalibrationFactorEmissionIndexCO2Property); 
		}
		String groundIdleCalibrationFactorEmissionIndexCO2Property = reader.getXMLPropertyByPath("//calibrations/emission_index_CO2/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorEmissionIndexCO2Property != null) {
			groundIdleCalibrationFactorEmissionIndexCO2 = Double.valueOf(groundIdleCalibrationFactorEmissionIndexCO2Property); 
		}
		
		//...............................................................
		// EmissionIndexSOx
		double takeOffCalibrationFactorEmissionIndexSOx = 1.0;
		double aprCalibrationFactorEmissionIndexSOx = 1.0;
		double climbCalibrationFactorEmissionIndexSOx = 1.0;
		double continuousCalibrationFactorEmissionIndexSOx = 1.0;
		double cruiseCalibrationFactorEmissionIndexSOx = 1.0;
		double flightIdleCalibrationFactorEmissionIndexSOx = 1.0;
		double groundIdleCalibrationFactorEmissionIndexSOx = 1.0;
		
		String takeOffCalibrationFactorEmissionIndexSOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_SOx/take_off_calibration_factor");
		if(takeOffCalibrationFactorEmissionIndexSOxProperty != null) {
			takeOffCalibrationFactorEmissionIndexSOx = Double.valueOf(takeOffCalibrationFactorEmissionIndexSOxProperty); 
		}
		String aprCalibrationFactorEmissionIndexSOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_SOx/apr_calibration_factor");
		if(aprCalibrationFactorEmissionIndexSOxProperty != null) {
			aprCalibrationFactorEmissionIndexSOx = Double.valueOf(aprCalibrationFactorEmissionIndexSOxProperty); 
		}
		String climbCalibrationFactorEmissionIndexSOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_SOx/climb_calibration_factor");
		if(climbCalibrationFactorEmissionIndexSOxProperty != null) {
			climbCalibrationFactorEmissionIndexSOx = Double.valueOf(climbCalibrationFactorEmissionIndexSOxProperty); 
		}
		String continuousCalibrationFactorEmissionIndexSOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_SOx/continuous_calibration_factor");
		if(continuousCalibrationFactorEmissionIndexSOxProperty != null) {
			continuousCalibrationFactorEmissionIndexSOx = Double.valueOf(continuousCalibrationFactorEmissionIndexSOxProperty); 
		}
		String cruiseCalibrationFactorEmissionIndexSOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_SOx/cruise_calibration_factor");
		if(cruiseCalibrationFactorEmissionIndexSOxProperty != null) {
			cruiseCalibrationFactorEmissionIndexSOx = Double.valueOf(cruiseCalibrationFactorEmissionIndexSOxProperty); 
		}
		String flightIdleCalibrationFactorEmissionIndexSOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_SOx/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorEmissionIndexSOxProperty != null) {
			flightIdleCalibrationFactorEmissionIndexSOx = Double.valueOf(flightIdleCalibrationFactorEmissionIndexSOxProperty); 
		}
		String groundIdleCalibrationFactorEmissionIndexSOxProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_SOx/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorEmissionIndexSOxProperty != null) {
			groundIdleCalibrationFactorEmissionIndexSOx = Double.valueOf(groundIdleCalibrationFactorEmissionIndexSOxProperty); 
		}
		
		//...............................................................
		// EmissionIndexH2O
		double takeOffCalibrationFactorEmissionIndexH2O = 1.0;
		double aprCalibrationFactorEmissionIndexH2O = 1.0;
		double climbCalibrationFactorEmissionIndexH2O = 1.0;
		double continuousCalibrationFactorEmissionIndexH2O = 1.0;
		double cruiseCalibrationFactorEmissionIndexH2O = 1.0;
		double flightIdleCalibrationFactorEmissionIndexH2O = 1.0;
		double groundIdleCalibrationFactorEmissionIndexH2O = 1.0;
		
		String takeOffCalibrationFactorEmissionIndexH2OProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_H2O/take_off_calibration_factor");
		if(takeOffCalibrationFactorEmissionIndexH2OProperty != null) {
			takeOffCalibrationFactorEmissionIndexH2O = Double.valueOf(takeOffCalibrationFactorEmissionIndexH2OProperty); 
		}
		String aprCalibrationFactorEmissionIndexH2OProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_H2O/apr_calibration_factor");
		if(aprCalibrationFactorEmissionIndexH2OProperty != null) {
			aprCalibrationFactorEmissionIndexH2O = Double.valueOf(aprCalibrationFactorEmissionIndexH2OProperty); 
		}
		String climbCalibrationFactorEmissionIndexH2OProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_H2O/climb_calibration_factor");
		if(climbCalibrationFactorEmissionIndexH2OProperty != null) {
			climbCalibrationFactorEmissionIndexH2O = Double.valueOf(climbCalibrationFactorEmissionIndexH2OProperty); 
		}
		String continuousCalibrationFactorEmissionIndexH2OProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_H2O/continuous_calibration_factor");
		if(continuousCalibrationFactorEmissionIndexH2OProperty != null) {
			continuousCalibrationFactorEmissionIndexH2O = Double.valueOf(continuousCalibrationFactorEmissionIndexH2OProperty); 
		}
		String cruiseCalibrationFactorEmissionIndexH2OProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_H2O/cruise_calibration_factor");
		if(cruiseCalibrationFactorEmissionIndexH2OProperty != null) {
			cruiseCalibrationFactorEmissionIndexH2O = Double.valueOf(cruiseCalibrationFactorEmissionIndexH2OProperty); 
		}
		String flightIdleCalibrationFactorEmissionIndexH2OProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_H2O/flight_idle_calibration_factor");
		if(flightIdleCalibrationFactorEmissionIndexH2OProperty != null) {
			flightIdleCalibrationFactorEmissionIndexH2O = Double.valueOf(flightIdleCalibrationFactorEmissionIndexH2OProperty); 
		}
		String groundIdleCalibrationFactorEmissionIndexH2OProperty = reader.getXMLPropertyByPath("//calibrations/emission_index_H2O/ground_idle_calibration_factor");
		if(groundIdleCalibrationFactorEmissionIndexH2OProperty != null) {
			groundIdleCalibrationFactorEmissionIndexH2O = Double.valueOf(groundIdleCalibrationFactorEmissionIndexH2OProperty); 
		}
		
		//===========================================================================================
		// READING PLOT LIST ...	
		List<PerformancePlotEnum> plotList = new ArrayList<PerformancePlotEnum>();
		if(theAircraft.getTheAnalysisManager().getPlotPerformance() == Boolean.TRUE) {

			//...............................................................
			// TAKE-OFF
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.TAKE_OFF)) {
				String takeOffSimulationsProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/takeoff/simulations/@perform");
				if (takeOffSimulationsProperty != null) {
					if(takeOffSimulationsProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.TAKE_OFF_SIMULATIONS);
				}

				String balancedFieldLengthChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/takeoff/balanced_field_length/@perform");
				if (balancedFieldLengthChartProperty != null) {
					if(balancedFieldLengthChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.BALANCED_FIELD_LENGTH);
				}
				
				String vmcPlotProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/takeoff/vMC/@perform");
				if (vmcPlotProperty != null) {
					if(vmcPlotProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.VMC);
				}
			}				
			//...............................................................
			// CLIMB 
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.CLIMB)) {

				String thrustDragClimbCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/thrust_drag/@perform");
				if (thrustDragClimbCurvesProperty != null) {
					if(thrustDragClimbCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.THRUST_DRAG_CURVES_CLIMB);
				}
				
				String powerNeededAndAvailableClimbCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/power_needed_and_available/@perform");
				if (powerNeededAndAvailableClimbCurvesProperty != null) {
					if(powerNeededAndAvailableClimbCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.POWER_NEEDED_AND_AVAILABLE_CURVES_CLIMB);
				}
				
				String climbEfficiencyChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/efficiency/@perform");
				if (climbEfficiencyChartProperty != null) {
					if(climbEfficiencyChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.EFFICIENCY_CURVES_CLIMB);
				}
				
				String rateOfClimbCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/rate_of_climb_curves/@perform");
				if (rateOfClimbCurvesProperty != null) {
					if(rateOfClimbCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.RATE_OF_CLIMB_CURVES);
				}

				String climbAngleCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/climb_angle_curves/@perform");
				if (climbAngleCurvesProperty != null) {
					if(climbAngleCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CLIMB_ANGLE_CURVES);
				}
				
				String maxRateOfClimbEnvelopeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/max_rate_of_climb_envelope/@perform");
				if (maxRateOfClimbEnvelopeProperty != null) {
					if(maxRateOfClimbEnvelopeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.MAX_RATE_OF_CLIMB_ENVELOPE);
				}	
				
				String maxClimbAngleEnvelopeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/max_climb_angle_envelope/@perform");
				if (maxClimbAngleEnvelopeProperty != null) {
					if(maxClimbAngleEnvelopeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.MAX_CLIMB_ANGLE_ENVELOPE);
				}	
				
				String climbTimeChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/climb_time/@perform");
				if (climbTimeChartProperty != null) {
					if(climbTimeChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CLIMB_TIME);
				}	
				
				String fuelUsedChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/climb/fuel_used/@perform");
				if (fuelUsedChartProperty != null) {
					if(fuelUsedChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CLIMB_FUEL_USED);
				}	
				
			}

			//...............................................................
			// CRUISE
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.CRUISE)) {

				String thrustDragChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/thrust_drag/@perform");
				if (thrustDragChartProperty != null) {
					if(thrustDragChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.THRUST_DRAG_CURVES_CRUISE);
				}

				String powerNeededAndAvailableCruiseCurvesProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/power_needed_and_available/@perform");
				if (powerNeededAndAvailableCruiseCurvesProperty != null) {
					if(powerNeededAndAvailableCruiseCurvesProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.POWER_NEEDED_AND_AVAILABLE_CURVES_CRUISE);
				}
				
				String efficiencyChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/efficiency/@perform");
				if (efficiencyChartProperty != null) {
					if(efficiencyChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.EFFICIENCY_CURVES);
				}

				String cruiseGridChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/cruise_grid/@perform");
				if (cruiseGridChartProperty != null) {
					if(cruiseGridChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CRUISE_GRID_CHART);
				}

				String cruiseFlightEnvelopeChartProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/cruise/flight_envelope/@perform");
				if (cruiseFlightEnvelopeChartProperty != null) {
					if(cruiseFlightEnvelopeChartProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.CRUISE_FLIGHT_ENVELOPE);
				}

			}
			//...............................................................
			// DESCENT
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.DESCENT)) {
				
				String descentProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/descent/@perform");
				if (descentProperty != null) {
					if(descentProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.DESCENT);
				}
				
			}
			//...............................................................
			// LANDING
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.LANDING)) {

				String landingSimulationsProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/landing/simulations/@perform");
				if (landingSimulationsProperty != null) {
					if(landingSimulationsProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.LANDING_SIMULATIONS);
				}

			}
			//...............................................................
			// PAYLOAD-RANGE
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.PAYLOAD_RANGE)) {

				String payloadRangeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/payload_range/@perform");
				if (payloadRangeProperty != null) {
					if(payloadRangeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.PAYLOAD_RANGE);
				}

			}
			//...............................................................
			// V-n DIAGRAM
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.V_n_DIAGRAM)) {

				String maneuveringFlightAndGustEnvelopeProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/maneuvering_flight_and_gust_envelope/@perform");
				if (maneuveringFlightAndGustEnvelopeProperty != null) {
					if(maneuveringFlightAndGustEnvelopeProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.FLIGHT_MANEUVERING_AND_GUST_DIAGRAM);
				}
			}
			//...............................................................
			// NOISE TRAJECTORIES
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.NOISE_TRAJECTORIES)) {

				String noiseTrajectoryTakeOffProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/noise_trajectories/take_off/@perform");
				if (noiseTrajectoryTakeOffProperty != null) {
					if(noiseTrajectoryTakeOffProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.NOISE_TRAJECTORY_TAKE_OFF);
				}

				String noiseTrajectoryLandingProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/noise_trajectories/landing/@perform");
				if (noiseTrajectoryLandingProperty != null) {
					if(noiseTrajectoryLandingProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.NOISE_TRAJECTORY_LANDING);
				}
			}
			//...............................................................
			// MISSION PROFILE
			if(theAircraft.getTheAnalysisManager().getTaskListPerformance().contains(PerformanceEnum.MISSION_PROFILE)) {

				String rangeProfileProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/mission_profile/range_profile/@perform");
				if (rangeProfileProperty != null) {
					if(rangeProfileProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.RANGE_PROFILE);
				}
				
				String timeProfileProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/mission_profile/time_profile/@perform");
				if (timeProfileProperty != null) {
					if(timeProfileProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.TIME_PROFILE);
				}
				
				String fuelUsedProfileProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/mission_profile/fuel_used_profile/@perform");
				if (fuelUsedProfileProperty != null) {
					if(fuelUsedProfileProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.FUEL_USED_PROFILE);
				}
				
				String weightProfileProperty = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//plot/mission_profile/weight_profile/@perform");
				if (weightProfileProperty != null) {
					if(weightProfileProperty.equalsIgnoreCase("TRUE")) 
						plotList.add(PerformancePlotEnum.WEIGHT_PROFILE);
				}
			}
		}
		
		//===========================================================================================
		// BUILDING THE CALCULATOR ...
		IACPerformanceManager thePerformanceManagerInterface = new IACPerformanceManager.Builder()
				.setId(id)
				.setTheAircraft(theAircraft)
				.setTheOperatingConditions(theOperatingConditions)
				.setMaximumTakeOffMass(maximumTakeOffMass.to(SI.KILOGRAM))
				.setOperatingEmptyMass(operatingEmptyMass.to(SI.KILOGRAM))
				.setMaximumFuelMass(maximumFuelMass.to(SI.KILOGRAM))
				.setSinglePassengerMass(singlePassengerMass.to(SI.KILOGRAM))
				.addAllXcgPositionList(centerOfGravityList)
				.putAllCLmaxClean(cLmaxClean)
				.putAllCLAlphaClean(cLAlphaClean)
				.putAllCLAlphaTakeOff(cLAlphaTakeOff)
				.putAllCLAlphaLanding(cLAlphaLanding)
				.putAllCLmaxTakeOff(cLmaxTakeOff)
				.putAllCLZeroTakeOff(cLZeroTakeOff)
				.putAllCLmaxLanding(cLmaxLanding)
				.putAllCLZeroLanding(cLZeroLanding)
				.putAllDeltaCD0LandingGears(deltaCD0LandingGears)
				.putAllDeltaCD0FlapTakeOff(deltaCD0TakeOff)
				.putAllDeltaCD0FlapLanding(cLZeroLanding)
				.putAllPolarCLCruise(polarCLCruise)
				.putAllPolarCDCruise(polarCDCruise)
				.putAllPolarCLClimb(polarCLClimb)
				.putAllPolarCDClimb(polarCDClimb)
				.putAllPolarCLTakeOff(polarCLTakeOff)
				.putAllPolarCDTakeOff(polarCDTakeOff)
				.putAllPolarCLLanding(polarCLLanding)
				.putAllPolarCDLanding(polarCDLanding)
				.putAllTauRudderMap(tauRudderMap)
				.setMuFunction(muInterpolatingFunction)
				.setMuBrakeFunction(muBrakeInterpolatingFunction)
				.setDtHold(dtHold)
				.setAlphaGround(alphaGround)
				.setWindSpeed(windSpeed.to(SI.METERS_PER_SECOND))
				.setObstacleTakeOff(obstacleTakeOff.to(SI.METER))
				.setKRotation(kRotation)
				.setAlphaDotRotation(alphaDotRotation)
				.setKCLmaxTakeOff(kCLmaxTakeOff)
				.setDragDueToEngineFailure(dragDueToEngineFailure)
				.setKAlphaDot(kAlphaDot)
				.setKLandingWeight(kLandingWeight)
				.setInitialALtitudeLanding(initialAltitudeLanding.to(SI.METER))
				.setObstacleLanding(obstacleLanding.to(SI.METER))
				.setApproachAngle(approachAngle.to(NonSI.DEGREE_ANGLE))
				.setKCLmaxLanding(kCLmaxLanding)
				.setKApproach(kApproach)
				.setKFlare(kFlare)
				.setKTouchDown(kTouchDown)
				.setFreeRollDuration(freeRollDuration.to(SI.SECOND))
				.setKClimbWeightAEO(kClimbWeightAEO)
				.setKClimbWeightOEI(kClimbWeightOEI)
				.setClimbSpeedCAS(climbSpeed.to(SI.METERS_PER_SECOND))
				.setInitialClimbAltitude(initialClimbAltitude.to(SI.METER))
				.setFinalClimbAltitude(finalClimbAltitude.to(SI.METER))
				.addAllAltitudeListCruise(altitudeListCruise.stream().map(a -> a.to(SI.METER)).collect(Collectors.toList()))
				.setKCruiseWeight(kCruiseWeight)
				.setRateOfDescent(rateOfDescent.to(SI.METERS_PER_SECOND))
				.setSpeedDescentCAS(speedDescentCAS.to(SI.METERS_PER_SECOND))
				.setKDescentWeight(kDescentWeight)
				.setInitialDescentAltitude(initialDescentAltitude.to(SI.METER))
				.setFinalDescentAltitude(finalDescentAltitude.to(SI.METER))
				.setCLmaxInverted(cLmaxInverted)
				.setTakeOffNoiseTrajectoryXEndSimulation(takeOffNoiseTrajectoryXEndSimulation)
				.setTakeOffNoiseTrajectoryCutbackAltitude(takeOffNoiseTrajectoryCutbackAltitude)
				.setTakeOffNoiseTrajectoryNumberOfThrustSettingCutback(takeOffNoiseTrajectoryNumberOfThrustSettingCutback)
				.setTakeOffNoiseTrajectoryLandingGearRetractionTimeInterval(takeOffNoiseTrajectoryLandingGearRetractionTimeInterval)
				.setTakeOffNoiseTrajectoryThrustReductionCutbackTimeInterval(takeOffNoiseTrajectoryThrustReductionCutbackTimeInterval)
				.setLandingNoiseTrajectoryInitialAltitude(landingNoiseTrajectoryInitialAltitude)
				.setLandingNoiseTrajectoryTrajectoryAngle(landingNoiseTrajectoryTrajectoryAngle)
				.setMissionRange(missionRange.to(SI.METER))
				.setAlternateCruiseLength(alternateCruiseLength.to(SI.METER))
				.setAlternateCruiseAltitude(alternateCruiseAltitude.to(SI.METER))
				.setHoldingDuration(holdingDuration.to(SI.SECOND))
				.setHoldingAltitude(holdingAltitude.to(SI.METER))
				.setHoldingMachNumber(holdingMachNumber)
				.setFuelReserve(fuelReserve)
				.setFirstGuessCruiseLength(missionRange.to(SI.METER).divide(3)) // first guess value equal to 1/3 of the total mission range
				.setTakeOffCalibrationFactorThrust(takeOffCalibrationFactorThrust)
				.setAprCalibrationFactorThrust(aprCalibrationFactorThrust)
				.setClimbCalibrationFactorThrust(climbCalibrationFactorThrust)
				.setContinuousCalibrationFactorThrust(continuousCalibrationFactorThrust)
				.setCruiseCalibrationFactorThrust(cruiseCalibrationFactorThrust)
				.setFlightIdleCalibrationFactorThrust(flightIdleCalibrationFactorThrust)
				.setGroundIdleCalibrationFactorThrust(groundIdleCalibrationFactorThrust)
				.setTakeOffCalibrationFactorSFC(takeOffCalibrationFactorSFC)
				.setAprCalibrationFactorSFC(aprCalibrationFactorSFC)
				.setClimbCalibrationFactorSFC(climbCalibrationFactorSFC)
				.setContinuousCalibrationFactorSFC(continuousCalibrationFactorSFC)
				.setCruiseCalibrationFactorSFC(cruiseCalibrationFactorSFC)
				.setFlightIdleCalibrationFactorSFC(flightIdleCalibrationFactorSFC)
				.setGroundIdleCalibrationFactorSFC(groundIdleCalibrationFactorSFC)
				.setTakeOffCalibrationFactorEmissionIndexNOx(takeOffCalibrationFactorEmissionIndexNOx)
				.setAprCalibrationFactorEmissionIndexNOx(aprCalibrationFactorEmissionIndexNOx)
				.setClimbCalibrationFactorEmissionIndexNOx(climbCalibrationFactorEmissionIndexNOx)
				.setContinuousCalibrationFactorEmissionIndexNOx(continuousCalibrationFactorEmissionIndexNOx)
				.setCruiseCalibrationFactorEmissionIndexNOx(cruiseCalibrationFactorEmissionIndexNOx)
				.setFlightIdleCalibrationFactorEmissionIndexNOx(flightIdleCalibrationFactorEmissionIndexNOx)
				.setGroundIdleCalibrationFactorEmissionIndexNOx(groundIdleCalibrationFactorEmissionIndexNOx)
				.setTakeOffCalibrationFactorEmissionIndexCO(takeOffCalibrationFactorEmissionIndexCO)
				.setAprCalibrationFactorEmissionIndexCO(aprCalibrationFactorEmissionIndexCO)
				.setClimbCalibrationFactorEmissionIndexCO(climbCalibrationFactorEmissionIndexCO)
				.setContinuousCalibrationFactorEmissionIndexCO(continuousCalibrationFactorEmissionIndexCO)
				.setCruiseCalibrationFactorEmissionIndexCO(cruiseCalibrationFactorEmissionIndexCO)
				.setFlightIdleCalibrationFactorEmissionIndexCO(flightIdleCalibrationFactorEmissionIndexCO)
				.setGroundIdleCalibrationFactorEmissionIndexCO(groundIdleCalibrationFactorEmissionIndexCO)
				.setTakeOffCalibrationFactorEmissionIndexHC(takeOffCalibrationFactorEmissionIndexHC)
				.setAprCalibrationFactorEmissionIndexHC(aprCalibrationFactorEmissionIndexHC)
				.setClimbCalibrationFactorEmissionIndexHC(climbCalibrationFactorEmissionIndexHC)
				.setContinuousCalibrationFactorEmissionIndexHC(continuousCalibrationFactorEmissionIndexHC)
				.setCruiseCalibrationFactorEmissionIndexHC(cruiseCalibrationFactorEmissionIndexHC)
				.setFlightIdleCalibrationFactorEmissionIndexHC(flightIdleCalibrationFactorEmissionIndexHC)
				.setGroundIdleCalibrationFactorEmissionIndexHC(groundIdleCalibrationFactorEmissionIndexHC)
				.setTakeOffCalibrationFactorEmissionIndexSoot(takeOffCalibrationFactorEmissionIndexSoot)
				.setAprCalibrationFactorEmissionIndexSoot(aprCalibrationFactorEmissionIndexSoot)
				.setClimbCalibrationFactorEmissionIndexSoot(climbCalibrationFactorEmissionIndexSoot)
				.setContinuousCalibrationFactorEmissionIndexSoot(continuousCalibrationFactorEmissionIndexSoot)
				.setCruiseCalibrationFactorEmissionIndexSoot(cruiseCalibrationFactorEmissionIndexSoot)
				.setFlightIdleCalibrationFactorEmissionIndexSoot(flightIdleCalibrationFactorEmissionIndexSoot)
				.setGroundIdleCalibrationFactorEmissionIndexSoot(groundIdleCalibrationFactorEmissionIndexSoot)
				.setTakeOffCalibrationFactorEmissionIndexCO2(takeOffCalibrationFactorEmissionIndexCO2)
				.setAprCalibrationFactorEmissionIndexCO2(aprCalibrationFactorEmissionIndexCO2)
				.setClimbCalibrationFactorEmissionIndexCO2(climbCalibrationFactorEmissionIndexCO2)
				.setContinuousCalibrationFactorEmissionIndexCO2(continuousCalibrationFactorEmissionIndexCO2)
				.setCruiseCalibrationFactorEmissionIndexCO2(cruiseCalibrationFactorEmissionIndexCO2)
				.setFlightIdleCalibrationFactorEmissionIndexCO2(flightIdleCalibrationFactorEmissionIndexCO2)
				.setGroundIdleCalibrationFactorEmissionIndexCO2(groundIdleCalibrationFactorEmissionIndexCO2)
				.setTakeOffCalibrationFactorEmissionIndexSOx(takeOffCalibrationFactorEmissionIndexSOx)
				.setAprCalibrationFactorEmissionIndexSOx(aprCalibrationFactorEmissionIndexSOx)
				.setClimbCalibrationFactorEmissionIndexSOx(climbCalibrationFactorEmissionIndexSOx)
				.setContinuousCalibrationFactorEmissionIndexSOx(continuousCalibrationFactorEmissionIndexSOx)
				.setCruiseCalibrationFactorEmissionIndexSOx(cruiseCalibrationFactorEmissionIndexSOx)
				.setFlightIdleCalibrationFactorEmissionIndexSOx(flightIdleCalibrationFactorEmissionIndexSOx)
				.setGroundIdleCalibrationFactorEmissionIndexSOx(groundIdleCalibrationFactorEmissionIndexSOx)
				.setTakeOffCalibrationFactorEmissionIndexH2O(takeOffCalibrationFactorEmissionIndexH2O)
				.setAprCalibrationFactorEmissionIndexH2O(aprCalibrationFactorEmissionIndexH2O)
				.setClimbCalibrationFactorEmissionIndexH2O(climbCalibrationFactorEmissionIndexH2O)
				.setContinuousCalibrationFactorEmissionIndexH2O(continuousCalibrationFactorEmissionIndexH2O)
				.setCruiseCalibrationFactorEmissionIndexH2O(cruiseCalibrationFactorEmissionIndexH2O)
				.setFlightIdleCalibrationFactorEmissionIndexH2O(flightIdleCalibrationFactorEmissionIndexH2O)
				.setGroundIdleCalibrationFactorEmissionIndexH2O(groundIdleCalibrationFactorEmissionIndexH2O)
				.addAllTaskList(theAircraft.getTheAnalysisManager().getTaskListPerformance())
				.addAllPlotList(plotList)
				.build();
		
		ACPerformanceManager thePerformanceManager = new ACPerformanceManager();
		thePerformanceManager.setThePerformanceInterface(thePerformanceManagerInterface);
		
		return thePerformanceManager;
	}

	/**
	 * This method reads the task list, initializes the related calculators inner classes and 
	 * performe the required calculation
	 */
	public void calculate(String resultsFolderPath) {

		initializeData();

		String performanceFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "PERFORMANCE"
				+ File.separator
				);

		for(int i=0; i<_thePerformanceInterface.getXcgPositionList().size(); i++) {

			String xcgFolderPath = JPADStaticWriteUtils.createNewFolder(
					performanceFolderPath 
					+ "XCG_" + _thePerformanceInterface.getXcgPositionList().get(i)
					+ File.separator
					);
			
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.TAKE_OFF)) {

				String takeOffFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "TAKE_OFF"
						+ File.separator
						);

				CalcTakeOff calcTakeOff = new CalcTakeOff();
				calcTakeOff.calculateVMC(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcTakeOff.plotVMC(takeOffFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));
				calcTakeOff.performTakeOffSimulation(
						_thePerformanceInterface.getMaximumTakeOffMass(), 
						_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff().to(SI.METER),
						_thePerformanceInterface.getXcgPositionList().get(i),
						_vMCMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).to(SI.METERS_PER_SECOND)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcTakeOff.plotTakeOffPerformance(takeOffFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));
				calcTakeOff.calculateBalancedFieldLength(
						_thePerformanceInterface.getXcgPositionList().get(i),
						_vMCMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).to(SI.METERS_PER_SECOND)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcTakeOff.plotBalancedFieldLength(takeOffFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CLIMB)) {

				String climbFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "CLIMB"
						+ File.separator
						);

				CalcClimb calcClimb = new CalcClimb();
				calcClimb.calculateClimbPerformance(
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKClimbWeightAEO()),
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKClimbWeightOEI()),
						_thePerformanceInterface.getInitialClimbAltitude(),
						_thePerformanceInterface.getFinalClimbAltitude(),
						true,
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcClimb.plotClimbPerformance(climbFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CRUISE)) {

				String cruiseFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "CRUISE"
						+ File.separator
						);

				_weightListCruiseMap.put(_thePerformanceInterface.getXcgPositionList().get(i), new ArrayList<Amount<Force>>());

				Amount<Force> cruiseWeight = 
						Amount.valueOf(
								(_thePerformanceInterface.getMaximumTakeOffMass()
										.times(_thePerformanceInterface.getKCruiseWeight())
										.times(AtmosphereCalc.g0)
										.getEstimatedValue()
										),
								SI.NEWTON
								);
				for(int k=0; k<5; k++) {			
					_weightListCruiseMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).add(
							Amount.valueOf(
									Math.round(
											(cruiseWeight)
											.minus((cruiseWeight)
													.times(0.05*(4-k))
													)
											.getEstimatedValue()
											),
									SI.NEWTON
									)
							);
				}

				CalcCruise calcCruise = new CalcCruise();
				calcCruise.calculateThrustAndDrag(
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKCruiseWeight()),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				calcCruise.calculateFlightEnvelope(
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKCruiseWeight()),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				calcCruise.calculateEfficiency(_thePerformanceInterface.getXcgPositionList().get(i));
				calcCruise.calculateCruiseGrid(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcCruise.plotCruiseOutput(cruiseFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.DESCENT)) {

				String descentFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "DESCENT"
						+ File.separator
						);

				CalcDescent calcDescent = new CalcDescent();
				calcDescent.calculateDescentPerformance(
						_thePerformanceInterface.getInitialDescentAltitude().to(SI.METER),
						_thePerformanceInterface.getFinalDescentAltitude().to(SI.METER),
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKDescentWeight()),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcDescent.plotDescentPerformance(descentFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.LANDING)) {

				String landingFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "LANDING"
						+ File.separator
						);

				CalcLanding calcLanding = new CalcLanding();
				calcLanding.performLandingSimulation(
						_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKLandingWeight()),
						_thePerformanceInterface.getXcgPositionList().get(i)
						);
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcLanding.plotLandingPerformance(landingFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));
			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {

				String payloadRangeFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "PAYLOAD_RANGE"
						+ File.separator
						);

				CalcPayloadRange calcPayloadRange = new CalcPayloadRange();
				calcPayloadRange.fromMissionProfile(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcPayloadRange.plotPayloadRange(payloadRangeFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.V_n_DIAGRAM)) {

				String maneuveringFlightAndGustEnvelopeFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "V-n_DIAGRAM"
						+ File.separator
						);

				CalcFlightManeuveringAndGustEnvelope calcEnvelope =  new CalcFlightManeuveringAndGustEnvelope();
				calcEnvelope.fromRegulations(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcEnvelope.plotVnDiagram(maneuveringFlightAndGustEnvelopeFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.NOISE_TRAJECTORIES)) {

				String noiseTrajectoriesFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "NOISE_TRAJECTORIES"
						+ File.separator
						);
				String takeOffNoiseTrajectoriesFolderPath = JPADStaticWriteUtils.createNewFolder(
						noiseTrajectoriesFolderPath 
						+ "TAKE-OFF"
						+ File.separator
						);
				String landingNoiseTrajectoriesFolderPath = JPADStaticWriteUtils.createNewFolder(
						noiseTrajectoriesFolderPath 
						+ "LANDING"
						+ File.separator
						);

				CalcNoiseTrajectories calcNoiseTrajectories =  new CalcNoiseTrajectories();
				calcNoiseTrajectories.calculateTakeOffNoiseTrajectory(_thePerformanceInterface.getXcgPositionList().get(i), takeOffNoiseTrajectoriesFolderPath);
				calcNoiseTrajectories.calculateLandingNoiseTrajectory(_thePerformanceInterface.getXcgPositionList().get(i), landingNoiseTrajectoriesFolderPath);
				
				List<Amount<Length>> groundDistanceList = new ArrayList<>();
				List<Amount<Length>> altitudeList = new ArrayList<>();
				List<Amount<Velocity>> speedTASList = new ArrayList<>();
				List<Amount<Velocity>> speedCASList = new ArrayList<>();
				List<Amount<Angle>> alphaList = new ArrayList<>();
				List<Amount<Angle>> gammaList = new ArrayList<>();
				List<Amount<Angle>> thetaList = new ArrayList<>();
				List<Amount<Force>> thrustList = new ArrayList<>();
				
				//---------------------------------------------------------------------------------------------------------------------------------------------------
				// SIDELINE VALUES - index (0)
				groundDistanceList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsGroundDistanceMap().get(1.0));
				altitudeList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsAltitudeMap().get(1.0));
				speedTASList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsSpeedTASMap().get(1.0));
				speedCASList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsSpeedCASMap().get(1.0));
				alphaList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsAlphaMap().get(1.0));
				gammaList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsGammaMap().get(1.0));
				thetaList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsThetaMap().get(1.0));
				thrustList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsThrustMap().get(1.0).divide(_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber()));
				
				//---------------------------------------------------------------------------------------------------------------------------------------------------
				// FLYOVER VALUES with CUTBACK - index (1)
				double phiCutback = _theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getPhiCutback();
				
				groundDistanceList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsGroundDistanceMap().get(phiCutback));
				altitudeList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsAltitudeMap().get(phiCutback));
				speedTASList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsSpeedTASMap().get(phiCutback));
				speedCASList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsSpeedCASMap().get(phiCutback));
				alphaList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsAlphaMap().get(phiCutback));
				gammaList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsGammaMap().get(phiCutback));
				thetaList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsThetaMap().get(phiCutback));
				thrustList.add(_theTakeOffNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsThrustMap().get(phiCutback).divide(_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber()));
				
				//---------------------------------------------------------------------------------------------------------------------------------------------------
				// APPROACH VALUES - index (2)
				groundDistanceList.add(_theLandingNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsGroundDistance());
				altitudeList.add(_theLandingNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsAltitude());
				speedTASList.add(_theLandingNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsSpeedTAS());
				speedCASList.add(_theLandingNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsSpeedCAS());
				alphaList.add(_theLandingNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsAlpha());
				gammaList.add(_theLandingNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsGamma());
				thetaList.add(_theLandingNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsTheta());
				thrustList.add(_theLandingNoiseTrajectoryCalculatorMap.get(_thePerformanceInterface.getXcgPositionList().get(i)).getCertificationPointsThrust().divide(_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineNumber()));
				
				_certificationPointsLongitudinalDistanceMap.put(_thePerformanceInterface.getXcgPositionList().get(i), groundDistanceList);
				_certificationPointsAltitudeMap.put(_thePerformanceInterface.getXcgPositionList().get(i), altitudeList);
				_certificationPointsSpeedTASMap.put(_thePerformanceInterface.getXcgPositionList().get(i), speedTASList);
				_certificationPointsSpeedCASMap.put(_thePerformanceInterface.getXcgPositionList().get(i), speedCASList);
				_certificationPointsAlphaMap.put(_thePerformanceInterface.getXcgPositionList().get(i), alphaList);
				_certificationPointsGammaMap.put(_thePerformanceInterface.getXcgPositionList().get(i), gammaList);
				_certificationPointsThetaMap.put(_thePerformanceInterface.getXcgPositionList().get(i), thetaList);
				_certificationPointsThrustMap.put(_thePerformanceInterface.getXcgPositionList().get(i), thrustList);

			}
			
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.MISSION_PROFILE)) {

				String missionProfilesFolderPath = JPADStaticWriteUtils.createNewFolder(
						xcgFolderPath 
						+ "MISSION_PROFILES"
						+ File.separator
						);

				CalcMissionProfile calcMissionProfile = new CalcMissionProfile();
				calcMissionProfile.calculateMissionProfileIterative(_thePerformanceInterface.getXcgPositionList().get(i));
				if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance() == true)
					calcMissionProfile.plotProfiles(missionProfilesFolderPath, _thePerformanceInterface.getXcgPositionList().get(i));

			}

			// PRINT RESULTS
			try {
				toXLSFile(
						xcgFolderPath + "Performance_" + _thePerformanceInterface.getXcgPositionList().get(i),
						_thePerformanceInterface.getXcgPositionList().get(i));
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 

		}
	}
	
	public void toXLSFile(String filenameWithPathAndExt, Double xcg) throws InvalidFormatException, IOException {
		
		Workbook wb;
		File outputFile = new File(filenameWithPathAndExt + ".xlsx");
		if (outputFile.exists()) { 
			outputFile.delete();		
			System.out.println("Deleting the old .xls file ...");
		} 
		
		if (outputFile.getName().endsWith(".xls")) {
			wb = new HSSFWorkbook();
		}
		else if (outputFile.getName().endsWith(".xlsx")) {
			wb = new XSSFWorkbook();
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}
		
		CellStyle styleHead = wb.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    styleHead.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    Font font = wb.createFont();
	    font.setFontHeightInPoints((short) 20);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        styleHead.setFont(font);
		
        //--------------------------------------------------------------------------------
        // TAKE-OFF ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.TAKE_OFF)) {
        	Sheet sheet = wb.createSheet("TAKE-OFF");
        	List<Object[]> dataListTakeOff = new ArrayList<>();

        	dataListTakeOff.add(new Object[] {"Description","Unit","Value"});
        	dataListTakeOff.add(new Object[] {"Ground roll distance","m", _groundRollDistanceTakeOffMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Rotation distance","m", _rotationDistanceTakeOffMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Airborne distance","m", _airborneDistanceTakeOffMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"AEO take-off distance","m", _takeOffDistanceAEOMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"FAR-25 take-off field length","m", _takeOffDistanceFAR25Map.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {"Balanced field length","m", _balancedFieldLengthMap.get(xcg).doubleValue(SI.METER)});
        	dataListTakeOff.add(new Object[] {" "});
           	dataListTakeOff.add(new Object[] {"Ground roll distance","ft", _groundRollDistanceTakeOffMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Rotation distance","ft", _rotationDistanceTakeOffMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Airborne distance","ft", _airborneDistanceTakeOffMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"AEO take-off distance","ft", _takeOffDistanceAEOMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"FAR-25 take-off field length","ft", _takeOffDistanceFAR25Map.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {"Balanced field length","ft", _balancedFieldLengthMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Stall speed take-off (VsTO)","m/s", _vStallTakeOffMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Decision speed (V1)","m/s", _v1Map.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Rotation speed (V_Rot)","m/s", _vRotationMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Minimum control speed (VMC)","m/s", _vMCMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Lift-off speed (V_LO)","m/s", _vLiftOffMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {"Take-off safety speed (V2)","m/s", _v2Map.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Stall speed take-off (VsTO)","kn", _vStallTakeOffMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Decision speed (V1)","kn", _v1Map.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Rotation speed (V_Rot)","kn", _vRotationMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Minimum control speed (VMC)","kn", _vMCMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Lift-off speed (V_LO)","kn", _vLiftOffMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {"Take-off safety speed (V2)","kn", _v2Map.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"V1/VsTO","", _v1Map.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V_Rot/VsTO","", _vRotationMap.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"VMC/VsTO"," ", _vMCMap.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V_LO/VsTO","", _vLiftOffMap.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {"V2/VsTO","", _v2Map.get(xcg).divide(_vStallTakeOffMap.get(xcg)).getEstimatedValue()});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Take-off duration","s", _takeOffDurationMap.get(xcg).doubleValue(SI.SECOND)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Take-off fuel used","kg", _takeOffFuelMap.get(xcg).doubleValue(SI.KILOGRAM)});
        	dataListTakeOff.add(new Object[] {" "});
        	dataListTakeOff.add(new Object[] {"Take-off NOx emissions","g", _takeOffNOxEmissionsMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListTakeOff.add(new Object[] {"Take-off CO emissions","g", _takeOffCOEmissionsMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListTakeOff.add(new Object[] {"Take-off HC emissions","g", _takeOffHCEmissionsMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListTakeOff.add(new Object[] {"Take-off Soot emissions","g", _takeOffSootEmissionsMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListTakeOff.add(new Object[] {"Take-off CO2 emissions","g", _takeOffCO2EmissionsMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListTakeOff.add(new Object[] {"Take-off SOx emissions","g", _takeOffSOxEmissionsMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListTakeOff.add(new Object[] {"Take-off H2O emissions","g", _takeOffH2OEmissionsMap.get(xcg).doubleValue(SI.GRAM)});

        	Row rowTakeOff = sheet.createRow(0);
        	Object[] objArrTakeOff = dataListTakeOff.get(0);
        	int cellnumTakeOff = 0;
        	for (Object obj : objArrTakeOff) {
        		Cell cell = rowTakeOff.createCell(cellnumTakeOff++);
        		cell.setCellStyle(styleHead);
        		if (obj instanceof Date) {
        			cell.setCellValue((Date) obj);
        		} else if (obj instanceof Boolean) {
        			cell.setCellValue((Boolean) obj);
        		} else if (obj instanceof String) {
        			cell.setCellValue((String) obj);
        		} else if (obj instanceof Double) {
        			cell.setCellValue((Double) obj);
        		}
        		sheet.setDefaultColumnWidth(35);
        		sheet.setColumnWidth(1, 2048);
        		sheet.setColumnWidth(2, 3840);
        	}

        	int rownumTakeOff = 1;
        	for (int i = 1; i < dataListTakeOff.size(); i++) {
        		objArrTakeOff = dataListTakeOff.get(i);
        		rowTakeOff = sheet.createRow(rownumTakeOff++);
        		cellnumTakeOff = 0;
        		for (Object obj : objArrTakeOff) {
        			Cell cell = rowTakeOff.createCell(cellnumTakeOff++);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        		}
        	}
        }
		//--------------------------------------------------------------------------------
		// CLIMB ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CLIMB)) {
        	Sheet sheetClimb = wb.createSheet("CLIMB");
        	List<Object[]> dataListClimb = new ArrayList<>();

        	dataListClimb.add(new Object[] {"Description","Unit","Value"});
        	dataListClimb.add(new Object[] {"Absolute ceiling AEO","m", _absoluteCeilingAEOMap.get(xcg).doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Absolute ceiling AEO","ft", _absoluteCeilingAEOMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Service ceiling AEO","m", _serviceCeilingAEOMap.get(xcg).doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Service ceiling AEO","ft", _serviceCeilingAEOMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Minimum time to climb AEO","min", _minimumClimbTimeAEOMap.get(xcg).doubleValue(NonSI.MINUTE)});
        	if(_climbTimeAtSpecificClimbSpeedAEOMap.get(xcg) != null)
        		dataListClimb.add(new Object[] {"Time to climb at given climb speed AEO","min", _climbTimeAtSpecificClimbSpeedAEOMap.get(xcg).doubleValue(NonSI.MINUTE)});
        	dataListClimb.add(new Object[] {"Fuel used during climb AEO","kg", _fuelUsedDuringClimbMap.get(xcg).doubleValue(SI.KILOGRAM)});
        	dataListClimb.add(new Object[] {"Total NOx emissions during climb AEO","g", _emissionsNOxDuringClimbMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListClimb.add(new Object[] {"Total CO emissions during climb AEO","g", _emissionsCODuringClimbMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListClimb.add(new Object[] {"Total HC emissions during climb AEO","g", _emissionsHCDuringClimbMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListClimb.add(new Object[] {"Total Soot emissions during climb AEO","g", _emissionsSootDuringClimbMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListClimb.add(new Object[] {"Total CO2 emissions during climb AEO","g", _emissionsCO2DuringClimbMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListClimb.add(new Object[] {"Total SOx emissions during climb AEO","g", _emissionsSOxDuringClimbMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListClimb.add(new Object[] {"Total H2O emissions during climb AEO","g", _emissionsH2ODuringClimbMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListClimb.add(new Object[] {" "});
        	dataListClimb.add(new Object[] {"Absolute ceiling OEI","m", _absoluteCeilingOEIMap.get(xcg).doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Absolute ceiling OEI","ft", _absoluteCeilingOEIMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListClimb.add(new Object[] {"Service ceiling OEI","m", _serviceCeilingOEIMap.get(xcg).doubleValue(SI.METER)});
        	dataListClimb.add(new Object[] {"Service ceiling OEI","ft", _serviceCeilingOEIMap.get(xcg).doubleValue(NonSI.FOOT)});

        	Row rowClimb = sheetClimb.createRow(0);
        	Object[] objArrClimb = dataListClimb.get(0);
        	int cellnumClimb = 0;
        	for (Object obj : objArrClimb) {
        		Cell cell = rowClimb.createCell(cellnumClimb++);
        		cell.setCellStyle(styleHead);
        		if (obj instanceof Date) {
        			cell.setCellValue((Date) obj);
        		} else if (obj instanceof Boolean) {
        			cell.setCellValue((Boolean) obj);
        		} else if (obj instanceof String) {
        			cell.setCellValue((String) obj);
        		} else if (obj instanceof Double) {
        			cell.setCellValue((Double) obj);
        		}
        		sheetClimb.setDefaultColumnWidth(35);
        		sheetClimb.setColumnWidth(1, 2048);
        		sheetClimb.setColumnWidth(2, 3840);
        	}

        	int rownumClimb = 1;
        	for (int i = 1; i < dataListClimb.size(); i++) {
        		objArrClimb = dataListClimb.get(i);
        		rowClimb = sheetClimb.createRow(rownumClimb++);
        		cellnumClimb = 0;
        		for (Object obj : objArrClimb) {
        			Cell cell = rowClimb.createCell(cellnumClimb++);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        		}
        	}
        }
        //--------------------------------------------------------------------------------
        // CRUISE ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CRUISE)) {
        	Sheet sheetCruise = wb.createSheet("CRUISE");
        	List<Object[]> dataListCruise = new ArrayList<>();
        	
        	dataListCruise.add(new Object[] {"Description","Unit","Value"});
        	dataListCruise.add(new Object[] {"Thrust at cruise altitude and Mach","N", _thrustAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(SI.NEWTON)});
        	dataListCruise.add(new Object[] {"Thrust at cruise altitude and Mach","lb", _thrustAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(NonSI.POUND_FORCE)});
        	dataListCruise.add(new Object[] {"Drag at cruise altitude and Mach","N", _dragAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(SI.NEWTON)});
        	dataListCruise.add(new Object[] {"Drag at cruise altitude and Mach","lb", _dragAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(NonSI.POUND_FORCE)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Power available at cruise altitude and Mach","W", _powerAvailableAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(SI.WATT)});
        	dataListCruise.add(new Object[] {"Power available at cruise altitude and Mach","hp", _powerAvailableAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(NonSI.HORSEPOWER)});
        	dataListCruise.add(new Object[] {"Power needed at cruise altitude and Mach","W", _powerNeededAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(SI.WATT)});
        	dataListCruise.add(new Object[] {"Power needed at cruise altitude and Mach","hp", _powerNeededAtCruiseAltitudeAndMachMap.get(xcg).doubleValue(NonSI.HORSEPOWER)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (CAS)","m/s", _minSpeesCASAtCruiseAltitudeMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (CAS)","m/s", _maxSpeesCASAtCruiseAltitudeMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (CAS)","kn", _minSpeesCASAtCruiseAltitudeMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (CAS)","kn", _maxSpeesCASAtCruiseAltitudeMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (TAS)","m/s", _minSpeesTASAtCruiseAltitudeMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (TAS)","m/s", _maxSpeesTASAtCruiseAltitudeMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListCruise.add(new Object[] {"Min speed at cruise altitude (TAS)","kn", _minSpeesTASAtCruiseAltitudeMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {"Max speed at cruise altitude (TAS)","kn", _maxSpeesTASAtCruiseAltitudeMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Min Mach number at cruise altitude","", _minMachAtCruiseAltitudeMap.get(xcg)});
        	dataListCruise.add(new Object[] {"Max Mach number at cruise altitude","", _maxMachAtCruiseAltitudeMap.get(xcg)});
        	dataListCruise.add(new Object[] {" "});
        	dataListCruise.add(new Object[] {"Efficiency at cruise altitude and Mach","", _efficiencyAtCruiseAltitudeAndMachMap.get(xcg)});
        	dataListCruise.add(new Object[] {" "});

        	Row rowCruise = sheetCruise.createRow(0);
        	Object[] objArrCruise = dataListCruise.get(0);
        	int cellnumCruise = 0;
        	for (Object obj : objArrCruise) {
        		Cell cell = rowCruise.createCell(cellnumCruise++);
        		cell.setCellStyle(styleHead);
        		if (obj instanceof Date) {
        			cell.setCellValue((Date) obj);
        		} else if (obj instanceof Boolean) {
        			cell.setCellValue((Boolean) obj);
        		} else if (obj instanceof String) {
        			cell.setCellValue((String) obj);
        		} else if (obj instanceof Double) {
        			cell.setCellValue((Double) obj);
        		}
        		sheetCruise.setDefaultColumnWidth(35);
        		sheetCruise.setColumnWidth(1, 2048);
        		sheetCruise.setColumnWidth(2, 3840);
        	}

        	int rownumCruise = 1;
        	for (int i = 1; i < dataListCruise.size(); i++) {
        		objArrCruise = dataListCruise.get(i);
        		rowCruise = sheetCruise.createRow(rownumCruise++);
        		cellnumCruise = 0;
        		for (Object obj : objArrCruise) {
        			Cell cell = rowCruise.createCell(cellnumCruise++);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        		}
        	}
        }
		//--------------------------------------------------------------------------------
        // DESCENT ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.DESCENT)) {
        	Sheet sheetDescent = wb.createSheet("DESCENT");
        	List<Object[]> dataListDescent = new ArrayList<>();

        	dataListDescent.add(new Object[] {"Description","Unit","Value"});
        	dataListDescent.add(new Object[] {"Descent length","nmi", _totalDescentLengthMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        	dataListDescent.add(new Object[] {"Descent length","km", _totalDescentLengthMap.get(xcg).doubleValue(SI.KILOMETER)});
        	dataListDescent.add(new Object[] {"Descent duration","min", _totalDescentTimeMap.get(xcg).doubleValue(NonSI.MINUTE)});
        	dataListDescent.add(new Object[] {"Fuel used during descent","kg", _totalDescentFuelUsedMap.get(xcg).doubleValue(SI.KILOGRAM)});
        	dataListDescent.add(new Object[] {"NOx emissions during descent","g", _totalDescentEmissionsNOxMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListDescent.add(new Object[] {"CO emissions during descent","g", _totalDescentEmissionsCOMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListDescent.add(new Object[] {"HC emissions during descent","g", _totalDescentEmissionsHCMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListDescent.add(new Object[] {"Soot emissions during descent","g", _totalDescentEmissionsSootMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListDescent.add(new Object[] {"CO2 emissions during descent","g", _totalDescentEmissionsCO2Map.get(xcg).doubleValue(SI.GRAM)});
        	dataListDescent.add(new Object[] {"SOx emissions during descent","g", _totalDescentEmissionsSOxMap.get(xcg).doubleValue(SI.GRAM)});
        	dataListDescent.add(new Object[] {"H2O emissions during descent","g", _totalDescentEmissionsH2OMap.get(xcg).doubleValue(SI.GRAM)});

        	Row rowDescent = sheetDescent.createRow(0);
        	Object[] objArrDescent = dataListDescent.get(0);
        	int cellnumDescent = 0;
        	for (Object obj : objArrDescent) {
        		Cell cell = rowDescent.createCell(cellnumDescent++);
        		cell.setCellStyle(styleHead);
        		if (obj instanceof Date) {
        			cell.setCellValue((Date) obj);
        		} else if (obj instanceof Boolean) {
        			cell.setCellValue((Boolean) obj);
        		} else if (obj instanceof String) {
        			cell.setCellValue((String) obj);
        		} else if (obj instanceof Double) {
        			cell.setCellValue((Double) obj);
        		}
        		sheetDescent.setDefaultColumnWidth(35);
        		sheetDescent.setColumnWidth(1, 2048);
        		sheetDescent.setColumnWidth(2, 3840);
        	}

        	int rownumDescent = 1;
        	for (int i = 1; i < dataListDescent.size(); i++) {
        		objArrDescent = dataListDescent.get(i);
        		rowDescent = sheetDescent.createRow(rownumDescent++);
        		cellnumDescent = 0;
        		for (Object obj : objArrDescent) {
        			Cell cell = rowDescent.createCell(cellnumDescent++);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        		}
        	}
        }
        //--------------------------------------------------------------------------------
        // LANDING ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.LANDING)) {
        	Sheet sheetLanding = wb.createSheet("LANDING");
        	List<Object[]> dataListLanding = new ArrayList<>();

        	dataListLanding.add(new Object[] {"Description","Unit","Value"});
        	dataListLanding.add(new Object[] {"Ground roll distance","m", _groundRollDistanceLandingMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Flare distance","m", _flareDistanceLandingMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Airborne distance","m", _airborneDistanceLandingMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Landing distance","m", _landingDistanceMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"FAR-25 landing field length","m", _landingDistanceFAR25Map.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {"Total distance","m", _totalDistanceMap.get(xcg).doubleValue(SI.METER)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Ground roll distance","ft", _groundRollDistanceLandingMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Flare distance","ft", _flareDistanceLandingMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Airborne distance","ft", _airborneDistanceLandingMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Landing distance","ft", _landingDistanceMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"FAR-25 landing field length","ft", _landingDistanceFAR25Map.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {"Total distance","ft", _totalDistanceMap.get(xcg).doubleValue(NonSI.FOOT)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Stall speed landing (VsLND)","m/s", _vStallLandingMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Touchdown speed (V_TD)","m/s", _vTouchDownMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Flare speed (V_Flare)","m/s", _vFlareMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {"Approach speed (V_A)","m/s", _vApproachMap.get(xcg).doubleValue(SI.METERS_PER_SECOND)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Stall speed landing (VsLND)","kn", _vStallLandingMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Touchdown speed (V_TD)","kn", _vTouchDownMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Flare speed (V_Flare)","kn", _vFlareMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {"Approach speed (V_A)","kn", _vApproachMap.get(xcg).doubleValue(NonSI.KNOT)});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"V_TD/VsLND","", _vTouchDownMap.get(xcg).divide(_vStallLandingMap.get(xcg)).getEstimatedValue()});
        	dataListLanding.add(new Object[] {"V_Flare/VsLND","", _vFlareMap.get(xcg).divide(_vStallLandingMap.get(xcg)).getEstimatedValue()});
        	dataListLanding.add(new Object[] {"V_A/VsLND","", _vApproachMap.get(xcg).divide(_vStallLandingMap.get(xcg)).getEstimatedValue()});
        	dataListLanding.add(new Object[] {" "});
        	dataListLanding.add(new Object[] {"Total duration","s", _totalDurationMap.get(xcg).doubleValue(SI.SECOND)});
        	dataListLanding.add(new Object[] {"Landing duration","s", _landingDurationMap.get(xcg).doubleValue(SI.SECOND)});

        	Row rowLanding = sheetLanding.createRow(0);
        	Object[] objArrLanding = dataListLanding.get(0);
        	int cellnumLanding = 0;
        	for (Object obj : objArrLanding) {
        		Cell cell = rowLanding.createCell(cellnumLanding++);
        		cell.setCellStyle(styleHead);
        		if (obj instanceof Date) {
        			cell.setCellValue((Date) obj);
        		} else if (obj instanceof Boolean) {
        			cell.setCellValue((Boolean) obj);
        		} else if (obj instanceof String) {
        			cell.setCellValue((String) obj);
        		} else if (obj instanceof Double) {
        			cell.setCellValue((Double) obj);
        		}
        		sheetLanding.setDefaultColumnWidth(35);
        		sheetLanding.setColumnWidth(1, 2048);
        		sheetLanding.setColumnWidth(2, 3840);
        	}

        	int rownumLanding = 1;
        	for (int i = 1; i < dataListLanding.size(); i++) {
        		objArrLanding = dataListLanding.get(i);
        		rowLanding = sheetLanding.createRow(rownumLanding++);
        		cellnumLanding = 0;
        		for (Object obj : objArrLanding) {
        			Cell cell = rowLanding.createCell(cellnumLanding++);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        		}
        	}
        }
        //--------------------------------------------------------------------------------
        // MISSION PROFILE ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.MISSION_PROFILE)) {
        	Sheet sheetMissionProfile = wb.createSheet("MISSION PROFILE");
        	List<Object[]> dataListMissionProfile = new ArrayList<>();

        	if(_theMissionProfileCalculatorMap.get(xcg).getMissionProfileStopped().equals(Boolean.FALSE)) {

        		dataListMissionProfile.add(new Object[] {"Description","Unit","Value"});
        		dataListMissionProfile.add(new Object[] {"Mission distance","nmi", _thePerformanceInterface.getMissionRange().doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Total mission distance (plus reserves)","nmi", _totalMissionRangeMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Block time","min", _blockTimeMap.get(xcg).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Total mission duration","min", _totalMissionTimeMap.get(xcg).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Aircraft mass at mission start","kg", _initialMissionMassMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft mass at mission end","kg", _endMissionMassMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Initial fuel mass for the assigned mission","kg", _initialFuelMassMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Block fuel","kg", _blockFuelMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Total fuel used","kg", _totalFuelUsedMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Fuel reserve","%", _thePerformanceInterface.getFuelReserve()*100});
        		dataListMissionProfile.add(new Object[] {"Design passengers number","", Integer.valueOf(_thePerformanceInterface.getTheAircraft().getCabinConfiguration().getDesignPassengerNumber()).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Passengers number for this mission","", _theMissionProfileCalculatorMap.get(xcg).getDeisngPassengersNumber()});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Take-off range","nmi", _rangeListMap.get(xcg).get(1).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Climb range","nmi", _rangeListMap.get(xcg).get(2).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(1).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Cruise range","nmi", _rangeListMap.get(xcg).get(3).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(2).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"First descent range","nmi", _rangeListMap.get(xcg).get(4).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(3).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Second climb range","nmi", _rangeListMap.get(xcg).get(5).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(4).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Alternate cruise range","nmi", _rangeListMap.get(xcg).get(6).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(5).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Second descent range","nmi", _rangeListMap.get(xcg).get(7).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(6).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Holding range","nmi", _rangeListMap.get(xcg).get(8).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(7).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Third descent range","nmi", _rangeListMap.get(xcg).get(9).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(8).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {"Landing range","nmi", _rangeListMap.get(xcg).get(10).to(NonSI.NAUTICAL_MILE).minus(_rangeListMap.get(xcg).get(9).to(NonSI.NAUTICAL_MILE)).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Altitude at take-off ending","ft", _altitudeListMap.get(xcg).get(1).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at climb ending","ft", _altitudeListMap.get(xcg).get(2).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at cruise ending","ft", _altitudeListMap.get(xcg).get(3).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at first descent ending","ft", _altitudeListMap.get(xcg).get(4).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at second climb ending","ft", _altitudeListMap.get(xcg).get(5).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at alternate cruise ending","ft", _altitudeListMap.get(xcg).get(6).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at second descent ending","ft", _altitudeListMap.get(xcg).get(7).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at holding ending","ft", _altitudeListMap.get(xcg).get(8).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at third descent ending","ft", _altitudeListMap.get(xcg).get(9).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {"Altitude at landing ending","ft", _altitudeListMap.get(xcg).get(10).doubleValue(NonSI.FOOT)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Take-off duration","min", _timeListMap.get(xcg).get(1).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Climb duration","min", _timeListMap.get(xcg).get(2).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(1).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Cruise duration","min", _timeListMap.get(xcg).get(3).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(2).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"First descent duration","min", _timeListMap.get(xcg).get(4).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(3).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Second climb duration","min", _timeListMap.get(xcg).get(5).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(4).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Alternate cruise duration","min", _timeListMap.get(xcg).get(6).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(5).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Second descent duration","min", _timeListMap.get(xcg).get(7).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(6).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Holding duration","min", _timeListMap.get(xcg).get(8).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(7).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Third descent duration","min", _timeListMap.get(xcg).get(9).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(8).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Landing duration","min", _timeListMap.get(xcg).get(10).to(NonSI.MINUTE).minus(_timeListMap.get(xcg).get(9).to(NonSI.MINUTE)).doubleValue(NonSI.MINUTE)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Take-off used fuel","kg", _fuelUsedListMap.get(xcg).get(1).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Climb used fuel","kg", _fuelUsedListMap.get(xcg).get(2).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(1).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Cruise used fuel","kg", _fuelUsedListMap.get(xcg).get(3).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(2).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"First descent used fuel","kg", _fuelUsedListMap.get(xcg).get(4).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(3).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Second Climb used fuel","kg", _fuelUsedListMap.get(xcg).get(5).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(4).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Alternate cruise used fuel","kg", _fuelUsedListMap.get(xcg).get(6).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(5).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Second descent used fuel","kg", _fuelUsedListMap.get(xcg).get(7).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(6).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Holding used fuel","kg", _fuelUsedListMap.get(xcg).get(8).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(7).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Third descent used fuel","kg", _fuelUsedListMap.get(xcg).get(9).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(8).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Landing used fuel","kg", _fuelUsedListMap.get(xcg).get(10).to(SI.KILOGRAM).minus(_fuelUsedListMap.get(xcg).get(9).to(SI.KILOGRAM)).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at take-off start","kg", _massListMap.get(xcg).get(1).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at climb start","kg", _massListMap.get(xcg).get(2).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at cruise start","kg", _massListMap.get(xcg).get(3).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at first descent start","kg", _massListMap.get(xcg).get(4).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at second climb start","kg", _massListMap.get(xcg).get(5).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at alternate cruise start","kg", _massListMap.get(xcg).get(6).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at second descent start","kg", _massListMap.get(xcg).get(7).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at holding start","kg", _massListMap.get(xcg).get(8).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at third descnet start","kg", _massListMap.get(xcg).get(9).doubleValue(SI.KILOGRAM)});
        		dataListMissionProfile.add(new Object[] {"Aircraft weight at landing start","kg", _massListMap.get(xcg).get(10).doubleValue(SI.KILOGRAM)});        	
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"TAKE-OFF"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at take-off start","kn", _speedTASMissionListMap.get(xcg).get(0).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at take-off ending","kn", _speedTASMissionListMap.get(xcg).get(1).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at take-off start","kn", _speedCASMissionListMap.get(xcg).get(0).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at take-off ending","kn", _speedCASMissionListMap.get(xcg).get(1).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at take-off start"," ", _machMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at take-off ending"," ", _machMissionListMap.get(xcg).get(1).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"CL at take-off start"," ", _liftingCoefficientMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at take-off ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at take-off start"," ", _dragCoefficientMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at take-off ending"," ", _dragCoefficientMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at take-off start"," ", _efficiencyMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at take-off ending"," ", _efficiencyMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at take-off start","lbf", _thrustMissionListMap.get(xcg).get(0).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at take-off ending","lbf", _thrustMissionListMap.get(xcg).get(1).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at take-off start","lbf", _dragMissionListMap.get(xcg).get(0).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at take-off ending","lbf", _dragMissionListMap.get(xcg).get(1).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at take-off start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(0).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at take-off ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(1).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at take-off start","deg", _climbAngleMissionListMap.get(xcg).get(0).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at take-off ending","deg", _climbAngleMissionListMap.get(xcg).get(1).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at take-off start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at take-off ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at take-off start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(0).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at take-off ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"CLIMB"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at climb start","kn", _speedTASMissionListMap.get(xcg).get(2).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at climb ending","kn", _speedTASMissionListMap.get(xcg).get(3).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at climb start","kn", _speedCASMissionListMap.get(xcg).get(2).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at climb ending","kn", _speedCASMissionListMap.get(xcg).get(3).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at climb start"," ", _machMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at climb ending"," ", _machMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at climb start"," ", _liftingCoefficientMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at climb ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at climb start"," ", _dragCoefficientMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at climb ending"," ", _dragCoefficientMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at climb start"," ", _efficiencyMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at climb ending"," ", _efficiencyMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at climb start","lbf", _thrustMissionListMap.get(xcg).get(2).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at climb ending","lbf", _thrustMissionListMap.get(xcg).get(3).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at climb start","lbf", _dragMissionListMap.get(xcg).get(2).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at climb ending","lbf", _dragMissionListMap.get(xcg).get(3).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at climb start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(2).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at climb ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(3).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at climb start","deg", _climbAngleMissionListMap.get(xcg).get(2).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at climb ending","deg", _climbAngleMissionListMap.get(xcg).get(3).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at climb start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at climb ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at climb start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(2).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at climb ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(3).doubleValue()});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"CRUISE"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at cruise start","kn", _speedTASMissionListMap.get(xcg).get(4).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at cruise ending","kn", _speedTASMissionListMap.get(xcg).get(5).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at cruise start","kn", _speedCASMissionListMap.get(xcg).get(4).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at cruise ending","kn", _speedCASMissionListMap.get(xcg).get(5).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at cruise start"," ", _machMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at cruise ending"," ", _machMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at cruise start"," ", _liftingCoefficientMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at cruise ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at cruise start"," ", _dragCoefficientMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at cruise ending"," ", _dragCoefficientMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at cruise start"," ", _efficiencyMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at cruise ending"," ", _efficiencyMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Throttle at cruise start"," ", _throttleMissionListMap.get(xcg).get(0).doubleValue()});
    			dataListMissionProfile.add(new Object[] {"Throttle at cruise ending"," ", _throttleMissionListMap.get(xcg).get(1).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at cruise start","lbf", _thrustMissionListMap.get(xcg).get(4).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at cruise ending","lbf", _thrustMissionListMap.get(xcg).get(5).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at cruise start","lbf", _dragMissionListMap.get(xcg).get(4).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at cruise ending","lbf", _dragMissionListMap.get(xcg).get(5).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at cruise start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(4).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at cruise ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(5).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at cruise start","deg", _climbAngleMissionListMap.get(xcg).get(4).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at cruise ending","deg", _climbAngleMissionListMap.get(xcg).get(5).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at cruise start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at cruise ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at cruise start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(4).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at cruise ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(5).doubleValue()});
        		dataListMissionProfile.add(new Object[] {" "});
        		dataListMissionProfile.add(new Object[] {"FIRST DESCENT"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at first descent start","kn", _speedTASMissionListMap.get(xcg).get(6).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at first descent ending","kn", _speedTASMissionListMap.get(xcg).get(7).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at first descent start","kn", _speedCASMissionListMap.get(xcg).get(6).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at first descent ending","kn", _speedCASMissionListMap.get(xcg).get(7).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at first descent start"," ", _machMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Mach at first descent ending"," ", _machMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at first descent start"," ", _liftingCoefficientMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at first descent ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at first descent start"," ", _dragCoefficientMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at first descent ending"," ", _dragCoefficientMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at first descent start"," ", _efficiencyMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at first descent ending"," ", _efficiencyMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at first descent start","lbf", _thrustMissionListMap.get(xcg).get(6).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Thrust at first descent ending","lbf", _thrustMissionListMap.get(xcg).get(7).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at first descent start", "lbf", _dragMissionListMap.get(xcg).get(6).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at first descent ending", "lbf", _dragMissionListMap.get(xcg).get(7).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at first descent start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(6).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at first descent ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(7).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at first descent start","deg", _climbAngleMissionListMap.get(xcg).get(6).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at first descent ending","deg", _climbAngleMissionListMap.get(xcg).get(7).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at first descent start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at first descent ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at first descent start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(6).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at first descent ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(7).doubleValue()});
        		dataListMissionProfile.add(new Object[] {" "});

        		if(_thePerformanceInterface.getAlternateCruiseAltitude().doubleValue(SI.METER) != Amount.valueOf(15.24, SI.METER).getEstimatedValue()) {
        			dataListMissionProfile.add(new Object[] {"SECOND CLIMB"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at second climb start","kn", _speedTASMissionListMap.get(xcg).get(8).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at second climb ending","kn", _speedTASMissionListMap.get(xcg).get(9).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at second climb start","kn", _speedCASMissionListMap.get(xcg).get(8).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at second climb ending","kn", _speedCASMissionListMap.get(xcg).get(9).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at second climb start"," ", _machMissionListMap.get(xcg).get(8).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at second climb ending"," ", _machMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at second climb start", " ", _liftingCoefficientMissionListMap.get(xcg).get(8).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at second climb ending", " ", _liftingCoefficientMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at second climb start", " ", _dragCoefficientMissionListMap.get(xcg).get(8).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at second climb ending", " ", _dragCoefficientMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at second climb start", " ", _efficiencyMissionListMap.get(xcg).get(8).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at second climb ending", " ", _efficiencyMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at second climb start", "lbf", _thrustMissionListMap.get(xcg).get(8).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at second climb ending", "lbf", _thrustMissionListMap.get(xcg).get(9).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at second climb start","lbf", _dragMissionListMap.get(xcg).get(8).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at second climb ending","lbf", _dragMissionListMap.get(xcg).get(9).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Rate of climb at second climb start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(8).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Rate of climb at second climb ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(9).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at second climb start","deg", _climbAngleMissionListMap.get(xcg).get(8).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at second climb ending","deg", _climbAngleMissionListMap.get(xcg).get(9).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at second climb start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(8).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at second climb ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(9).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at second climb start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(8).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at second climb ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(9).doubleValue()});
        			dataListMissionProfile.add(new Object[] {" "});
        			dataListMissionProfile.add(new Object[] {"ALTERNATE CRUISE"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at alternate cruise start","kn", _speedTASMissionListMap.get(xcg).get(10).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at alternate cruise ending","kn", _speedTASMissionListMap.get(xcg).get(11).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at alternate cruise start","kn", _speedCASMissionListMap.get(xcg).get(10).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at alternate cruise ending","kn", _speedCASMissionListMap.get(xcg).get(11).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at alternate cruise start"," ", _machMissionListMap.get(xcg).get(10).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at alternate cruise ending"," ", _machMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at alternate cruise start"," ", _liftingCoefficientMissionListMap.get(xcg).get(10).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at alternate cruise ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at alternate cruise start"," ", _dragCoefficientMissionListMap.get(xcg).get(10).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at alternate cruise ending"," ", _dragCoefficientMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at alternate cruise start"," ", _efficiencyMissionListMap.get(xcg).get(10).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at alternate cruise ending"," ", _efficiencyMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Throttle at alternate cruise start"," ", _throttleMissionListMap.get(xcg).get(2).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Throttle at alternate cruise ending"," ", _throttleMissionListMap.get(xcg).get(3).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at alternate cruise start","lbf", _thrustMissionListMap.get(xcg).get(10).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at alternate cruise ending","lbf", _thrustMissionListMap.get(xcg).get(11).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at alternate cruise start","lbf", _dragMissionListMap.get(xcg).get(10).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at alternate cruise ending","lbf", _dragMissionListMap.get(xcg).get(11).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Rate of climb at alternate cruise start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(10).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Rate of climb at alternate cruise ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(11).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at alternate cruise start","deg", _climbAngleMissionListMap.get(xcg).get(10).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at alternate cruise ending","deg", _climbAngleMissionListMap.get(xcg).get(11).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at alternate cruise start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(10).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at alternate cruise ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(11).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at alternate cruise start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(10).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at alternate cruise ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(11).doubleValue()});
        			dataListMissionProfile.add(new Object[] {" "});
        			dataListMissionProfile.add(new Object[] {"SECOND DESCENT"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at second descent start","kn", _speedTASMissionListMap.get(xcg).get(12).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at second descent ending","kn", _speedTASMissionListMap.get(xcg).get(13).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at second descent start","kn", _speedCASMissionListMap.get(xcg).get(12).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at second descent ending","kn", _speedCASMissionListMap.get(xcg).get(13).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at second descent start"," ", _machMissionListMap.get(xcg).get(12).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at second descent ending"," ", _machMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at second descent start"," ", _liftingCoefficientMissionListMap.get(xcg).get(12).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at second descent ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at second descent start"," ", _dragCoefficientMissionListMap.get(xcg).get(12).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at second descent ending"," ", _dragCoefficientMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at second descent start"," ", _efficiencyMissionListMap.get(xcg).get(12).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at second descent ending"," ", _efficiencyMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at second descent start","lbf", _thrustMissionListMap.get(xcg).get(12).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at second descent ending","lbf", _thrustMissionListMap.get(xcg).get(13).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at second descent start","lbf", _dragMissionListMap.get(xcg).get(12).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at second descent ending","lbf", _dragMissionListMap.get(xcg).get(13).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Rate of climb at second descent start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(12).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Rate of climb at second descent ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(13).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at second descent start","deg", _climbAngleMissionListMap.get(xcg).get(12).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at second descent ending","deg", _climbAngleMissionListMap.get(xcg).get(13).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at second descent start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(12).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at second descent ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(13).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at second descent start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(12).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at second descent ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(13).doubleValue()});
        			dataListMissionProfile.add(new Object[] {" "});
        		}
        		if(_thePerformanceInterface.getHoldingDuration().doubleValue(NonSI.MINUTE) != 0.0) {
        			dataListMissionProfile.add(new Object[] {"HOLDING"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at holding start","kn", _speedTASMissionListMap.get(xcg).get(14).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at holding ending","kn", _speedTASMissionListMap.get(xcg).get(15).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at holding start","kn", _speedCASMissionListMap.get(xcg).get(14).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at holding ending","kn", _speedCASMissionListMap.get(xcg).get(15).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at holding start"," ", _machMissionListMap.get(xcg).get(14).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at holding ending"," ", _machMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at holding start"," ", _liftingCoefficientMissionListMap.get(xcg).get(14).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at holding ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at holding start"," ", _dragCoefficientMissionListMap.get(xcg).get(14).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at holding ending"," ", _dragCoefficientMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at holding start"," ", _efficiencyMissionListMap.get(xcg).get(14).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at holding ending"," ", _efficiencyMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Throttle at holding start"," ", _throttleMissionListMap.get(xcg).get(4).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Throttle at holding ending"," ", _throttleMissionListMap.get(xcg).get(5).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at holding start","lbf", _thrustMissionListMap.get(xcg).get(14).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at holding ending","lbf", _thrustMissionListMap.get(xcg).get(15).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at holding start","lbf", _dragMissionListMap.get(xcg).get(14).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at holding ending","lbf", _dragMissionListMap.get(xcg).get(15).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Rate of climb at holding start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(14).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Rate of climb at holding ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(15).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at holding start","deg", _climbAngleMissionListMap.get(xcg).get(14).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at holding ending","deg", _climbAngleMissionListMap.get(xcg).get(15).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at holding start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(14).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at holding ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(15).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at holding start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(14).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at holding ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(15).doubleValue()});
        			dataListMissionProfile.add(new Object[] {" "});
        			dataListMissionProfile.add(new Object[] {"THIRD DESCENT"});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at third descent start","kn", _speedTASMissionListMap.get(xcg).get(16).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (TAS) at third descent ending","kn", _speedTASMissionListMap.get(xcg).get(17).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at third descent start","kn", _speedCASMissionListMap.get(xcg).get(16).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Speed (CAS) at third descent ending","kn", _speedCASMissionListMap.get(xcg).get(17).doubleValue(NonSI.KNOT)});
        			dataListMissionProfile.add(new Object[] {"Mach at third descent start"," ", _machMissionListMap.get(xcg).get(16).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Mach at third descent ending"," ", _machMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at third descent start"," ", _liftingCoefficientMissionListMap.get(xcg).get(16).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CL at third descent ending"," ", _liftingCoefficientMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at third descent start"," ", _dragCoefficientMissionListMap.get(xcg).get(16).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"CD at third descent ending"," ", _dragCoefficientMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at third descent start"," ", _efficiencyMissionListMap.get(xcg).get(16).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Efficiency at third descent ending"," ", _efficiencyMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {"Thrust at third descent start","lbf", _thrustMissionListMap.get(xcg).get(16).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Thrust at third descent ending","lbf", _thrustMissionListMap.get(xcg).get(17).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at third descent start","lbf", _dragMissionListMap.get(xcg).get(16).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Drag at third descent ending","lbf", _dragMissionListMap.get(xcg).get(17).doubleValue(NonSI.POUND_FORCE)});
        			dataListMissionProfile.add(new Object[] {"Rate of climb at third descent start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(16).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Rate of climb at third descent ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(17).doubleValue(MyUnits.FOOT_PER_MINUTE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at third descent start","deg", _climbAngleMissionListMap.get(xcg).get(16).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Climb angle at third descent ending","deg", _climbAngleMissionListMap.get(xcg).get(17).doubleValue(NonSI.DEGREE_ANGLE)});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at third descent start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(16).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"Fuel flow at third descent ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(17).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at third descent start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(16).doubleValue()});
            		dataListMissionProfile.add(new Object[] {"SFC at third descent ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(17).doubleValue()});
        			dataListMissionProfile.add(new Object[] {" "});
        		}
        		dataListMissionProfile.add(new Object[] {"LANDING"});
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at landing start","kn", _speedTASMissionListMap.get(xcg).get(18).doubleValue(NonSI.KNOT)});        	
        		dataListMissionProfile.add(new Object[] {"Speed (TAS) at landing ending","kn", _speedTASMissionListMap.get(xcg).get(19).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at landing start","kn", _speedCASMissionListMap.get(xcg).get(18).doubleValue(NonSI.KNOT)});        	
        		dataListMissionProfile.add(new Object[] {"Speed (CAS) at landing ending","kn", _speedCASMissionListMap.get(xcg).get(19).doubleValue(NonSI.KNOT)});
        		dataListMissionProfile.add(new Object[] {"Mach at landing start"," ", _machMissionListMap.get(xcg).get(18).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"Mach at landing ending"," ", _machMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CL at landing start", " ", _liftingCoefficientMissionListMap.get(xcg).get(18).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"CL at landing ending", " ", _liftingCoefficientMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"CD at landing start", " ", _dragCoefficientMissionListMap.get(xcg).get(18).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"CD at landing ending", " ", _dragCoefficientMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Efficiency at landing start", " ", _efficiencyMissionListMap.get(xcg).get(18).doubleValue()});        	
        		dataListMissionProfile.add(new Object[] {"Efficiency at landing ending", " ", _efficiencyMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Thrust at landing start", "lbf", _thrustMissionListMap.get(xcg).get(18).doubleValue(NonSI.POUND_FORCE)});        	
        		dataListMissionProfile.add(new Object[] {"Thrust at landing ending", "lbf", _thrustMissionListMap.get(xcg).get(19).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Drag at landing start", "lbf", _dragMissionListMap.get(xcg).get(18).doubleValue(NonSI.POUND_FORCE)});        	
        		dataListMissionProfile.add(new Object[] {"Drag at landing ending", "lbf", _dragMissionListMap.get(xcg).get(19).doubleValue(NonSI.POUND_FORCE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at landing start","ft/min", _rateOfClimbMissionListMap.get(xcg).get(18).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Rate of climb at landing ending","ft/min", _rateOfClimbMissionListMap.get(xcg).get(19).doubleValue(MyUnits.FOOT_PER_MINUTE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at landing start","deg", _climbAngleMissionListMap.get(xcg).get(18).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Climb angle at landing ending","deg", _climbAngleMissionListMap.get(xcg).get(19).doubleValue(NonSI.DEGREE_ANGLE)});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at landing start","lb/hr", _fuelFlowMissionListMap.get(xcg).get(18).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"Fuel flow at landing ending","lb/hr", _fuelFlowMissionListMap.get(xcg).get(19).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at landing start","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(18).doubleValue()});
        		dataListMissionProfile.add(new Object[] {"SFC at landing ending","lb/(lb*hr)", _sfcMissionListMap.get(xcg).get(19).doubleValue()});

        		Row rowMissionProfile = sheetMissionProfile.createRow(0);
        		Object[] objArrMissionProfile = dataListMissionProfile.get(0);
        		int cellnumMissionProfile = 0;
        		for (Object obj : objArrMissionProfile) {
        			Cell cell = rowMissionProfile.createCell(cellnumMissionProfile++);
        			cell.setCellStyle(styleHead);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        			sheetMissionProfile.setDefaultColumnWidth(35);
        			sheetMissionProfile.setColumnWidth(1, 2048);
        			sheetMissionProfile.setColumnWidth(2, 3840);
        		}

        		int rownumMissionProfile = 1;
        		for (int i = 1; i < dataListMissionProfile.size(); i++) {
        			objArrMissionProfile = dataListMissionProfile.get(i);
        			rowMissionProfile = sheetMissionProfile.createRow(rownumMissionProfile++);
        			cellnumMissionProfile = 0;
        			for (Object obj : objArrMissionProfile) {
        				Cell cell = rowMissionProfile.createCell(cellnumMissionProfile++);
        				if (obj instanceof Date) {
        					cell.setCellValue((Date) obj);
        				} else if (obj instanceof Boolean) {
        					cell.setCellValue((Boolean) obj);
        				} else if (obj instanceof String) {
        					cell.setCellValue((String) obj);
        				} else if (obj instanceof Double) {
        					cell.setCellValue((Double) obj);
        				}
        			}
        		}
        	}
        }
        //--------------------------------------------------------------------------------
        // PAYLOAD-RANGE ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {
        	Sheet sheetPayloadRange = wb.createSheet("PAYLOAD-RANGE");
        	List<Object[]> dataListPayloadRange = new ArrayList<>();

        	if(_thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
        			&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtDesignPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
        			&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxFuel().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
        			&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtZeroPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
        			) {

        		dataListPayloadRange.add(new Object[] {"Description","Unit","Value"});
        		dataListPayloadRange.add(new Object[] {"ALTITUDE","ft",_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(NonSI.FOOT)});
        		dataListPayloadRange.add(new Object[] {"MACH"," ",_thePerformanceInterface.getTheOperatingConditions().getMachCruise()});
        		dataListPayloadRange.add(new Object[] {" "});
        		dataListPayloadRange.add(new Object[] {"RANGE AT MAX PAYLOAD"});
        		dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Payload mass","kg", _maxPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtMaxPayloadMap.get(xcg).doubleValue()});
        		dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _requiredMassAtMaxPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {""});
        		dataListPayloadRange.add(new Object[] {"RANGE AT DESIGN PAYLOAD"});
        		dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtDesignPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Payload mass","kg", _designPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtDesignPayloadMap.get(xcg).doubleValue()});
        		dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _requiredMassAtDesignPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {""});
        		dataListPayloadRange.add(new Object[] {"RANGE AT MAX FUEL"});
        		dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtMaxFuelMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Payload mass","kg", _payloadAtMaxFuelMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Passengers number","", _passengersNumberAtMaxFuelMap.get(xcg).doubleValue()});
        		dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePerformanceInterface.getMaximumFuelMass().doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"RANGE AT ZERO PAYLOAD"});
        		dataListPayloadRange.add(new Object[] {"Range","nmi", _rangeAtZeroPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE)});
        		dataListPayloadRange.add(new Object[] {"Aircraft mass","kg", _takeOffMassAtZeroPayloadMap.get(xcg).doubleValue(SI.KILOGRAM)});
        		dataListPayloadRange.add(new Object[] {"Payload mass","kg", 0.0});
        		dataListPayloadRange.add(new Object[] {"Passengers number","", 0.0});
        		dataListPayloadRange.add(new Object[] {"Fuel mass","kg", _thePerformanceInterface.getMaximumFuelMass().doubleValue(SI.KILOGRAM)});

        		Row rowPayloadRange = sheetPayloadRange.createRow(0);
        		Object[] objArrPayloadRange = dataListPayloadRange.get(0);
        		int cellnumPayloadRange = 0;
        		for (Object obj : objArrPayloadRange) {
        			Cell cell = rowPayloadRange.createCell(cellnumPayloadRange++);
        			cell.setCellStyle(styleHead);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        			sheetPayloadRange.setDefaultColumnWidth(35);
        			sheetPayloadRange.setColumnWidth(1, 2048);
        			sheetPayloadRange.setColumnWidth(2, 3840);
        		}

        		int rownumPayloadRange = 1;
        		for (int i = 1; i < dataListPayloadRange.size(); i++) {
        			objArrPayloadRange = dataListPayloadRange.get(i);
        			rowPayloadRange = sheetPayloadRange.createRow(rownumPayloadRange++);
        			cellnumPayloadRange = 0;
        			for (Object obj : objArrPayloadRange) {
        				Cell cell = rowPayloadRange.createCell(cellnumPayloadRange++);
        				if (obj instanceof Date) {
        					cell.setCellValue((Date) obj);
        				} else if (obj instanceof Boolean) {
        					cell.setCellValue((Boolean) obj);
        				} else if (obj instanceof String) {
        					cell.setCellValue((String) obj);
        				} else if (obj instanceof Double) {
        					cell.setCellValue((Double) obj);
        				}
        			}
        		}
        	}
        }
        //--------------------------------------------------------------------------------
        // NOISE TRAJECTORIES RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.NOISE_TRAJECTORIES)) {
        	Sheet sheetNoiseTrajectories = wb.createSheet("NOISE TRAJECTORIES");
        	List<Object[]> dataListNoiseTrajectories = new ArrayList<>();

        	dataListNoiseTrajectories.add(new Object[] {"Description","Unit","Value"});
        	dataListNoiseTrajectories.add(new Object[] {"SIDELINE ( 100% MAX-TO )"});
        	dataListNoiseTrajectories.add(new Object[] {"Ground distance","m", _certificationPointsLongitudinalDistanceMap.get(xcg).get(0).doubleValue(SI.METER)});
        	dataListNoiseTrajectories.add(new Object[] {"Ground distance","ft", _certificationPointsLongitudinalDistanceMap.get(xcg).get(0).doubleValue(NonSI.FOOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Altitude","m", _certificationPointsAltitudeMap.get(xcg).get(0).doubleValue(SI.METER)});
        	dataListNoiseTrajectories.add(new Object[] {"Altitude","ft", _certificationPointsAltitudeMap.get(xcg).get(0).doubleValue(NonSI.FOOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (TAS)","m/s", _certificationPointsSpeedTASMap.get(xcg).get(0).doubleValue(SI.METERS_PER_SECOND)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (TAS)","kts", _certificationPointsSpeedTASMap.get(xcg).get(0).doubleValue(NonSI.KNOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (CAS)","m/s", _certificationPointsSpeedCASMap.get(xcg).get(0).doubleValue(SI.METERS_PER_SECOND)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (CAS)","kts", _certificationPointsSpeedCASMap.get(xcg).get(0).doubleValue(NonSI.KNOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Alpha","deg", _certificationPointsAlphaMap.get(xcg).get(0).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Gamma","deg", _certificationPointsGammaMap.get(xcg).get(0).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Theta","deg", _certificationPointsThetaMap.get(xcg).get(0).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Thrust (x1 eng.)","N", _certificationPointsThrustMap.get(xcg).get(0).doubleValue(SI.NEWTON)});
        	dataListNoiseTrajectories.add(new Object[] {"Thrust (x1 eng.)","lbf", _certificationPointsThrustMap.get(xcg).get(0).doubleValue(NonSI.POUND_FORCE)});
        	dataListNoiseTrajectories.add(new Object[] {" "});
        	dataListNoiseTrajectories.add(new Object[] {"FLYOVER - CUTBACK ( " + _theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).getCutbackAltitude().doubleValue(NonSI.FOOT) + "ft, " + Math.round((_theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).getPhiCutback()*100)) + "% MAX-TO )"});
        	dataListNoiseTrajectories.add(new Object[] {"Ground distance","m", _certificationPointsLongitudinalDistanceMap.get(xcg).get(1).doubleValue(SI.METER)});
        	dataListNoiseTrajectories.add(new Object[] {"Ground distance","ft", _certificationPointsLongitudinalDistanceMap.get(xcg).get(1).doubleValue(NonSI.FOOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Altitude","m", _certificationPointsAltitudeMap.get(xcg).get(1).doubleValue(SI.METER)});
        	dataListNoiseTrajectories.add(new Object[] {"Altitude","ft", _certificationPointsAltitudeMap.get(xcg).get(1).doubleValue(NonSI.FOOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (TAS)","m/s", _certificationPointsSpeedTASMap.get(xcg).get(1).doubleValue(SI.METERS_PER_SECOND)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (TAS)","kts", _certificationPointsSpeedTASMap.get(xcg).get(1).doubleValue(NonSI.KNOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (CAS)","m/s", _certificationPointsSpeedCASMap.get(xcg).get(1).doubleValue(SI.METERS_PER_SECOND)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (CAS)","kts", _certificationPointsSpeedCASMap.get(xcg).get(1).doubleValue(NonSI.KNOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Alpha","deg", _certificationPointsAlphaMap.get(xcg).get(1).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Gamma","deg", _certificationPointsGammaMap.get(xcg).get(1).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Theta","deg", _certificationPointsThetaMap.get(xcg).get(1).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Thrust (x1 eng.)","N", _certificationPointsThrustMap.get(xcg).get(1).doubleValue(SI.NEWTON)});
        	dataListNoiseTrajectories.add(new Object[] {"Thrust (x1 eng.)","lbf", _certificationPointsThrustMap.get(xcg).get(1).doubleValue(NonSI.POUND_FORCE)});
        	dataListNoiseTrajectories.add(new Object[] {" "});
        	dataListNoiseTrajectories.add(new Object[] {"APPROACH"});
        	dataListNoiseTrajectories.add(new Object[] {"Ground distance (from touch-down)","m", _certificationPointsLongitudinalDistanceMap.get(xcg).get(2).doubleValue(SI.METER)});
        	dataListNoiseTrajectories.add(new Object[] {"Ground distance (from touch-down)","ft", _certificationPointsLongitudinalDistanceMap.get(xcg).get(2).doubleValue(NonSI.FOOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Altitude","m", _certificationPointsAltitudeMap.get(xcg).get(2).doubleValue(SI.METER)});
        	dataListNoiseTrajectories.add(new Object[] {"Altitude","ft", _certificationPointsAltitudeMap.get(xcg).get(2).doubleValue(NonSI.FOOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (TAS)","m/s", _certificationPointsSpeedTASMap.get(xcg).get(2).doubleValue(SI.METERS_PER_SECOND)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (TAS)","kts", _certificationPointsSpeedTASMap.get(xcg).get(2).doubleValue(NonSI.KNOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (CAS)","m/s", _certificationPointsSpeedCASMap.get(xcg).get(2).doubleValue(SI.METERS_PER_SECOND)});
        	dataListNoiseTrajectories.add(new Object[] {"Speed (CAS)","kts", _certificationPointsSpeedCASMap.get(xcg).get(2).doubleValue(NonSI.KNOT)});
        	dataListNoiseTrajectories.add(new Object[] {"Alpha","deg", _certificationPointsAlphaMap.get(xcg).get(2).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Gamma","deg", _certificationPointsGammaMap.get(xcg).get(2).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Theta","deg", _certificationPointsThetaMap.get(xcg).get(2).doubleValue(NonSI.DEGREE_ANGLE)});
        	dataListNoiseTrajectories.add(new Object[] {"Thrust (x1 eng.)","N", _certificationPointsThrustMap.get(xcg).get(2).doubleValue(SI.NEWTON)});
        	dataListNoiseTrajectories.add(new Object[] {"Thrust (x1 eng.)","lbf", _certificationPointsThrustMap.get(xcg).get(2).doubleValue(NonSI.POUND_FORCE)});

        	Row rowNoiseTrajectories = sheetNoiseTrajectories.createRow(0);
        	Object[] objArrNoiseTrajectories = dataListNoiseTrajectories.get(0);
        	int cellnumNoiseTrajectories = 0;
        	for (Object obj : objArrNoiseTrajectories) {
        		Cell cell = rowNoiseTrajectories.createCell(cellnumNoiseTrajectories++);
        		cell.setCellStyle(styleHead);
        		if (obj instanceof Date) {
        			cell.setCellValue((Date) obj);
        		} else if (obj instanceof Boolean) {
        			cell.setCellValue((Boolean) obj);
        		} else if (obj instanceof String) {
        			cell.setCellValue((String) obj);
        		} else if (obj instanceof Double) {
        			cell.setCellValue((Double) obj);
        		}
        		sheetNoiseTrajectories.setDefaultColumnWidth(35);
        		sheetNoiseTrajectories.setColumnWidth(1, 2048);
        		sheetNoiseTrajectories.setColumnWidth(2, 3840);
        	}

        	int rownumVnDiagram = 1;
        	for (int i = 1; i < dataListNoiseTrajectories.size(); i++) {
        		objArrNoiseTrajectories = dataListNoiseTrajectories.get(i);
        		rowNoiseTrajectories = sheetNoiseTrajectories.createRow(rownumVnDiagram++);
        		cellnumNoiseTrajectories = 0;
        		for (Object obj : objArrNoiseTrajectories) {
        			Cell cell = rowNoiseTrajectories.createCell(cellnumNoiseTrajectories++);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        		}
        	}
        }
        //--------------------------------------------------------------------------------
        // V-n DIAGRAM ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.V_n_DIAGRAM)) {
        	Sheet sheetVnDiagram = wb.createSheet("V-n DIAGRAM");
        	List<Object[]> dataListVnDiagram = new ArrayList<>();

        	dataListVnDiagram.add(new Object[] {"Description","Unit","Value"});
        	dataListVnDiagram.add(new Object[] {"REGULATION"," ",_thePerformanceInterface.getTheAircraft().getRegulations().toString()});
        	dataListVnDiagram.add(new Object[] {"AIRCRAFT TYPE"," ",_thePerformanceInterface.getTheAircraft().getTypeVehicle().toString()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"BASIC MANEUVERING DIAGRAM"});
        	dataListVnDiagram.add(new Object[] {"Stall speed clean","m/s", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedClean().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed clean","kn", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedClean().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Stall speed inverted","m/s", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed inverted","kn", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Point A"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorManeuveringSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point C"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorCruisingSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point D"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDiveSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point E"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDiveSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point F"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorCruisingSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point H"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorManeuveringSpeedInverted()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"GUST ENVELOPE"});
        	dataListVnDiagram.add(new Object[] {"Point A'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorManeuveringSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point C'"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorCruisingSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point D'"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDiveSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point E'"});
        	dataListVnDiagram.add(new Object[] {"Dive speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Dive speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDiveSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDiveSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point F'"});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Cruising speed","kn", _theEnvelopeCalculatorMap.get(xcg).getCruisingSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorCruisingSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point H'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed (inverted)","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorManeuveringSpeedInvertedWithGust()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"FLAP MANEUVERING DIAGRAM"});
        	dataListVnDiagram.add(new Object[] {"Stall speed full flap","m/s", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedFullFlap().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Stall speed full flap","kn", _theEnvelopeCalculatorMap.get(xcg).getStallSpeedFullFlap().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Point A_flap"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeed()});
        	dataListVnDiagram.add(new Object[] {"Point I"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeed()});
        	dataListVnDiagram.add(new Object[] {" "});
        	dataListVnDiagram.add(new Object[] {"GUST ENVELOPE (with flaps)"});
        	dataListVnDiagram.add(new Object[] {"Point A'_flap"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","m/s", _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed full flap","kn", _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeedWithGust()});
        	dataListVnDiagram.add(new Object[] {"Point I'"});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","m/s", _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed().doubleValue(SI.METERS_PER_SECOND)});
        	dataListVnDiagram.add(new Object[] {"Maneuvering speed","kn", _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed().doubleValue(NonSI.KNOT)});
        	dataListVnDiagram.add(new Object[] {"Load factor","", _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeedWithGust()});

        	Row rowVnDiagram = sheetVnDiagram.createRow(0);
        	Object[] objArrVnDiagram = dataListVnDiagram.get(0);
        	int cellnumVnDiagram = 0;
        	for (Object obj : objArrVnDiagram) {
        		Cell cell = rowVnDiagram.createCell(cellnumVnDiagram++);
        		cell.setCellStyle(styleHead);
        		if (obj instanceof Date) {
        			cell.setCellValue((Date) obj);
        		} else if (obj instanceof Boolean) {
        			cell.setCellValue((Boolean) obj);
        		} else if (obj instanceof String) {
        			cell.setCellValue((String) obj);
        		} else if (obj instanceof Double) {
        			cell.setCellValue((Double) obj);
        		}
        		sheetVnDiagram.setDefaultColumnWidth(35);
        		sheetVnDiagram.setColumnWidth(1, 2048);
        		sheetVnDiagram.setColumnWidth(2, 3840);
        	}

        	int rownumVnDiagram = 1;
        	for (int i = 1; i < dataListVnDiagram.size(); i++) {
        		objArrVnDiagram = dataListVnDiagram.get(i);
        		rowVnDiagram = sheetVnDiagram.createRow(rownumVnDiagram++);
        		cellnumVnDiagram = 0;
        		for (Object obj : objArrVnDiagram) {
        			Cell cell = rowVnDiagram.createCell(cellnumVnDiagram++);
        			if (obj instanceof Date) {
        				cell.setCellValue((Date) obj);
        			} else if (obj instanceof Boolean) {
        				cell.setCellValue((Boolean) obj);
        			} else if (obj instanceof String) {
        				cell.setCellValue((String) obj);
        			} else if (obj instanceof Double) {
        				cell.setCellValue((Double) obj);
        			}
        		}
        	}
        }
		//--------------------------------------------------------------------------------
		// XLS FILE CREATION:
		//--------------------------------------------------------------------------------
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("\n\tPerformance results Excel file has been generated!\n");
	}
	
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\n\n\t-------------------------------------\n")
				.append("\tPerformance Analysis\n")
				.append("\t-------------------------------------\n")
				;

		for(int i=0; i<_thePerformanceInterface.getXcgPositionList().size(); i++) {

			Double xcg = _thePerformanceInterface.getXcgPositionList().get(i);
			
			sb.append("\n\t-------------------------------------\n")
			.append("\tXCG = " + xcg + "\n")
			.append("\t-------------------------------------\n")
			;
			
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.TAKE_OFF)) {

				sb.append("\tTAKE-OFF\n")
				.append("\t-------------------------------------\n")
				.append("\t\tGround roll distance = " + _groundRollDistanceTakeOffMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tRotation distance = " + _rotationDistanceTakeOffMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAirborne distance = " + _airborneDistanceTakeOffMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAEO take-off distance = " + _takeOffDistanceAEOMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tFAR-25 take-off field length = " + _takeOffDistanceFAR25Map.get(xcg).to(SI.METER) + "\n")
				.append("\t\tBalanced field length = " + _balancedFieldLengthMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tGround roll distance = " + _groundRollDistanceTakeOffMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tRotation distance = " + _rotationDistanceTakeOffMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tAirborne distance = " + _airborneDistanceTakeOffMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tAEO take-off distance = " + _takeOffDistanceAEOMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tFAR-25 take-off field length = " + _takeOffDistanceFAR25Map.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tBalanced field length = " + _balancedFieldLengthMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tStall speed take-off (VsTO)= " + _vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tDecision speed (V1) = " + _v1Map.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tRotation speed (V_Rot) = " + _vRotationMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMiminum control speed (VMC) = " + _vMCMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tLift-off speed (V_LO) = " + _vLiftOffMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tTake-off safety speed (V2) = " + _v2Map.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tStall speed take-off (VsTO)= " + _vStallTakeOffMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tDecision speed (V1) = " + _v1Map.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tRotation speed (V_Rot) = " + _vRotationMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tMiminum control speed (VMC) = " + _vMCMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tLift-off speed (V_LO) = " + _vLiftOffMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tTake-off safety speed (V2) = " + _v2Map.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tV1/VsTO = " + _v1Map.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\tV_Rot/VsTO = " + _vRotationMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\tVMC/VsTO = " + _vMCMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\tV_LO/VsTO = " + _vLiftOffMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\tV2/VsTO = " + _v2Map.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallTakeOffMap.get(xcg).to(SI.METERS_PER_SECOND)) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off duration = " + _takeOffDurationMap.get(xcg) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off fuel used = " + _takeOffFuelMap.get(xcg) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tTake-off NOx emissions = " + _takeOffNOxEmissionsMap.get(xcg) + "\n")
				.append("\t\tTake-off CO emissions = " + _takeOffCOEmissionsMap.get(xcg) + "\n")
				.append("\t\tTake-off HC emissions = " + _takeOffHCEmissionsMap.get(xcg) + "\n")
				.append("\t\tTake-off Soot emissions = " + _takeOffSootEmissionsMap.get(xcg) + "\n")
				.append("\t\tTake-off CO2 emissions = " + _takeOffCO2EmissionsMap.get(xcg) + "\n")
				.append("\t\tTake-off SOx emissions = " + _takeOffSOxEmissionsMap.get(xcg) + "\n")
				.append("\t\tTake-off H2O emissions = " + _takeOffH2OEmissionsMap.get(xcg) + "\n")
				.append("\t-------------------------------------\n")
				;
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CLIMB)) {

				sb.append("\tCLIMB\n")
				.append("\t-------------------------------------\n")
				.append("\t\tAbsolute ceiling AEO = " + _absoluteCeilingAEOMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAbsolute ceiling AEO = " + _absoluteCeilingAEOMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tService ceiling AEO = " + _serviceCeilingAEOMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tService ceiling AEO = " + _serviceCeilingAEOMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tMinimum time to climb AEO = " + _minimumClimbTimeAEOMap.get(xcg).to(NonSI.MINUTE) + "\n");
				if(_climbTimeAtSpecificClimbSpeedAEOMap.get(xcg) != null)
					sb.append("\t\tTime to climb at given climb speed AEO = " + _climbTimeAtSpecificClimbSpeedAEOMap.get(xcg).to(NonSI.MINUTE) + "\n");
				sb.append("\t\tFuel used durign climb AEO = " + _fuelUsedDuringClimbMap.get(xcg).to(SI.KILOGRAM) + "\n")
				.append("\t\tTotal NOx Emissions AEO = " + _emissionsNOxDuringClimbMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tTotal CO Emissions AEO = " + _emissionsCODuringClimbMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tTotal HC Emissions AEO = " + _emissionsHCDuringClimbMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tTotal Soot Emissions AEO = " + _emissionsSootDuringClimbMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tTotal CO2 Emissions AEO = " + _emissionsCO2DuringClimbMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tTotal SOx Emissions AEO = " + _emissionsSOxDuringClimbMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tTotal H2O Emissions AEO = " + _emissionsH2ODuringClimbMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tAbsolute ceiling OEI = " + _absoluteCeilingOEIMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAbsolute ceiling OEI = " + _absoluteCeilingOEIMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tService ceiling OEI = " + _serviceCeilingOEIMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tService ceiling OEI = " + _serviceCeilingOEIMap.get(xcg).to(NonSI.FOOT) + "\n");

				sb.append("\t-------------------------------------\n");

			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.CRUISE)) {

				sb.append("\tCRUISE\n")
				.append("\t-------------------------------------\n")
				.append("\t\tThrust at cruise altitude and Mach = " + _thrustAtCruiseAltitudeAndMachMap.get(xcg).to(SI.NEWTON) + "\n")
				.append("\t\tThrust at cruise altitude and Mach = " + _thrustAtCruiseAltitudeAndMachMap.get(xcg).to(NonSI.POUND_FORCE) + "\n")
				.append("\t\tDrag at cruise altitude and Mach = " + _dragAtCruiseAltitudeAndMachMap.get(xcg).to(SI.NEWTON) + "\n")
				.append("\t\tDrag at cruise altitude and Mach = " + _dragAtCruiseAltitudeAndMachMap.get(xcg).to(NonSI.POUND_FORCE) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tPower available at cruise altitude and Mach = " + _powerAvailableAtCruiseAltitudeAndMachMap.get(xcg).to(SI.WATT) + "\n")
				.append("\t\tPower available at cruise altitude and Mach = " + _powerAvailableAtCruiseAltitudeAndMachMap.get(xcg).to(NonSI.HORSEPOWER) + "\n")
				.append("\t\tPower needed at cruise altitude and Mach = " + _powerNeededAtCruiseAltitudeAndMachMap.get(xcg).to(SI.WATT) + "\n")
				.append("\t\tPower needed at cruise altitude and Mach = " + _powerNeededAtCruiseAltitudeAndMachMap.get(xcg).to(NonSI.HORSEPOWER) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tMin CAS speed at cruise altitude = " + _minSpeesCASAtCruiseAltitudeMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMax CAS speed at cruise altitude = " + _maxSpeesCASAtCruiseAltitudeMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tMin CAS speed at cruise altitude = " + _minSpeesCASAtCruiseAltitudeMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMax CAS speed at cruise altitude = " + _maxSpeesCASAtCruiseAltitudeMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tMin TAS speed at cruise altitude = " + _minSpeesTASAtCruiseAltitudeMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMax TAS speed at cruise altitude = " + _maxSpeesTASAtCruiseAltitudeMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tMin TAS speed at cruise altitude = " + _minSpeesTASAtCruiseAltitudeMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tMax TAS speed at cruise altitude = " + _maxSpeesTASAtCruiseAltitudeMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tMin Mach at cruise altitude = " + _minMachAtCruiseAltitudeMap.get(xcg) + "\n")
				.append("\t\tMax Mach at cruise altitude = " + _maxMachAtCruiseAltitudeMap.get(xcg) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tEfficiency at cruise altitude and Mach = " + _efficiencyAtCruiseAltitudeAndMachMap.get(xcg) + "\n")
				.append("\t-------------------------------------\n");

			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.DESCENT)) {

				sb.append("\tDESCENT\n")
				.append("\t-------------------------------------\n")
				.append("\t\tDescent length = " + _totalDescentLengthMap.get(xcg).to(SI.KILOMETER) + "\n")
				.append("\t\tDescent length = " + _totalDescentLengthMap.get(xcg).to(NonSI.NAUTICAL_MILE) + "\n")
				.append("\t\tDescent duration = " + _totalDescentTimeMap.get(xcg).to(NonSI.MINUTE) + "\n")
				.append("\t\tFuel used during descent = " + _totalDescentFuelUsedMap.get(xcg).to(SI.KILOGRAM) + "\n")
				.append("\t\tNOx emissions during descent = " + _totalDescentEmissionsNOxMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tCO emissions during descent = " + _totalDescentEmissionsCOMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tHC emissions during descent = " + _totalDescentEmissionsHCMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tSoot emissions during descent = " + _totalDescentEmissionsSootMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tCO2 emissions during descent = " + _totalDescentEmissionsCO2Map.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tSOx emissions during descent = " + _totalDescentEmissionsSOxMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t\tH2O emissions during descent = " + _totalDescentEmissionsH2OMap.get(xcg).to(SI.GRAM) + "\n")
				.append("\t-------------------------------------\n");
				;

			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.LANDING)) {

				sb.append("\tLANDING\n")
				.append("\t-------------------------------------\n")
				.append("\t\tGround roll distance = " + _groundRollDistanceLandingMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tFlare distance = " + _flareDistanceLandingMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tAirborne distance = " + _airborneDistanceLandingMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tLanding distance = " + _landingDistanceMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\tFAR-25 landing field length = " + _landingDistanceFAR25Map.get(xcg).to(SI.METER) + "\n")
				.append("\t\tTotal distance = " + _totalDistanceMap.get(xcg).to(SI.METER) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tGround roll distance = " + _groundRollDistanceLandingMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tFlare distance = " + _flareDistanceLandingMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tAirborne distance = " + _airborneDistanceLandingMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tLanding distance = " + _landingDistanceMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tFAR-25 landing field length = " + _landingDistanceFAR25Map.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\tTotal distance = " + _totalDistanceMap.get(xcg).to(NonSI.FOOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tStall speed landing (VsLND)= " + _vStallLandingMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tTouchdown speed (V_TD) = " + _vTouchDownMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tFlare speed (V_Flare) = " + _vFlareMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\tApproach speed (V_Approach) = " + _vApproachMap.get(xcg).to(SI.METERS_PER_SECOND) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tStall speed landing (VsLND)= " + _vStallLandingMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tTouchdown speed (V_TD) = " + _vTouchDownMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tFlare speed (V_Flare) = " + _vFlareMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\tApproach speed (V_Approach) = " + _vApproachMap.get(xcg).to(NonSI.KNOT) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tV_TD/VsLND = " + _vTouchDownMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallLandingMap.get(xcg).to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
				.append("\t\tV_Flare/VsLND = " + _vFlareMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallLandingMap.get(xcg).to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
				.append("\t\tV_Approach/VsLND = " + _vApproachMap.get(xcg).to(SI.METERS_PER_SECOND).divide(_vStallLandingMap.get(xcg).to(SI.METERS_PER_SECOND)).getEstimatedValue() + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tLanding duration = " + _landingDurationMap.get(xcg).to(SI.SECOND) + "\n")
				.append("\t\tTotal duration = " + _totalDurationMap.get(xcg).to(SI.SECOND) + "\n")
				.append("\t-------------------------------------\n")
				;
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.MISSION_PROFILE)) {
				sb.append("\tMISSION PROFILE\n");
				if(_theMissionProfileCalculatorMap.get(xcg).getMissionProfileStopped().equals(Boolean.FALSE))
					sb.append(_theMissionProfileCalculatorMap.get(xcg).toString());
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.PAYLOAD_RANGE)) {
				sb.append("\tPAYLOAD-RANGE\n");
				if(_thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtDesignPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxFuel().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						&& _thePayloadRangeCalculatorMap.get(xcg).getRangeAtZeroPayload().doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						)
				sb.append(_thePayloadRangeCalculatorMap.get(xcg).toString());
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.NOISE_TRAJECTORIES)) {
				sb.append("\tNOISE TRAJECTORIES\n")
				.append("\t-------------------------------------\n")
				.append("\t\tSideline ( 100% MAX-TO )\n")
				.append("\t\t\tGround Distance = " + _certificationPointsLongitudinalDistanceMap.get(xcg).get(0) + "\n")
				.append("\t\t\tAltitude = " + _certificationPointsAltitudeMap.get(xcg).get(0) + "\n")
				.append("\t\t\tSpeed (TAS) = " + _certificationPointsSpeedTASMap.get(xcg).get(0) + "\n")
				.append("\t\t\tSpeed (CAS) = " + _certificationPointsSpeedCASMap.get(xcg).get(0) + "\n")
				.append("\t\t\tAlpha = " + _certificationPointsAlphaMap.get(xcg).get(0) + "\n")
				.append("\t\t\tGamma = " + _certificationPointsGammaMap.get(xcg).get(0) + "\n")
				.append("\t\t\tTheta = " + _certificationPointsThetaMap.get(xcg).get(0) + "\n")
				.append("\t\t\tThrust (x1 eng.) = " + _certificationPointsThrustMap.get(xcg).get(0) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tFlyover - Cutback ( " + _theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).getCutbackAltitude() + ", " + Math.round((_theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).getPhiCutback()*100)) + "% MAX-TO )" + "\n")
				.append("\t\t\tGround Distance = " + _certificationPointsLongitudinalDistanceMap.get(xcg).get(1) + "\n")
				.append("\t\t\tAltitude = " + _certificationPointsAltitudeMap.get(xcg).get(1) + "\n")
				.append("\t\t\tSpeed (TAS) = " + _certificationPointsSpeedTASMap.get(xcg).get(1) + "\n")
				.append("\t\t\tSpeed (CAS) = " + _certificationPointsSpeedCASMap.get(xcg).get(1) + "\n")
				.append("\t\t\tAlpha = " + _certificationPointsAlphaMap.get(xcg).get(1) + "\n")
				.append("\t\t\tGamma = " + _certificationPointsGammaMap.get(xcg).get(1) + "\n")
				.append("\t\t\tTheta = " + _certificationPointsThetaMap.get(xcg).get(1) + "\n")
				.append("\t\t\tThrust (x1 eng.) = " + _certificationPointsThrustMap.get(xcg).get(1) + "\n")
				.append("\t\t.....................................\n")
				.append("\t\tApproach \n")
				.append("\t\t\tGround Distance (from touch-down) = " + _certificationPointsLongitudinalDistanceMap.get(xcg).get(2) + "\n")
				.append("\t\t\tAltitude = " + _certificationPointsAltitudeMap.get(xcg).get(2) + "\n")
				.append("\t\t\tSpeed (TAS) = " + _certificationPointsSpeedTASMap.get(xcg).get(2) + "\n")
				.append("\t\t\tSpeed (CAS) = " + _certificationPointsSpeedCASMap.get(xcg).get(2) + "\n")
				.append("\t\t\tAlpha = " + _certificationPointsAlphaMap.get(xcg).get(2) + "\n")
				.append("\t\t\tGamma = " + _certificationPointsGammaMap.get(xcg).get(2) + "\n")
				.append("\t\t\tTheta = " + _certificationPointsThetaMap.get(xcg).get(2) + "\n")
				.append("\t\t\tThrust (x1 eng.) = " + _certificationPointsThrustMap.get(xcg).get(2) + "\n")
				.append("\t-------------------------------------\n")
				;
			}
			if(_thePerformanceInterface.getTaskList().contains(PerformanceEnum.V_n_DIAGRAM)) {
				sb.append("\tV-n DIAGRAM\n")
				.append(_theEnvelopeCalculatorMap.get(xcg).toString());
			}
		}

		return sb.toString();
	}
	
	
	//............................................................................
	// TAKE-OFF INNER CLASS
	//............................................................................
	public class CalcTakeOff {
		
		public void performTakeOffSimulation(
				Amount<Mass> takeOffMass,
				Amount<Length> altitude,
				double xcg,
				Amount<Velocity> vMC
				) {
			
			Amount<Length> wingToGroundDistance = 
					_thePerformanceInterface.getTheAircraft().getFuselage().getHeightFromGround()
					.plus(_thePerformanceInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2))
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getZApexConstructionAxes()
							.plus(_thePerformanceInterface.getTheAircraft().getWing().getSemiSpan()
									.times(Math.sin(
											_thePerformanceInterface.getTheAircraft().getWing()	
												
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			_theTakeOffCalculatorMap.put(
					xcg, 
					new TakeOffCalc(
							_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(),
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
							_thePerformanceInterface.getTheAircraft().getPowerPlant(),
							_thePerformanceInterface.getPolarCLTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCDTakeOff().get(xcg),
							altitude.to(SI.METER),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureTakeOff(), 
							takeOffMass.to(SI.KILOGRAM),
							_thePerformanceInterface.getDtHold(),
							_thePerformanceInterface.getKCLmaxTakeOff(),
							_thePerformanceInterface.getKRotation(),
							_thePerformanceInterface.getAlphaDotRotation(),
							_thePerformanceInterface.getDragDueToEngineFailure(),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleTakeOff(), 
							_thePerformanceInterface.getKAlphaDot(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getMuBrakeFunction(),
							_thePerformanceInterface.getObstacleTakeOff(),
							wingToGroundDistance,
							_thePerformanceInterface.getWindSpeed(),
							_thePerformanceInterface.getAlphaGround(),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg),
							_thePerformanceInterface.getCLZeroTakeOff().get(xcg),
							_thePerformanceInterface.getCLAlphaTakeOff().get(xcg).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
							_thePerformanceInterface.getTakeOffCalibrationFactorThrust(),
							_thePerformanceInterface.getAprCalibrationFactorThrust(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorThrust(),
							_thePerformanceInterface.getTakeOffCalibrationFactorSFC(),
							_thePerformanceInterface.getAprCalibrationFactorSFC(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorSFC(),
							_thePerformanceInterface.getTakeOffCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getTakeOffCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getTakeOffCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getTakeOffCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getTakeOffCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getTakeOffCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getTakeOffCalibrationFactorEmissionIndexH2O(),
							_thePerformanceInterface.getAprCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getAprCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getAprCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getAprCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getAprCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getAprCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getAprCalibrationFactorEmissionIndexH2O(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexH2O()
							)
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theTakeOffCalculatorMap.get(xcg).calculateTakeOffDistanceODE(null, false, false, vMC);
			
			// Distances:
			_groundRollDistanceTakeOffMap.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getTakeOffResults().getGroundDistance().get(0).to(NonSI.FOOT)
					);
			_rotationDistanceTakeOffMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getTakeOffResults().getGroundDistance().get(1)
					.minus(_groundRollDistanceTakeOffMap.get(xcg)).to(NonSI.FOOT)
					);
			_airborneDistanceTakeOffMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getTakeOffResults().getGroundDistance().get(2)
					.minus(_rotationDistanceTakeOffMap.get(xcg))
					.minus(_groundRollDistanceTakeOffMap.get(xcg)).to(NonSI.FOOT));
			_takeOffDistanceAEOMap.put(
					xcg, 
					_groundRollDistanceTakeOffMap.get(xcg)
					.plus(_rotationDistanceTakeOffMap.get(xcg))
					.plus(_airborneDistanceTakeOffMap.get(xcg)).to(NonSI.FOOT));
			_takeOffDistanceFAR25Map.put(
					xcg, 
					_takeOffDistanceAEOMap.get(xcg).times(1.15).to(NonSI.FOOT)
					);
			
			// Velocities:
			_vStallTakeOffMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getvSTakeOff().to(NonSI.KNOT)
					);
			_vRotationMap.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getvRot().to(NonSI.KNOT)
					);
			_vLiftOffMap.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getvLO().to(NonSI.KNOT)
					);
			_v2Map.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getV2().to(NonSI.KNOT)
					);
			
			// Duration:
			_takeOffDurationMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getTakeOffResults().getTime().get(2)
					);
			
			// Fuel used:
			_takeOffFuelMap.put(
					xcg,
					Amount.valueOf(
							_theTakeOffCalculatorMap.get(xcg).getFuelUsed().stream()
							.mapToDouble(f -> f.doubleValue(SI.KILOGRAM))
							.sum(),
							SI.KILOGRAM
							)
					);
			
			// Emission NOx:
			_takeOffNOxEmissionsMap.put(
					xcg,
					Amount.valueOf(
							_theTakeOffCalculatorMap.get(xcg).getEmissionsNOx().stream()
							.mapToDouble(e -> e.doubleValue(SI.GRAM))
							.sum(),
							SI.GRAM
							)
					);
			
			// Emission CO:
			_takeOffCOEmissionsMap.put(
					xcg,
					Amount.valueOf(
							_theTakeOffCalculatorMap.get(xcg).getEmissionsCO().stream()
							.mapToDouble(e -> e.doubleValue(SI.GRAM))
							.sum(),
							SI.GRAM
							)
					);
			
			// Emission HC:
			_takeOffHCEmissionsMap.put(
					xcg,
					Amount.valueOf(
							_theTakeOffCalculatorMap.get(xcg).getEmissionsHC().stream()
							.mapToDouble(e -> e.doubleValue(SI.GRAM))
							.sum(),
							SI.GRAM
							)
					);
			
			// Emission Soot:
			_takeOffSootEmissionsMap.put(
					xcg,
					Amount.valueOf(
							_theTakeOffCalculatorMap.get(xcg).getEmissionsSoot().stream()
							.mapToDouble(e -> e.doubleValue(SI.GRAM))
							.sum(),
							SI.GRAM
							)
					);
			
			// Emission CO2:
			_takeOffCO2EmissionsMap.put(
					xcg,
					Amount.valueOf(
							_theTakeOffCalculatorMap.get(xcg).getEmissionsCO2().stream()
							.mapToDouble(e -> e.doubleValue(SI.GRAM))
							.sum(),
							SI.GRAM
							)
					);
			
			// Emission SOx:
			_takeOffSOxEmissionsMap.put(
					xcg,
					Amount.valueOf(
							_theTakeOffCalculatorMap.get(xcg).getEmissionsSOx().stream()
							.mapToDouble(e -> e.doubleValue(SI.GRAM))
							.sum(),
							SI.GRAM
							)
					);

			// Emission H2O:
			_takeOffH2OEmissionsMap.put(
					xcg,
					Amount.valueOf(
							_theTakeOffCalculatorMap.get(xcg).getEmissionsH2O().stream()
							.mapToDouble(e -> e.doubleValue(SI.GRAM))
							.sum(),
							SI.GRAM
							)
					);
			
		}
		
		public void calculateBalancedFieldLength(Double xcg, Amount<Velocity> vMC) {
			
			_theTakeOffCalculatorMap.get(xcg).calculateBalancedFieldLength(vMC);
			
			_v1Map.put(
					xcg, 
					_theTakeOffCalculatorMap.get(xcg).getV1().to(NonSI.KNOT)
					);
			_balancedFieldLengthMap.put(
					xcg,
					_theTakeOffCalculatorMap.get(xcg).getBalancedFieldLength().to(NonSI.FOOT)
					);
			
		}
		
		public void calculateVMC(Double xcg) {
			
			Amount<Length> dimensionalXcg = 
					_thePerformanceInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER).times(xcg)
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER));
			
			String veDSCDatabaseFileName = "VeDSC_database.h5";
			
			VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
					new VeDSCDatabaseReader(
							MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), veDSCDatabaseFileName
							),
					MyConfiguration.getDir(FoldersEnum.DATABASE_DIR)
					);

			// GETTING THE FUSELAGE HEGHT AR V-TAIL MAC (c/4)
			List<Amount<Length>> vX = _thePerformanceInterface.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountX();
			List<Amount<Length>> vZUpper = _thePerformanceInterface.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountZ();
			List<Amount<Length>> vZLower = _thePerformanceInterface.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountZ();
			
			List<Amount<Length>> sectionHeightsList = new ArrayList<>();
			List<Amount<Length>> xListInterpolation = new ArrayList<>();
			for(int i=vX.size()-5; i<vX.size(); i++) {
				sectionHeightsList.add(
						vZUpper.get(i).minus(vZLower.get(i))
						);
				xListInterpolation.add(vX.get(i));
			}
			
			Amount<Length> diameterAtVTailQuarteMAC = 
					Amount.valueOf( 
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(xListInterpolation),
									MyArrayUtils.convertListOfAmountTodoubleArray(sectionHeightsList),
									_thePerformanceInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX()
									.plus(_thePerformanceInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
									.plus(_thePerformanceInterface.getTheAircraft().getVTail().getMeanAerodynamicChord().times(0.25))
									.doubleValue(SI.METER)
									),
							SI.METER
							);
			
			double tailConeTipToFuselageRadiusRatio = 
					_thePerformanceInterface.getTheAircraft().getFuselage().getTailTipOffset()
					.divide(_thePerformanceInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2))
					.getEstimatedValue();
			
			veDSCDatabaseReader.runAnalysis(
					_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(), 
					_thePerformanceInterface.getTheAircraft().getWing().getPositionRelativeToAttachment(), 
					_thePerformanceInterface.getTheAircraft().getVTail().getAspectRatio(), 
					_thePerformanceInterface.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER), 
					_thePerformanceInterface.getTheAircraft().getHTail().getPositionRelativeToAttachment(),
					diameterAtVTailQuarteMAC.doubleValue(SI.METER), 
					tailConeTipToFuselageRadiusRatio
					);

			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getTheBalance() == null)
				_thePerformanceInterface.getTheAircraft().calculateArms(_thePerformanceInterface.getTheAircraft().getVTail(), dimensionalXcg);
			
			// cNb vertical [1/deg]
			double cNbVertical = MomentCalc.calcCNbetaVerticalTailVEDSC(
					_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(), 
					_thePerformanceInterface.getTheAircraft().getVTail().getAspectRatio(),
					_thePerformanceInterface.getTheAircraft().getVTail().getLiftingSurfaceArm().doubleValue(SI.METER),
					_thePerformanceInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER),
					_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
					_thePerformanceInterface.getTheAircraft().getVTail().getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
					_thePerformanceInterface.getTheAircraft().getVTail().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
					_thePerformanceInterface.getTheAircraft().getVTail().getAirfoilList().get(0)
						.getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
					_thePerformanceInterface.getTheOperatingConditions().getMachTakeOff(), 
					veDSCDatabaseReader.getkFv(),
					veDSCDatabaseReader.getkWv(),
					veDSCDatabaseReader.getkHv()
					);
			
			//..................................................................................
			// CALCULATING THE THRUST YAWING MOMENT
			double[] speed = MyArrayUtils.linspace(
					SpeedCalc.calculateTAS(
							0.05,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureTakeOff()
							).doubleValue(SI.METERS_PER_SECOND),
					SpeedCalc.calculateSpeedStall(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureTakeOff(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg)
							).times(1.2).doubleValue(SI.METERS_PER_SECOND),
					250
					);

			List<Amount<Force>> thrust = new ArrayList<>();
			thrust = ThrustCalc.calculateThrustVsSpeed(
					EngineOperatingConditionEnum.APR,
					_thePerformanceInterface.getTheAircraft().getPowerPlant(),
					MyArrayUtils.convertDoubleArrayToListOfAmount(speed, SI.METERS_PER_SECOND),
					_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff(),
					_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureTakeOff(),
					_thePerformanceInterface.getTheOperatingConditions().getThrottleTakeOff(), 
					true,
					_thePerformanceInterface.getAprCalibrationFactorThrust()
					);

			List<Amount<Length>> enginesArms = new ArrayList<>();
			for(int i=0; i<_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().size(); i++)
				enginesArms.add(_thePerformanceInterface.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes());
			
			Amount<Length> maxEngineArm = 
					Amount.valueOf(
							MyArrayUtils.getMax(
									MyArrayUtils.convertListOfAmountToDoubleArray(
											enginesArms
											)
									),
							SI.METER
							);
			
			_thrustMomentOEIMap.put(
					xcg, 
					new double[thrust.size()]
					);
			
			for(int i=0; i < thrust.size(); i++){
				_thrustMomentOEIMap.get(xcg)[i] = thrust.get(i).doubleValue(SI.NEWTON)*maxEngineArm.doubleValue(SI.METER);
			}

			//..................................................................................
			// CALCULATING THE VERTICAL TAIL YAWING MOMENT
			_yawingMomentOEIMap.put(xcg, new double[_thrustMomentOEIMap.get(xcg).length]);
			
			for(int i=0; i < thrust.size(); i++){
			_yawingMomentOEIMap.get(xcg)[i] = cNbVertical*
					_thePerformanceInterface.getTauRudderMap().get(xcg).value(
							_thePerformanceInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE)
							)*
					_thePerformanceInterface.getTheAircraft().getVTail().getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE)*
					0.5*
					_thePerformanceInterface.getTheOperatingConditions().getDensityTakeOff().getEstimatedValue()*
					Math.pow(speed[i],2)*
					_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE)*
					_thePerformanceInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER);
			}
			
			//..................................................................................
			// CALCULATING THE VMC
			
			double[] curvesIntersection = MyArrayUtils.intersectArraysSimple(
					_thrustMomentOEIMap.get(xcg),
					_yawingMomentOEIMap.get(xcg)
					);
			int indexOfVMC = 0;
			for(int i=0; i<curvesIntersection.length; i++)
				if(curvesIntersection[i] != 0.0) {
					indexOfVMC = i;
				}			

			if(indexOfVMC != 0)
				_vMCMap.put(
						xcg, 
						Amount.valueOf(
								speed[indexOfVMC],
								SI.METERS_PER_SECOND
								).to(NonSI.KNOT)
						);
			else {
				System.err.println("WARNING: (VMC - TAKE-OFF) NO INTERSECTION FOUND ...");
				_vMCMap.put(
						xcg, 
						Amount.valueOf(
								0.0,
								SI.METERS_PER_SECOND
								).to(NonSI.KNOT)
						);
			}
			
		}

		public void plotTakeOffPerformance(String takeOffFolderPath, Double xcg) {

			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.TAKE_OFF_SIMULATIONS))
				try {
					_theTakeOffCalculatorMap.get(xcg).createTakeOffCharts(
							takeOffFolderPath,
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

		}
		
		public void plotBalancedFieldLength(String takeOffFolderPath, Double xcg) {
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.BALANCED_FIELD_LENGTH))
				_theTakeOffCalculatorMap.get(xcg).createBalancedFieldLengthChart(
						takeOffFolderPath,
						_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
						);
			
		}
		
		public void plotVMC(String takeOffFolderPath, Double xcg) {
			
			double[] speed = MyArrayUtils.linspace(
					SpeedCalc.calculateTAS(
							0.05,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureTakeOff()
							).doubleValue(SI.METERS_PER_SECOND)
					/SpeedCalc.calculateSpeedStall(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeTakeOff(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureTakeOff(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg)
							).doubleValue(SI.METERS_PER_SECOND),
					1.20, // maximum possible value of the VMC
					250
					);
			
			double[][] thrustPlotVector = new double [2][speed.length];
			double[][] speedPlotVector = new double [2][speed.length];
			
			for(int i=0; i < speed.length; i++){
				speedPlotVector[0][i] = speed[i];
				speedPlotVector[1][i] = speed[i];
				thrustPlotVector[0][i] = _thrustMomentOEIMap.get(xcg)[i];
				thrustPlotVector[1][i] = _yawingMomentOEIMap.get(xcg)[i];
			}
			String[] legendValue = new String[2];
			legendValue[0] = "Thrust Moment";
			legendValue[1] = "Yawning Moment";
			
			MyChartToFileUtils.plot(speedPlotVector,thrustPlotVector,
					null, null, null, null,
					"V/VsTO", "Thrust - Yawing Moment",
					"", "N m",legendValue,
					takeOffFolderPath, "VMC",
					_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance());
			
		}
		
	}
	//............................................................................
	// END OF THE TAKE-OFF INNER CLASS
	//............................................................................
	
	//............................................................................
	// CLIMB INNER CLASS
	//............................................................................
	public class CalcClimb {

		public void calculateClimbPerformance(
				Amount<Mass> startClimbMassAEO,
				Amount<Mass> startClimbMassOEI,
				Amount<Length> initialClimbAltitude,
				Amount<Length> finalClimbAltitude,
				boolean performOEI,
				Double xcg
				) {
			
			_theClimbCalculatorMap.put(
					xcg, 
					new ClimbCalc(
							_thePerformanceInterface.getTheAircraft(),
							_thePerformanceInterface.getTheOperatingConditions(),
							_thePerformanceInterface.getCLmaxClean().get(xcg),
							_thePerformanceInterface.getPolarCLClimb().get(xcg),
							_thePerformanceInterface.getPolarCDClimb().get(xcg),
							_thePerformanceInterface.getClimbSpeedCAS(),
							_thePerformanceInterface.getDragDueToEngineFailure(),
							_thePerformanceInterface.getClimbCalibrationFactorThrust(),
							_thePerformanceInterface.getContinuousCalibrationFactorThrust(),
							_thePerformanceInterface.getClimbCalibrationFactorSFC(),
							_thePerformanceInterface.getClimbCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getClimbCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getClimbCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getClimbCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getClimbCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getClimbCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getClimbCalibrationFactorEmissionIndexH2O()
							)
					);
			
			_theClimbCalculatorMap.get(xcg).calculateClimbPerformance(
					startClimbMassAEO,
					startClimbMassOEI,
					initialClimbAltitude,
					finalClimbAltitude,
					performOEI,
					true
					);
			
			_rcAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getRCMapAEO());
			_rcOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getRCMapOEI());
			_ceilingAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getCeilingMapAEO());
			_ceilingOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getCeilingMapOEI());
			_dragListAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getDragListAEO());
			_thrustListAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getThrustListOEI());
			_dragListOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getDragListOEI());
			_thrustListOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getThrustListOEI());
			_efficiencyAltitudeAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getEfficiencyMapAltitudeAEO());
			_absoluteCeilingAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getAbsoluteCeilingAEO());
			_serviceCeilingAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getServiceCeilingAEO());
			_minimumClimbTimeAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getMinimumClimbTimeAEO());
			_climbTimeAtSpecificClimbSpeedAEOMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTimeAtSpecificClimbSpeedAEO());
			_fuelUsedDuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalFuelUsed());
			_emissionsNOxDuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalEmissionsNOx());
			_emissionsCODuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalEmissionsCO());
			_emissionsHCDuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalEmissionsHC());
			_emissionsSootDuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalEmissionsSoot());
			_emissionsCO2DuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalEmissionsCO2());
			_emissionsSOxDuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalEmissionsSOx());
			_emissionsH2ODuringClimbMap.put(xcg, _theClimbCalculatorMap.get(xcg).getClimbTotalEmissionsH2O());
			
			_absoluteCeilingOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getAbsoluteCeilingOEI());
			_serviceCeilingOEIMap.put(xcg, _theClimbCalculatorMap.get(xcg).getServiceCeilingOEI());
			
		}
		
		public void plotClimbPerformance(String climbFolderPath, Double xcg) {
			
			_theClimbCalculatorMap.get(xcg).plotClimbPerformance(_thePerformanceInterface.getPlotList(), climbFolderPath);
			
		}
	}
	//............................................................................
	// END OF THE CLIMB INNER CLASS
	//............................................................................

	//............................................................................
	// CRUISE INNER CLASS
	//............................................................................
	public class CalcCruise {
		
		public void calculateThrustAndDrag(Amount<Mass> startCruiseMass, Double xcg) {
			
			//--------------------------------------------------------------------
			// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
			Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing());

			_dragListAltitudeParameterizationMap.put(xcg, new ArrayList<DragMap>());
			_thrustListAltitudeParameterizationMap.put(xcg, new ArrayList<ThrustMap>());

			double[] speedArrayAltitudeParameterization = new double[100];

			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				//..................................................................................................
				speedArrayAltitudeParameterization = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_thePerformanceInterface.getAltitudeListCruise().get(i),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
								startCruiseMass,
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
								).doubleValue(SI.METERS_PER_SECOND),
						SpeedCalc.calculateTAS(
								_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
								_thePerformanceInterface.getAltitudeListCruise().get(i),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
								).doubleValue(SI.METERS_PER_SECOND),
						100
						);
				
				if (!MathArrays.isMonotonic(speedArrayAltitudeParameterization, OrderDirection.INCREASING, true)) {
					System.err.println("WARNING: (THRUST CALCULATION ALTITUDE PARAMETERIZATION - CRUISE) THE SPEED ARRAY IS NOT MONOTONIC INCREASING. CRUISE STALL SPEED IS BIGGER THAN CRUISE SPEED. TERMINATING ...");
					return;
				}
				
				//..................................................................................................
				_dragListAltitudeParameterizationMap.get(xcg).add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getAltitudeListCruise().get(i),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
								startCruiseMass,
								MyArrayUtils.convertDoubleArrayToListOfAmount(speedArrayAltitudeParameterization, SI.METERS_PER_SECOND),
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								_thePerformanceInterface.getPolarCLCruise().get(xcg),
								_thePerformanceInterface.getPolarCDCruise().get(xcg),
//								MyArrayUtils.convertToDoublePrimitive(
//										Arrays.stream(_thePerformanceInterface.getPolarCDCruise().get(xcg))
//										.map(cd -> cd + (
//												-0.000000000002553*Math.pow(startCruiseMass.doubleValue(SI.KILOGRAM), 2)
//												+ 0.000000209147028*startCruiseMass.doubleValue(SI.KILOGRAM)	
//												-0.003767654434394
//												)
//												)
//										.collect(Collectors.toList())
//										),
								_thePerformanceInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);

				//..................................................................................................
				_thrustListAltitudeParameterizationMap.get(xcg).add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								_thePerformanceInterface.getAltitudeListCruise().get(i), 
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(), 
								startCruiseMass, 
								MyArrayUtils.convertDoubleArrayToListOfAmount(speedArrayAltitudeParameterization, SI.METERS_PER_SECOND), 
								EngineOperatingConditionEnum.CRUISE, 
								_thePerformanceInterface.getTheAircraft().getPowerPlant(), 
								false,
								_thePerformanceInterface.getCruiseCalibrationFactorThrust()
								)
						);
			}

			List<Amount<Force>> thrustAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Force>> dragAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Power>> powerAvailableAltitudesAtCruiseMach = new ArrayList<>();
			List<Amount<Power>> powerNeededAltitudesAtCruiseMach = new ArrayList<>();
			
			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				
				thrustAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()),
										MyArrayUtils.convertListOfAmountTodoubleArray(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getThrust()),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
												).doubleValue(SI.METERS_PER_SECOND)
										),
								SI.NEWTON
								)
						);
				dragAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()),
												MyArrayUtils.convertListOfAmountTodoubleArray(_dragListAltitudeParameterizationMap.get(xcg).get(i).getDrag()),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
												).doubleValue(SI.METERS_PER_SECOND)
										),
								SI.NEWTON
								)
						);
				powerAvailableAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()),
										MyArrayUtils.convertListOfAmountTodoubleArray(_thrustListAltitudeParameterizationMap.get(xcg).get(i).getPower()),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
												).doubleValue(SI.METERS_PER_SECOND)
										),
								SI.WATT
								)
						);
				powerNeededAltitudesAtCruiseMach.add(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()),
										MyArrayUtils.convertListOfAmountTodoubleArray(_dragListAltitudeParameterizationMap.get(xcg).get(i).getPower()),
										SpeedCalc.calculateTAS(
												_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
												_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
												).doubleValue(SI.METERS_PER_SECOND)
										),
								SI.WATT
								)
						);
			}
				
				
			_thrustAtCruiseAltitudeAndMachMap.put(
					xcg, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(thrustAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.NEWTON
							)
					);
			_dragAtCruiseAltitudeAndMachMap.put(
					xcg, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(dragAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.NEWTON
							)
					);
			_powerAvailableAtCruiseAltitudeAndMachMap.put(
					xcg,  
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(powerAvailableAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.WATT
							)
					);
			_powerNeededAtCruiseAltitudeAndMachMap.put(
					xcg,  
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertListOfAmountTodoubleArray(_thePerformanceInterface.getAltitudeListCruise()),
									MyArrayUtils.convertListOfAmountTodoubleArray(powerNeededAltitudesAtCruiseMach),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.WATT
							)
					); 
			

			//--------------------------------------------------------------------
			// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
			_dragListWeightParameterizationMap.put(xcg, new ArrayList<DragMap>());
			_thrustListWeightParameterizationMap.put(xcg, new ArrayList<ThrustMap>());
			
			double[] speedArrayWeightParameterization = new double[100];

			for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
				//..................................................................................................
				speedArrayWeightParameterization = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
								Amount.valueOf(_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.KILOGRAM),
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
								).doubleValue(SI.METERS_PER_SECOND),
						SpeedCalc.calculateTAS(
								_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
								Amount.valueOf(_thePerformanceInterface.getAltitudeListCruise().stream().mapToDouble(h -> h.doubleValue(SI.METER)).min().getAsDouble(), SI.METER),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
								).doubleValue(SI.METERS_PER_SECOND),
						100
						);
				
				if (!MathArrays.isMonotonic(speedArrayWeightParameterization, OrderDirection.INCREASING, true)) {
					System.err.println("WARNING: (THRUST CALCULATION WEIGHT PARAMETERIZATION - CRUISE) THE SPEED ARRAY IS NOT MONOTONIC INCREASING. CRUISE STALL SPEED IS BIGGER THAN CRUISE SPEED. TERMINATING ...");
					System.exit(1);
				}
				//..................................................................................................
//				double currentWeight = _weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON)/9.81;
				_dragListWeightParameterizationMap.get(xcg).add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
								Amount.valueOf(_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.KILOGRAM),
								MyArrayUtils.convertDoubleArrayToListOfAmount(speedArrayWeightParameterization, SI.METERS_PER_SECOND),
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								_thePerformanceInterface.getPolarCLCruise().get(xcg),
								_thePerformanceInterface.getPolarCDCruise().get(xcg),
//								MyArrayUtils.convertToDoublePrimitive(
//										Arrays.stream(_thePerformanceInterface.getPolarCDCruise().get(xcg))
//										.map(cd -> cd + (
//												-0.000000000002553*Math.pow(currentWeight, 2)
//												+ 0.000000209147028*currentWeight	
//												-0.003767654434394
//												)
//												)
//										.collect(Collectors.toList())
//										),
								_thePerformanceInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);
			}
			//..................................................................................................
			_thrustListWeightParameterizationMap.get(xcg).add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
							Amount.valueOf(_weightListCruiseMap.get(xcg).get(0).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.KILOGRAM),
							MyArrayUtils.convertDoubleArrayToListOfAmount(
									MyArrayUtils.linspace(
											SpeedCalc.calculateSpeedStall(
													_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
													_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
													Amount.valueOf(_weightListCruiseMap.get(xcg).get(0).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.KILOGRAM),
													_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
													MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
													).doubleValue(SI.METERS_PER_SECOND),
											SpeedCalc.calculateTAS(
													_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
													Amount.valueOf(_thePerformanceInterface.getAltitudeListCruise().stream().mapToDouble(h -> h.doubleValue(SI.METER)).min().getAsDouble(), SI.METER),
													_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
													).doubleValue(SI.METERS_PER_SECOND),
											100
											),
									SI.METERS_PER_SECOND
									), 
							EngineOperatingConditionEnum.CRUISE, 
							_thePerformanceInterface.getTheAircraft().getPowerPlant(), 
							false,
							_thePerformanceInterface.getCruiseCalibrationFactorThrust()
							)
					);
		}

		public void calculateFlightEnvelope(Amount<Mass> startCruiseMass, Double xcg) {

			Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing());

			_intersectionListMap.put(xcg, new ArrayList<>());
			_cruiseEnvelopeListMap.put(xcg, new ArrayList<>());

			List<DragMap> dragList = new ArrayList<>();
			List<ThrustMap> thrustList = new ArrayList<>();
			List<Amount<Length>> altitude = new ArrayList<>();
			altitude.add(Amount.valueOf(0.0, SI.METER));
			Amount<Length> deltaAltitude = Amount.valueOf(100, SI.METER);
			
			int nPointSpeed = 5000;
			double[] speedArray = new double[nPointSpeed];
			int i=0;
			
			// FIRST ITERATION STEP:
			//..................................................................................................
			speedArray = MyArrayUtils.linspace(
					SpeedCalc.calculateSpeedStall(
							altitude.get(0),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
							startCruiseMass,
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
							).doubleValue(SI.METERS_PER_SECOND),
					SpeedCalc.calculateTAS(
							2.0, 
							altitude.get(0),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
							).doubleValue(SI.METERS_PER_SECOND),
					nPointSpeed
					);
			//..................................................................................................
			dragList.add(
					DragCalc.calculateDragAndPowerRequired(
							altitude.get(0), 
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
							startCruiseMass, 
							MyArrayUtils.convertDoubleArrayToListOfAmount(speedArray, SI.METERS_PER_SECOND), 
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							_thePerformanceInterface.getPolarCLCruise().get(xcg),
							_thePerformanceInterface.getPolarCDCruise().get(xcg),
//							MyArrayUtils.convertToDoublePrimitive(
//									Arrays.stream(_thePerformanceInterface.getPolarCDCruise().get(xcg))
//									.map(cd -> cd + (
//											-0.000000000002553*Math.pow(startCruiseMass.doubleValue(SI.KILOGRAM), 2)
//											+ 0.000000209147028*startCruiseMass.doubleValue(SI.KILOGRAM)	
//											-0.003767654434394
//											)
//											)
//									.collect(Collectors.toList())
//									),
							_thePerformanceInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
							meanAirfoil.getThicknessToChordRatio(),
							meanAirfoil.getType()
							)
					);
					
			//..................................................................................................
			thrustList.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							altitude.get(0), 
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(), 
							startCruiseMass, 
							MyArrayUtils.convertDoubleArrayToListOfAmount(speedArray, SI.METERS_PER_SECOND), 
							EngineOperatingConditionEnum.CRUISE, 
							_thePerformanceInterface.getTheAircraft().getPowerPlant(), 
							false,
							_thePerformanceInterface.getCruiseCalibrationFactorThrust()
							)
					);
			//..................................................................................................
			_intersectionListMap.get(xcg).add(
					PerformanceCalcUtils.calculateDragThrustIntersection(
							altitude.get(0),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
							MyArrayUtils.convertDoubleArrayToListOfAmount(speedArray, SI.METERS_PER_SECOND), 
							startCruiseMass, 
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
							EngineOperatingConditionEnum.CRUISE, 
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							dragList,
							thrustList
							)
					);

			while((_intersectionListMap.get(xcg).get(_intersectionListMap.get(xcg).size()-1).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND)
					- _intersectionListMap.get(xcg).get(_intersectionListMap.get(xcg).size()-1).getMinSpeed().doubleValue(SI.METERS_PER_SECOND))
					>= 0.0001
					) {

				if ((_intersectionListMap.get(xcg).get(_intersectionListMap.get(xcg).size()-1).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND)
						- _intersectionListMap.get(xcg).get(_intersectionListMap.get(xcg).size()-1).getMinSpeed().doubleValue(SI.METERS_PER_SECOND))
						< 0.0001
						) {
					System.err.println("");
				}
				
				if(i >= 1)
					altitude.add(altitude.get(i-1).plus(deltaAltitude));

				if(altitude.get(i).doubleValue(NonSI.FOOT) >= 50000) {
					System.err.println("WARNING: (FLIGHT ENVELOPE - CRUISE) MAXIMUM ALTITUDE OF 50000ft REACHED!!");
					break;
				}
					
				//..................................................................................................
				speedArray = MyArrayUtils.linspace(
						SpeedCalc.calculateSpeedStall(
								altitude.get(i),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
								startCruiseMass,
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
								).doubleValue(SI.METERS_PER_SECOND),
						SpeedCalc.calculateTAS(
								2.0, 
								altitude.get(i),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
								).doubleValue(SI.METERS_PER_SECOND),
						nPointSpeed
						);
				//..................................................................................................
				dragList.add(
						DragCalc.calculateDragAndPowerRequired(
								altitude.get(i), 
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
								startCruiseMass, 
								MyArrayUtils.convertDoubleArrayToListOfAmount(speedArray, SI.METERS_PER_SECOND),
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								_thePerformanceInterface.getPolarCLCruise().get(xcg),
								_thePerformanceInterface.getPolarCDCruise().get(xcg),
//								MyArrayUtils.convertToDoublePrimitive(
//										Arrays.stream(_thePerformanceInterface.getPolarCDCruise().get(xcg))
//										.map(cd -> cd + (
//												-0.000000000002553*Math.pow(startCruiseMass.doubleValue(SI.KILOGRAM), 2)
//												+ 0.000000209147028*startCruiseMass.doubleValue(SI.KILOGRAM)	
//												-0.003767654434394
//												)
//												)
//										.collect(Collectors.toList())
//										),
								_thePerformanceInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);

				//..................................................................................................
				thrustList.add(
						ThrustCalc.calculateThrustAndPowerAvailable(
								altitude.get(i), 
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
								startCruiseMass, 
								MyArrayUtils.convertDoubleArrayToListOfAmount(speedArray, SI.METERS_PER_SECOND), 
								EngineOperatingConditionEnum.CRUISE, 
								_thePerformanceInterface.getTheAircraft().getPowerPlant(), 
								false,
								_thePerformanceInterface.getCruiseCalibrationFactorThrust()
								)
						);
				//..................................................................................................
				_intersectionListMap.get(xcg).add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								altitude.get(i), 
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
								MyArrayUtils.convertDoubleArrayToListOfAmount(speedArray, SI.METERS_PER_SECOND),
								startCruiseMass,
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(), 
								EngineOperatingConditionEnum.CRUISE, 
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								dragList,
								thrustList
								)
						);
				i++;
			}

			for (int j=0; j<altitude.size(); j++) 
				_cruiseEnvelopeListMap.get(xcg).add(
						PerformanceCalcUtils.calculateEnvelope(
								altitude.get(j),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
								startCruiseMass,
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(), 
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)), 
								EngineOperatingConditionEnum.CRUISE,
								_intersectionListMap.get(xcg)
								)
						);

			List<Double> altitudeList = new ArrayList<>();
			List<Double> minSpeedTASList = new ArrayList<>();
			List<Double> minSpeedCASList = new ArrayList<>();
			List<Double> minMachList = new ArrayList<>();
			List<Double> maxSpeedTASList = new ArrayList<>();
			List<Double> maxSpeedCASList = new ArrayList<>();
			List<Double> maxMachList = new ArrayList<>();

			for(int j=0; j<_cruiseEnvelopeListMap.get(xcg).size(); j++) {

				double sigma = OperatingConditions.getAtmosphere(
						_cruiseEnvelopeListMap.get(xcg).get(j).getAltitude().doubleValue(SI.METER),
						_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
						).getDensity()*1000/1.225; 

				if(_cruiseEnvelopeListMap.get(xcg).get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) != 0.0) {
					altitudeList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getAltitude().doubleValue(SI.METER));
					minSpeedTASList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMinSpeed().doubleValue(SI.METERS_PER_SECOND));
					minSpeedCASList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMinSpeed().doubleValue(SI.METERS_PER_SECOND)*(Math.sqrt(sigma)));
					minMachList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMinMach());
					maxSpeedTASList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND));
					maxSpeedCASList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND)*(Math.sqrt(sigma)));
					maxMachList.add(_cruiseEnvelopeListMap.get(xcg).get(j).getMaxMach());
				}
			}

			_minSpeesTASAtCruiseAltitudeMap.put(
					xcg,  
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(minSpeedTASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							)
					);
			_maxSpeesTASAtCruiseAltitudeMap.put(
					xcg, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(maxSpeedTASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							)
					);
			_minSpeesCASAtCruiseAltitudeMap.put(
					xcg, 
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(minSpeedCASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							)
					);
			_maxSpeesCASAtCruiseAltitudeMap.put(
					xcg,  
					Amount.valueOf(
							MyMathUtils.getInterpolatedValue1DLinear(
									MyArrayUtils.convertToDoublePrimitive(altitudeList),
									MyArrayUtils.convertToDoublePrimitive(maxSpeedCASList),
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
									),
							SI.METERS_PER_SECOND
							)
					);
			_minMachAtCruiseAltitudeMap.put(
					xcg, 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(altitudeList),
							MyArrayUtils.convertToDoublePrimitive(minMachList),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							)
					);
			_maxMachAtCruiseAltitudeMap.put(
					xcg,  
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertToDoublePrimitive(altitudeList),
							MyArrayUtils.convertToDoublePrimitive(maxMachList),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							)
					);

			if(_maxMachAtCruiseAltitudeMap.get(xcg) < _thePerformanceInterface.getTheOperatingConditions().getMachCruise()) {
				System.err.println("WARNING: (FLIGHT ENVELOPE - CRUISE) THE CHOSEN CRUISE MACH NUMBER IS NOT INSIDE THE FLIGHT ENVELOPE");
			}
		}

		public void calculateEfficiency(Double xcg) {

			_efficiencyAltitudeMap.put(xcg, new HashMap<>());
			_efficiencyWeightMap.put(xcg, new HashMap<>());

			//--------------------------------------------------------------------
			// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				List<Amount<Force>> liftAltitudeParameterization = new ArrayList<>();
				List<Double> efficiencyListCurrentAltitude = new ArrayList<>();
				for(int j=0; j<_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed().size(); j++) {
					liftAltitudeParameterization.add(
							LiftCalc.calculateLiftAtSpeed(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(), 
									_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
									_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed().get(j), 
									LiftCalc.calculateLiftCoeff(
											Amount.valueOf(
													_thePerformanceInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)
													*_thePerformanceInterface.getKCruiseWeight()
													*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
													SI.NEWTON
													),
											_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed().get(j),
											_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
											_dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(), 
											_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
											)
									)
							);
					efficiencyListCurrentAltitude.add(
							liftAltitudeParameterization.get(j).doubleValue(SI.NEWTON)
							/ _dragListAltitudeParameterizationMap.get(xcg).get(i).getDrag().get(j).doubleValue(SI.NEWTON)
							);
				}
				_efficiencyAltitudeMap.get(xcg).put(
						"Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude(),
						efficiencyListCurrentAltitude
						);
			}
			
			List<Double> efficiencyAltitudesAtCruiseMach = new ArrayList<>();
			
			for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++)
				efficiencyAltitudesAtCruiseMach.add(
						MyMathUtils.getInterpolatedValue1DLinear(
								_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
								.stream().mapToDouble(v -> v.doubleValue(SI.METERS_PER_SECOND)).toArray(),
								_efficiencyAltitudeMap.get(xcg).get("Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude())
								.stream().mapToDouble(h -> h).toArray(),
								SpeedCalc.calculateTAS(
										_thePerformanceInterface.getTheOperatingConditions().getMachCruise(),
										_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
										_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
										).doubleValue(SI.METERS_PER_SECOND)
								)
						);
			
			_efficiencyAtCruiseAltitudeAndMachMap.put(
					xcg, 
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_thePerformanceInterface.getAltitudeListCruise().stream()
									.map(a -> a.to(SI.METER))
									.collect(Collectors.toList())
									),
							MyArrayUtils.convertToDoublePrimitive(efficiencyAltitudesAtCruiseMach),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							)
					);
			
			//--------------------------------------------------------------------
			// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
			for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
				List<Amount<Force>> liftWeightParameterization = new ArrayList<>();
				List<Double> efficiencyListCurrentWeight = new ArrayList<>();
				for(int j=0; j<_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed().size(); j++) {
					liftWeightParameterization.add(
							LiftCalc.calculateLiftAtSpeed(
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(), 
									_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
									_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed().get(j), 
									LiftCalc.calculateLiftCoeff(
											Amount.valueOf(_dragListWeightParameterizationMap.get(xcg).get(i).getWeight().doubleValue(SI.KILOGRAM)
													*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.NEWTON), 
											_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed().get(j), 
											_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
											_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(), 
											_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
											)
									)
							);
					efficiencyListCurrentWeight.add(
							liftWeightParameterization.get(j).doubleValue(SI.NEWTON)
							/ _dragListWeightParameterizationMap.get(xcg).get(i).getDrag().get(j).doubleValue(SI.NEWTON)
							);
				}
				_efficiencyWeightMap.get(xcg).put(
						"Weight = " + _dragListWeightParameterizationMap.get(xcg).get(i).getWeight(),
						efficiencyListCurrentWeight
						);
			}			
		}
		
		public void calculateCruiseGrid(Double xcg) {

			Airfoil meanAirfoil = LSGeometryCalc.calculateMeanAirfoil(_thePerformanceInterface.getTheAircraft().getWing());
			
			List<DragMap> dragListWeightParameterization = new ArrayList<DragMap>();
			List<ThrustMap> thrustListWeightParameterization = new ArrayList<ThrustMap>();
			
			double[] speedArrayWeightParameterization = new double[100];
			speedArrayWeightParameterization = MyArrayUtils.linspace(
					SpeedCalc.calculateSpeedStall(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
							Amount.valueOf(_weightListCruiseMap.get(xcg).get(_weightListCruiseMap.get(xcg).size()-1).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.KILOGRAM), 
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg))
							).doubleValue(SI.METERS_PER_SECOND),
					SpeedCalc.calculateTAS(
							1.0,
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
							).doubleValue(SI.METERS_PER_SECOND),
					100
					);
			
			for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
				//..................................................................................................
//				double currentWeight = _weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON)/9.81;
				dragListWeightParameterization.add(
						DragCalc.calculateDragAndPowerRequired(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
								Amount.valueOf(_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.KILOGRAM),
								MyArrayUtils.convertDoubleArrayToListOfAmount(speedArrayWeightParameterization, SI.METERS_PER_SECOND),
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								_thePerformanceInterface.getPolarCLCruise().get(xcg),
								_thePerformanceInterface.getPolarCDCruise().get(xcg),
//								MyArrayUtils.convertToDoublePrimitive(
//										Arrays.stream(_thePerformanceInterface.getPolarCDCruise().get(xcg))
//										.map(cd -> cd + (
//												-0.000000000002553*Math.pow(currentWeight, 2)
//												+ 0.000000209147028*currentWeight	
//												-0.003767654434394
//												)
//												)
//										.collect(Collectors.toList())
//										),
								_thePerformanceInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
								meanAirfoil.getThicknessToChordRatio(),
								meanAirfoil.getType()
								)
						);
			}
			//..................................................................................................
			thrustListWeightParameterization.add(
					ThrustCalc.calculateThrustAndPowerAvailable(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(), 
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(), 
							Amount.valueOf(_weightListCruiseMap.get(xcg).get(0).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.KILOGRAM), 
							MyArrayUtils.convertDoubleArrayToListOfAmount(speedArrayWeightParameterization, SI.METERS_PER_SECOND), 
							EngineOperatingConditionEnum.CRUISE, 
							_thePerformanceInterface.getTheAircraft().getPowerPlant(), 
							false,
							_thePerformanceInterface.getCruiseCalibrationFactorThrust()
							)
					);
			
			List<DragThrustIntersectionMap> intersectionList = new ArrayList<>();
			for(int i=0; i<_dragListWeightParameterizationMap.get(xcg).size(); i++) {
				intersectionList.add(
						PerformanceCalcUtils.calculateDragThrustIntersection(
								_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(), 
								_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
								thrustListWeightParameterization.get(0).getSpeed(), 
								Amount.valueOf(_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND), SI.KILOGRAM), 
								_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(), 
								EngineOperatingConditionEnum.CRUISE, 
								_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
								MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
								dragListWeightParameterization,
								thrustListWeightParameterization
								)
						);
			}
			
			double[] machArray = null;
			double[] efficiency = null;
			double[] sfc = null;
			double[] specificRange = null;
 			
			_specificRangeMap.put(xcg, new ArrayList<>());
			
			for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) { 
				if(intersectionList.get(i).getMaxMach() != 0.0) {
					machArray = MyArrayUtils.linspace(
							intersectionList.get(i).getMinMach(),
							intersectionList.get(i).getMaxMach(),
							_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed().size()
							);

					sfc = SpecificRangeCalc.calculateSfcVsMach(
							machArray, 
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(), 
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(), 
							_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(), 
							_thePerformanceInterface.getCruiseCalibrationFactorSFC(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant()
							);

					efficiency = MyArrayUtils.convertToDoublePrimitive(
							_efficiencyWeightMap.get(xcg).get(
									"Weight = " + _dragListWeightParameterizationMap.get(xcg).get(i).getWeight()
									)
							);

					specificRange = SpecificRangeCalc.calculateSpecificRangeVsMach(
							Amount.valueOf(
									_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
									SI.KILOGRAM
									),
							machArray, 
							sfc, 
							efficiency, 
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(), 
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise()
							);

					_specificRangeMap.get(xcg).add(
							new SpecificRangeMap(
									_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
									_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
									_thePerformanceInterface.getTheOperatingConditions().getThrottleCruise(),
									Amount.valueOf(
											_weightListCruiseMap.get(xcg).get(i).doubleValue(SI.NEWTON)/AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
											SI.KILOGRAM
											),
									EngineOperatingConditionEnum.CRUISE,
									specificRange,
									machArray,
									efficiency,
									sfc
									)
							);
				}
			}
		}
				
		public void plotCruiseOutput(String cruiseFolderPath, Double xcg) {

			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.THRUST_DRAG_CURVES_CRUISE)) {

				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedAltitudeParameterization_SI = new ArrayList<>();
				List<Double[]> speedAltitudeParameterization_Imperial = new ArrayList<>();
				List<Double[]> dragAndThrustAltitudes_SI = new ArrayList<>();
				List<Double[]> dragAndThrustAltitudes_Imperial = new ArrayList<>();
				List<String> legendAltitudes_SI = new ArrayList<String>();
				List<String> legendAltitudes_Imperial = new ArrayList<String>();

				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
									)
							);
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					dragAndThrustAltitudes_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getDrag()
									.stream().mapToDouble(x -> x.doubleValue(SI.NEWTON)).toArray()
									)
							);
					dragAndThrustAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getDrag()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.POUND_FORCE))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Drag at " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(SI.METER) + " m");
					legendAltitudes_Imperial.add("Drag at " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(NonSI.FOOT) + " ft");
				}
				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
									)
							);
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT))
									.toArray()
									)
							);
					dragAndThrustAltitudes_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_thrustListAltitudeParameterizationMap.get(xcg).get(i).getThrust()
									.stream().mapToDouble(x -> x.doubleValue(SI.NEWTON)).toArray()
									)
							);
					dragAndThrustAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_thrustListAltitudeParameterizationMap.get(xcg).get(i).getThrust()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.POUND_FORCE))
									.toArray()
									)
							);
					legendAltitudes_SI.add("Thrust at " + _thrustListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(SI.METER) + " m");
					legendAltitudes_Imperial.add("Thrust at " + _thrustListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(NonSI.FOOT) + " ft");
				}

				try {
					MyChartToFileUtils.plot(
							speedAltitudeParameterization_SI, dragAndThrustAltitudes_SI,
							"Drag and Thrust curves at different altitudes",
							"Speed", "Forces",
							null, null, null, null,
							"m/s", "N",
							true, legendAltitudes_SI,
							cruiseFolderPath, "Drag_and_Thrust_curves_altitudes_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedAltitudeParameterization_Imperial, dragAndThrustAltitudes_Imperial,
							"Drag and Thrust curves at different altitudes",
							"Speed", "Forces",
							null, null, null, null,
							"kn", "lb",
							true, legendAltitudes_Imperial,
							cruiseFolderPath, "Drag_and_Thrust_curves_altitudes_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				List<Double[]> speedWeightsParameterization_SI = new ArrayList<Double[]>();
				List<Double[]> speedWeightsParameterization_Imperial = new ArrayList<Double[]>();
				List<Double[]> dragAndThrustWeights_SI = new ArrayList<Double[]>();
				List<Double[]> dragAndThrustWeights_Imperial = new ArrayList<Double[]>();
				List<String> legendWeights = new ArrayList<String>();

				for (int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
					speedWeightsParameterization_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
									)
							);
					speedWeightsParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT)).toArray()
									)
							);
					dragAndThrustWeights_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getDrag()
									.stream().mapToDouble(x -> x.doubleValue(SI.NEWTON)).toArray()
									)
							);
					dragAndThrustWeights_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getDrag()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.POUND_FORCE)).toArray()
									)
							);
					legendWeights.add("Drag at " + Math.round(_dragListWeightParameterizationMap.get(xcg).get(i).getWeight().doubleValue(SI.KILOGRAM)) + " kg");
				}
				speedWeightsParameterization_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_thrustListWeightParameterizationMap.get(xcg).get(0).getSpeed()
								.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
								)
						);
				speedWeightsParameterization_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_thrustListWeightParameterizationMap.get(xcg).get(0).getSpeed()
								.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT)).toArray()
								)
						);
				dragAndThrustWeights_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_thrustListWeightParameterizationMap.get(xcg).get(0).getThrust()
								.stream().mapToDouble(x -> x.doubleValue(SI.NEWTON)).toArray()
								)
						);
				dragAndThrustWeights_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_thrustListWeightParameterizationMap.get(xcg).get(0).getThrust()
								.stream().mapToDouble(x -> x.doubleValue(NonSI.POUND_FORCE)).toArray()
								)
						);				
				legendWeights.add("Thrust");

				try {
					MyChartToFileUtils.plot(
							speedWeightsParameterization_SI, dragAndThrustWeights_SI,
							"Drag and Thrust curves at different weights",
							"Speed", "Forces",
							null, null, null, null,
							"m/s", "N",
							true, legendWeights,
							cruiseFolderPath, "Drag_and_Thrust_curves_weights_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedWeightsParameterization_Imperial, dragAndThrustWeights_Imperial,
							"Drag and Thrust curves at different weights",
							"Speed", "Forces",
							null, null, null, null,
							"kn", "lb",
							true, legendWeights,
							cruiseFolderPath, "Drag_and_Thrust_curves_weights_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			}

			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.POWER_NEEDED_AND_AVAILABLE_CURVES_CRUISE)) {

				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedAltitudeParameterization_SI = new ArrayList<Double[]>();
				List<Double[]> speedAltitudeParameterization_Imperial = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableAltitudes_SI = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableAltitudes_Imperial = new ArrayList<Double[]>();
				List<String> legendAltitudes_SI = new ArrayList<String>();
				List<String> legendAltitudes_Imperial = new ArrayList<String>();

				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
									)
							);
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT)).toArray()
									)
							);
					powerNeededAndAvailableAltitudes_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getPower()
									.stream().mapToDouble(x -> x.doubleValue(SI.WATT)).toArray()
									)
							);
					powerNeededAndAvailableAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getPower()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.HORSEPOWER)).toArray()
									)
							);
					legendAltitudes_SI.add("Power needed at " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(SI.METER) + " m");
					legendAltitudes_Imperial.add("Power needed at " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(NonSI.FOOT) + " ft");
				}
				for (int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
					speedAltitudeParameterization_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
									)
							);
					speedAltitudeParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_thrustListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT)).toArray()
									)
							);
					powerNeededAndAvailableAltitudes_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_thrustListAltitudeParameterizationMap.get(xcg).get(i).getPower()
									.stream().mapToDouble(x -> x.doubleValue(SI.WATT)).toArray()
									)
							);
					powerNeededAndAvailableAltitudes_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_thrustListAltitudeParameterizationMap.get(xcg).get(i).getPower()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.HORSEPOWER)).toArray()
									)
							);
					legendAltitudes_SI.add("Power available at " + _thrustListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(SI.METER) + " m");
					legendAltitudes_Imperial.add("Power available at " + _thrustListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(NonSI.FOOT) + " ft");
				}

				try {
					MyChartToFileUtils.plot(
							speedAltitudeParameterization_SI, powerNeededAndAvailableAltitudes_SI,
							"Power needed and available at different altitudes",
							"Speed", "Powers",
							null, null, null, null,
							"m/s", "W",
							true, legendAltitudes_SI,
							cruiseFolderPath, "Power_needed_and_available_altitudes_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedAltitudeParameterization_Imperial, powerNeededAndAvailableAltitudes_Imperial,
							"Power needed and available at different altitudes",
							"Speed", "Powers",
							null, null, null, null,
							"kn", "hp",
							true, legendAltitudes_Imperial,
							cruiseFolderPath, "Power_needed_and_available_altitudes_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				
				List<Double[]> speedWeightsParameterization_SI = new ArrayList<Double[]>();
				List<Double[]> speedWeightsParameterization_Imperial = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableWeights_SI = new ArrayList<Double[]>();
				List<Double[]> powerNeededAndAvailableWeights_Imperial = new ArrayList<Double[]>();
				List<String> legendWeights = new ArrayList<String>();

				for (int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
					speedWeightsParameterization_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
									)
							);
					speedWeightsParameterization_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT)).toArray()
									)
							);
					powerNeededAndAvailableWeights_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getPower()
									.stream().mapToDouble(x -> x.doubleValue(SI.WATT)).toArray()
									)
							);
					powerNeededAndAvailableWeights_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getPower()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.HORSEPOWER)).toArray()
									)
							);
					legendWeights.add("Power needed at " + Math.round(_dragListWeightParameterizationMap.get(xcg).get(i).getWeight().doubleValue(SI.KILOGRAM)) + " kg");
				}
				speedWeightsParameterization_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_thrustListWeightParameterizationMap.get(xcg).get(0).getSpeed()
								.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
								)
						);
				speedWeightsParameterization_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_thrustListWeightParameterizationMap.get(xcg).get(0).getSpeed()
								.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT)).toArray()
								)
						);
				powerNeededAndAvailableWeights_SI.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_thrustListWeightParameterizationMap.get(xcg).get(0).getPower()
								.stream().mapToDouble(x -> x.doubleValue(SI.WATT)).toArray()
								)
						);
				powerNeededAndAvailableWeights_Imperial.add(
						MyArrayUtils.convertFromDoubleToPrimitive(
								_thrustListWeightParameterizationMap.get(xcg).get(0).getPower()
								.stream().mapToDouble(x -> x.doubleValue(NonSI.HORSEPOWER)).toArray()
								)
						);
				legendWeights.add("Power available");

				try {
					MyChartToFileUtils.plot(
							speedWeightsParameterization_SI, powerNeededAndAvailableWeights_SI,
							"Power needed and available at different weights",
							"Speed", "Powers",
							null, null, null, null,
							"m/s", "W",
							true, legendWeights,
							cruiseFolderPath, "Power_needed_and_available_weights_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedWeightsParameterization_Imperial, powerNeededAndAvailableWeights_Imperial,
							"Power needed and available at different weights",
							"Speed", "Powers",
							null, null, null, null,
							"kn", "hp",
							true, legendWeights,
							cruiseFolderPath, "Power_needed_and_available_weights_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.CRUISE_FLIGHT_ENVELOPE)) {
				
				List<Double> altitudeList_SI = new ArrayList<>();
				List<Double> altitudeList_Imperial = new ArrayList<>();
				List<Double> speedTASList_SI = new ArrayList<>();
				List<Double> speedTASList_Imperial = new ArrayList<>();
				List<Double> speedCASList_SI = new ArrayList<>();
				List<Double> speedCASList_Imperial = new ArrayList<>();
				List<Double> machList = new ArrayList<>();
			
				for(int i=0; i<_cruiseEnvelopeListMap.get(xcg).size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude().doubleValue(SI.METER),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
							).getDensity()*1000/1.225; 
					
					// MIN VALUES
					if(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed().doubleValue(SI.METERS_PER_SECOND) != 0.0) {
						altitudeList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude().doubleValue(SI.METER));
						altitudeList_Imperial.add(_cruiseEnvelopeListMap.get(xcg).get(i).getAltitude().doubleValue(NonSI.FOOT));
						speedTASList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed().doubleValue(SI.METERS_PER_SECOND));
						speedTASList_Imperial.add(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed().doubleValue(NonSI.KNOT));
						speedCASList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed().times(Math.sqrt(sigma)).doubleValue(SI.METERS_PER_SECOND));
						speedCASList_Imperial.add(_cruiseEnvelopeListMap.get(xcg).get(i).getMinSpeed().times(Math.sqrt(sigma)).doubleValue(NonSI.KNOT));
						machList.add(_cruiseEnvelopeListMap.get(xcg).get(i).getMinMach());
					}
				}
				for(int i=0; i<_cruiseEnvelopeListMap.get(xcg).size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getAltitude().doubleValue(SI.METER),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise().doubleValue(SI.CELSIUS)
							).getDensity()*1000/1.225; 
					
					// MAX VALUES
					if(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND) != 0.0) {
						altitudeList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getAltitude().doubleValue(SI.METER));
						altitudeList_Imperial.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getAltitude().doubleValue(NonSI.FOOT));
						speedTASList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed().doubleValue(SI.METERS_PER_SECOND));
						speedTASList_Imperial.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed().doubleValue(NonSI.KNOT));
						speedCASList_SI.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed().times(Math.sqrt(sigma)).doubleValue(SI.METERS_PER_SECOND));
						speedCASList_Imperial.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxSpeed().times(Math.sqrt(sigma)).doubleValue(NonSI.KNOT));
						machList.add(_cruiseEnvelopeListMap.get(xcg).get(_cruiseEnvelopeListMap.get(xcg).size()-1-i).getMaxMach());
					}
				}
				
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedTASList_SI),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_SI),
						null, null, 0.0, null,
						"Speed(TAS)", "Altitude",
						"m/s", "m",
						cruiseFolderPath, "Cruise_flight_envelope_TAS_SI",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedCASList_SI),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_SI),
						null, null, 0.0, null,
						"Speed(CAS)", "Altitude",
						"m/s", "m",
						cruiseFolderPath, "Cruise_flight_envelope_CAS_SI",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(machList),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_SI),
						null, null, 0.0, null,
						"Mach", "Altitude",
						"", "m",
						cruiseFolderPath, "Cruise_flight_envelope_MACH_SI",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedTASList_Imperial),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_Imperial),
						null, null, 0.0, null,
						"Speed(TAS)", "Altitude",
						"kn", "ft",
						cruiseFolderPath, "Cruise_flight_envelope_TAS_IMPERIAL",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(speedCASList_Imperial),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_Imperial),
						null, null, 0.0, null,
						"Speed(CAS)", "Altitude",
						"kn", "ft",
						cruiseFolderPath, "Cruise_flight_envelope_CAS_IMPERIAL",true
						);
				MyChartToFileUtils.plotNoLegend(
						MyArrayUtils.convertToDoublePrimitive(machList),
						MyArrayUtils.convertToDoublePrimitive(altitudeList_Imperial),
						null, null, 0.0, null,
						"Mach", "Altitude",
						"", "ft",
						cruiseFolderPath, "Cruise_flight_envelope_MACH_IMPERIAL",true
						);
				
			}
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.EFFICIENCY_CURVES)) {
				
				//--------------------------------------------------------------------
				// ALTITUDE PARAMETERIZATION AT FIXED WEIGHT
				List<Double[]> speedListAltitudeParameterization_TAS_SI = new ArrayList<>();
				List<Double[]> speedListAltitudeParameterization_TAS_Imperial = new ArrayList<>();
				List<Double[]> speedListAltitudeParameterization_CAS_SI = new ArrayList<>();
				List<Double[]> speedListAltitudeParameterization_CAS_Imperial = new ArrayList<>();
				List<Double[]> machListAltitudeParameterization = new ArrayList<>();
				List<Double[]> efficiencyListAltitudeParameterization = new ArrayList<>();
				List<String> legendAltitude_SI = new ArrayList<>();
				List<String> legendAltitude_Imperial = new ArrayList<>();
				for(int i=0; i<_thePerformanceInterface.getAltitudeListCruise().size(); i++) {
				
					double sigma = OperatingConditions.getAtmosphere(
							_thePerformanceInterface.getAltitudeListCruise().get(i).doubleValue(SI.METER)
							).getDensity()*1000/1.225; 
					
					double speedOfSound = OperatingConditions.getAtmosphere(
							_thePerformanceInterface.getAltitudeListCruise().get(i).doubleValue(SI.METER)
							).getSpeedOfSound(); 
					
					speedListAltitudeParameterization_TAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
									)
							);
					speedListAltitudeParameterization_TAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT)).toArray()
									)
							);
					speedListAltitudeParameterization_CAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)*Math.sqrt(sigma)).toArray()
									)
							);
					speedListAltitudeParameterization_CAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream()
									.mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)*Math.sqrt(sigma))
									.toArray()
									)
							);
					machListAltitudeParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListAltitudeParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)/speedOfSound)
									.toArray()
									)
							);
					efficiencyListAltitudeParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									MyArrayUtils.convertToDoublePrimitive(
											_efficiencyAltitudeMap.get(xcg).get(
													"Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude()
													)
											)
									)
							);
					legendAltitude_SI.add("Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(SI.METER));
					legendAltitude_Imperial.add("Altitude = " + _dragListAltitudeParameterizationMap.get(xcg).get(i).getAltitude().doubleValue(NonSI.FOOT));
				}
				
				try {
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization_TAS_SI, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed (TAS)", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendAltitude_SI,
							cruiseFolderPath, "Efficiency_curves_altitude_TAS_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization_CAS_SI, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed (CAS)", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendAltitude_SI,
							cruiseFolderPath, "Efficiency_curves_altitude_CAS_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							machListAltitudeParameterization, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Mach", "Efficiency",
							null, null, null, null,
							" ", " ",
							true, legendAltitude_SI,
							cruiseFolderPath, "Efficiency_curves_altitude_Mach_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization_TAS_Imperial, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed (TAS)", "Efficiency",
							null, null, null, null,
							"kn", "",
							true, legendAltitude_Imperial,
							cruiseFolderPath, "Efficiency_curves_altitude_TAS_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListAltitudeParameterization_CAS_Imperial, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Speed (CAS)", "Efficiency",
							null, null, null, null,
							"kn", "",
							true, legendAltitude_Imperial,
							cruiseFolderPath, "Efficiency_curves_altitude_CAS_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							machListAltitudeParameterization, efficiencyListAltitudeParameterization,
							"Efficiency curves at different altitudes",
							"Mach", "Efficiency",
							null, null, null, null,
							" ", " ",
							true, legendAltitude_Imperial,
							cruiseFolderPath, "Efficiency_curves_altitude_Mach_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
				//--------------------------------------------------------------------
				// WEIGHT PARAMETERIZATION AT FIXED ALTITUDE
				List<Double[]> speedListWeightParameterization_TAS_SI = new ArrayList<>();
				List<Double[]> speedListWeightParameterization_TAS_Imperial = new ArrayList<>();
				List<Double[]> speedListWeightParameterization_CAS_SI = new ArrayList<>();
				List<Double[]> speedListWeightParameterization_CAS_Imperial = new ArrayList<>();
				List<Double[]> machListWeightParameterization = new ArrayList<>();
				List<Double[]> efficiencyListWeightParameterization = new ArrayList<>();
				List<String> legendWeight = new ArrayList<>();
				for(int i=0; i<_weightListCruiseMap.get(xcg).size(); i++) {
					
					double sigma = OperatingConditions.getAtmosphere(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							).getDensity()*1000/1.225; 
					
					double speedOfSound = OperatingConditions.getAtmosphere(
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise().doubleValue(SI.METER)
							).getSpeedOfSound(); 
					
					speedListWeightParameterization_TAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)).toArray()
									)
							);
					speedListWeightParameterization_TAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(NonSI.KNOT)).toArray()
									)
							);
					speedListWeightParameterization_CAS_SI.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)*Math.sqrt(sigma))
									.toArray()
									)
							);
					speedListWeightParameterization_CAS_Imperial.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream()
									.mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)*Math.sqrt(sigma))
									.toArray()
									)
							);
					machListWeightParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									_dragListWeightParameterizationMap.get(xcg).get(i).getSpeed()
									.stream().mapToDouble(x -> x.doubleValue(SI.METERS_PER_SECOND)/speedOfSound).toArray()
									)
							);
					efficiencyListWeightParameterization.add(
							MyArrayUtils.convertFromDoubleToPrimitive(
									MyArrayUtils.convertToDoublePrimitive(
											_efficiencyWeightMap.get(xcg).get(
													"Weight = " + _dragListWeightParameterizationMap.get(xcg).get(i).getWeight()
													)
											)
									)
							);
					legendWeight.add("Weight = " + _dragListWeightParameterizationMap.get(xcg).get(i).getWeight());
				}
				
				try {
					MyChartToFileUtils.plot(
							speedListWeightParameterization_TAS_SI, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed (TAS)", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_TAS_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListWeightParameterization_CAS_SI, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed (CAS)", "Efficiency",
							null, null, null, null,
							"m/s", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_CAS_SI",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListWeightParameterization_TAS_Imperial, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed (TAS)", "Efficiency",
							null, null, null, null,
							"kn", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_TAS_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							speedListWeightParameterization_CAS_Imperial, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Speed (CAS)", "Efficiency",
							null, null, null, null,
							"kn", "",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_CAS_IMPERIAL",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					MyChartToFileUtils.plot(
							machListWeightParameterization, efficiencyListWeightParameterization,
							"Efficiency curves at different weights",
							"Mach", "Efficiency",
							null, null, null, null,
							" ", " ",
							true, legendWeight,
							cruiseFolderPath, "Efficiency_curves_weights_Mach",
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.CRUISE_GRID_CHART)) {
				
				List<Double[]> specificRange = new ArrayList<>();
				List<Double[]> sfc = new ArrayList<>();
				List<Double[]> mach = new ArrayList<>();
				List<String> legend = new ArrayList<>();
				
				for(int i=0; i<_specificRangeMap.get(xcg).size(); i++) {
					specificRange.add(MyArrayUtils.convertFromDoubleToPrimitive(_specificRangeMap.get(xcg).get(i).getSpecificRange()));
					sfc.add(MyArrayUtils.convertFromDoubleToPrimitive(_specificRangeMap.get(xcg).get(i).getSfc()));
					mach.add(MyArrayUtils.convertFromDoubleToPrimitive(_specificRangeMap.get(xcg).get(i).getMach()));
					legend.add("Mass = " + _specificRangeMap.get(xcg).get(i).getWeight().doubleValue(SI.KILOGRAM));
				}
				
				try {
					SpecificRangeCalc.createSpecificRangeChart(
							specificRange,
							mach,
							legend,
							cruiseFolderPath,
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
					SpecificRangeCalc.createSfcChart(
							sfc,
							mach,
							legend,
							cruiseFolderPath,
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	//............................................................................
	// END OF THE CRUISE INNER CLASS
	//............................................................................
	
	//............................................................................
	// DESCENT INNER CLASS
	//............................................................................
	public class CalcDescent {
		
		public void calculateDescentPerformance(
				Amount<Length> initialDescentAltitude,
				Amount<Length> endDescentAltitude,
				Amount<Mass> initialDescentMass,
				Double xcg
				) {
			
			_theDescentCalculatorMap.put(
					xcg, 
					new DescentCalc(
							_thePerformanceInterface.getTheAircraft(),
							_thePerformanceInterface.getTheOperatingConditions(),
							_thePerformanceInterface.getSpeedDescentCAS(),
							_thePerformanceInterface.getRateOfDescent(),
							initialDescentAltitude,
							endDescentAltitude,
							initialDescentMass,
							_thePerformanceInterface.getPolarCLCruise().get(xcg),
							_thePerformanceInterface.getPolarCDCruise().get(xcg),
							_thePerformanceInterface.getCruiseCalibrationFactorThrust(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorThrust(),
							_thePerformanceInterface.getCruiseCalibrationFactorSFC(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorSFC(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexH2O(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexH2O()
							)
					);
					
			_theDescentCalculatorMap.get(xcg).calculateDescentPerformance();
			
			_descentLengthsMap.put(xcg, _theDescentCalculatorMap.get(xcg).getDescentLengths());
			_descentTimesMap.put(xcg, _theDescentCalculatorMap.get(xcg).getDescentTimes());
			_descentAnglesMap.put(xcg, _theDescentCalculatorMap.get(xcg).getDescentAngles());
			_totalDescentLengthMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentLength());
			_totalDescentTimeMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentTime());
			_totalDescentFuelUsedMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentFuelUsed());
			_totalDescentEmissionsNOxMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentNOxEmissions());
			_totalDescentEmissionsCOMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentCOEmissions());
			_totalDescentEmissionsHCMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentHCEmissions());
			_totalDescentEmissionsSootMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentSootEmissions());
			_totalDescentEmissionsCO2Map.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentCO2Emissions());
			_totalDescentEmissionsSOxMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentSOxEmissions());
			_totalDescentEmissionsH2OMap.put(xcg, _theDescentCalculatorMap.get(xcg).getTotalDescentH2OEmissions());
		}
		
		public void plotDescentPerformance(String descentFolderPath, Double xcg) {
			
			_theDescentCalculatorMap.get(xcg).plotDescentPerformance(descentFolderPath);
			
		}
		
	}
	//............................................................................
	// END OF THE DESCENT INNER CLASS
	//............................................................................
	
	//............................................................................
	// LANDING INNER CLASS
	//............................................................................
	public class CalcLanding {

		public void performLandingSimulation(Amount<Mass> landingMass, Double xcg) {

			Amount<Length> wingToGroundDistance = 
					_thePerformanceInterface.getTheAircraft().getFuselage().getHeightFromGround()
					.plus(_thePerformanceInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2))
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getZApexConstructionAxes()
							.plus(_thePerformanceInterface.getTheAircraft().getWing().getSemiSpan()
									.times(Math.sin(
											_thePerformanceInterface.getTheAircraft().getWing()	
												
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);
			
			_theLandingCalculatorMap.put(
					xcg, 
					new LandingCalc(
							_thePerformanceInterface.getInitialALtitudeLanding(),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeLanding(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureLanding(),
							_thePerformanceInterface.getApproachAngle(),
							_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKLandingWeight()), 
							_thePerformanceInterface.getTheAircraft().getPowerPlant(),
							_thePerformanceInterface.getPolarCLLanding().get(xcg),
							_thePerformanceInterface.getPolarCDLanding().get(xcg), 
							_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(),
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
							_thePerformanceInterface.getFreeRollDuration(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getMuBrakeFunction(), 
							wingToGroundDistance, 
							_thePerformanceInterface.getKCLmaxLanding(),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLZeroLanding().get(xcg),
							_thePerformanceInterface.getCLAlphaLanding().get(xcg),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleLanding(),
							_thePerformanceInterface.getCruiseCalibrationFactorThrust(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorThrust(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorThrust(),
							_thePerformanceInterface.getCruiseCalibrationFactorSFC(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorSFC(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorSFC(), 
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getCruiseCalibrationFactorEmissionIndexH2O(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorEmissionIndexH2O(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexNOx(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexCO(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexHC(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexSoot(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexCO2(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexSOx(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorEmissionIndexH2O(),
							true
							)			
					);
			
			//------------------------------------------------------------
			// SIMULATION
			_theLandingCalculatorMap.get(xcg).calculateLanding(true);
			
			// Distances:
			_groundRollDistanceLandingMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsGround());
			_flareDistanceLandingMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsFlare());
			_airborneDistanceLandingMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsApproach());
			_landingDistanceMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsLanding());
			_landingDistanceFAR25Map.put(xcg, _theLandingCalculatorMap.get(xcg).getsLanding().divide(0.6));
			_totalDistanceMap.put(xcg, _theLandingCalculatorMap.get(xcg).getsTotal());
			
			// Velocities:
			_vStallLandingMap.put(xcg, _theLandingCalculatorMap.get(xcg).getvSLanding());
			_vTouchDownMap.put(xcg, _theLandingCalculatorMap.get(xcg).getvTouchDownEffective());
			_vFlareMap.put(xcg, _theLandingCalculatorMap.get(xcg).getvFlareEffective());
			_vApproachMap.put(xcg, _theLandingCalculatorMap.get(xcg).getvApproach());
			
			// Duration:
			_landingDurationMap.put(xcg, _theLandingCalculatorMap.get(xcg).getLandingTime());
			_totalDurationMap.put(xcg, _theLandingCalculatorMap.get(xcg).getTotalTime());
			
		}
		public void plotLandingPerformance(String landingFolderPath, Double xcg) {
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.LANDING_SIMULATIONS)) {
				try {
					_theLandingCalculatorMap.get(xcg).createOutputCharts(landingFolderPath);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	//............................................................................
	// END OF THE LANDING INNER CLASS
	//............................................................................

	//............................................................................
	// PAYLOAD-RANGE INNER CLASS
	//............................................................................
	public class CalcPayloadRange {

		public void fromMissionProfile(Double xcg) {

			if (_vMCMap.get(xcg) == null) {
				CalcTakeOff calcTakeOff = new CalcTakeOff();
				calcTakeOff.calculateVMC(xcg);
			}
			
			_thePayloadRangeCalculatorMap.put(
					xcg, 
					new PayloadRangeCalcMissionProfile(
							_thePerformanceInterface.getTheAircraft(),
							_thePerformanceInterface.getTheOperatingConditions(),
							_thePerformanceInterface.getTakeOffMissionAltitude(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getOperatingEmptyMass(),
							_thePerformanceInterface.getMaximumFuelMass(),
							_thePerformanceInterface.getSinglePassengerMass(),
							_thePerformanceInterface.getFirstGuessCruiseLength(),
							_thePerformanceInterface.getCalculateSFCCruise(),
							_thePerformanceInterface.getCalculateSFCAlternateCruise(),
							_thePerformanceInterface.getCalculateSFCHolding(),
							_thePerformanceInterface.getSfcFunctionCruise(),
							_thePerformanceInterface.getSfcFunctionAlternateCruise(),
							_thePerformanceInterface.getSfcFunctionHolding(),
							_thePerformanceInterface.getAlternateCruiseLength(),
							_thePerformanceInterface.getAlternateCruiseAltitude(),
							_thePerformanceInterface.getHoldingDuration(),
							_thePerformanceInterface.getHoldingAltitude(),
							_thePerformanceInterface.getHoldingMachNumber(),
							_thePerformanceInterface.getLandingFuelFlow(),
							_thePerformanceInterface.getFuelReserve(),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg),
							_thePerformanceInterface.getCLAlphaTakeOff().get(xcg),
							_thePerformanceInterface.getCLZeroTakeOff().get(xcg),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLZeroLanding().get(xcg),
							_thePerformanceInterface.getPolarCLTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCDTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCLClimb().get(xcg),
							_thePerformanceInterface.getPolarCDClimb().get(xcg),
							_thePerformanceInterface.getPolarCLCruise().get(xcg),
							_thePerformanceInterface.getPolarCDCruise().get(xcg),
							_thePerformanceInterface.getPolarCLLanding().get(xcg),
							_thePerformanceInterface.getPolarCDLanding().get(xcg),
							_thePerformanceInterface.getWindSpeed(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getMuBrakeFunction(),
							_thePerformanceInterface.getDtHold(),
							_thePerformanceInterface.getAlphaGround(),
							_thePerformanceInterface.getObstacleTakeOff(),
							_thePerformanceInterface.getKRotation(),
							_thePerformanceInterface.getAlphaDotRotation(),
							_thePerformanceInterface.getKCLmax(),
							_thePerformanceInterface.getDragDueToEngineFailure(),
							_thePerformanceInterface.getKAlphaDot(),
							_thePerformanceInterface.getObstacleLanding(),
							_thePerformanceInterface.getThetaApproach(),
							_thePerformanceInterface.getKApproach(),
							_thePerformanceInterface.getKFlare(),
							_thePerformanceInterface.getKTouchDown(),
							_thePerformanceInterface.getFreeRollDuration(),
							_thePerformanceInterface.getClimbSpeedCAS(),
							_thePerformanceInterface.getSpeedDescentCAS(),
							_thePerformanceInterface.getRateOfDescent()
							)
					);
			
			//------------------------------------------------------------
			// CRUISE MACH AND ALTITUDE
			_thePayloadRangeCalculatorMap.get(xcg).createPayloadRange(_vMCMap.get(xcg).to(SI.METERS_PER_SECOND));
			
			_rangeAtMaxPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxPayload());
			_rangeAtDesignPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeAtDesignPayload());
			_rangeAtMaxFuelMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeAtMaxFuel());	
			_rangeAtZeroPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeAtZeroPayload());
			_takeOffMassAtZeroPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getTakeOffMassZeroPayload());
			_maxPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getMaxPayload());
			_designPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getDesignPayload());
			_payloadAtMaxFuelMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPayloadAtMaxFuel());
			_passengersNumberAtMaxPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPassengersNumberAtMaxPayload());
			_passengersNumberAtDesignPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPassengersNumberAtDesignPayload());
			_passengersNumberAtMaxFuelMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPassengersNumberAtMaxFuel());
			_requiredMassAtMaxPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRequiredMassAtMaxPayload());
			_requiredMassAtDesignPayloadMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRequiredMassAtDesignPayload());
			
			_rangeArrayMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getRangeArray());
			_payloadArrayMap.put(xcg, _thePayloadRangeCalculatorMap.get(xcg).getPayloadArray());
			
		}
		public void plotPayloadRange(String payloadRangeFolderPath, Double xcg) {
			
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.PAYLOAD_RANGE)) {
				if(_rangeAtMaxPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE) != 0.0 
						&& _rangeAtDesignPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE) != 0.0 
						&& _rangeAtMaxFuelMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE) != 0.0 
						&& _rangeAtZeroPayloadMap.get(xcg).doubleValue(NonSI.NAUTICAL_MILE) != 0.0
						)
					_thePayloadRangeCalculatorMap.get(xcg).createPayloadRangeChart(payloadRangeFolderPath);
				else
					System.err.println("WARNING: (PLOT - PAYLOAD-RANGE) UNABLE TO PLOT THE PAYLOAD-RANGE DIAGRAM");
			}
			
		}
	}
	//............................................................................
	// END OF THE PAYLOAD-RANGE INNER CLASS
	//............................................................................
	
	//............................................................................
	// FLIGHT MANEUVERING AND GUST ENVELOPE INNER CLASS
	//............................................................................
	public class CalcFlightManeuveringAndGustEnvelope {
		
		public void fromRegulations(Double xcg) {
			
			_theEnvelopeCalculatorMap.put(
					xcg, 
					new FlightManeuveringEnvelopeCalc(
							_thePerformanceInterface.getTheAircraft().getRegulations(),
							_thePerformanceInterface.getTheAircraft().getTypeVehicle(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance(),
							_thePerformanceInterface.getCLmaxClean().get(xcg),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLmaxInverted(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPositiveLimitLoadFactor(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getNegativeLimitLoadFactor(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getVMaxCruise(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getVDive(),
							_thePerformanceInterface.getCLAlphaClean().get(xcg),
							_thePerformanceInterface.getTheAircraft().getWing().getMeanAerodynamicChord(),
							_thePerformanceInterface.getTheOperatingConditions().getAltitudeCruise(),
							_thePerformanceInterface.getTheOperatingConditions().getDeltaTemperatureCruise(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKLandingWeight()),
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform()
							)
					);
			
			_theEnvelopeCalculatorMap.get(xcg).calculateManeuveringEnvelope();
			
			_stallSpeedCleanMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getStallSpeedClean());
			_stallSpeedFullFlapMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getStallSpeedFullFlap());
			_stallSpeedInvertedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getStallSpeedInverted());
			_maneuveringSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeed());
			_maneuveringFlapSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getManeuveringFlapSpeed());
			_maneuveringSpeedInvertedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getManeuveringSpeedInverted());
			_designFlapSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getDesignFlapSpeed());
			_positiveLoadFactorManeuveringSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorManeuveringSpeed());
			_positiveLoadFactorCruisingSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorCruisingSpeed());
			_positiveLoadFactorDiveSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDiveSpeed());
			_positiveLoadFactorDesignFlapSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeed());
			_negativeLoadFactorManeuveringSpeedInvertedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorManeuveringSpeedInverted());
			_negativeLoadFactorCruisingSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorCruisingSpeed());
			_negativeLoadFactorDiveSpeedMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDiveSpeed());
			_positiveLoadFactorManeuveringSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorManeuveringSpeedWithGust());
			_positiveLoadFactorCruisingSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorCruisingSpeedWithGust());
			_positiveLoadFactorDiveSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDiveSpeedWithGust());
			_positiveLoadFactorDesignFlapSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getPositiveLoadFactorDesignFlapSpeedWithGust());
			_negativeLoadFactorManeuveringSpeedInvertedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorManeuveringSpeedInvertedWithGust());
			_negativeLoadFactorCruisingSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorCruisingSpeedWithGust());
			_negativeLoadFactorDiveSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDiveSpeedWithGust());
			_negativeLoadFactorDesignFlapSpeedWithGustMap.put(xcg, _theEnvelopeCalculatorMap.get(xcg).getNegativeLoadFactorDesignFlapSpeedWithGust());
		
		}
		public void plotVnDiagram(String maneuveringEnvelopeFolderPath, Double xcg) {	
			if(_thePerformanceInterface.getPlotList().contains(PerformancePlotEnum.FLIGHT_MANEUVERING_AND_GUST_DIAGRAM)) 
				_theEnvelopeCalculatorMap.get(xcg).plotManeuveringEnvelope(maneuveringEnvelopeFolderPath);;
			
		}
		
	}
	//............................................................................
	// END OF THE FLIGHT MANEUVERING AND GUST ENVELOPE INNER CLASS
	//............................................................................	
	
	//............................................................................
	// NOISE TRAJECTORIES INNER CLASS
	//............................................................................
	public class CalcNoiseTrajectories {
		
		public void calculateTakeOffNoiseTrajectory(double xcg, String noiseTrajectoriesFolderPath) {
			
			Amount<Length> wingToGroundDistance = 
					_thePerformanceInterface.getTheAircraft().getFuselage().getHeightFromGround()
					.plus(_thePerformanceInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2))
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getZApexConstructionAxes()
							.plus(_thePerformanceInterface.getTheAircraft().getWing().getSemiSpan()
									.times(Math.sin(
											_thePerformanceInterface.getTheAircraft().getWing()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);

			if(_vMCMap.get(xcg) == null) {
				CalcTakeOff calcTakeOff = new CalcTakeOff();
				calcTakeOff.calculateVMC(xcg);
			}

			_theTakeOffNoiseTrajectoryCalculatorMap.put(
					xcg,
					new TakeOffNoiseTrajectoryCalc(
							_thePerformanceInterface.getTakeOffNoiseTrajectoryXEndSimulation(),
							_thePerformanceInterface.getTakeOffNoiseTrajectoryCutbackAltitude(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getTheAircraft().getPowerPlant(),
							_thePerformanceInterface.getPolarCLTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCDTakeOff().get(xcg), 
							wingToGroundDistance, 
							_thePerformanceInterface.getDeltaCD0LandingGears().get(xcg),
							_thePerformanceInterface.getDragDueToEngineFailure(),
							_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(),
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(), 
							_thePerformanceInterface.getDtHold(), 
							_thePerformanceInterface.getTakeOffNoiseTrajectoryLandingGearRetractionTimeInterval(),
							_thePerformanceInterface.getTakeOffNoiseTrajectoryThrustReductionCutbackTimeInterval(),
							_thePerformanceInterface.getKCLmaxTakeOff(),
							_thePerformanceInterface.getKRotation(), 
							_thePerformanceInterface.getAlphaDotRotation(),
							_thePerformanceInterface.getKAlphaDot(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg), 
							_thePerformanceInterface.getCLZeroTakeOff().get(xcg), 
							_thePerformanceInterface.getCLAlphaTakeOff().get(xcg),
							_thePerformanceInterface.getTakeOffCalibrationFactorThrust(),
							_thePerformanceInterface.getTakeOffCalibrationFactorSFC(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							)
					);

			_theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).calculateNoiseTakeOffTrajectory(false, null, true, _vMCMap.get(xcg));
			_theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).calculateNoiseTakeOffTrajectory(true, null, true, _vMCMap.get(xcg));

			double lowestPhiCutback = _theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).getPhiCutback();
			if(_thePerformanceInterface.getTakeOffNoiseTrajectoryNumberOfThrustSettingCutback() != 0) {
				
				double[] phiArray = MyArrayUtils.linspace( (lowestPhiCutback + 0.1), 0.9, _thePerformanceInterface.getTakeOffNoiseTrajectoryNumberOfThrustSettingCutback());

				Arrays.stream(phiArray).forEach(
						throttle -> _theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).calculateNoiseTakeOffTrajectory(
								true,
								throttle, 
								true,
								_vMCMap.get(xcg)
								)
						);
				
			}
			
			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance().equals(Boolean.TRUE)) {
				if(_theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).isTargetSpeedFlag() == true)
					try {
						_theTakeOffNoiseTrajectoryCalculatorMap.get(xcg).createOutputCharts(noiseTrajectoriesFolderPath, true);
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				else {
					System.err.println("TERMINATING ... ");
					System.exit(1);
				}
			}
		}
		
		public void calculateLandingNoiseTrajectory(Double xcg, String noiseTrajectoriesFolderPath) {
			
			Amount<Length> wingToGroundDistance = 
					_thePerformanceInterface.getTheAircraft().getFuselage().getHeightFromGround()
					.plus(_thePerformanceInterface.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2))
					.plus(_thePerformanceInterface.getTheAircraft().getWing().getZApexConstructionAxes()
							.plus(_thePerformanceInterface.getTheAircraft().getWing().getSemiSpan()
									.times(Math.sin(
											_thePerformanceInterface.getTheAircraft().getWing()	
											.getDihedralMean()
											.doubleValue(SI.RADIAN)
											)
											)
									)
							);

			_theLandingNoiseTrajectoryCalculatorMap.put(
					xcg,
					new LandingNoiseTrajectoryCalc(
							_thePerformanceInterface.getLandingNoiseTrajectoryInitialAltitude(),
							_thePerformanceInterface.getLandingNoiseTrajectoryTrajectoryAngle(),
							_thePerformanceInterface.getMaximumTakeOffMass().times(_thePerformanceInterface.getKLandingWeight()),
							_thePerformanceInterface.getTheAircraft().getPowerPlant(),
							_thePerformanceInterface.getPolarCLLanding().get(xcg),
							_thePerformanceInterface.getPolarCDLanding().get(xcg),
							_thePerformanceInterface.getTheAircraft().getWing().getAspectRatio(), 
							_thePerformanceInterface.getTheAircraft().getWing().getSurfacePlanform(),
							_thePerformanceInterface.getFreeRollDuration(),
							_thePerformanceInterface.getMuFunction(), 
							_thePerformanceInterface.getMuBrakeFunction(),
							wingToGroundDistance,
							_thePerformanceInterface.getKCLmaxLanding(),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLZeroLanding().get(xcg),
							_thePerformanceInterface.getCLAlphaLanding().get(xcg),
							_thePerformanceInterface.getTheOperatingConditions().getThrottleLanding(),
							_thePerformanceInterface.getCruiseCalibrationFactorThrust(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorThrust(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorThrust(),
							_thePerformanceInterface.getCruiseCalibrationFactorSFC(),
							_thePerformanceInterface.getFlightIdleCalibrationFactorSFC(),
							_thePerformanceInterface.getGroundIdleCalibrationFactorSFC(),
							_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getCreateCSVPerformance()
							)
					);

			_theLandingNoiseTrajectoryCalculatorMap.get(xcg).calculateNoiseLandingTrajectory(true);

			if(_thePerformanceInterface.getTheAircraft().getTheAnalysisManager().getPlotPerformance().equals(Boolean.TRUE)) {
				if(_theLandingNoiseTrajectoryCalculatorMap.get(xcg).isTargetRDandAltitudeFlag() == true)
					try {
						_theLandingNoiseTrajectoryCalculatorMap.get(xcg).createOutputCharts(noiseTrajectoriesFolderPath, true);
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				else {
					System.err.println("TERMINATING ... ");
					System.exit(1);
				}
			}
		}
	}
	//............................................................................
	// END OF THE NOISE TRAJECTORIES INNER CLASS
	//............................................................................	
	
	//............................................................................
	// MISSION PROFILE INNER CLASS
	//............................................................................
	public class CalcMissionProfile {

		public void calculateMissionProfileIterative(Double xcg) {

			if (_vMCMap.get(xcg) == null) {
				CalcTakeOff calcTakeOff = new CalcTakeOff();
				calcTakeOff.calculateVMC(xcg);
			}
			
			_theMissionProfileCalculatorMap.put(
					xcg, 
					new MissionProfileCalc(
							_thePerformanceInterface.getTheAircraft(),
							_thePerformanceInterface.getTheOperatingConditions(),
							_thePerformanceInterface.getMaximumTakeOffMass(),
							_thePerformanceInterface.getOperatingEmptyMass(),
							_thePerformanceInterface.getSinglePassengerMass(),
							_thePerformanceInterface.getTheAircraft().getCabinConfiguration().getDesignPassengerNumber(),
							_thePerformanceInterface.getFirstGuessInitialMissionFuelMass(),
							_thePerformanceInterface.getMissionRange(),
							_thePerformanceInterface.getTakeOffMissionAltitude(),
							_thePerformanceInterface.getFirstGuessCruiseLength(),
							_thePerformanceInterface.getCalculateSFCCruise(),
							_thePerformanceInterface.getCalculateSFCAlternateCruise(),
							_thePerformanceInterface.getCalculateSFCHolding(),
							_thePerformanceInterface.getSfcFunctionCruise(),
							_thePerformanceInterface.getSfcFunctionAlternateCruise(),
							_thePerformanceInterface.getSfcFunctionHolding(),
							_thePerformanceInterface.getAlternateCruiseLength(),
							_thePerformanceInterface.getAlternateCruiseAltitude(),
							_thePerformanceInterface.getHoldingDuration(),
							_thePerformanceInterface.getHoldingAltitude(),
							_thePerformanceInterface.getHoldingMachNumber(),
							_thePerformanceInterface.getLandingFuelFlow(),
							_thePerformanceInterface.getFuelReserve(),
							MyArrayUtils.getMax(_thePerformanceInterface.getPolarCLCruise().get(xcg)),
							_thePerformanceInterface.getCLmaxTakeOff().get(xcg),
							_thePerformanceInterface.getCLAlphaTakeOff().get(xcg),
							_thePerformanceInterface.getCLZeroTakeOff().get(xcg),
							_thePerformanceInterface.getCLmaxLanding().get(xcg),
							_thePerformanceInterface.getCLZeroLanding().get(xcg),
							_thePerformanceInterface.getPolarCLTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCDTakeOff().get(xcg),
							_thePerformanceInterface.getPolarCLClimb().get(xcg),
							_thePerformanceInterface.getPolarCDClimb().get(xcg),
							_thePerformanceInterface.getPolarCLCruise().get(xcg),
							_thePerformanceInterface.getPolarCDCruise().get(xcg),
							_thePerformanceInterface.getPolarCLLanding().get(xcg),
							_thePerformanceInterface.getPolarCDLanding().get(xcg),
							_thePerformanceInterface.getWindSpeed(),
							_thePerformanceInterface.getMuFunction(),
							_thePerformanceInterface.getMuBrakeFunction(),
							_thePerformanceInterface.getDtHold(),
							_thePerformanceInterface.getAlphaGround(),
							_thePerformanceInterface.getObstacleTakeOff(),
							_thePerformanceInterface.getKRotation(),
							_thePerformanceInterface.getAlphaDotRotation(),
							_thePerformanceInterface.getKCLmax(),
							_thePerformanceInterface.getDragDueToEngineFailure(),
							_thePerformanceInterface.getKAlphaDot(),
							_thePerformanceInterface.getObstacleLanding(),
							_thePerformanceInterface.getThetaApproach(),
							_thePerformanceInterface.getKApproach(),
							_thePerformanceInterface.getKFlare(),
							_thePerformanceInterface.getKTouchDown(),
							_thePerformanceInterface.getFreeRollDuration(),
							_thePerformanceInterface.getClimbSpeedCAS(),
							_thePerformanceInterface.getSpeedDescentCAS(),
							_thePerformanceInterface.getRateOfDescent()
							)
					);
				
			_theMissionProfileCalculatorMap.get(xcg).calculateProfiles(_vMCMap.get(xcg).to(SI.METERS_PER_SECOND));
			
			_altitudeListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getAltitudeList());
			_rangeListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getRangeList());
			_timeListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getTimeList());
			_fuelUsedListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getFuelUsedList());
			_massListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getMassList());
			
			_speedTASMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getSpeedTASMissionList());
			_speedCASMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getSpeedCASMissionList());
			_machMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getMachMissionList());
			_liftingCoefficientMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getLiftingCoefficientMissionList());
			_dragCoefficientMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getDragCoefficientMissionList());
			_efficiencyMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getEfficiencyMissionList());
			_thrustMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getThrustMissionList());
			_dragMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getDragMissionList());
			_rateOfClimbMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getRateOfClimbMissionList());
			_climbAngleMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getClimbAngleMissionList());
			_fuelFlowMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getFuelFlowMissionList());
			_sfcMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getSFCMissionList());
			_throttleMissionListMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getThrottleMissionList());
			
			_initialFuelMassMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getInitialFuelMass());
			_totalFuelUsedMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getTotalFuel());
			_blockFuelMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getBlockFuel());
			_totalMissionTimeMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getTotalTime());
			_blockTimeMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getBlockTime());
			_initialMissionMassMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getInitialMissionMass());
			_endMissionMassMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getEndMissionMass());
			_totalMissionRangeMap.put(xcg, _theMissionProfileCalculatorMap.get(xcg).getTotalRange());
		}
		
		public void plotProfiles(String missionProfilesFolderPath, Double xcg) {
			
			if(_theMissionProfileCalculatorMap.get(xcg).getMissionProfileStopped().equals(Boolean.FALSE))
				_theMissionProfileCalculatorMap.get(xcg).plotProfiles(
						_thePerformanceInterface.getPlotList(),
						missionProfilesFolderPath
						);
			else
				System.err.println("WARNING: (PLOT - MISSION PROFILE) UNABLE TO GENERATE MISSION PROFILE CHARTS");
			
		}
		
	}
	//............................................................................
	// END OF THE MISSION PROFILE INNER CLASS
	//............................................................................	

	//------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------
	
	public IACPerformanceManager getThePerformanceInterface() {
		return _thePerformanceInterface;
	}

	public void setThePerformanceInterface(IACPerformanceManager _thePerformanceInterface) {
		this._thePerformanceInterface = _thePerformanceInterface;
	}

	public Map<Double, TakeOffCalc> getTheTakeOffCalculatorMap() {
		return _theTakeOffCalculatorMap;
	}

	public void setTheTakeOffCalculatorMap(Map<Double, TakeOffCalc> _theTakeOffCalculatorMap) {
		this._theTakeOffCalculatorMap = _theTakeOffCalculatorMap;
	}

	public Map<Double, Amount<Length>> getTakeOffDistanceAEOMap() {
		return _takeOffDistanceAEOMap;
	}

	public void setTakeOffDistanceAEOMap(Map<Double, Amount<Length>> _takeOffDistanceAEOMap) {
		this._takeOffDistanceAEOMap = _takeOffDistanceAEOMap;
	}

	public Map<Double, Amount<Length>> getTakeOffDistanceFAR25Map() {
		return _takeOffDistanceFAR25Map;
	}

	public void setTakeOffDistanceFAR25Map(Map<Double, Amount<Length>> _takeOffDistanceFAR25Map) {
		this._takeOffDistanceFAR25Map = _takeOffDistanceFAR25Map;
	}

	public Map<Double, Amount<Length>> getBalancedFieldLengthMap() {
		return _balancedFieldLengthMap;
	}

	public void setBalancedFieldLengthMap(Map<Double, Amount<Length>> _balancedFieldLengthMap) {
		this._balancedFieldLengthMap = _balancedFieldLengthMap;
	}

	public Map<Double, Amount<Length>> getGroundRollDistanceTakeOffMap() {
		return _groundRollDistanceTakeOffMap;
	}

	public void setGroundRollDistanceTakeOffMap(Map<Double, Amount<Length>> _groundRollDistanceTakeOffMap) {
		this._groundRollDistanceTakeOffMap = _groundRollDistanceTakeOffMap;
	}

	public Map<Double, Amount<Length>> getRotationDistanceTakeOffMap() {
		return _rotationDistanceTakeOffMap;
	}

	public void setRotationDistanceTakeOffMap(Map<Double, Amount<Length>> _rotationDistanceTakeOffMap) {
		this._rotationDistanceTakeOffMap = _rotationDistanceTakeOffMap;
	}

	public Map<Double, Amount<Length>> getAirborneDistanceTakeOffMap() {
		return _airborneDistanceTakeOffMap;
	}

	public void setAirborneDistanceTakeOffMap(Map<Double, Amount<Length>> _airborneDistanceTakeOffMap) {
		this._airborneDistanceTakeOffMap = _airborneDistanceTakeOffMap;
	}

	public Map<Double, Amount<Velocity>> getVStallTakeOffMap() {
		return _vStallTakeOffMap;
	}

	public void setVStallTakeOffMap(Map<Double, Amount<Velocity>> _vStallTakeOffMap) {
		this._vStallTakeOffMap = _vStallTakeOffMap;
	}

	public Map<Double, Amount<Velocity>> getVMCMap() {
		return _vMCMap;
	}

	public void setVMCMap(Map<Double, Amount<Velocity>> _vMCMap) {
		this._vMCMap = _vMCMap;
	}

	public Map<Double, Amount<Velocity>> getVRotationMap() {
		return _vRotationMap;
	}

	public void setVRotationMap(Map<Double, Amount<Velocity>> _vRotationMap) {
		this._vRotationMap = _vRotationMap;
	}

	public Map<Double, Amount<Velocity>> getVLiftOffMap() {
		return _vLiftOffMap;
	}

	public void setVLiftOffMap(Map<Double, Amount<Velocity>> _vLiftOffMap) {
		this._vLiftOffMap = _vLiftOffMap;
	}

	public Map<Double, Amount<Velocity>> getV1Map() {
		return _v1Map;
	}

	public void setV1Map(Map<Double, Amount<Velocity>> _v1Map) {
		this._v1Map = _v1Map;
	}

	public Map<Double, Amount<Velocity>> getV2Map() {
		return _v2Map;
	}

	public void setV2Map(Map<Double, Amount<Velocity>> _v2Map) {
		this._v2Map = _v2Map;
	}

	public Map<Double, Amount<Duration>> getTakeOffDurationMap() {
		return _takeOffDurationMap;
	}

	public void setTakeOffDurationMap(Map<Double, Amount<Duration>> _takeOffDurationMap) {
		this._takeOffDurationMap = _takeOffDurationMap;
	}

	public Map<Double, double[]> getThrustMomentOEIMap() {
		return _thrustMomentOEIMap;
	}

	public void setThrustMomentOEIMap(Map<Double, double[]> _thrustMomentOEIMap) {
		this._thrustMomentOEIMap = _thrustMomentOEIMap;
	}

	public Map<Double, double[]> getYawingMomentOEIMap() {
		return _yawingMomentOEIMap;
	}

	public void setYawingMomentOEIMap(Map<Double, double[]> _yawingMomentOEIMap) {
		this._yawingMomentOEIMap = _yawingMomentOEIMap;
	}

	public Map<Double, ClimbCalc> getTheClimbCalculatorMap() {
		return _theClimbCalculatorMap;
	}

	public void setTheClimbCalculatorMap(Map<Double, ClimbCalc> _theClimbCalculatorMap) {
		this._theClimbCalculatorMap = _theClimbCalculatorMap;
	}

	public Map<Double, List<RCMap>> getRcAEOMap() {
		return _rcAEOMap;
	}

	public void setRcAEOMap(Map<Double, List<RCMap>> _rcMapAEOMap) {
		this._rcAEOMap = _rcMapAEOMap;
	}

	public Map<Double, List<RCMap>> getRcOEIMap() {
		return _rcOEIMap;
	}

	public void setRcOEIMap(Map<Double, List<RCMap>> _rcMapOEIMap) {
		this._rcOEIMap = _rcMapOEIMap;
	}

	public Map<Double, CeilingMap> getCeilingAEOMap() {
		return _ceilingAEOMap;
	}

	public void setCeilingAEOMap(Map<Double, CeilingMap> _ceilingMapAEOMap) {
		this._ceilingAEOMap = _ceilingMapAEOMap;
	}

	public Map<Double, CeilingMap> getCeilingOEIMap() {
		return _ceilingOEIMap;
	}

	public void setCeilingOEIMap(Map<Double, CeilingMap> _ceilingMapOEIMap) {
		this._ceilingOEIMap = _ceilingMapOEIMap;
	}

	public Map<Double, List<DragMap>> getDragListAEOMap() {
		return _dragListAEOMap;
	}

	public void setDragListAEOMap(Map<Double, List<DragMap>> _dragListAEOMap) {
		this._dragListAEOMap = _dragListAEOMap;
	}

	public Map<Double, List<ThrustMap>> getThrustListAEOMap() {
		return _thrustListAEOMap;
	}

	public void setThrustListAEOMap(Map<Double, List<ThrustMap>> _thrustListAEOMap) {
		this._thrustListAEOMap = _thrustListAEOMap;
	}

	public Map<Double, List<DragMap>> getDragListOEIMap() {
		return _dragListOEIMap;
	}

	public void setDragListOEIMap(Map<Double, List<DragMap>> _dragListOEIMap) {
		this._dragListOEIMap = _dragListOEIMap;
	}

	public Map<Double, List<ThrustMap>> getThrustListOEIMap() {
		return _thrustListOEIMap;
	}

	public void setThrustListOEIMap(Map<Double, List<ThrustMap>> _thrustListOEIMap) {
		this._thrustListOEIMap = _thrustListOEIMap;
	}

	public Map<Double, Map<String, List<Double>>> getEfficiencyAltitudeAEOMap() {
		return _efficiencyAltitudeAEOMap;
	}

	public void setEfficiencyAltitudeAEOMap(Map<Double, Map<String, List<Double>>> _efficiencyMapAltitudeAEOMap) {
		this._efficiencyAltitudeAEOMap = _efficiencyMapAltitudeAEOMap;
	}

	public Map<Double, Map<String, List<Double>>> getEfficiencyAltitudeOEIMap() {
		return _efficiencyAltitudeOEIMap;
	}

	public void setEfficiencyAltitudeOEIMap(Map<Double, Map<String, List<Double>>> _efficiencyMapAltitudeOEIMap) {
		this._efficiencyAltitudeOEIMap = _efficiencyMapAltitudeOEIMap;
	}

	public Map<Double, Amount<Length>> getAbsoluteCeilingAEOMap() {
		return _absoluteCeilingAEOMap;
	}

	public void setAbsoluteCeilingAEOMap(Map<Double, Amount<Length>> _absoluteCeilingAEOMap) {
		this._absoluteCeilingAEOMap = _absoluteCeilingAEOMap;
	}

	public Map<Double, Amount<Length>> getServiceCeilingAEOMap() {
		return _serviceCeilingAEOMap;
	}

	public void setServiceCeilingAEOMap(Map<Double, Amount<Length>> _serviceCeilingAEOMap) {
		this._serviceCeilingAEOMap = _serviceCeilingAEOMap;
	}

	public Map<Double, Amount<Duration>> getMinimumClimbTimeAEOMap() {
		return _minimumClimbTimeAEOMap;
	}

	public void setMinimumClimbTimeAEOMap(Map<Double, Amount<Duration>> _minimumClimbTimeAEOMap) {
		this._minimumClimbTimeAEOMap = _minimumClimbTimeAEOMap;
	}

	public Map<Double, Amount<Duration>> getClimbTimeAtSpecificClimbSpeedAEOMap() {
		return _climbTimeAtSpecificClimbSpeedAEOMap;
	}

	public void setClimbTimeAtSpecificClimbSpeedAEOMap(
			Map<Double, Amount<Duration>> _climbTimeAtSpecificClimbSpeedAEOMap) {
		this._climbTimeAtSpecificClimbSpeedAEOMap = _climbTimeAtSpecificClimbSpeedAEOMap;
	}

	public Map<Double, Amount<Mass>> getFuelUsedDuringClimbMap() {
		return _fuelUsedDuringClimbMap;
	}

	public void setFuelUsedDuringClimbMap(Map<Double, Amount<Mass>> _fuelUsedDuringClimbMap) {
		this._fuelUsedDuringClimbMap = _fuelUsedDuringClimbMap;
	}

	public Map<Double, Amount<Length>> getAbsoluteCeilingOEIMap() {
		return _absoluteCeilingOEIMap;
	}

	public void setAbsoluteCeilingOEIMap(Map<Double, Amount<Length>> _absoluteCeilingOEIMap) {
		this._absoluteCeilingOEIMap = _absoluteCeilingOEIMap;
	}

	public Map<Double, Amount<Length>> getServiceCeilingOEIMap() {
		return _serviceCeilingOEIMap;
	}

	public void setServiceCeilingOEIMap(Map<Double, Amount<Length>> _serviceCeilingOEIMap) {
		this._serviceCeilingOEIMap = _serviceCeilingOEIMap;
	}

	public Map<Double, List<DragMap>> getDragListAltitudeParameterizationMap() {
		return _dragListAltitudeParameterizationMap;
	}

	public void setDragListAltitudeParameterizationMap(Map<Double, List<DragMap>> _dragListAltitudeParameterizationMap) {
		this._dragListAltitudeParameterizationMap = _dragListAltitudeParameterizationMap;
	}

	public Map<Double, List<ThrustMap>> getThrustListAltitudeParameterizationMap() {
		return _thrustListAltitudeParameterizationMap;
	}

	public void setThrustListAltitudeParameterizationMap(
			Map<Double, List<ThrustMap>> _thrustListAltitudeParameterizationMap) {
		this._thrustListAltitudeParameterizationMap = _thrustListAltitudeParameterizationMap;
	}

	public Map<Double, List<DragMap>> getDragListWeightParameterizationMap() {
		return _dragListWeightParameterizationMap;
	}

	public void setDragListWeightParameterizationMap(Map<Double, List<DragMap>> _dragListWeightParameterizationMap) {
		this._dragListWeightParameterizationMap = _dragListWeightParameterizationMap;
	}

	public Map<Double, List<ThrustMap>> getThrustListWeightParameterizationMap() {
		return _thrustListWeightParameterizationMap;
	}

	public void setThrustListWeightParameterizationMap(Map<Double, List<ThrustMap>> _thrustListWeightParameterizationMap) {
		this._thrustListWeightParameterizationMap = _thrustListWeightParameterizationMap;
	}

	public Map<Double, List<Amount<Force>>> getWeightListCruiseMap() {
		return _weightListCruiseMap;
	}

	public void setWeightListCruiseMap(Map<Double, List<Amount<Force>>> _weightListCruiseMap) {
		this._weightListCruiseMap = _weightListCruiseMap;
	}

	public Map<Double, List<DragThrustIntersectionMap>> getIntersectionListMap() {
		return _intersectionListMap;
	}

	public void setIntersectionListMap(Map<Double, List<DragThrustIntersectionMap>> _intersectionListMap) {
		this._intersectionListMap = _intersectionListMap;
	}

	public Map<Double, List<FlightEnvelopeMap>> getCruiseEnvelopeListMap() {
		return _cruiseEnvelopeListMap;
	}

	public void setCruiseEnvelopeListMap(Map<Double, List<FlightEnvelopeMap>> _cruiseEnvelopeListMap) {
		this._cruiseEnvelopeListMap = _cruiseEnvelopeListMap;
	}

	public Map<Double, Map<String, List<Double>>> getEfficiencyAltitudeMap() {
		return _efficiencyAltitudeMap;
	}

	public void setEfficiencyAltitudeMap(Map<Double, Map<String, List<Double>>> _efficiencyMapAltitudeMap) {
		this._efficiencyAltitudeMap = _efficiencyMapAltitudeMap;
	}

	public Map<Double, Map<String, List<Double>>> getEfficiencyWeightMap() {
		return _efficiencyWeightMap;
	}

	public void setEfficiencyWeightMap(Map<Double, Map<String, List<Double>>> _efficiencyMapWeightMap) {
		this._efficiencyWeightMap = _efficiencyMapWeightMap;
	}

	public Map<Double, List<SpecificRangeMap>> getSpecificRangeMap() {
		return _specificRangeMap;
	}

	public void setSpecificRangeMap(Map<Double, List<SpecificRangeMap>> _specificRangeMapMap) {
		this._specificRangeMap = _specificRangeMapMap;
	}

	public Map<Double, Amount<Velocity>> getMaxSpeesTASAtCruiseAltitudeMap() {
		return _maxSpeesTASAtCruiseAltitudeMap;
	}

	public void setMaxSpeesTASAtCruiseAltitudeMap(Map<Double, Amount<Velocity>> _maxSpeesTASAtCruiseAltitudeMap) {
		this._maxSpeesTASAtCruiseAltitudeMap = _maxSpeesTASAtCruiseAltitudeMap;
	}

	public Map<Double, Amount<Velocity>> getMinSpeesTASAtCruiseAltitudeMap() {
		return _minSpeesTASAtCruiseAltitudeMap;
	}

	public void setMinSpeesTASAtCruiseAltitudeMap(Map<Double, Amount<Velocity>> _minSpeesTASAtCruiseAltitudeMap) {
		this._minSpeesTASAtCruiseAltitudeMap = _minSpeesTASAtCruiseAltitudeMap;
	}

	public Map<Double, Amount<Velocity>> getMaxSpeesCASAtCruiseAltitudeMap() {
		return _maxSpeesCASAtCruiseAltitudeMap;
	}

	public void setMaxSpeesCASAtCruiseAltitudeMap(Map<Double, Amount<Velocity>> _maxSpeesCASAtCruiseAltitudeMap) {
		this._maxSpeesCASAtCruiseAltitudeMap = _maxSpeesCASAtCruiseAltitudeMap;
	}

	public Map<Double, Amount<Velocity>> getMinSpeesCASAtCruiseAltitudeMap() {
		return _minSpeesCASAtCruiseAltitudeMap;
	}

	public void setMinSpeesCASAtCruiseAltitudeMap(Map<Double, Amount<Velocity>> _minSpeesCASAtCruiseAltitudeMap) {
		this._minSpeesCASAtCruiseAltitudeMap = _minSpeesCASAtCruiseAltitudeMap;
	}

	public Map<Double, Double> getMaxMachAtCruiseAltitudeMap() {
		return _maxMachAtCruiseAltitudeMap;
	}

	public void setMaxMachAtCruiseAltitudeMap(Map<Double, Double> _maxMachAtCruiseAltitudeMap) {
		this._maxMachAtCruiseAltitudeMap = _maxMachAtCruiseAltitudeMap;
	}

	public Map<Double, Double> getMinMachAtCruiseAltitudeMap() {
		return _minMachAtCruiseAltitudeMap;
	}

	public void setMinMachAtCruiseAltitudeMap(Map<Double, Double> _minMachAtCruiseAltitudeMap) {
		this._minMachAtCruiseAltitudeMap = _minMachAtCruiseAltitudeMap;
	}

	public Map<Double, Double> getEfficiencyAtCruiseAltitudeAndMachMap() {
		return _efficiencyAtCruiseAltitudeAndMachMap;
	}

	public void setEfficiencyAtCruiseAltitudeAndMachMap(Map<Double, Double> _efficiencyAtCruiseAltitudeAndMachMap) {
		this._efficiencyAtCruiseAltitudeAndMachMap = _efficiencyAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, Amount<Force>> getThrustAtCruiseAltitudeAndMachMap() {
		return _thrustAtCruiseAltitudeAndMachMap;
	}

	public void setThrustAtCruiseAltitudeAndMachMap(Map<Double, Amount<Force>> _thrustAtCruiseAltitudeAndMachMap) {
		this._thrustAtCruiseAltitudeAndMachMap = _thrustAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, Amount<Force>> getDragAtCruiseAltitudeAndMachMap() {
		return _dragAtCruiseAltitudeAndMachMap;
	}

	public void setDragAtCruiseAltitudeAndMachMap(Map<Double, Amount<Force>> _dragAtCruiseAltitudeAndMachMap) {
		this._dragAtCruiseAltitudeAndMachMap = _dragAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, Amount<Power>> getPowerAvailableAtCruiseAltitudeAndMachMap() {
		return _powerAvailableAtCruiseAltitudeAndMachMap;
	}

	public void setPowerAvailableAtCruiseAltitudeAndMachMap(
			Map<Double, Amount<Power>> _powerAvailableAtCruiseAltitudeAndMachMap) {
		this._powerAvailableAtCruiseAltitudeAndMachMap = _powerAvailableAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, Amount<Power>> getPowerNeededAtCruiseAltitudeAndMachMap() {
		return _powerNeededAtCruiseAltitudeAndMachMap;
	}

	public void setPowerNeededAtCruiseAltitudeAndMachMap(
			Map<Double, Amount<Power>> _powerNeededAtCruiseAltitudeAndMachMap) {
		this._powerNeededAtCruiseAltitudeAndMachMap = _powerNeededAtCruiseAltitudeAndMachMap;
	}

	public Map<Double, DescentCalc> getTheDescentCalculatorMap() {
		return _theDescentCalculatorMap;
	}

	public void setTheDescentCalculatorMap(Map<Double, DescentCalc> _theDescentCalculatorMap) {
		this._theDescentCalculatorMap = _theDescentCalculatorMap;
	}

	public Map<Double, List<Amount<Length>>> getDescentLengthsMap() {
		return _descentLengthsMap;
	}

	public void setDescentLengthsMap(Map<Double, List<Amount<Length>>> _descentLengthsMap) {
		this._descentLengthsMap = _descentLengthsMap;
	}

	public Map<Double, List<Amount<Duration>>> getDescentTimesMap() {
		return _descentTimesMap;
	}

	public void setDescentTimesMap(Map<Double, List<Amount<Duration>>> _descentTimesMap) {
		this._descentTimesMap = _descentTimesMap;
	}

	public Map<Double, List<Amount<Angle>>> getDescentAnglesMap() {
		return _descentAnglesMap;
	}

	public void setDescentAnglesMap(Map<Double, List<Amount<Angle>>> _descentAnglesMap) {
		this._descentAnglesMap = _descentAnglesMap;
	}

	public Map<Double, Amount<Length>> getTotalDescentLengthMap() {
		return _totalDescentLengthMap;
	}

	public void setTotalDescentLengthMap(Map<Double, Amount<Length>> _totalDescentLengthMap) {
		this._totalDescentLengthMap = _totalDescentLengthMap;
	}

	public Map<Double, Amount<Duration>> getTotalDescentTimeMap() {
		return _totalDescentTimeMap;
	}

	public void setTotalDescentTimeMap(Map<Double, Amount<Duration>> _totalDescentTimeMap) {
		this._totalDescentTimeMap = _totalDescentTimeMap;
	}

	public Map<Double, Amount<Mass>> getTotalDescentFuelUsedMap() {
		return _totalDescentFuelUsedMap;
	}

	public void setTotalDescentFuelUsedMap(Map<Double, Amount<Mass>> _totalDescentFuelUsedMap) {
		this._totalDescentFuelUsedMap = _totalDescentFuelUsedMap;
	}

	public Map<Double, LandingCalc> getTheLandingCalculatorMap() {
		return _theLandingCalculatorMap;
	}

	public void setTheLandingCalculatorMap(Map<Double, LandingCalc> _theLandingCalculatorMap) {
		this._theLandingCalculatorMap = _theLandingCalculatorMap;
	}

	public Map<Double, Amount<Length>> getLandingDistanceMap() {
		return _landingDistanceMap;
	}

	public void setLandingDistanceMap(Map<Double, Amount<Length>> _landingDistanceMap) {
		this._landingDistanceMap = _landingDistanceMap;
	}

	public Map<Double, Amount<Length>> getLandingDistanceFAR25Map() {
		return _landingDistanceFAR25Map;
	}

	public void setLandingDistanceFAR25Map(Map<Double, Amount<Length>> _landingDistanceFAR25Map) {
		this._landingDistanceFAR25Map = _landingDistanceFAR25Map;
	}

	public Map<Double, Amount<Length>> getGroundRollDistanceLandingMap() {
		return _groundRollDistanceLandingMap;
	}

	public void setGroundRollDistanceLandingMap(Map<Double, Amount<Length>> _groundRollDistanceLandingMap) {
		this._groundRollDistanceLandingMap = _groundRollDistanceLandingMap;
	}

	public Map<Double, Amount<Length>> getFlareDistanceLandingMap() {
		return _flareDistanceLandingMap;
	}

	public void setFlareDistanceLandingMap(Map<Double, Amount<Length>> _flareDistanceLandingMap) {
		this._flareDistanceLandingMap = _flareDistanceLandingMap;
	}

	public Map<Double, Amount<Length>> getAirborneDistanceLandingMap() {
		return _airborneDistanceLandingMap;
	}

	public void setAirborneDistanceLandingMap(Map<Double, Amount<Length>> _airborneDistanceLandingMap) {
		this._airborneDistanceLandingMap = _airborneDistanceLandingMap;
	}

	public Map<Double, Amount<Velocity>> getVStallLandingMap() {
		return _vStallLandingMap;
	}

	public void setVStallLandingMap(Map<Double, Amount<Velocity>> _vStallLandingMap) {
		this._vStallLandingMap = _vStallLandingMap;
	}

	public Map<Double, Amount<Velocity>> getVApproachMap() {
		return _vApproachMap;
	}

	public void setVApproachMap(Map<Double, Amount<Velocity>> _vApproachMap) {
		this._vApproachMap = _vApproachMap;
	}

	public Map<Double, Amount<Velocity>> getVFlareMap() {
		return _vFlareMap;
	}

	public void setVFlareMap(Map<Double, Amount<Velocity>> _vFlareMap) {
		this._vFlareMap = _vFlareMap;
	}

	public Map<Double, Amount<Velocity>> getVTouchDownMap() {
		return _vTouchDownMap;
	}

	public void setVTouchDownMap(Map<Double, Amount<Velocity>> _vTouchDownMap) {
		this._vTouchDownMap = _vTouchDownMap;
	}

	public Map<Double, Amount<Duration>> getLandingDurationMap() {
		return _landingDurationMap;
	}

	public void setLandingDurationMap(Map<Double, Amount<Duration>> _landingDurationMap) {
		this._landingDurationMap = _landingDurationMap;
	}

	public Map<Double, PayloadRangeCalcMissionProfile> getThePayloadRangeCalculatorMap() {
		return _thePayloadRangeCalculatorMap;
	}

	public void setThePayloadRangeCalculatorMap(
			Map<Double, PayloadRangeCalcMissionProfile> _thePayloadRangeCalculatorMap) {
		this._thePayloadRangeCalculatorMap = _thePayloadRangeCalculatorMap;
	}

	public Map<Double, Amount<Length>> getRangeAtMaxPayloadMap() {
		return _rangeAtMaxPayloadMap;
	}

	public void setRangeAtMaxPayloadMap(Map<Double, Amount<Length>> _rangeAtMaxPayloadMap) {
		this._rangeAtMaxPayloadMap = _rangeAtMaxPayloadMap;
	}

	public Map<Double, Amount<Length>> getRangeAtDesignPayloadMap() {
		return _rangeAtDesignPayloadMap;
	}

	public void setRangeAtDesignPayloadMap(Map<Double, Amount<Length>> _rangeAtDesignPayloadMap) {
		this._rangeAtDesignPayloadMap = _rangeAtDesignPayloadMap;
	}

	public Map<Double, Amount<Length>> getRangeAtMaxFuelMap() {
		return _rangeAtMaxFuelMap;
	}

	public void setRangeAtMaxFuelMap(Map<Double, Amount<Length>> _rangeAtMaxFuelMap) {
		this._rangeAtMaxFuelMap = _rangeAtMaxFuelMap;
	}

	public Map<Double, Amount<Length>> getRangeAtZeroPayloadMap() {
		return _rangeAtZeroPayloadMap;
	}

	public void setRangeAtZeroPayloadMap(Map<Double, Amount<Length>> _rangeAtZeroPayloadMap) {
		this._rangeAtZeroPayloadMap = _rangeAtZeroPayloadMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffMassAtZeroPayloadMap() {
		return _takeOffMassAtZeroPayloadMap;
	}

	public void setTakeOffMassAtZeroPayloadMap(Map<Double, Amount<Mass>> _takeOffMassAtZeroPayloadMap) {
		this._takeOffMassAtZeroPayloadMap = _takeOffMassAtZeroPayloadMap;
	}

	public Map<Double, Amount<Mass>> getMaxPayloadMap() {
		return _maxPayloadMap;
	}

	public void setMaxPayloadMap(Map<Double, Amount<Mass>> _maxPayloadMap) {
		this._maxPayloadMap = _maxPayloadMap;
	}

	public Map<Double, Amount<Mass>> getDesignPayloadMap() {
		return _designPayloadMap;
	}

	public void setDesignPayloadMap(Map<Double, Amount<Mass>> _designPayloadMap) {
		this._designPayloadMap = _designPayloadMap;
	}

	public Map<Double, Amount<Mass>> getPayloadAtMaxFuelMap() {
		return _payloadAtMaxFuelMap;
	}

	public void setPayloadAtMaxFuelMap(Map<Double, Amount<Mass>> _payloadAtMaxFuelMap) {
		this._payloadAtMaxFuelMap = _payloadAtMaxFuelMap;
	}

	public Map<Double, Integer> getPassengersNumberAtMaxPayloadMap() {
		return _passengersNumberAtMaxPayloadMap;
	}

	public void setPassengersNumberAtMaxPayloadMap(Map<Double, Integer> _passengersNumberAtMaxPayloadMap) {
		this._passengersNumberAtMaxPayloadMap = _passengersNumberAtMaxPayloadMap;
	}

	public Map<Double, Integer> getPassengersNumberAtDesignPayloadMap() {
		return _passengersNumberAtDesignPayloadMap;
	}

	public void setPassengersNumberAtDesignPayloadMap(Map<Double, Integer> _passengersNumberAtDesignPayloadMap) {
		this._passengersNumberAtDesignPayloadMap = _passengersNumberAtDesignPayloadMap;
	}

	public Map<Double, Integer> getPassengersNumberAtMaxFuelMap() {
		return _passengersNumberAtMaxFuelMap;
	}

	public void setPassengersNumberAtMaxFuelMap(Map<Double, Integer> _passengersNumberAtMaxFuelMap) {
		this._passengersNumberAtMaxFuelMap = _passengersNumberAtMaxFuelMap;
	}

	public Map<Double, Amount<Mass>> getRequiredMassAtMaxPayloadMap() {
		return _requiredMassAtMaxPayloadMap;
	}

	public void setRequiredMassAtMaxPayloadMap(Map<Double, Amount<Mass>> _requiredMassAtMaxPayloadMap) {
		this._requiredMassAtMaxPayloadMap = _requiredMassAtMaxPayloadMap;
	}

	public Map<Double, Amount<Mass>> getRequiredMassAtDesignPayloadMap() {
		return _requiredMassAtDesignPayloadMap;
	}

	public void setRequiredMassAtDesignPayloadMap(Map<Double, Amount<Mass>> _requiredMassAtDesignPayloadMap) {
		this._requiredMassAtDesignPayloadMap = _requiredMassAtDesignPayloadMap;
	}

	public Map<Double, List<Amount<Length>>> getRangeArrayMap() {
		return _rangeArrayMap;
	}

	public void setRangeArrayMap(Map<Double, List<Amount<Length>>> _rangeArrayMap) {
		this._rangeArrayMap = _rangeArrayMap;
	}

	public Map<Double, List<Double>> getPayloadArrayMap() {
		return _payloadArrayMap;
	}

	public void setPayloadArrayMap(Map<Double, List<Double>> _payloadArrayMap) {
		this._payloadArrayMap = _payloadArrayMap;
	}

	public Map<Double, double[][]> getRangeMatrixMap() {
		return _rangeMatrixMap;
	}

	public void setRangeMatrixMap(Map<Double, double[][]> _rangeMatrixMap) {
		this._rangeMatrixMap = _rangeMatrixMap;
	}

	public Map<Double, double[][]> getPayloadMatrixMap() {
		return _payloadMatrixMap;
	}

	public void setPayloadMatrixMap(Map<Double, double[][]> _payloadMatrixMap) {
		this._payloadMatrixMap = _payloadMatrixMap;
	}

	public Map<Double, FlightManeuveringEnvelopeCalc> getTheEnvelopeCalculatorMap() {
		return _theEnvelopeCalculatorMap;
	}

	public void setTheEnvelopeCalculatorMap(Map<Double, FlightManeuveringEnvelopeCalc> _theEnvelopeCalculatorMap) {
		this._theEnvelopeCalculatorMap = _theEnvelopeCalculatorMap;
	}

	public Map<Double, Amount<Velocity>> getStallSpeedFullFlapMap() {
		return _stallSpeedFullFlapMap;
	}

	public void setStallSpeedFullFlapMap(Map<Double, Amount<Velocity>> _stallSpeedFullFlapMap) {
		this._stallSpeedFullFlapMap = _stallSpeedFullFlapMap;
	}

	public Map<Double, Amount<Velocity>> getStallSpeedCleanMap() {
		return _stallSpeedCleanMap;
	}

	public void setStallSpeedCleanMap(Map<Double, Amount<Velocity>> _stallSpeedCleanMap) {
		this._stallSpeedCleanMap = _stallSpeedCleanMap;
	}

	public Map<Double, Amount<Velocity>> getStallSpeedInvertedMap() {
		return _stallSpeedInvertedMap;
	}

	public void setStallSpeedInvertedMap(Map<Double, Amount<Velocity>> _stallSpeedInvertedMap) {
		this._stallSpeedInvertedMap = _stallSpeedInvertedMap;
	}

	public Map<Double, Amount<Velocity>> getManeuveringSpeedMap() {
		return _maneuveringSpeedMap;
	}

	public void setManeuveringSpeedMap(Map<Double, Amount<Velocity>> _maneuveringSpeedMap) {
		this._maneuveringSpeedMap = _maneuveringSpeedMap;
	}

	public Map<Double, Amount<Velocity>> getManeuveringFlapSpeedMap() {
		return _maneuveringFlapSpeedMap;
	}

	public void setManeuveringFlapSpeedMap(Map<Double, Amount<Velocity>> _maneuveringFlapSpeedMap) {
		this._maneuveringFlapSpeedMap = _maneuveringFlapSpeedMap;
	}

	public Map<Double, Amount<Velocity>> getManeuveringSpeedInvertedMap() {
		return _maneuveringSpeedInvertedMap;
	}

	public void setManeuveringSpeedInvertedMap(Map<Double, Amount<Velocity>> _maneuveringSpeedInvertedMap) {
		this._maneuveringSpeedInvertedMap = _maneuveringSpeedInvertedMap;
	}

	public Map<Double, Amount<Velocity>> getDesignFlapSpeedMap() {
		return _designFlapSpeedMap;
	}

	public void setDesignFlapSpeedMap(Map<Double, Amount<Velocity>> _designFlapSpeedMap) {
		this._designFlapSpeedMap = _designFlapSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorManeuveringSpeedMap() {
		return _positiveLoadFactorManeuveringSpeedMap;
	}

	public void setPositiveLoadFactorManeuveringSpeedMap(Map<Double, Double> _positiveLoadFactorManeuveringSpeedMap) {
		this._positiveLoadFactorManeuveringSpeedMap = _positiveLoadFactorManeuveringSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorCruisingSpeedMap() {
		return _positiveLoadFactorCruisingSpeedMap;
	}

	public void setPositiveLoadFactorCruisingSpeedMap(Map<Double, Double> _positiveLoadFactorCruisingSpeedMap) {
		this._positiveLoadFactorCruisingSpeedMap = _positiveLoadFactorCruisingSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorDiveSpeedMap() {
		return _positiveLoadFactorDiveSpeedMap;
	}

	public void setPositiveLoadFactorDiveSpeedMap(Map<Double, Double> _positiveLoadFactorDiveSpeedMap) {
		this._positiveLoadFactorDiveSpeedMap = _positiveLoadFactorDiveSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorDesignFlapSpeedMap() {
		return _positiveLoadFactorDesignFlapSpeedMap;
	}

	public void setPositiveLoadFactorDesignFlapSpeedMap(Map<Double, Double> _positiveLoadFactorDesignFlapSpeedMap) {
		this._positiveLoadFactorDesignFlapSpeedMap = _positiveLoadFactorDesignFlapSpeedMap;
	}

	public Map<Double, Double> getNegativeLoadFactorManeuveringSpeedInvertedMap() {
		return _negativeLoadFactorManeuveringSpeedInvertedMap;
	}

	public void setNegativeLoadFactorManeuveringSpeedInvertedMap(
			Map<Double, Double> _negativeLoadFactorManeuveringSpeedInvertedMap) {
		this._negativeLoadFactorManeuveringSpeedInvertedMap = _negativeLoadFactorManeuveringSpeedInvertedMap;
	}

	public Map<Double, Double> getNegativeLoadFactorCruisingSpeedMap() {
		return _negativeLoadFactorCruisingSpeedMap;
	}

	public void setNegativeLoadFactorCruisingSpeedMap(Map<Double, Double> _negativeLoadFactorCruisingSpeedMap) {
		this._negativeLoadFactorCruisingSpeedMap = _negativeLoadFactorCruisingSpeedMap;
	}

	public Map<Double, Double> getNegativeLoadFactorDiveSpeedMap() {
		return _negativeLoadFactorDiveSpeedMap;
	}

	public void setNegativeLoadFactorDiveSpeedMap(Map<Double, Double> _negativeLoadFactorDiveSpeedMap) {
		this._negativeLoadFactorDiveSpeedMap = _negativeLoadFactorDiveSpeedMap;
	}

	public Map<Double, Double> getPositiveLoadFactorManeuveringSpeedWithGustMap() {
		return _positiveLoadFactorManeuveringSpeedWithGustMap;
	}

	public void setPositiveLoadFactorManeuveringSpeedWithGustMap(
			Map<Double, Double> _positiveLoadFactorManeuveringSpeedWithGustMap) {
		this._positiveLoadFactorManeuveringSpeedWithGustMap = _positiveLoadFactorManeuveringSpeedWithGustMap;
	}

	public Map<Double, Double> getPositiveLoadFactorCruisingSpeedWithGustMap() {
		return _positiveLoadFactorCruisingSpeedWithGustMap;
	}

	public void setPositiveLoadFactorCruisingSpeedWithGustMap(
			Map<Double, Double> _positiveLoadFactorCruisingSpeedWithGustMap) {
		this._positiveLoadFactorCruisingSpeedWithGustMap = _positiveLoadFactorCruisingSpeedWithGustMap;
	}

	public Map<Double, Double> getPositiveLoadFactorDiveSpeedWithGustMap() {
		return _positiveLoadFactorDiveSpeedWithGustMap;
	}

	public void setPositiveLoadFactorDiveSpeedWithGustMap(Map<Double, Double> _positiveLoadFactorDiveSpeedWithGustMap) {
		this._positiveLoadFactorDiveSpeedWithGustMap = _positiveLoadFactorDiveSpeedWithGustMap;
	}

	public Map<Double, Double> getPositiveLoadFactorDesignFlapSpeedWithGustMap() {
		return _positiveLoadFactorDesignFlapSpeedWithGustMap;
	}

	public void setPositiveLoadFactorDesignFlapSpeedWithGustMap(
			Map<Double, Double> _positiveLoadFactorDesignFlapSpeedWithGustMap) {
		this._positiveLoadFactorDesignFlapSpeedWithGustMap = _positiveLoadFactorDesignFlapSpeedWithGustMap;
	}

	public Map<Double, Double> getNegativeLoadFactorManeuveringSpeedInvertedWithGustMap() {
		return _negativeLoadFactorManeuveringSpeedInvertedWithGustMap;
	}

	public void setNegativeLoadFactorManeuveringSpeedInvertedWithGustMap(
			Map<Double, Double> _negativeLoadFactorManeuveringSpeedInvertedWithGustMap) {
		this._negativeLoadFactorManeuveringSpeedInvertedWithGustMap = _negativeLoadFactorManeuveringSpeedInvertedWithGustMap;
	}

	public Map<Double, Double> getNegativeLoadFactorCruisingSpeedWithGustMap() {
		return _negativeLoadFactorCruisingSpeedWithGustMap;
	}

	public void setNegativeLoadFactorCruisingSpeedWithGustMap(
			Map<Double, Double> _negativeLoadFactorCruisingSpeedWithGustMap) {
		this._negativeLoadFactorCruisingSpeedWithGustMap = _negativeLoadFactorCruisingSpeedWithGustMap;
	}

	public Map<Double, Double> getNegativeLoadFactorDiveSpeedWithGustMap() {
		return _negativeLoadFactorDiveSpeedWithGustMap;
	}

	public void setNegativeLoadFactorDiveSpeedWithGustMap(Map<Double, Double> _negativeLoadFactorDiveSpeedWithGustMap) {
		this._negativeLoadFactorDiveSpeedWithGustMap = _negativeLoadFactorDiveSpeedWithGustMap;
	}

	public Map<Double, Double> getNegativeLoadFactorDesignFlapSpeedWithGustMap() {
		return _negativeLoadFactorDesignFlapSpeedWithGustMap;
	}

	public void setNegativeLoadFactorDesignFlapSpeedWithGustMap(
			Map<Double, Double> _negativeLoadFactorDesignFlapSpeedWithGustMap) {
		this._negativeLoadFactorDesignFlapSpeedWithGustMap = _negativeLoadFactorDesignFlapSpeedWithGustMap;
	}

	public Map<Double, MissionProfileCalc> getTheMissionProfileCalculatorMap() {
		return _theMissionProfileCalculatorMap;
	}

	public void setTheMissionProfileCalculatorMap(Map<Double, MissionProfileCalc> _theMissionProfileCalculatorMap) {
		this._theMissionProfileCalculatorMap = _theMissionProfileCalculatorMap;
	}

	public Map<Double, List<Amount<Length>>> getAltitudeListMap() {
		return _altitudeListMap;
	}

	public void setAltitudeListMap(Map<Double, List<Amount<Length>>> _altitudeListMap) {
		this._altitudeListMap = _altitudeListMap;
	}

	public Map<Double, List<Amount<Length>>> getRangeListMap() {
		return _rangeListMap;
	}

	public void setRangeListMap(Map<Double, List<Amount<Length>>> _rangeListMap) {
		this._rangeListMap = _rangeListMap;
	}

	public Map<Double, List<Amount<Duration>>> getTimeListMap() {
		return _timeListMap;
	}

	public void setTimeListMap(Map<Double, List<Amount<Duration>>> _timeListMap) {
		this._timeListMap = _timeListMap;
	}

	public Map<Double, List<Amount<Mass>>> getFuelUsedListMap() {
		return _fuelUsedListMap;
	}

	public void setFuelUsedListMap(Map<Double, List<Amount<Mass>>> _fuelUsedListMap) {
		this._fuelUsedListMap = _fuelUsedListMap;
	}

	public Map<Double, List<Amount<Mass>>> getMassListMap() {
		return _massListMap;
	}

	public void setMassListMap(Map<Double, List<Amount<Mass>>> _massListMap) {
		this._massListMap = _massListMap;
	}

	public Map<Double, List<Amount<Velocity>>> getSpeedTASMissionListMap() {
		return _speedTASMissionListMap;
	}

	public void setSpeedTASMissionListMap(Map<Double, List<Amount<Velocity>>> _speedTASMissionListMap) {
		this._speedTASMissionListMap = _speedTASMissionListMap;
	}

	public Map<Double, List<Double>> getMachMissionListMap() {
		return _machMissionListMap;
	}

	public void setMachMissionListMap(Map<Double, List<Double>> _machMissionListMap) {
		this._machMissionListMap = _machMissionListMap;
	}

	public Map<Double, List<Double>> getLiftingCoefficientMissionListMap() {
		return _liftingCoefficientMissionListMap;
	}

	public void setLiftingCoefficientMissionListMap(Map<Double, List<Double>> _liftingCoefficientMissionListMap) {
		this._liftingCoefficientMissionListMap = _liftingCoefficientMissionListMap;
	}

	public Map<Double, List<Double>> getDragCoefficientMissionListMap() {
		return _dragCoefficientMissionListMap;
	}

	public void setDragCoefficientMissionListMap(Map<Double, List<Double>> _dragCoefficientMissionListMap) {
		this._dragCoefficientMissionListMap = _dragCoefficientMissionListMap;
	}

	public Map<Double, List<Double>> getEfficiencyMissionListMap() {
		return _efficiencyMissionListMap;
	}

	public void setEfficiencyMissionListMap(Map<Double, List<Double>> _efficiencyMissionListMap) {
		this._efficiencyMissionListMap = _efficiencyMissionListMap;
	}

	public Map<Double, List<Amount<Force>>> getThrustMissionListMap() {
		return _thrustMissionListMap;
	}

	public void setThrustMissionListMap(Map<Double, List<Amount<Force>>> _thrustMissionListMap) {
		this._thrustMissionListMap = _thrustMissionListMap;
	}

	public Map<Double, List<Amount<Force>>> getDragMissionListMap() {
		return _dragMissionListMap;
	}

	public void setDragMissionListMap(Map<Double, List<Amount<Force>>> _dragMissionListMap) {
		this._dragMissionListMap = _dragMissionListMap;
	}

	public Map<Double, Amount<Mass>> getInitialFuelMassMap() {
		return _initialFuelMassMap;
	}

	public void setInitialFuelMassMap(Map<Double, Amount<Mass>> _initialFuelMassMap) {
		this._initialFuelMassMap = _initialFuelMassMap;
	}

	public Map<Double, Amount<Mass>> getTotalFuelUsedMap() {
		return _totalFuelUsedMap;
	}

	public void setTotalFuelUsedMap(Map<Double, Amount<Mass>> _totalFuelUsedMap) {
		this._totalFuelUsedMap = _totalFuelUsedMap;
	}

	public Map<Double, Amount<Duration>> getTotalMissionTimeMap() {
		return _totalMissionTimeMap;
	}

	public void setTotalMissionTimeMap(Map<Double, Amount<Duration>> _totalMissionTimeMap) {
		this._totalMissionTimeMap = _totalMissionTimeMap;
	}

	public Map<Double, Amount<Mass>> getInitialMissionMassMap() {
		return _initialMissionMassMap;
	}

	public void setInitialMissionMassMap(Map<Double, Amount<Mass>> _initialMissionMassMap) {
		this._initialMissionMassMap = _initialMissionMassMap;
	}

	public Map<Double, Amount<Mass>> getEndMissionMassMap() {
		return _endMissionMassMap;
	}

	public void setEndMissionMassMap(Map<Double, Amount<Mass>> _endMissionMassMap) {
		this._endMissionMassMap = _endMissionMassMap;
	}

	public Map<Double, List<Amount<Velocity>>> getSpeedCASMissionListMap() {
		return _speedCASMissionListMap;
	}

	public void setSpeedCASMissionListMap(Map<Double, List<Amount<Velocity>>> _speedCASMissionListMap) {
		this._speedCASMissionListMap = _speedCASMissionListMap;
	}

	public Map<Double, List<Amount<Velocity>>> getRateOfClimbMissionListMap() {
		return _rateOfClimbMissionListMap;
	}

	public void setRateOfClimbMissionListMap(Map<Double, List<Amount<Velocity>>> _rateOfClimbMissionListMap) {
		this._rateOfClimbMissionListMap = _rateOfClimbMissionListMap;
	}

	public Map<Double, List<Amount<Angle>>> getClimbAngleMissionListMap() {
		return _climbAngleMissionListMap;
	}

	public void setClimbAngleMissionListMap(Map<Double, List<Amount<Angle>>> _climbAngleMissionListMap) {
		this._climbAngleMissionListMap = _climbAngleMissionListMap;
	}

	public Map<Double, List<Double>> getFuelFlowMissionListMap() {
		return _fuelFlowMissionListMap;
	}

	public void setFuelFlowMissionListMap(Map<Double, List<Double>> _fuelFlowMissionListMap) {
		this._fuelFlowMissionListMap = _fuelFlowMissionListMap;
	}

	public Map<Double, List<Double>> getSFCMissionListMap() {
		return _sfcMissionListMap;
	}

	public void setSFCMissionListMap(Map<Double, List<Double>> _sfcMissionListMap) {
		this._sfcMissionListMap = _sfcMissionListMap;
	}

	public Map<Double, Amount<Mass>> getBlockFuelMap() {
		return _blockFuelMap;
	}

	public void setBlockFuelMap(Map<Double, Amount<Mass>> _blockFuelMap) {
		this._blockFuelMap = _blockFuelMap;
	}

	public Map<Double, Amount<Duration>> getBlockTimeMap() {
		return _blockTimeMap;
	}

	public void setBlockTimeMap(Map<Double, Amount<Duration>> _blockTimeMap) {
		this._blockTimeMap = _blockTimeMap;
	}

	public Map<Double, Amount<Length>> getTotalMissionRangeMap() {
		return _totalMissionRangeMap;
	}

	public void setTotalMissionRangeMap(Map<Double, Amount<Length>> _totalMissionRangeMap) {
		this._totalMissionRangeMap = _totalMissionRangeMap;
	}

	public Map<Double, List<Double>> getThrottleMissionListMap() {
		return _throttleMissionListMap;
	}

	public void setThrottleMissionListMap(Map<Double, List<Double>> _throttleMissionListMap) {
		this._throttleMissionListMap = _throttleMissionListMap;
	}

	public Map<Double, Amount<Length>> getTotalDistanceMap() {
		return _totalDistanceMap;
	}

	public void setTotalDistanceMap(Map<Double, Amount<Length>> _totalDistanceMap) {
		this._totalDistanceMap = _totalDistanceMap;
	}

	public Map<Double, Amount<Duration>> getTotalDurationMap() {
		return _totalDurationMap;
	}

	public void setTotalDurationMap(Map<Double, Amount<Duration>> _totalDurationMap) {
		this._totalDurationMap = _totalDurationMap;
	}

	public Map<Double, List<Amount<Length>>> getCertificationPointsLongitudinalDistanceMap() {
		return _certificationPointsLongitudinalDistanceMap;
	}

	public void setCertificationPointsLongitudinalDistanceMap(Map<Double, List<Amount<Length>>> _certificationPointsLongitudinalDistanceMap) {
		this._certificationPointsLongitudinalDistanceMap = _certificationPointsLongitudinalDistanceMap;
	}

	public Map<Double, List<Amount<Length>>> getCertificationPointsAltitudeMap() {
		return _certificationPointsAltitudeMap;
	}

	public void setCertificationPointsAltitudeMap(Map<Double, List<Amount<Length>>> _certificationPointsAltitudeMap) {
		this._certificationPointsAltitudeMap = _certificationPointsAltitudeMap;
	}

	public Map<Double, List<Amount<Velocity>>> getCertificationPointsSpeedTASMap() {
		return _certificationPointsSpeedTASMap;
	}

	public void setCertificationPointsSpeedTASMap(Map<Double, List<Amount<Velocity>>> _certificationPointsSpeedTASMap) {
		this._certificationPointsSpeedTASMap = _certificationPointsSpeedTASMap;
	}

	public Map<Double, List<Amount<Velocity>>> getCertificationPointsSpeedCASMap() {
		return _certificationPointsSpeedCASMap;
	}

	public void setCertificationPointsSpeedCASMap(Map<Double, List<Amount<Velocity>>> _certificationPointsSpeedCASMap) {
		this._certificationPointsSpeedCASMap = _certificationPointsSpeedCASMap;
	}

	public Map<Double, List<Amount<Angle>>> getCertificationPointsGammaMap() {
		return _certificationPointsGammaMap;
	}

	public void setCertificationPointsGammaMap(Map<Double, List<Amount<Angle>>> _certificationPointsGammaMap) {
		this._certificationPointsGammaMap = _certificationPointsGammaMap;
	}

	public Map<Double, List<Amount<Angle>>> getCertificationPointsAlphaMap() {
		return _certificationPointsAlphaMap;
	}

	public void setCertificationPointsAlphaMap(Map<Double, List<Amount<Angle>>> _certificationPointsAlphaMap) {
		this._certificationPointsAlphaMap = _certificationPointsAlphaMap;
	}

	public Map<Double, List<Amount<Angle>>> getCertificationPointsThetaMap() {
		return _certificationPointsThetaMap;
	}

	public void setCertificationPointsThetaMap(Map<Double, List<Amount<Angle>>> _certificationPointsThetaMap) {
		this._certificationPointsThetaMap = _certificationPointsThetaMap;
	}

	public Map<Double, List<Amount<Force>>> getCertificationPointsThrustMap() {
		return _certificationPointsThrustMap;
	}

	public void setCertificationPointsThrustMap(Map<Double, List<Amount<Force>>> _certificationPointsThrustMap) {
		this._certificationPointsThrustMap = _certificationPointsThrustMap;
	}

	public Map<Double, TakeOffNoiseTrajectoryCalc> getTheTakeOffNoiseTrajectoryCalculatorMap() {
		return _theTakeOffNoiseTrajectoryCalculatorMap;
	}

	public void setTheTakeOffNoiseTrajectoryCalculatorMap(Map<Double, TakeOffNoiseTrajectoryCalc> _theTakeOffNoiseTrajectoryCalculatorMap) {
		this._theTakeOffNoiseTrajectoryCalculatorMap = _theTakeOffNoiseTrajectoryCalculatorMap;
	}

	public Map<Double, LandingNoiseTrajectoryCalc> getTheLandingNoiseTrajectoryCalculatorMap() {
		return _theLandingNoiseTrajectoryCalculatorMap;
	}

	public void setTheLandingNoiseTrajectoryCalculatorMap(Map<Double, LandingNoiseTrajectoryCalc> _theLandingNoiseTrajectoryCalculatorMap) {
		this._theLandingNoiseTrajectoryCalculatorMap = _theLandingNoiseTrajectoryCalculatorMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffFuelMap() {
		return _takeOffFuelMap;
	}

	public void setTakeOffFuelMap(Map<Double, Amount<Mass>> _takeOffFuelMap) {
		this._takeOffFuelMap = _takeOffFuelMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffNOxEmissionsMap() {
		return _takeOffNOxEmissionsMap;
	}

	public void setTakeOffNOxEmissionsMap(Map<Double, Amount<Mass>> _takeOffNOxEmissionsMap) {
		this._takeOffNOxEmissionsMap = _takeOffNOxEmissionsMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffCOEmissionsMap() {
		return _takeOffCOEmissionsMap;
	}

	public void setTakeOffCOEmissionsMap(Map<Double, Amount<Mass>> _takeOffCOEmissionsMap) {
		this._takeOffCOEmissionsMap = _takeOffCOEmissionsMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffHCEmissionsMap() {
		return _takeOffHCEmissionsMap;
	}

	public void setTakeOffHCEmissionsMap(Map<Double, Amount<Mass>> _takeOffHCEmissionsMap) {
		this._takeOffHCEmissionsMap = _takeOffHCEmissionsMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffSootEmissionsMap() {
		return _takeOffSootEmissionsMap;
	}

	public void setTakeOffSootEmissionsMap(Map<Double, Amount<Mass>> _takeOffSootEmissionsMap) {
		this._takeOffSootEmissionsMap = _takeOffSootEmissionsMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffCO2EmissionsMap() {
		return _takeOffCO2EmissionsMap;
	}

	public void setTakeOffCO2EmissionsMap(Map<Double, Amount<Mass>> _takeOffCO2EmissionsMap) {
		this._takeOffCO2EmissionsMap = _takeOffCO2EmissionsMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffSOxEmissionsMap() {
		return _takeOffSOxEmissionsMap;
	}

	public void setTakeOffSOxEmissionsMap(Map<Double, Amount<Mass>> _takeOffSOxEmissionsMap) {
		this._takeOffSOxEmissionsMap = _takeOffSOxEmissionsMap;
	}

	public Map<Double, Amount<Mass>> getTakeOffH2OEmissionsMap() {
		return _takeOffH2OEmissionsMap;
	}

	public void setTakeOffH2OEmissionsMap(Map<Double, Amount<Mass>> _takeOffH2OEmissionsMap) {
		this._takeOffH2OEmissionsMap = _takeOffH2OEmissionsMap;
	}

	public Map<Double, Amount<Mass>> getEmissionsNOxDuringClimbMap() {
		return _emissionsNOxDuringClimbMap;
	}

	public void setEmissionsNOxDuringClimbMap(Map<Double, Amount<Mass>> _emissionsNOxDuringClimbMap) {
		this._emissionsNOxDuringClimbMap = _emissionsNOxDuringClimbMap;
	}

	public Map<Double, Amount<Mass>> getEmissionsCODuringClimbMap() {
		return _emissionsCODuringClimbMap;
	}

	public void setEmissionsCODuringClimbMap(Map<Double, Amount<Mass>> _emissionsCODuringClimbMap) {
		this._emissionsCODuringClimbMap = _emissionsCODuringClimbMap;
	}

	public Map<Double, Amount<Mass>> getEmissionsHCDuringClimbMap() {
		return _emissionsHCDuringClimbMap;
	}

	public void setEmissionsHCDuringClimbMap(Map<Double, Amount<Mass>> _emissionsHCDuringClimbMap) {
		this._emissionsHCDuringClimbMap = _emissionsHCDuringClimbMap;
	}

	public Map<Double, Amount<Mass>> getEmissionsSootDuringClimbMap() {
		return _emissionsSootDuringClimbMap;
	}

	public void setEmissionsSootDuringClimbMap(Map<Double, Amount<Mass>> _emissionsSootDuringClimbMap) {
		this._emissionsSootDuringClimbMap = _emissionsSootDuringClimbMap;
	}

	public Map<Double, Amount<Mass>> getEmissionsCO2DuringClimbMap() {
		return _emissionsCO2DuringClimbMap;
	}

	public void setEmissionsCO2DuringClimbMap(Map<Double, Amount<Mass>> _emissionsCO2DuringClimbMap) {
		this._emissionsCO2DuringClimbMap = _emissionsCO2DuringClimbMap;
	}

	public Map<Double, Amount<Mass>> getEmissionsSOxDuringClimbMap() {
		return _emissionsSOxDuringClimbMap;
	}

	public void setEmissionsSOxDuringClimbMap(Map<Double, Amount<Mass>> _emissionsSOxDuringClimbMap) {
		this._emissionsSOxDuringClimbMap = _emissionsSOxDuringClimbMap;
	}

	public Map<Double, Amount<Mass>> getEmissionsH2ODuringClimbMap() {
		return _emissionsH2ODuringClimbMap;
	}

	public void setEmissionsH2ODuringClimbMap(Map<Double, Amount<Mass>> _emissionsH2ODuringClimbMap) {
		this._emissionsH2ODuringClimbMap = _emissionsH2ODuringClimbMap;
	}

	public Map<Double, Amount<Mass>> getTotalDescentEmissionsNOxMap() {
		return _totalDescentEmissionsNOxMap;
	}

	public void setTotalDescentEmissionsNOxMap(Map<Double, Amount<Mass>> _totalDescentEmissionsNOxMap) {
		this._totalDescentEmissionsNOxMap = _totalDescentEmissionsNOxMap;
	}

	public Map<Double, Amount<Mass>> getTotalDescentEmissionsCOMap() {
		return _totalDescentEmissionsCOMap;
	}

	public void setTotalDescentEmissionsCOMap(Map<Double, Amount<Mass>> _totalDescentEmissionsCOMap) {
		this._totalDescentEmissionsCOMap = _totalDescentEmissionsCOMap;
	}

	public Map<Double, Amount<Mass>> getTotalDescentEmissionsHCMap() {
		return _totalDescentEmissionsHCMap;
	}

	public void setTotalDescentEmissionsHCMap(Map<Double, Amount<Mass>> _totalDescentEmissionsHCMap) {
		this._totalDescentEmissionsHCMap = _totalDescentEmissionsHCMap;
	}

	public Map<Double, Amount<Mass>> getTotalDescentEmissionsSootMap() {
		return _totalDescentEmissionsSootMap;
	}

	public void setTotalDescentEmissionsSootMap(Map<Double, Amount<Mass>> _totalDescentEmissionsSootMap) {
		this._totalDescentEmissionsSootMap = _totalDescentEmissionsSootMap;
	}

	public Map<Double, Amount<Mass>> getTotalDescentEmissionsCO2Map() {
		return _totalDescentEmissionsCO2Map;
	}

	public void setTotalDescentEmissionsCO2Map(Map<Double, Amount<Mass>> _totalDescentEmissionsCO2Map) {
		this._totalDescentEmissionsCO2Map = _totalDescentEmissionsCO2Map;
	}

	public Map<Double, Amount<Mass>> getTotalDescentEmissionsSOxMap() {
		return _totalDescentEmissionsSOxMap;
	}

	public void setTotalDescentEmissionsSOxMap(Map<Double, Amount<Mass>> _totalDescentEmissionsSOxMap) {
		this._totalDescentEmissionsSOxMap = _totalDescentEmissionsSOxMap;
	}

	public Map<Double, Amount<Mass>> getTotalDescentEmissionsH2OMap() {
		return _totalDescentEmissionsH2OMap;
	}

	public void setTotalDescentEmissionsH2OMap(Map<Double, Amount<Mass>> _totalDescentEmissionsH2OMap) {
		this._totalDescentEmissionsH2OMap = _totalDescentEmissionsH2OMap;
	}

}
