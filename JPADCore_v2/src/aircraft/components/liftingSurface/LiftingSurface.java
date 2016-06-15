package aircraft.components.liftingSurface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PositionRelativeToAttachmentEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.GeometryCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class LiftingSurface implements ILiftingSurface{

	private String _id = null;
	private ComponentEnum _type;

	// TODO : THESE DATA WILL COME FROM AIRCRAFT CLASS
	private PositionRelativeToAttachmentEnum _positionRelativeToAttachment;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Angle> _riggingAngle = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
	
	private LiftingSurfaceCreator _liftingSurfaceCreator;

	private AerodynamicDatabaseReader _aeroDatabaseReader;
	
	private CenterOfGravity _cg;
	private Amount<Length> _xCG, _yCG, _zCG;
	Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	Double[] _percentDifferenceXCG;
	Double[] _percentDifferenceYCG;
	
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
		Map <MethodEnum, Amount<Length>> __xCGMap;
		Map <MethodEnum, Amount<Length>> __yCGMap;
		Map <AnalysisTypeEnum, List<MethodEnum>> __methodsMap;
		
		public LiftingSurfaceBuilder(String id, ComponentEnum type, AerodynamicDatabaseReader aeroDatabaseReader) {
			// required parameter
			this.__id = id;
			this.__type = type;
			this.__aeroDatabaseReader = aeroDatabaseReader;
			
			// optional parameters ...
			this.__airfoilList = new ArrayList<Airfoil>(); 
			this.__xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
			this.__yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
			this.__methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
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
		this._id = builder.__id; 
		this._type = builder.__type;
		this._xApexConstructionAxes = builder.__xApexConstructionAxes; 
		this._yApexConstructionAxes = builder.__yApexConstructionAxes; 
		this._zApexConstructionAxes = builder.__zApexConstructionAxes;
		this._liftingSurfaceCreator = builder.__liftingSurfaceCreator;
		this._aeroDatabaseReader = builder.__aeroDatabaseReader;
		this._airfoilList = builder.__airfoilList;
		this._xCGMap = builder.__xCGMap;
		this._yCGMap = builder.__yCGMap;
		this._methodsMap = builder.__methodsMap;
	}

	public void calculateCG(MethodEnum method, ComponentEnum type) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();
		
		// THESE DATA WILL COME FROM AIRCRAFT CLASS
		_cg.setLRForigin(_xApexConstructionAxes,
						 _yApexConstructionAxes,
						 _zApexConstructionAxes
						 );
		
		_cg.set_xLRFref(getChordRoot().times(0.4));
		_cg.set_yLRFref(getSpan().times(0.5*0.4));
		_cg.set_zLRFref(Amount.valueOf(0., SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		methodsList = new ArrayList<MethodEnum>();

		_xCG = Amount.valueOf(0., SI.METER);
		_yCG = Amount.valueOf(0., SI.METER);
		_zCG = Amount.valueOf(0., SI.METER);

		Double lambda = _liftingSurfaceCreator.getTaperRatioEquivalentWing(),
				span = getSpan().getEstimatedValue(),
				xRearSpar,
				xFrontSpar;

		switch (type) {
		case WING : {
			switch(method) {

			//		 Bad results ...
			case SFORZA : { // page 359 Sforza (2014) - Aircraft Design
				methodsList.add(method);
				_yCG = Amount.valueOf(
						(span/6) * 
						((1+2*lambda)/(1-lambda)),
						SI.METER);

				_xCG = Amount.valueOf(
						_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.35*(span/2) 
						, SI.METER);

				xRearSpar = 0.6*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue());
				xFrontSpar = 0.25*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue());

				_xCG = Amount.valueOf(
						0.7*(xRearSpar - xFrontSpar)
						+ 0.25*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);

				//				System.out.println("x: " + _xCG 
				//				+ ", y: " + _yCG 
				//				+ ", xLE: " + getXLEAtYEquivalent(_yCG.getEstimatedValue()));
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;

			}

		} break;

		case HORIZONTAL_TAIL : {

			switch(method) {

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.38*(span/2) 
						, SI.METER);

				_xCG = Amount.valueOf(
						0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case VERTICAL_TAIL : {

			switch(method) {

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);

				if (_positionRelativeToAttachment
						.equals(PositionRelativeToAttachmentEnum.T_TAIL)) {
					_yCG = Amount.valueOf(
							0.55*(span/2) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
							, SI.METER);
				} else if (
						(_positionRelativeToAttachment
								.equals(PositionRelativeToAttachmentEnum.CONVENTIONAL))
						|| 
						(_positionRelativeToAttachment
								.equals(PositionRelativeToAttachmentEnum.CROSS))
						){
					_yCG = Amount.valueOf(
							0.38*(span/2) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
							, SI.METER);
				}

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case CANARD : {

		} break;

		default : {} break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];
		_percentDifferenceYCG = new Double[_yCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.set_yLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_yLRFref(), 
				_yCGMap,
				_percentDifferenceYCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

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
		if(recalculate)
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return _liftingSurfaceCreator.getTaperRatioEquivalentWing();
	}

	@Override
	public LiftingSurfaceCreator getEquivalentWing(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate);
	}

	@Override
	public Amount<Length> getChordRootEquivalent(Boolean recalculate) {
		if(recalculate) 
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return _liftingSurfaceCreator.getRootChordEquivalentWing();
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
		if(recalculate)
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return LSGeometryCalc.calculateSweep(
				_liftingSurfaceCreator.getEquivalentWingAspectRatio(),
				_liftingSurfaceCreator.getTaperRatioEquivalentWing(),
				_liftingSurfaceCreator.getSweepQuarterChordEquivalentWing().doubleValue(SI.RADIAN),
				0.0,
				0.25
				).to(NonSI.DEGREE_ANGLE);
				
	}

	@Override
	public Amount<Angle> getSweepHalfChordEquivalent(Boolean recalculate) {
		if(recalculate) 
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return LSGeometryCalc.calculateSweep(
				_liftingSurfaceCreator.getEquivalentWingAspectRatio(),
				_liftingSurfaceCreator.getTaperRatioEquivalentWing(),
				_liftingSurfaceCreator.getSweepQuarterChordEquivalentWing().doubleValue(SI.RADIAN),
				0.0,
				0.5
				).to(NonSI.DEGREE_ANGLE);
	}

	@Override
	public Amount<Angle> getSweepQuarterChordEquivalent(Boolean recalculate) {
		if(recalculate)
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return _liftingSurfaceCreator.getSweepQuarterChordEquivalentWing();
	}

	@Override
	public LiftingSurfaceCreator getLiftingSurfaceCreator() {
		return _liftingSurfaceCreator;
	}

	@Override
	public void calculateGeometry(ComponentEnum type, Boolean mirrored) {
		_liftingSurfaceCreator.calculateGeometry(type, mirrored);
	}

	@Override
	public void calculateGeometry(int nSections, ComponentEnum type, Boolean mirrored) {
		_liftingSurfaceCreator.calculateGeometry(nSections, type, mirrored);
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public ComponentEnum getType() {
		return _type;
	}

	@Override
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}
	
	@Override
	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	@Override
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public void setType(ComponentEnum _type) {
		this._type = _type;
	}

	@Override
	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	@Override
	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	@Override
	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	@Override
	public void setLiftingSurfaceCreator(LiftingSurfaceCreator _liftingSurfaceCreator) {
		this._liftingSurfaceCreator = _liftingSurfaceCreator;
	}

	@Override
	public AerodynamicDatabaseReader getAerodynamicDatabaseReader() {
		return this._aeroDatabaseReader;
	}
	
	@Override
	public void setAerodynamicDatabaseReader(AerodynamicDatabaseReader aeroDatabaseReader) {
		this._aeroDatabaseReader = aeroDatabaseReader;
	}

	@Override
	public Amount<Angle> getRiggingAngle() {
		return this._riggingAngle;
	}
	
	@Override
	public void setRiggingAngle (Amount<Angle> iW) {
		this._riggingAngle = iW;
	}

	@Override
	public CenterOfGravity getCG() {
		return _cg;
	}

	@Override
	public Amount<Length> getXCG() {
		return _xCG;
	}

	@Override
	public Amount<Length> getYCG() {
		return _yCG;
	}

	@Override
	public Amount<Length> getZCG() {
		return _zCG;
	}

	@Override
	public void setCG(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	@Override
	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	@Override
	public void setYCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}

	@Override
	public void setZCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	@Override
	public PositionRelativeToAttachmentEnum getPositionRelativeToAttachment() {
		return _positionRelativeToAttachment;
	}

	@Override
	public void setPositionRelativeToAttachment(PositionRelativeToAttachmentEnum _positionRelativeToAttachment) {
		this._positionRelativeToAttachment = _positionRelativeToAttachment;
	}
}
