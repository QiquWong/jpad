package calculators.weights;

import javax.measure.quantity.Mass;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.enumerations.PrimaryElectricSystemsEnum;
import standaloneutils.atmosphere.AtmosphereCalc;

public class SystemsWeightCalc {

	public static Amount<Mass> calculateControlSurfaceMassJenkinson (Aircraft aircraft) {
		
		return Amount.valueOf(
				Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().times(0.04).getEstimatedValue(), 0.684),
				SI.KILOGRAM);
		
	}
	
	public static Amount<Mass> calculateControlSurfaceMassTorenbeek1982 (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.4915*Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().getEstimatedValue(), 2/3), 
				SI.KILOGRAM);
		
	}
	
	public static Amount<Mass> calculateAPUMassTorenbeek1982 (Aircraft aircraft) {
		
		return Amount.valueOf(
				2.25*1.17*Math.pow(
						aircraft.getCabinConfiguration().getActualPassengerNumber()*0.5, 
						3/5
						), 
				SI.KILOGRAM);
		
	}
	
	public static Amount<Mass> calculateInstrumentsAndNavigationMassTorenbeek1982 (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.347*Math.pow(
						aircraft.getTheAnalysisManager().getTheWeights().getManufacturerEmptyMass().doubleValue(SI.KILOGRAM), 
						5/9
						)*Math.pow(
								aircraft.getTheAnalysisManager().getTheWeights().getTheWeightsManagerInterface().getReferenceMissionRange().doubleValue(SI.KILOMETER), 
								0.25
								), 
				SI.KILOGRAM);
		
	}
	
	public static Amount<Mass> calculateElectricalSystemsMassTorenbeek1982 (Aircraft aircraft) {
		
		Amount<Mass> _electricalSystemsMass = Amount.valueOf(0.0, SI.KILOGRAM);

		if(aircraft.getSystems().getTheSystemsInterface().getPrimaryElectricSystemsType().equals(PrimaryElectricSystemsEnum.DC)) {
			_electricalSystemsMass = Amount.valueOf(
					0.02*aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM) + 181,
					SI.KILOGRAM);
		}
		else if(aircraft.getSystems().getTheSystemsInterface().getPrimaryElectricSystemsType().equals(PrimaryElectricSystemsEnum.AC)) {

			double pel = 0.0;
			Amount<Volume> fuselageCabinVolume = Amount.valueOf(
					aircraft.getFuselage().getCylinderSectionArea().doubleValue(SI.SQUARE_METRE)
					*aircraft.getFuselage().getCylinderLength().doubleValue(SI.METER), 
					SI.CUBIC_METRE
					);
			if(fuselageCabinVolume.doubleValue(SI.CUBIC_METRE) < 227)
				pel = fuselageCabinVolume.doubleValue(SI.CUBIC_METRE)*0.565;
			else
				pel = Math.pow(fuselageCabinVolume.doubleValue(SI.CUBIC_METRE), 0.7)*3.64;

			_electricalSystemsMass = Amount.valueOf(
					16.3*pel*(1-0.033*Math.sqrt(pel)), 
					SI.KILOGRAM);

		}
		
		return _electricalSystemsMass;
		
	}
		
	public static Amount<Mass> calculateAirConditionAndAntiIcingTorenbeek1982 (Aircraft aircraft) {
		
		return Amount.valueOf(
				14.0*Math.pow(aircraft.getFuselage().getCylinderLength().doubleValue(SI.METER), 1.28), 
				SI.KILOGRAM);
		
	}
	
	public static Amount<Mass> calculateFurnishingsAndEquipmentsMassTorenbeek1982 (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.196*Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().doubleValue(SI.KILOGRAM), 0.91), 
				SI.KILOGRAM);
		
	}
	
	/*
	 * page 257 Torenbeek 2013
	 */
	public static Amount<Mass> calculateFurnishingsAndEquipmentsMassTorenbeek2013 (Aircraft aircraft) {
		
		return Amount.valueOf(
				(12
						* aircraft.getFuselage().getFuselageLength().getEstimatedValue()
						* aircraft.getFuselage().getEquivalentDiameterCylinderGM().getEstimatedValue() 
						* ( 3
								* aircraft.getFuselage().getEquivalentDiameterCylinderGM().getEstimatedValue() 
								+ 0.5 * aircraft.getFuselage().getDeckNumber() + 1) + 3500) /
				AtmosphereCalc.g0.getEstimatedValue(),
				SI.KILOGRAM);
		
	}
	
}
