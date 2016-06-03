package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;
import org.jscience.physics.amount.Amount;

public interface ISlatCreator {

	Double getInnerStationSpanwisePosition();
	Double getOuterStationSpanwisePosition();
	void setInnerStationSpanwisePosition(Double etaIn);
	void setOuterStationSpanwisePosition(Double etaOut);
	
	Double getInnerChordRatio();
	void setInnerChordRatio(Double cfcIn);
	Double getOuterChordRatio();
	void setOuterChordRatio(Double cfcOut);
	Double getMeanChordRatio();
	void setMeanChordRatio(Double cfcOut);
	
	Double getExtensionRatio();
	void setExtensionRatio(Double extensionRatio);
	
	Amount<Angle> getMinimumDeflection();
	void setMinimumDeflection(Amount<Angle> deltaSlatMin);
	Amount<Angle> getMaximumDeflection();
	void setMaximumDeflection(Amount<Angle> deltaSlatMax);
	
	public void calculateMeanChordRatio(Double cfcIn, Double cfcOut);
	
}
