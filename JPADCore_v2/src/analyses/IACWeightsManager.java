package analyses;

import javax.annotation.Nullable;
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
	boolean getEstimateMissionFuelFlag();
	@Nullable
	Amount<Mass> getMissionFuel();
	@Nullable
	Amount<Length> getReferenceMissionRange();
	@Nullable
	Amount<Length> getCruiseRange();
	@Nullable
	double getCruiseSFC();
	@Nullable
	double getCruiseEfficiency();
	@Nullable
	Amount<Length> getAlternateCruiseRange();
	@Nullable
	Amount<Length> getAlternateCruiseAltitide();
	@Nullable
	double getAlternateCruiseMachNumber();
	@Nullable
	double getAlternateCruiseSFC();
	@Nullable
	double getAlternateCruiseEfficiency();
	@Nullable
	Amount<Duration> getHoldingDuration();
	@Nullable
	Amount<Length> getHoldingAltitide();
	@Nullable
	double getHoldingMachNumber();
	@Nullable
	double getHoldingSFC();
	@Nullable
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
	double getHydraulicAndPneumaticCalibrationFactor();
	double getElectricalSystemsCalibrationFactor();
	double getControlSurfaceCalibrationFactor();
	double getFurnishingsAndEquipmentsCalibrationFactor();
	
	class Builder extends IACWeightsManager_Builder {
		public Builder() {
			
		}
	}
}
