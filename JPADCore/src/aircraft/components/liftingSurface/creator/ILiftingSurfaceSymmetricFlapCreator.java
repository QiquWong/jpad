package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.FlapTypeEnum;

public interface ILiftingSurfaceSymmetricFlapCreator {

	FlapTypeEnum getType();
	
	Double getInnerStationSpanwisePosition();
	Double getOuterStationSpanwisePosition();
	
	Double getChordRatio();
	
	Amount<Angle> getDeflection();
	
	// TODO: ADD THE CALCULATION OF THE DELTA_FLAP/DELTA_FLAP_REF
	
}
