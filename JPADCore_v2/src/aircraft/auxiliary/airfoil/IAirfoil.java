package aircraft.auxiliary.airfoil;

import java.util.List;

import javax.measure.quantity.Angle;

import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;

@FreeBuilder
public interface IAirfoil {

	String getName();
	AirfoilTypeEnum getType();
	AirfoilFamilyEnum getFamily();
	double getThicknessToChordRatio();
	double getRadiusLeadingEdgeNormalized();
	Double[] getXCoordinates();
	Double[] getZCoordinates();
	Amount<Angle> getAlphaZeroLift();
	Amount<Angle> getAlphaEndLinearTrait();
	Amount<Angle> getAlphaStall();
	Amount<?> getClAlphaLinearTrait();
	double getCdMin();
	Amount<?> getClAtCdMin();
	double getClAtAlphaZero();
	double getClEndLinearTrait();
	double getClMax();
	double getKFactorDragPolar();
	double getLaminarBucketSemiExtension();
	double getLaminarBucketDepth();
	Amount<?> getCmAlphaQuarterChord();
	double getXACNormalized();
	double getCmAC();
	double getCmACAtStall();
	double getCriticalMach();
	double getXTransitionUpper();
	double getXTransitionLower();
	boolean getClCurveFromFile();
	boolean getCdCurveFromFile();
	boolean getCmCurveFromFile();
	List<Double> getClCurve();
	List<Double> getCdCurve();
	List<Double> getCmCurve();
	List<Amount<Angle>> getAlphaForClCurve();
	List<Double> getClForCdCurve();
	List<Double> getClForCmCurve();
	
	class Builder extends IAirfoil_Builder {
		public Builder() {
			
		}
	}
}
