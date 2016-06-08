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
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.GeometryCalc;
import standaloneutils.MyArrayUtils;

public class LiftingSurface extends AeroComponent implements ILiftingSurface{

	private String _id = null;
	private ComponentEnum _type;
	private Amount<Length> _xApexConstructionAxes = null; 
	private Amount<Length> _yApexConstructionAxes = null; 
	private Amount<Length> _zApexConstructionAxes = null;

	private LiftingSurfaceCreator _liftingSurfaceCreator;

	private AerodynamicDatabaseReader _aeroDatabaseReader;
	
	private List<Airfoil> _airfoilList;
	
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
		private AerodynamicDatabaseReader __aeroDatabaseReader;
		
		public LiftingSurfaceBuilder(String id, ComponentEnum type, AerodynamicDatabaseReader aeroDatabaseReader) {
			// required parameter
			this.__id = id;
			this.__type = type;
			this.__aeroDatabaseReader = aeroDatabaseReader;
			
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
		this._aeroDatabaseReader = builder.__aeroDatabaseReader;
		this._airfoilList = builder.__airfoilList;
	}

	@Override
	public List<Airfoil> populateAirfoilList(
			AerodynamicDatabaseReader aeroDatabaseReader,
			Boolean equivalentWingFlag
			) {	
		
		int nPanels = this._liftingSurfaceCreator.getPanels().size();

		if(!equivalentWingFlag) {
			Airfoil airfoilRoot = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilRoot);

			for(int i=0; i<nPanels - 2; i++) {

				Airfoil innerAirfoil = new Airfoil(
						this._liftingSurfaceCreator.getPanels().get(i).getAirfoilTip(),
						aeroDatabaseReader
						); 
				this._airfoilList.add(innerAirfoil);
			}

			Airfoil airfoilTip = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(nPanels - 1).getAirfoilTip(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilTip);
		}

		else{
			Airfoil airfoilRoot = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilRoot);

			Airfoil airfoilKink = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(0).getAirfoilTip(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilKink);

			Airfoil airfoilTip = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(1).getAirfoilTip(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilTip);
		}

		return this._airfoilList;
	}
	
	@Override
	public List<Airfoil> getAirfoilList() {	
		return this._airfoilList;
	}
	
	@Override
	public void setAirfoilList(List<Airfoil> airfoilList) {	
		this._airfoilList = airfoilList;
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
	public void calculateGeometry(ComponentEnum type) {
		_liftingSurfaceCreator.calculateGeometry(type);
	}

	@Override
	public void calculateGeometry(int nSections, ComponentEnum type) {
		_liftingSurfaceCreator.calculateGeometry(nSections, type);
	}

	public String get_id() {
		return _id;
	}

	public ComponentEnum getType() {
		return _type;
	}

	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public void setType(ComponentEnum _type) {
		this._type = _type;
	}

	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public void setLiftingSurfaceCreator(LiftingSurfaceCreator _liftingSurfaceCreator) {
		this._liftingSurfaceCreator = _liftingSurfaceCreator;
	}

	public AerodynamicDatabaseReader getAerodynamicDatabaseReader() {
		return this._aeroDatabaseReader;
	}
	
	public void setAerodynamicDatabaseReader(AerodynamicDatabaseReader aeroDatabaseReader) {
		this._aeroDatabaseReader = aeroDatabaseReader;
	}

}
