package aircraft.components.fuselage.creator;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.creator.SpoilerCreator;
import configuration.enumerations.WindshieldTypeEnum;

public interface IFuselageCreator {
	
	String getId();
	void setId(String id);
	Boolean getPressurized();
	// Global data
	int getDeckNumber();
	void setDeckNumber(int dn);
	Amount<Length> getLenF();
	Amount<Mass> getMassReference();
	void setMassReference(Amount<Mass> massRef);
	Amount<Length> getRoughness();
	void setRoughness(Amount<Length> roughness);
	// Nose trunk
	Amount<Length> getLenN();
	Double getLambdaN();
	Double getLenRatioNF();
	Amount<Angle> getWindshieldAngle();
	void setWindshieldAngle(Amount<Angle> windshieldAngle);
	//Cylindrical trunk
	Double getLenRatioCF();
	Double getLambdaC();
	Amount<Length> getLenC();
	Amount<Length> getHeightFromGround();
	Amount<Angle> getUpsweepAngle();
	//Tail trunk 	
	Double getLenRatioTF();
	void setUpsweepAngle(Amount<Angle> upsweepAngle);
	Double getLambdaT();
	
	// Other
	public void calculateGeometry(
			int np_N, int np_C, int np_T, // no. points @ Nose/Cabin/Tail
			int np_SecUp, int np_SecLow   // no. points @ Upper/Lower section
			);
	public void calculateGeometry();

// These are in IFuselage interface
//	List<Amount<Length>> getXYZ0();
//	Amount<Length> getX0();
//	Amount<Length> getY0();
//	Amount<Length> getZ0();
//	void setXYZ0(Amount<Length> x0, Amount<Length> y0, Amount<Length> z0);

	
	Double getLambdaF();
	void setLenF(Amount<Length> lenF);
	Amount<Area> getsWet();
	Amount<Area> getsFront();
	Amount<Length> getSectionCylinderWidth();
	WindshieldTypeEnum getWindshieldType();
	Amount<Length> getEquivalentDiameterCylinderAM();
	Amount<Length> getEquivalentDiameterGM();
	Amount<Length> getEquivalentDiameterCylinderGM();
	Amount<Length> getSectionCylinderHeight();
	Amount<Length> getLenT();
	List<SpoilerCreator> getSpoilers();
	void setSpoilers(List<SpoilerCreator> spoilers);
	
}
