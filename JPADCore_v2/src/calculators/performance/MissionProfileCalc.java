package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.OperatingConditions;

public class MissionProfileCalc {

	//--------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	//............................................................................................
	// Input:
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	
	private Amount<Length> _takeOffLength;
	private Amount<Length> _rcMapAOE;
	private Amount<Length> _firstGuessCruiseLength;
	private Amount<Length> _alternateCruiseLength;
	private Amount<Length> _descentLength;
	private Amount<Length> _landingLength;
	
	private Amount<Duration> _takeOffDuration;
	private Amount<Duration> _climbDuration;
	private Amount<Duration> _holdingDuration;
	private Amount<Duration> _descentDuration;
	private Amount<Duration> _landingDuration;
	
	private Double _takeOffFuelFlow;
	private Double _landingFuelFlow;
	
	//............................................................................................
	// Output:
	private List<Amount<Length>> _altitudeList;
	private List<Amount<Length>> _rangeList;
	private List<Amount<Duration>> _timeList;
	private List<Amount<Mass>> _fuelUsedList;
	private List<Amount<Mass>> _massList;
	private Amount<Mass> _totalFuelUsed;
	private Amount<Duration> _totalMissionTime;
	private Amount<Mass> _endMissionMass;
	
	//--------------------------------------------------------------------------------------------
	// BUILDER:
	public MissionProfileCalc(
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			Amount<Length> takeOffLength,
			Amount<Length> rcMapAOE,
			Amount<Length> firstGuessCruiseLength,
			Amount<Length> alternateCruiseLength,
			Amount<Length> descentLength,
			Amount<Length> landingLength,
			Amount<Duration> takeOffDuration,
			Amount<Duration> climbDuration,
			Amount<Duration> holdingDuration,
			Amount<Duration> descentDuration,
			Amount<Duration> landingDuration,
			Double takeOffFuelFlow,
			Double landingFuelFlow
			) {
		
		this._theAircraft = theAircraft;
		this._theOperatingConditions = theOperatingConditions;
		this._takeOffLength = takeOffLength;
		this._rcMapAOE = rcMapAOE;
		this._firstGuessCruiseLength = firstGuessCruiseLength;
		this._alternateCruiseLength = alternateCruiseLength;
		this._descentLength = descentLength;
		this._landingLength = landingLength;
		this._takeOffDuration = takeOffDuration;
		this._climbDuration = climbDuration;
		this._holdingDuration = holdingDuration;
		this._descentDuration = descentDuration;
		this._landingDuration = landingDuration;
		this._takeOffFuelFlow = takeOffFuelFlow;
		this._landingFuelFlow = landingFuelFlow;
		
		this._altitudeList = new ArrayList<>();
		this._rangeList = new ArrayList<>();
		this._timeList = new ArrayList<>();
		this._fuelUsedList = new ArrayList<>();
		this._massList = new ArrayList<>();
		
	}
	
	//--------------------------------------------------------------------------------------------
	// METHODS:
	
	public void calculateRangeProfile() {
		
		
		
	}
	
	public void calculateDurationProfile() {
		
		
		
	}
	
	public void calculateFuelUsedProfile() {
		
		
		
	}
	
