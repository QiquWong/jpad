package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;

import org.jscience.physics.amount.Amount;

public interface ISpoilerCreator {

	Double getInnerStationSpanwisePosition();
	Double getOuterStationSpanwisePosition();
	void setInnerStationSpanwisePosition(Double etaIn);
	void setOuterStationSpanwisePosition(Double etaOut);
	
	Double getInnerStationChordwisePosition();
	Double getOuterStationChordwisePosition();
	void setInnerStationChordwisePosition(Double xIn);
	void setOuterStationChordwisePosition(Double xOut);
	
	Amount<Angle> getMinimumDeflection();
	void setMinimumDeflection(Amount<Angle> deltaSpoilerMin);
	Amount<Angle> getMaximumDeflection();
	void setMaximumDeflection(Amount<Angle> deltaSpoilerMax);
	
}
