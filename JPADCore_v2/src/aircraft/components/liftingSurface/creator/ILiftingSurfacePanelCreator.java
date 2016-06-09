package aircraft.components.liftingSurface.creator;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;

public interface ILiftingSurfacePanelCreator {

	/*
	 * Given root chord, tip chord, span, l.e. sweep angle
	 * calculates the rest of the wing parameters:
	 * - planform and wetted surface, 
	 * - taper ratio, 
	 * - aspect ratio, 
	 * - sweep angle of quarter-chord line,
	 * - sweep angle of half-chord line,
	 * - sweep angle of trailing edge line,
	 * - mean aerodynamic chord and its position in LRF 
	 */
	public void calculateGeometry();

	String getId();
	void setId (String name);
	
	Amount<Length> getChordRoot();
	void setChordRoot(Amount<Length> cr);

	Amount<Length> getChordTip();
	void setChordTip(Amount<Length> ct);
	
	AirfoilCreator getAirfoilRoot();
	void setAirfoilRoot(AirfoilCreator a);

	AirfoilCreator getAirfoilTip();
	void setAirfoilTip(AirfoilCreator a);

	Amount<Angle> getTwistGeometricAtTip();
	void setTwistGeometricAtTip(Amount<Angle> epsilonG);

	Amount<Angle> getTwistAerodynamicAtTip();
	
	Amount<Length> getSemiSpan();
	void setSemiSpan(Amount<Length> b);
	
	Amount<Angle> getSweepLeadingEdge();
	Amount<Angle> getSweepQuarterChord();
	Amount<Angle> getSweepHalfChord();
	Amount<Angle> getSweepAtTrailingEdge();
	void setSweepAtLeadingEdge(Amount<Angle> lambda);
	
	Amount<Angle> getDihedral();
	void setDihedral(Amount<Angle> gamma);
	
	public Amount<Length> getMeanAerodynamicChord();
	List<Amount<Length>> getMeanAerodynamicChordLeadingEdge();
	Amount<Length> getMeanAerodynamicChordLeadingEdgeX();
	Amount<Length> getMeanAerodynamicChordLeadingEdgeY();
	Amount<Length> getMeanAerodynamicChordLeadingEdgeZ();

	public Amount<Area> getSurfacePlanform();
	public Amount<Area> getSurfaceWetted();

	public Double getAspectRatio();
	public Double getTaperRatio();
	
	/** 
	 * Returns the chord of the wing panel at y station (distance from local root)
	 * 
	 * @author Agostino De Marco
	 * @param y in meter
	 * @return in meter
	 */
	default double getChordAtY(Double y) {

		double chord = 
			(
				(4 * getSurfacePlanform().getEstimatedValue())/
					(getSemiSpan().getEstimatedValue() * (1 + getTaperRatio()))
			)
			*(
				1 - (
						4 * y * (1 - getTaperRatio())/getSemiSpan().getEstimatedValue() 
					)
			);
		return chord;

	}


}
