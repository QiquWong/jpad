package aircraft.components.fuselage;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.creator.FuselageCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import analyses.OperatingConditions;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.WindshieldType;

public interface IFuselage {

	String getId();
	void setId(String _id);
	// GLOBAL DATA
	public int getDeckNumber();
	public Amount<Length> getLength();
	public Amount<Mass> getReferenceMass();
	public Amount<Length> getRoughness();
	
	// NOSE TRUNK
	public Double getNoseLengthRatio();
	public Double getFinesseRatio();
	public Amount<Length> getNoseTipHeightOffset();
	public Double getNoseDxCapPercent();
	public WindshieldType getWindshieldType();
	public Amount<Length> getWindshieldWidht();
	public Amount<Length> getWindshieldHeight();
	public Double getNoseMidSectionLowerToTotalHeightRatio();
	public Double getNoseMidSectionRhoUpper();
	public Double getNoseMidSectionRhoLower();
	
	// CYLINDRICAL TRUNK
	public Double getCylindricalLengthRatio();
	public Amount<Length> getSectionWidht();
	public Amount<Length> getSectionHeight();
	public Amount<Length> getHeightFromGround();
	public Double getSectionLowerToTotalHeightRatio();
	public Double getSectionRhoUpper();
	public Double getSectionRhoLower();
	
	// TAIL TRUNK
	public Amount<Length> getTailTipHeightOffset();
	public Double getTailDxCapPercent();
	public Double getTailMidSectionLowerToTotalHeightRatio();
	public Double getTailMidSectionRhoUpper();
	public Double getTailMidSectionRhoLower();
	
	// SPOILERS
	public List<SpoilerCreator> getSpoilers();
	
	// DERIVED DATA
	public void calculateGeometry(
			int np_N, int np_C, int np_T, // no. points @ Nose/Cabin/Tail
			int np_SecUp, int np_SecLow   // no. points @ Upper/Lower section
			);
	
//	public void calculateGeometry();
	
	public FuselageCreator getFuselageCreator();
	public void calculateCG(Aircraft aircraft, OperatingConditions conditions, MethodEnum method);
	public void calculateMass(Aircraft aircraft, OperatingConditions conditions, MethodEnum method);
	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap();
	public void setZ0(Amount<Length> z);
	public Amount<Length> getX0();
	public void setX0(Amount<Length> x);
	public Amount<Length> getY0();
	public void setY0(Amount<Length> y);
	public Amount<Length> getZ0();
	public ComponentEnum getType();
	Amount<Area> getsWet();
	
}
