package aircraft.components.liftingSurface.adm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import javaslang.Tuple2;
import javaslang.Tuple5;
import javaslang.Tuple6;
import standaloneutils.customdata.MyArray;

public abstract class AbstractLiftingSurface implements ILiftingSurface {

	protected String id;

	protected List<LiftingSurfacePanel> panels;
	protected List<SymmetricFlaps> symmetricFlaps;
	protected List<Slats> slats;
	protected List<AsymmetricFlaps> asymmetricFlaps;
	protected List<Spoilers> spoilers;

	// in BRF
	protected Amount<Length> x0;
	protected Amount<Length> y0;
	protected Amount<Length> z0;

	// in LRF
	protected Amount<Length> xPole;
	protected Amount<Length> yPole;
	protected Amount<Length> zPole;

	protected Amount<Length> meanAerodynamicChord;
	protected Amount<Length> meanAerodynamicChordLeadingEdgeX;
	protected Amount<Length> meanAerodynamicChordLeadingEdgeY;
	protected Amount<Length> meanAerodynamicChordLeadingEdgeZ;

	protected Amount<Area> surfacePlanform;
	protected Amount<Area> surfaceWetted;

	protected Amount<Length> semiSpan, span;
	protected Double aspectRatio;
	protected Double taperRatio;

	protected LiftingSurfacePanel equivalentWing;

	//=======================================================================

	MyArray _eta;

	List<Amount<Length>> _yBreakPoints;
	List<Amount<Length>> _xLEBreakPoints;
	List<Amount<Length>> _zLEBreakPoints;
	List<Amount<Length>> _chordsBreakPoints;
	List<Amount<Angle>> _twistsBreakPoints;

//	Map<LiftingSurfacePanel, List<Amount<Length>>> _panelToYStations;
	
	List<
			Tuple2<
				LiftingSurfacePanel,
				Tuple5<
					List<Amount<Length>>, // Ys
					List<Amount<Length>>, // chords
					List<Amount<Length>>, // Xle
					List<Amount<Length>>, // Zle
					List<Amount<Angle>>   // twist
					> 
				>
		> _panelToSpanwiseDiscretizedVariables;

	List<Amount<Length>> _yStationActual; // MyArray _yStationActual;

//	MyArray _chordsVsYActual;
//	MyArray _xLEvsYActual;
//	MyArray _xTEvsYActual;


	//=======================================================================

	public List<LiftingSurfacePanel> getPanels() {
		return panels;
	}

	public List<SymmetricFlaps> getSymmetricFlaps() {
		return symmetricFlaps;
	}

	public List<Slats> getSlats() {
		return slats;
	}

	public List<AsymmetricFlaps> getAsymmetricFlaps() {
		return asymmetricFlaps;
	}

	public List<Spoilers> getSpoilers() {
		return spoilers;
	}

}
