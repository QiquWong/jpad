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
	
	Amount<Angle> getDeflection();
	void setDeflection(Amount<Angle> deltaFlap);
	
	
	public void calculateMeanChordRatio(Double cfcIn, Double cfcOut);
	
}
