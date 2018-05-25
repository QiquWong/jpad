package analyses;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;

@FreeBuilder
public interface IACWeightsManager {

	String getId();
	Aircraft getTheAircraft();
	OperatingConditions getTheOperatingConditions();
	
	// GLOBAL DATA
	Amount<Mass> getFirstGuessMaxTakeOffMass();
	double getRelativeMaximumLandingMass();
	Amount<Mass> getSinglePassengerMass();
	
	// MISSION DATA
	Amount<Length> getReferenceMissionRange();
	Amount<Length> getCruiseRange();
	double getCruiseSFC();
	double getCruiseEfficiency();
	Amount<Length> getAlternateCruiseRange();
	Amount<Length> getAlternateCruiseAltitide();
	double getAlternateCruiseMachNumber();
	double getAlternateCruiseSFC();
	double getAlternateCruiseEfficiency();
	Amount<Duration> getHoldingDuration();
	Amount<Length> getHoldingAltitide();
	double getHoldingMachNumber();
	double getHoldingSFC();
	double getHoldingEfficiency();
	
	// CALIBRATION
	double getFuselageCalibrationFactor();
	double getWingCalibrationFactor();
	double getHTailCalibrationFactor();
	double getVTailCalibrationFactor();
	double getCanardCalibrationFactor();
	double getNacellesCalibrationFactor();
	double getPowerPlantCalibrationFactor();
	double getLandingGearsCalibrationFactor();
	double getAPUCalibrationFactor();
	double getAirConditioningAndAntiIcingSystemCalibrationFactor();
	double getInstrumentsAndNavigationSystemCalibrationFactor();
	double getElectricalSystemsCalibrationFactor();
	double getControlSurfaceCalibrationFactor();
	double getFurnishingsAndEquipmentsCalibrationFactor();
	
	class Builder extends IACWeightsManager_Builder {
		public Builder() {
			
		}
	}
}
