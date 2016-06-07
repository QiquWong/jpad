package aircraft.components.liftingSurface.creator;

import java.util.List;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import org.jscience.physics.amount.Amount;
import javaslang.Tuple2;

public interface ILiftingSurfaceCreator {

	public void calculateGeometry(int numberSpanwiseStations);
	public void calculateGeometry();

	List<Amount<Length>> getXYZ0();
	Amount<Length> getX0();
	Amount<Length> getY0();
	Amount<Length> getZ0();
	void setXYZ0(Amount<Length> x0, Amount<Length> y0, Amount<Length> z0);

	List<Amount<Length>> getXYZPole();
	Amount<Length> getXPole();
	Amount<Length> getYPole();
	Amount<Length> getZPole();
	void setXYZPole(Amount<Length> xp, Amount<Length> yp, Amount<Length> zp);

	void discretizeGeometry(int numberSpanwiseStations);

	public Amount<Length> getMeanAerodynamicChord(boolean recalculate);
	public Amount<Length> getMeanAerodynamicChord();

	List<Amount<Length>> getMeanAerodynamicChordLeadingEdge(boolean recalculate);
	List<Amount<Length>> getMeanAerodynamicChordLeadingEdge();

	Amount<Length> getMeanAerodynamicChordLeadingEdgeX(boolean recalculate);
	Amount<Length> getMeanAerodynamicChordLeadingEdgeX();
	Amount<Length> getMeanAerodynamicChordLeadingEdgeY(boolean recalculate);
	Amount<Length> getMeanAerodynamicChordLeadingEdgeY();
	Amount<Length> getMeanAerodynamicChordLeadingEdgeZ(boolean recalculate);
	Amount<Length> getMeanAerodynamicChordLeadingEdgeZ();

	public Amount<Area> getSurfacePlanform(boolean recalculate);
	public Amount<Area> getSurfacePlanform();

	public Amount<Area> getSurfaceWetted(boolean recalculate);
	public Amount<Area> getSurfaceWetted();

	public Amount<Length> getSemiSpan(boolean recalculate);
	public Amount<Length> getSemiSpan();
	public Amount<Length> getSpan(boolean recalculate);
	public Amount<Length> getSpan();

	public Double getAspectRatio(boolean recalculate);
	public Double getAspectRatio();

	public Double getTaperRatio(boolean recalculate);
	public Double getTaperRatio();

	public LiftingSurfaceCreator getEquivalentWing(boolean recalculate);

	public List<LiftingSurfacePanelCreator> getPanels();
	public void addPanel(LiftingSurfacePanelCreator panel);

	public List<SymmetricFlapCreator> getSymmetricFlaps();
	public void addSymmetricFlaps(SymmetricFlapCreator symmetricFlaps);
	
	public List<SlatCreator> getSlats();
	public void addSlats(SlatCreator slats);
	
	public List<AsymmetricFlapCreator> getAsymmetricFlaps();
	public void addAsymmetricFlaps(AsymmetricFlapCreator asymmetricFlaps);
	
	public List<SpoilerCreator> getSpoilers();
	public void addSpoilers(SpoilerCreator spoilers);

	public List<Amount<Length>> getDiscretizedYs();
	public List<Amount<Length>> getDiscretizedChords();
	public List<Amount<Length>> getDiscretizedXle();
	public List<Amount<Length>> getDiscretizedZle();
	public List<Amount<Angle>> getDiscretizedTwists();

	public Amount<Length> getXLEAtYActual(Double yStation);
	public Amount<Angle> getDihedralAtYActual(Double yStation);
	public Amount<Angle> getDihedralSemispanAtYActual(Double yStation);
	
	public List<
		Tuple2<
			Amount<Length>, // Xs
			Amount<Length>  // Ys
			>
		> getDiscretizedTopViewAsList();

	public Double[][] getDiscretizedTopViewAsArray();
	
	public Amount<Angle> getAngleOfIncidence();

	public void reportPanelsToSpanwiseDiscretizedVariables();
}
