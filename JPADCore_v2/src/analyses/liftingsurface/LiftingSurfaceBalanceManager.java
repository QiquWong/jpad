package analyses.liftingsurface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class LiftingSurfaceBalanceManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Map <MethodEnum, Amount<Length>> _xCGMap;
	private Map <MethodEnum, Amount<Length>> _yCGMap;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap; 
	private List<MethodEnum> _methodsList;
	private Amount<Length> _xCG, _yCG;
	private CenterOfGravity _cg;
	double[] _percentDifferenceXCG;
	double[] _percentDifferenceYCG;
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public LiftingSurfaceBalanceManager () {
		
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._xCGMap = new HashMap<>();
		this._yCGMap = new HashMap<>();
		this._methodsMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		
	}

	public void calculateCG(ComponentEnum type, Map<ComponentEnum, MethodEnum> methodsMap) {
//		calculateCG(MethodEnum.SFORZA, type);
		calculateCG(MethodEnum.TORENBEEK_1982, type);
		
		if(!methodsMap.get(type).equals(MethodEnum.AVERAGE)) { 
			_cg.setXLRF(_xCGMap.get(methodsMap.get(type)));
			_cg.setYLRF(_yCGMap.get(methodsMap.get(type)));
		}
		else {
			_percentDifferenceXCG = new double[_xCGMap.size()];
			_percentDifferenceYCG = new double[_yCGMap.size()];

			_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getXLRFref(), 
					_xCGMap,
					_percentDifferenceXCG,
					100.).getFilteredMean(), SI.METER));

			_cg.setYLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getYLRFref(), 
					_yCGMap,
					_percentDifferenceYCG,
					100.).getFilteredMean(), SI.METER));
		}
		_cg.calculateCGinBRF(type);
	}
	
	private void calculateCG(MethodEnum method, ComponentEnum type) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

		_cg = new CenterOfGravity();
		
		_cg.setLRForigin(_xApexConstructionAxes,
						 _yApexConstructionAxes,
						 _zApexConstructionAxes
				);

		_cg.setXLRFref(_liftingSurfaceCreator.getPanels().get(0).getChordRoot().to(SI.METER).times(0.4));
		_cg.setYLRFref(_liftingSurfaceCreator.getSpan().to(SI.METER).times(0.5*0.4));
		_cg.setZLRFref(Amount.valueOf(0., SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		methodsList = new ArrayList<MethodEnum>();

		_xCG = Amount.valueOf(0., SI.METER);
		_yCG = Amount.valueOf(0., SI.METER);
		_zCG = Amount.valueOf(0., SI.METER);

		@SuppressWarnings("unused")
		Double lambda = 0.0;
		if(type.equals(ComponentEnum.WING))
			lambda = _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getTaperRatio();
		else
			lambda = _liftingSurfaceCreator.getPanels().get(0).getTaperRatio();
		Double span = _liftingSurfaceCreator.getSpan().doubleValue(SI.METER);
		Double xRearSpar = _liftingSurfaceCreator.getSecondarySparDimensionlessPosition();
		Double xFrontSpar = _liftingSurfaceCreator.getMainSparDimensionlessPosition();

		switch (type) {
		case WING : {
			switch(method) {

//			//		 Bad results ...
//			case SFORZA : { // page 359 Sforza (2014) - Aircraft Design
//				methodsList.add(method);
//				_yCG = Amount.valueOf(
//						(span/6) * 
//						((1+2*lambda)/(1-lambda)),
//						SI.METER);
//
//				_xCG = Amount.valueOf(
//						(_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())/2)
//						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
//						, SI.METER);
//				_xCGMap.put(method, _xCG);
//				_yCGMap.put(method, _yCG);
//			} break;

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.35*(span/2) 
						, SI.METER);

				xRearSpar = 0.6*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER));
				xFrontSpar = 0.25*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER));

				_xCG = Amount.valueOf(
						0.7*(xRearSpar - xFrontSpar)
						+ 0.25*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER))
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.doubleValue(SI.METER))
						, SI.METER);

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
						(0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER)))
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.doubleValue(SI.METER))
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

				if (_positionRelativeToAttachment > 0.8) {
					_yCG = Amount.valueOf(
							0.55*(span) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER))
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.doubleValue(SI.METER))
							, SI.METER);
				} else {
					_yCG = Amount.valueOf(
							0.38*(span) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.doubleValue(SI.METER))
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.doubleValue(SI.METER))
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

	}
	
	//------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	//------------------------------------------------------------------------------
	
	public Map<MethodEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}

	public void setXCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}

	public Map<MethodEnum, Amount<Length>> getYCGMap() {
		return _yCGMap;
	}

	public void setYCGMap(Map<MethodEnum, Amount<Length>> _yCGMap) {
		this._yCGMap = _yCGMap;
	}
	
	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}

	public void setMethodsMap(Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap) {
		this._methodsMap = _methodsMap;
	}

	public List<MethodEnum> getMethodsList() {
		return _methodsList;
	}

	public void setMethodsList(List<MethodEnum> _methodsList) {
		this._methodsList = _methodsList;
	}

	public Amount<Length> getXCG() {
		return _xCG;
	}

	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}
	
	public Amount<Length> getYCG() {
		return _yCG;
	}

	public void setYCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}

	public CenterOfGravity getCG() {
		return _cg;
	}

	public void setCG(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	public double[] getPercentDifferenceXCG() {
		return _percentDifferenceXCG;
	}

	public void setPercentDifferenceXCG(double[] _percentDifferenceXCG) {
		this._percentDifferenceXCG = _percentDifferenceXCG;
	}
	
	public double[] getPercentDifferenceYCG() {
		return _percentDifferenceYCG;
	}

	public void setPercentDifferenceYCG(double[] _percentDifferenceYCG) {
		this._percentDifferenceYCG = _percentDifferenceYCG;
	}
	
}
