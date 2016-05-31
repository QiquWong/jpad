package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;

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
	
	Amount<Angle> getDeflection();
	void setDeflection(Amount<Angle> deltaFlap);
	
	public void calculateMeanChordRatio(Double cfcIn, Double cfcOut);
}
