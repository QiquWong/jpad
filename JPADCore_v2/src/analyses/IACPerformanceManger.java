package analyses;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import calculators.performance.FlightManeuveringEnvelopeCalc;
import calculators.performance.LandingCalc;
import calculators.performance.PayloadRangeCalcBreguet;
import calculators.performance.TakeOffCalc;
import calculators.performance.customdata.CeilingMap;
import calculators.performance.customdata.DragMap;
import calculators.performance.customdata.RCMap;
import calculators.performance.customdata.SpecificRangeMap;
import calculators.performance.customdata.ThrustMap;
import configuration.enumerations.PerformanceEnum;
import configuration.enumerations.PerformancePlotEnum;

public interface IACPerformanceManger {

	public void calculatePerformance(String resultsFolderPath);
	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException;
	
	public String getId();
	public void setId(String _id);
	
	public Aircraft getTheAircraft() ;
	public void setTheAircraft(Aircraft _theAircraft) ;

	public OperatingConditions getTheOperatingConditions() ;
	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) ;

	public Amount<Mass> getMaximumTakeOffMass() ;
	public void setMaximumTakeOffMass(Amount<Mass> _maximumTakeOffMass) ;

	public Amount<Mass> getOperatingEmptyMass();
	public void setOperatingEmptyMass(Amount<Mass> _operatingEmptyMass) ;

	public Amount<Mass> getMaximumFuelMass();
	public void setMaximumFuelMass(Amount<Mass> _maximumFuelMass) ;

	public Amount<Mass> getSinglePassengerMass();
	public void setSinglePassengerMass(Amount<Mass> _singlePassengerMass) ;

	public Double getCLmaxClean() ;
	public void setCLmaxClean(Double _cLmaxClean) ;

	public Amount<?> getCLAlphaClean() ;
	public void setCLAlphaClean(Amount<?> _cLAlphaClean) ;

	public Amount<?> getCLAlphaTakeOff() ;
	public void setCLAlphaTakeOff(Amount<?> _cLAlphaHighLift) ;

	public Amount<?> getCLAlphaLanding() ;
	public void setCLAlphaLanding(Amount<?> _cLAlphaLanding) ;

	public Amount<Duration> getDtRotation() ;
	public void setDtRotation(Amount<Duration> _dtRotation) ;

	public Amount<Duration> getDtHold() ;
	public void setDtHold(Amount<Duration> _dtHold) ;

	public Amount<Angle> getAlphaGround() ;
	public void setAlphaGround(Amount<Angle> _alphaGround) ;

	public Amount<Velocity> getWindSpeed() ;
	public void setWindSpeed(Amount<Velocity> _windSpeed) ;

	public Amount<Length> getObstacleTakeOff() ;
	public void setObstacleTakeOff(Amount<Length> _obstacleTakeOff) ;

	public Double getCLmaxTakeOff() ;
	public void setCLmaxTakeOff(Double _cLmaxTakeOff) ;

	public Double getCLZeroTakeOff() ;
	public void setCLZeroTakeOff(Double _cLZeroTakeOff) ;

	public Double getCLZeroLanding() ;
	public void setCLZeroLanding(Double _cLZeroLanding) ;

	public Double getKRotation() ;
	public void setKRotation(Double _kRotation) ;

	public Double getKCLmax() ;
	public void setKCLmax(Double _kCLmax) ;

	public Double getDragDueToEngineFailure() ;
	public void setDragDueToEngineFailure(Double _kDragDueToFailure) ;

	public Double getKAlphaDot() ;
	public void setKAlphaDot(Double _kAlphaDot) ;

	public Double getCLmaxLanding() ;
	public void setCLmaxLanding(Double _cLmaxLanding) ;

	public Amount<Length> getObstacleLanding() ;
	public void setObstacleLanding(Amount<Length> _obstacleLanding) ;

	public Amount<Angle> getThetaApproach() ;
	public void setThetaApproach(Amount<Angle> _thetaApproach) ;

	public Double getKApproach() ;
	public void setKApproach(Double _kApproach) ;

	public Double getKFlare() ;
	public void setKFlare(Double _kFlare) ;

	public Double getKTouchDown() ;
	public void setKTouchDown(Double _kTouchDown) ;

	public Amount<Duration> getFreeRollDuration() ;
	public void setFreeRollDuration(Amount<Duration> _freeRollDuration) ;

	public Double getCLmaxInverted() ;
	public void setCLmaxInverted(Double _cLmaxInverted) ;

	public List<PerformanceEnum> getTaskList() ;
	public void setTaskList(List<PerformanceEnum> _taskList) ;

	public List<PerformancePlotEnum> getPlotList() ;
	public void setPlotList(List<PerformancePlotEnum> _plotList) ;

	public Amount<Length> getTakeOffDistanceAOE() ;
	public void setTakeOffDistanceAOE(Amount<Length> _takeOffDistanceAOE) ;

	public Amount<Length> getTakeOffDistanceFAR25() ;
	public void setTakeOffDistanceFAR25(Amount<Length> _takeOffDistanceFAR25) ;

	public Amount<Length> getBalancedFieldLength() ;
	public void setBalancedFieldLength(Amount<Length> _balancedFieldLength) ;

	public Amount<Length> getGroundRollDistanceTakeOff() ;
	public void setGroundRollDistanceTakeOff(Amount<Length> _groundRoll) ;

	public Amount<Length> getRotationDistanceTakeOff() ;
	public void setRotationDistanceTakeOff(Amount<Length> _rotation) ;

	public Amount<Length> getAirborneDistanceTakeOff() ;
	public void setAirborneDistanceTakeOff(Amount<Length> _airborne) ;

	public Amount<Velocity> getVStallTakeOff() ;
	public void setVStallTakeOff(Amount<Velocity> _vsTO) ;

	public Amount<Velocity> getVRotation() ;
	public void setVRotation(Amount<Velocity> _vRotation) ;

	public Amount<Velocity> getVLiftOff() ;
	public void setVLiftOff(Amount<Velocity> _vLiftOff) ;

	public Amount<Velocity> getV1() ;
	public void setV1(Amount<Velocity> _v1) ;

	public Amount<Velocity> getV2() ;
	public void setV2(Amount<Velocity> _v2) ;

	public Double[] getPolarCLCruise() ;
	public void setPolarCLCruise(Double[] _polarCLCruise) ;

	public Double[] getPolarCDCruise() ;
	public void setPolarCDCruise(Double[] _polarCDCruise) ;

	public Double[] getPolarCLClimb() ;
	public void setPolarCLClimb(Double[] _polarCLClimb) ;

	public Double[] getPolarCDClimb() ;
	public void setPolarCDClimb(Double[] _polarCDClimb) ;

	public Double[] getPolarCLTakeOff() ;
	public void setPolarCLTakeOff(Double[] _polarCLTakeOff) ;

	public Double[] getPolarCDTakeOff() ;
	public void setPolarCDTakeOff(Double[] _polarCDTakeOff) ;

	public Double[] getPolarCLLanding() ;
	public void setPolarCLLanding(Double[] _polarCLLanding) ;

	public Double[] getPolarCDLanding() ;
	public void setPolarCDLanding(Double[] _polarCDLanding) ;

	public Amount<Length> getLandingDistance() ;
	public void setLandingDistance(Amount<Length> _landingDistance) ;

	public Amount<Length> getLandingDistanceFAR25() ;
	public void setLandingDistanceFAR25(Amount<Length> _landingDistanceFAR25) ;

	public Amount<Length> getGroundRollDistanceLanding() ;
	public void setGroundRollDistanceLanding(Amount<Length> _groundRollDistanceLanding) ;

	public Amount<Length> getFlareDistanceLanding() ;
	public void setFlareDistanceLanding(Amount<Length> _rotationDistanceLanding) ;

	public Amount<Length> getAirborneDistanceLanding() ;
	public void setAirborneDistanceLanding(Amount<Length> _airborneDistanceLanding) ;

	public Amount<Velocity> getVStallLanding() ;
	public void setVStallLanding(Amount<Velocity> _vStallLanding) ;

	public Amount<Velocity> getVApproach() ;
	public void setVApproach(Amount<Velocity> _vApproach) ;

	public Amount<Velocity> getVFlare() ;
	public void setVFlare(Amount<Velocity> _vFlare) ;

	public Amount<Velocity> getVTouchDown() ;
	public void setVTouchDown(Amount<Velocity> _vTouchDown) ;

	public Amount<Velocity> getStallSpeedFullFlap() ;
	public void setStallSpeedFullFlap(Amount<Velocity> _stallSpeedFullFlap) ;

	public Amount<Velocity> getStallSpeedClean() ;
	public void setStallSpeedClean(Amount<Velocity> _stallSpeedClean) ;

	public Amount<Velocity> getStallSpeedInverted() ;
	public void setStallSpeedInverted(Amount<Velocity> _stallSpeedInverted) ;

	public Amount<Velocity> getManeuveringSpeed() ;
	public void setManeuveringSpeed(Amount<Velocity> _maneuveringSpeed) ;

	public Amount<Velocity> getManeuveringFlapSpeed() ;
	public void setManeuveringFlapSpeed(Amount<Velocity> _maneuveringFlapSpeed) ;

	public Amount<Velocity> getManeuveringSpeedInverted() ;
	public void setManeuveringSpeedInverted(Amount<Velocity> _maneuveringSpeedInverted) ;

	public Amount<Velocity> getDesignFlapSpeed() ;
	public void setDesignFlapSpeed(Amount<Velocity> _designFlapSpeed) ;

	public Double getPositiveLoadFactorManeuveringSpeed() ;
	public void setPositiveLoadFactorManeuveringSpeed(Double _positiveLoadFactorManeuveringSpeed) ;

	public Double getPositiveLoadFactorCruisingSpeed();
	public void setPositiveLoadFactorCruisingSpeed(Double _positiveLoadFactorCruisingSpeed);

	public Double getPositiveLoadFactorDiveSpeed();
	public void setPositiveLoadFactorDiveSpeed(Double _positiveLoadFactorDiveSpeed);

	public Double getPositiveLoadFactorDesignFlapSpeed();
	public void setPositiveLoadFactorDesignFlapSpeed(Double _positiveLoadFactorDesignFlapSpeed);

	public Double getNegativeLoadFactorManeuveringSpeedInverted();
	public void setNegativeLoadFactorManeuveringSpeedInverted(Double _negativeLoadFactorManeuveringSpeedInverted);

	public Double getNegativeLoadFactorCruisingSpeed();
	public void setNegativeLoadFactorCruisingSpeed(Double _negativeLoadFactorCruisingSpeed);

	public Double getNegativeLoadFactorDiveSpeed();
	public void setNegativeLoadFactorDiveSpeed(Double _negativeLoadFactorDiveSpeed);

	public Double getPositiveLoadFactorManeuveringSpeedWithGust();
	public void setPositiveLoadFactorManeuveringSpeedWithGust(Double _positiveLoadFactorManeuveringSpeedWithGust);

	public Double getPositiveLoadFactorCruisingSpeedWithGust();
	public void setPositiveLoadFactorCruisingSpeedWithGust(Double _positiveLoadFactorCruisingSpeedWithGust);

	public Double getPositiveLoadFactorDiveSpeedWithGust();
	public void setPositiveLoadFactorDiveSpeedWithGust(Double _positiveLoadFactorDiveSpeedWithGust);

	public Double getPositiveLoadFactorDesignFlapSpeedWithGust();
	public void setPositiveLoadFactorDesignFlapSpeedWithGust(Double _positiveLoadFactorDesignFlapSpeedWithGust);

	public Double getNegativeLoadFactorManeuveringSpeedInvertedWithGust();
	public void setNegativeLoadFactorManeuveringSpeedInvertedWithGust(
			Double _negativeLoadFactorManeuveringSpeedInvertedWithGust);

	public Double getNegativeLoadFactorCruisingSpeedWithGust();
	public void setNegativeLoadFactorCruisingSpeedWithGust(Double _negativeLoadFactorCruisingSpeedWithGust);

	public Double getNegativeLoadFactorDiveSpeedWithGust();
	public void setNegativeLoadFactorDiveSpeedWithGust(Double _negativeLoadFactorDiveSpeedWithGust);

	public Double getNegativeLoadFactorDesignFlapSpeedWithGust();
	public void setNegativeLoadFactorDesignFlapSpeedWithGust(Double _negativeLoadFactorDesignFlapSpeedWithGust);

	public Amount<Velocity> getSpeedDescentCAS();
	public Amount<Velocity> getRateOfDescent();

	public void setRateOfDescent(Amount<Velocity> _rateOfDescent);
	public void setSpeedDescentCAS(Amount<Velocity> _vDescent);

	public TakeOffCalc getTheTakeOffCalculator();
	public void setTheTakeOffCalculator(TakeOffCalc _theTakeOffCalculator);

	public LandingCalc getTheLandingCalculator();
	public void setTheLandingCalculator(LandingCalc _theLandingCalculator);

	public FlightManeuveringEnvelopeCalc getTheEnvelopeCalculator();
	public void setTheEnvelopeCalculator(FlightManeuveringEnvelopeCalc _theEnvelopeCalculator);

	public Amount<Velocity> getMaxRateOfClimbAtCruiseAltitudeAOE();
	public void setMaxRateOfClimbAtCruiseAltitudeAOE(Amount<Velocity> _maxRateOfClimbAtCruiseAltitude);

	public Amount<Angle> getMaxThetaAtCruiseAltitudeAOE();
	public void setMaxThetaAtCruiseAltitudeAOE(Amount<Angle> _maxThetaAtCruiseAltitude);

	public Amount<Length> getAbsoluteCeilingAOE();
	public void setAbsoluteCeilingAOE(Amount<Length> _absoluteCeiling);

	public Amount<Length> getServiceCeilingAOE();
	public void setServiceCeilingAOE(Amount<Length> _serviceCeiling);

	public Amount<Duration> getMinimumClimbTimeAOE();
	public void setMinimumClimbTimeAOE(Amount<Duration> _minimumClimbTime);

	public Amount<Duration> getClimbTimeAtSpecificClimbSpeedAOE();
	public void setClimbTimeAtSpecificClimbSpeedAOE(Amount<Duration> _climbTimeAtSpecificClimbSpeed);
	
	public Amount<Length> getAbsoluteCeilingOEI();
	public void setAbsoluteCeilingOEI(Amount<Length> _absoluteCeilingOEI);

	public Amount<Length> getServiceCeilingOEI();
	public void setServiceCeilingOEI(Amount<Length> _serviceCeilingOEI);

	public List<RCMap> getRCMapAOE();
	public void setRCMapAOE(List<RCMap> _rcMapAOE);

	public List<RCMap> getRCMapOEI();
	public void setRCMapOEI(List<RCMap> _rcMapOEI);

	public CeilingMap getCeilingMapAOE();
	public void setCeilingMapAOE(CeilingMap _ceilingMapAOE);

	public CeilingMap getCeilingMapOEI();
	public void setCeilingMapOEI(CeilingMap _ceilingMapOEI);

	public Amount<Velocity> getClimbSpeed();
	public void setClimbSpeed(Amount<Velocity> _climbSpeed);

	public List<DragMap> getDragListAltitudeParameterization();
	public void setDragListAltitudeParameterization(List<DragMap> _dragListAltitudeParameterization);

	public List<ThrustMap> getThrustListAltitudeParameterization();
	public void setThrustListAltitudeParameterization(List<ThrustMap> _thrustListAltitudeParameterization);

	public List<DragMap> getDragListWeightParameterization();
	public void setDragListWeightParameterization(List<DragMap> _dragListWeightParameterization);

	public List<ThrustMap> getThrustListWeightParameterization();
	public void setThrustListWeightParameterization(List<ThrustMap> _thrustListWeightParameterization);

	public List<Amount<Force>> getWeightListCruise();
	public void setWeightListCruise(List<Amount<Force>> _weightListCruise);

	public List<Amount<Length>> getAltitudeListCruise();
	public void setAltitudeListCruise(List<Amount<Length>> _altitudeListCruise);

	public Map<String, List<Double>> getEfficiencyMapAltitude();
	public void setEfficiencyMapAltitude(Map<String, List<Double>> _efficiencyMapAltitude);

	public Map<String, List<Double>> getEfficiencyMapWeight();
	public void setEfficiencyMapWeight(Map<String, List<Double>> _efficiencyMapWeight);

	public List<SpecificRangeMap> getSpecificRangeMap();
	public void setSpecificRangeMap(List<SpecificRangeMap> _specificRangeMap);

	public List<Amount<Length>> getAltitudeList();
	public void setAltitudeList(List<Amount<Length>> _altitudeList);

	public List<Amount<Length>> getRangeList();
	public void setRangeList(List<Amount<Length>> _rangeList);

	public List<Amount<Duration>> getTimeList();
	public void setTimeList(List<Amount<Duration>> _timeList);

	public List<Amount<Mass>> getFuelUsedList();
	public void setFuelUsedList(List<Amount<Mass>> _fuelUsedList);

	public List<Amount<Mass>> getMassList();
	public void setMassList(List<Amount<Mass>> _weightList);

	public Amount<Mass> getTotalFuelUsed();
	public void setTotalFuelUsed(Amount<Mass> _totalFuelUsed);

	public Amount<Duration> getTotalMissionTime();
	public void setTotalMissionTime(Amount<Duration> _totalMissionTime);

	public Amount<Duration> getTakeOffDuration();
	public void setTakeOffDuration(Amount<Duration> _takeOffDuration);

	public Amount<Duration> getLandingDuration();
	public void setLandingDuration(Amount<Duration> _landingDuration);

	public List<DragMap> getDragListAOE();
	public void setDragListAOE(List<DragMap> _dragListAOE);
	
	public List<DragMap> getDragListOEI();
	public void setDragListOEI(List<DragMap> _dragListOEI);
	
	public List<ThrustMap> getThrustListAOE();
	public void setThrustListAOE(List<ThrustMap> _thrustListAOE);
	
	public List<ThrustMap> getThrustListOEI();
	public void setThrustListOEI(List<ThrustMap> _thrustListOEI);

	public List<Amount<Length>> getDescentLengths();
	public void setDescentLengths(List<Amount<Length>> _descentLength);

	public List<Amount<Duration>> getDescentTimes();
	public void setDescentTimes(List<Amount<Duration>> _descentTime);

	public List<Amount<Angle>> getDescentAngles();
	public void setDescentAngles(List<Amount<Angle>> _descentAngle);

	public Amount<Length> getTotalDescentLength();
	public void setTotalDescentLength(Amount<Length> _totalDescentLength);

	public Amount<Duration> getTotalDescentTime();
	public void setTotalDescentTime(Amount<Duration> _totalDescentTime);

	public Amount<Velocity> getMaxSpeesTASAtCruiseAltitude();
	public void setMaxSpeesTASAtCruiseAltitude(Amount<Velocity> _maxSpeesTASAtCruiseAltitude);

	public Amount<Velocity> getMinSpeesTASAtCruiseAltitude();
	public void setMinSpeesTASAtCruiseAltitude(Amount<Velocity> _minSpeesTASAtCruiseAltitude);

	public Amount<Velocity> getMaxSpeesCASAtCruiseAltitude();
	public void setMaxSpeesCASAtCruiseAltitude(Amount<Velocity> _maxSpeesCASAtCruiseAltitude);

	public Amount<Velocity> getMinSpeesCASAtCruiseAltitude();
	public void setMinSpeesCASAtCruiseAltitude(Amount<Velocity> _minSpeesCASAtCruiseAltitude);

	public Double getMaxMachAtCruiseAltitude();
	public void setMaxMachAtCruiseAltitude(Double _maxMachAtCruiseAltitude);

	public Double getMinMachAtCruiseAltitude();
	public void setMinMachAtCruiseAltitude(Double _minMachAtCruiseAltitude);

	public Double getEfficiencyAtCruiseAltitudeAndMach();
	public void setEfficiencyAtCruiseAltitudeAndMach(Double _efficiencyAtCruiseAltitudeAndMach);

	public Amount<Force> getThrustAtCruiseAltitudeAndMach();
	public void setThrustAtCruiseAltitudeAndMach(Amount<Force> _thrustAtCruiseAltitudeAndMach);

	public Amount<Force> getDragAtCruiseAltitudeAndMach();
	public void setDragAtCruiseAltitudeAndMach(Amount<Force> _dragAtCruiseAltitudeAndMach);

	public Amount<Power> getPowerAvailableAtCruiseAltitudeAndMach();
	public void setPowerAvailableAtCruiseAltitudeAndMach(Amount<Power> _powerAvailableAtCruiseAltitudeAndMach);

	public Amount<Power> getPowerNeededAtCruiseAltitudeAndMach();
	public void setPowerNeededAtCruiseAltitudeAndMach(Amount<Power> _powerNeededAtCruiseAltitudeAndMach);

	public Amount<Mass> getEndMissionMass();
	public void setEndMissionMass(Amount<Mass> _endMissionMass);

	public Amount<Length> getXCGMTOM();
	public void setXCGMTOM(Amount<Length> _xCGMaxAft);

	public Amount<Velocity> getVMC();
	public void setVMC(Amount<Velocity> _vMC);
	
	public Amount<Length> getXCGMaxAft();
	public void setXCGMaxAft(Amount<Length> _xCGMaxAft);
	
	public double[] getThrustMomentOEI();
	public void setThrustMomentOEI(double[] _thrustMomentOEI);

	public double[] getYawingMomentOEI();
	public void setYawingMomentOEI(double[] _yawingMomentOEI);
	
}
