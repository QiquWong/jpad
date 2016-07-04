package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.FlapTypeEnum;

public interface ISymmetricFlapCreator {

	FlapTypeEnum getType();
	void setType(FlapTypeEnum flapType);
	
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
	
	Amount<Angle> getMinimumDeflection();
	void setMinimumDeflection(Amount<Angle> deltaFlapMin);
	Amount<Angle> getMaximumDeflection();
	void setMaximumDeflection(Amount<Angle> deltaFlapMax);
	
	public void calculateMeanChordRatio(Double cfcIn, Double cfcOut);
}
