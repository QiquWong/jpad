package aircraft.components.fuselage.creator;

import java.util.List;

import javax.measure.quantity.Length;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.creator.SpoilerCreator;
import configuration.enumerations.WindshieldTypeEnum;

@FreeBuilder
public interface IFuselageCreator {
	
	String getId();
	int getDeckNumber();
	boolean isPressurized();
	Amount<Length> getFuselageLength();
	double getNoseLengthRatio();
	double getCylinderLengthRatio();
	Amount<Length> getSectionCylinderWidth();
	Amount<Length> getSectionCylinderHeight();
	Amount<Length> getHeightFromGround();
	Amount<Length> getRoughness();
	Amount<Length> getNoseTipOffset();
	Amount<Length> getTailTipOffest();
	double getNoseCapOffsetPercent();
	double getTailCapOffsetPercent();
	WindshieldTypeEnum getWindshieldType();
	Amount<Length> getWindshieldHeight();
	Amount<Length> getWindshieldWidth();
	
	// how lower part is different from half diameter
	double getSectionNoseMidLowerToTotalHeightRatio();
	double getSectionCylinderLowerToTotalHeightRatio();
	double getSectionTailMidLowerToTotalHeightRatio();
	// shape index, 1 --> close to a rectangle; 0 --> close to a circle
	double getSectionCylinderRhoUpper();
	double getSectionCylinderRhoLower();
	double getSectionMidNoseRhoUpper();
	double getSectionMidTailRhoUpper();
	double getSectionMidNoseRhoLower();
	double getSectionMidTailRhoLower();
	
	List<SpoilerCreator> getSpoilers();
	
	class Builder extends IFuselageCreator_Builder  { 
		 public Builder() {
			 
			 
		 }
	}
}
