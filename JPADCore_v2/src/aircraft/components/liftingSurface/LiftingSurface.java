package aircraft.components.liftingSurface;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.componentmodel.AeroComponent;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import configuration.enumerations.ComponentEnum;
import standaloneutils.GeometryCalc;
import standaloneutils.MyArrayUtils;

public class LiftingSurface extends AeroComponent implements ILiftingSurface{

	private String _id = null;
	private ComponentEnum _type;
	private Amount<Length> _xApexConstructionAxes = null; 
	private Amount<Length> _yApexConstructionAxes = null; 
	private Amount<Length> _zApexConstructionAxes = null;

	private LiftingSurfaceCreator _liftingSurfaceCreator;

	private List<Airfoil> _airfoilList;
	
	private Amount<Area> _surface = null;
	private Double _aspectRatio = null; 
	private Double _taperRatioEquivalent = null;
	private Double _taperRatioActual = null; 
	private Double _taperRatioOpt = null; // (oswald factor)
	private Amount<Angle> _sweepQuarterChordEq = null; 
	private Amount<Angle> _sweepHalfChordEq = null; 
	private Amount<Angle> _dihedralMean = null;

	//	private double deltaFactorDrag;

	//================================================
	// Builder pattern via a nested public static class
	public static class LiftingSurfaceBuilder {

		private String __id = null;
		private ComponentEnum __type;
		private Amount<Length> __xApexConstructionAxes = null; 
		private Amount<Length> __yApexConstructionAxes = null; 
		private Amount<Length> __zApexConstructionAxes = null;
		private LiftingSurfaceCreator __liftingSurfaceCreator;
		private List<Airfoil> __airfoilList;
		
		public LiftingSurfaceBuilder(String id, ComponentEnum type) {
			// required parameter
			this.__id = id;
			this.__type = type;

			// optional parameters ...
			this.__airfoilList = new ArrayList<Airfoil>();
			
		}

		public LiftingSurfaceBuilder liftingSurfaceCreator(LiftingSurfaceCreator lsc) {
			this.__liftingSurfaceCreator = lsc;
			return this;
		}
		
		public LiftingSurface build() {
			return new LiftingSurface(this);
		}

	}

	private LiftingSurface(LiftingSurfaceBuilder builder) {
		super(builder.__id, builder.__type);
		this._id = builder.__id; 
		this._type = builder.__type;
		this._xApexConstructionAxes = builder.__xApexConstructionAxes; 
		this._yApexConstructionAxes = builder.__yApexConstructionAxes; 
		this._zApexConstructionAxes = builder.__zApexConstructionAxes;
		this._liftingSurfaceCreator = builder.__liftingSurfaceCreator;
		this._airfoilList = builder.__airfoilList;  
	}

	@Override
	public List<Airfoil> getAirfoilList() {
		
		Airfoil airfoilRoot = _liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot();
		
		return ;
	}
	
	@Override
	public double getChordAtYActual(Double y) {
		return GeometryCalc.getChordAtYActual(
				MyArrayUtils.convertListOfAmountTodoubleArray(_liftingSurfaceCreator.getDiscretizedYs()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(_liftingSurfaceCreator.getDiscretizedChords()),
				y
				);
	}
	
	@Override
	public Amount<Area> getSurface() {
		return _liftingSurfaceCreator.getSurfacePlanform();
	}

	@Override
	public double getAspectRatio() {
		return _liftingSurfaceCreator.getAspectRatio();
	}

	@Override
	public Amount<Length> getSpan() {
		return _liftingSurfaceCreator.getSpan();
	}

	@Override
	public Amount<Length> getSemiSpan() {
		return _liftingSurfaceCreator.getSemiSpan();
	}

	@Override
	public double getTaperRatio() {
		return _liftingSurfaceCreator.getTaperRatio();
	}

	@Override
	public double getTaperRatioEquivalent(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate).getTaperRatio();
	}

	@Override
	public LiftingSurfaceCreator getEquivalentWing(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate);
	}

	@Override
	public Amount<Length> getChordRootEquivalent(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate).getPanels().get(0).getChordRoot();
	}

	@Override
	public Amount<Length> getChordRoot() {
		return _liftingSurfaceCreator.getPanels().get(0).getChordRoot();
	}

	@Override
	public Amount<Length> getChordTip() {
		return _liftingSurfaceCreator.getPanels().get(
				_liftingSurfaceCreator.getPanels().size()-1
				)
				.getChordTip();
	}

	@Override
	public Amount<Angle> getSweepLEEquivalent(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate).getPanels().get(0).getSweepLeadingEdge();
	}

	@Override
	public Amount<Angle> getSweepHalfChordEquivalent(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate).getPanels().get(0).getSweepHalfChord();
	}

	@Override
	public Amount<Angle> getSweepQuarterChordEquivalent(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate).getPanels().get(0).getSweepQuarterChord();
	}

	@Override
	public Amount<Angle> getDihedralEquivalent(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate).getPanels().get(0).getDihedral();
	}

	@Override
	public LiftingSurfaceCreator getLiftingSurfaceCreator() {
		return _liftingSurfaceCreator;
	}

	@Override
	public void calculateGeometry() {
		_liftingSurfaceCreator.calculateGeometry();
	}

	@Override
	public void calculateGeometry(int nSections) {
		_liftingSurfaceCreator.calculateGeometry(nSections);
	}

	public String get_id() {
		return _id;
	}

	public ComponentEnum get_type() {
		return _type;
	}

	public Amount<Length> get_xApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public Amount<Length> get_yApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public Amount<Length> get_zApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public void set_type(ComponentEnum _type) {
		this._type = _type;
	}

	public void set_xApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public void set_yApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public void set_zApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public void set_liftingSurfaceCreator(LiftingSurfaceCreator _liftingSurfaceCreator) {
		this._liftingSurfaceCreator = _liftingSurfaceCreator;
	}

}