	//--------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	public List<Amount<Length>> getAltitudeList() {
		return _altitudeList;
	}
	public void setAltitudeList(List<Amount<Length>> _altitudeList) {
		this._altitudeList = _altitudeList;
	}
	public List<Amount<Length>> getRangeList() {
		return _rangeList;
	}
	public void setRangeList(List<Amount<Length>> _rangeList) {
		this._rangeList = _rangeList;
	}
	public List<Amount<Duration>> getTimeList() {
		return _timeList;
	}
	public void setTimeList(List<Amount<Duration>> _timeList) {
		this._timeList = _timeList;
	}
	public List<Amount<Mass>> getFuelUsedList() {
		return _fuelUsedList;
	}
	public void setFuelUsedList(List<Amount<Mass>> _fuelUsedList) {
		this._fuelUsedList = _fuelUsedList;
	}
	public List<Amount<Mass>> getMassList() {
		return _massList;
	}
	public void setMassList(List<Amount<Mass>> _massList) {
		this._massList = _massList;
	}
	public Amount<Mass> getTotalFuelUsed() {
		return _totalFuelUsed;
	}
	public void setTotalFuelUsed(Amount<Mass> _totalFuelUsed) {
		this._totalFuelUsed = _totalFuelUsed;
	}
	public Amount<Duration> getTotalMissionTime() {
		return _totalMissionTime;
	}
	public void setTotalMissionTime(Amount<Duration> _totalMissionTime) {
		this._totalMissionTime = _totalMissionTime;
	}
	public Amount<Mass> getEndMissionMass() {
		return _endMissionMass;
	}
	public void setEndMissionMass(Amount<Mass> _endMissionMass) {
		this._endMissionMass = _endMissionMass;
	}
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
	public Amount<Length> getTakeOffLength() {
		return _takeOffLength;
	}
	public void setTakeOffLength(Amount<Length> _takeOffLength) {
		this._takeOffLength = _takeOffLength;
	}
	public Amount<Length> getRCMapAOE() {
		return _rcMapAOE;
	}
	public void setRCMapAOE(Amount<Length> _rcMapAOE) {
		this._rcMapAOE = _rcMapAOE;
	}
	public Amount<Length> getFirstGuessCruiseLength() {
		return _firstGuessCruiseLength;
	}
	public void setFirstGuessCruiseLength(Amount<Length> _firstGuessCruiseLength) {
		this._firstGuessCruiseLength = _firstGuessCruiseLength;
	}
	public Amount<Length> getAlternateCruiseLength() {
		return _alternateCruiseLength;
	}
	public void setAlternateCruiseLength(Amount<Length> _alternateCruiseLength) {
		this._alternateCruiseLength = _alternateCruiseLength;
	}
	public Amount<Length> getDescentLength() {
		return _descentLength;
	}
	public void setDescentLength(Amount<Length> _descentLength) {
		this._descentLength = _descentLength;
	}
	public Amount<Length> getLandingLength() {
		return _landingLength;
	}
	public void setLandingLength(Amount<Length> _landingLength) {
		this._landingLength = _landingLength;
	}
	public Amount<Duration> getTakeOffDuration() {
		return _takeOffDuration;
	}
	public void setTakeOffDuration(Amount<Duration> _takeOffDuration) {
		this._takeOffDuration = _takeOffDuration;
	}
	public Amount<Duration> getClimbDuration() {
		return _climbDuration;
	}
	public void setClimbDuration(Amount<Duration> _climbDuration) {
		this._climbDuration = _climbDuration;
	}
	public Amount<Duration> getHoldingDuration() {
		return _holdingDuration;
	}
	public void setHoldingDuration(Amount<Duration> _holdingDuration) {
		this._holdingDuration = _holdingDuration;
	}
	public Amount<Duration> getDescentDuration() {
		return _descentDuration;
	}
	public void setDescentDuration(Amount<Duration> _descentDuration) {
		this._descentDuration = _descentDuration;
	}
	public Amount<Duration> getLandingDuration() {
		return _landingDuration;
	}
	public void setLandingDuration(Amount<Duration> _landingDuration) {
		this._landingDuration = _landingDuration;
	}
	public Double getTakeOffFuelFlow() {
		return _takeOffFuelFlow;
	}
	public void setTakeOffFuelFlow(Double _takeOffFuelFlow) {
		this._takeOffFuelFlow = _takeOffFuelFlow;
	}
	public Double getLandingFuelFlow() {
		return _landingFuelFlow;
	}
	public void setLandingFuelFlow(Double _landingFuelFlow) {
		this._landingFuelFlow = _landingFuelFlow;
	}
}
