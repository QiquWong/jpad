package aircraft.components.liftingSurface.airfoils;

import java.util.List;

import javax.annotation.Nullable;
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
	double[] getXCoordinates();
	double[] getZCoordinates();
	@Nullable
	Amount<Angle> getAlphaZeroLift();
	@Nullable
	Amount<Angle> getAlphaEndLinearTrait();
	@Nullable
	Amount<Angle> getAlphaStall();
	@Nullable
	Amount<?> getClAlphaLinearTrait();
	@Nullable
	double getCdMin();
	@Nullable
	double getClAtCdMin();
	@Nullable
	double getClAtAlphaZero();
	@Nullable
	double getClEndLinearTrait();
	@Nullable
	double getClMax();
	@Nullable
	double getKFactorDragPolar();
	@Nullable
	double getLaminarBucketSemiExtension();
	@Nullable
	double getLaminarBucketDepth();
	@Nullable
	Amount<?> getCmAlphaQuarterChord();
	@Nullable
	double getXACNormalized();
	@Nullable
	double getCmAC();
	@Nullable
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
