package analyses.liftingsurface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.balance.LiftingSurfaceBalanceCalc;
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
		this._methodsList = new ArrayList<>();
		
	}

	public void calculateCG(Aircraft aircraft, ComponentEnum liftingSurfaceType, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		
		switch(aircraft.getWing().getType()) {
		
		case WING:
			calculateCG(aircraft, MethodEnum.SFORZA, liftingSurfaceType);
			calculateCG(aircraft, MethodEnum.TORENBEEK_1982, liftingSurfaceType);
		break;
		case HORIZONTAL_TAIL:
			calculateCG(aircraft, MethodEnum.TORENBEEK_1982, liftingSurfaceType);
		break;
		case VERTICAL_TAIL:
			calculateCG(aircraft, MethodEnum.TORENBEEK_1982, liftingSurfaceType);
		break;
		case CANARD:
			calculateCG(aircraft, MethodEnum.TORENBEEK_1982, liftingSurfaceType);
		break;
		default:
			break;

		}
		
		if(!methodsMapWeights.get(liftingSurfaceType).equals(MethodEnum.AVERAGE)) { 
			_cg.setXLRF(_xCGMap.get(methodsMapWeights.get(liftingSurfaceType)));
			_cg.setYLRF(_yCGMap.get(methodsMapWeights.get(liftingSurfaceType)));
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
		_cg.calculateCGinBRF(liftingSurfaceType);
	}
	
	private void calculateCG(Aircraft aircraft, MethodEnum method, ComponentEnum type) {

		switch (type) {
		case WING : {
			
			_cg = new CenterOfGravity();
			_cg.setLRForigin(
					aircraft.getWing().getXApexConstructionAxes(),
					aircraft.getWing().getYApexConstructionAxes(),
					aircraft.getWing().getZApexConstructionAxes()
					);

			_cg.setXLRFref(aircraft.getWing().getPanels().get(0).getChordRoot().to(SI.METER).times(0.4));
			_cg.setYLRFref(aircraft.getWing().getSpan().to(SI.METER).times(0.5*0.4));
			_cg.setZLRFref(Amount.valueOf(0., SI.METER));
			
			switch(method) {

			case SFORZA : { 
				_methodsList.add(method);
				_xCG = LiftingSurfaceBalanceCalc.calculateWingXCGSforza(aircraft);
				_yCG = LiftingSurfaceBalanceCalc.calculateWingYCGSforza(aircraft);
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			case TORENBEEK_1982 : { 
				_methodsList.add(method);
				_xCG = LiftingSurfaceBalanceCalc.calculateWingXCGTorenbeek(aircraft);
				_yCG = LiftingSurfaceBalanceCalc.calculateWingYCGTorenbeek(aircraft); 
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;

			}

		} break;

		case HORIZONTAL_TAIL : {

			_cg = new CenterOfGravity();
			_cg.setLRForigin(
					aircraft.getHTail().getXApexConstructionAxes(),
					aircraft.getHTail().getYApexConstructionAxes(),
					aircraft.getHTail().getZApexConstructionAxes()
					);

			_cg.setXLRFref(aircraft.getHTail().getPanels().get(0).getChordRoot().to(SI.METER).times(0.4));
			_cg.setYLRFref(aircraft.getHTail().getSpan().to(SI.METER).times(0.5*0.4));
			_cg.setZLRFref(Amount.valueOf(0., SI.METER));
			
			switch(method) {

			case TORENBEEK_1982 : { 
				_methodsList.add(method);
				_xCG = LiftingSurfaceBalanceCalc.calculateHTailXCGTorenbeek(aircraft);
				_yCG = LiftingSurfaceBalanceCalc.calculateHTailYCGTorenbeek(aircraft);
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case VERTICAL_TAIL : {

			_cg = new CenterOfGravity();
			_cg.setLRForigin(
					aircraft.getVTail().getXApexConstructionAxes(),
					aircraft.getVTail().getYApexConstructionAxes(),
					aircraft.getVTail().getZApexConstructionAxes()
					);

			_cg.setXLRFref(aircraft.getVTail().getPanels().get(0).getChordRoot().to(SI.METER).times(0.4));
			_cg.setYLRFref(aircraft.getVTail().getSpan().to(SI.METER).times(0.5*0.4));
			_cg.setZLRFref(Amount.valueOf(0., SI.METER));
			
			switch(method) {

			case TORENBEEK_1982 : { 
				_methodsList.add(method);
				_xCG = LiftingSurfaceBalanceCalc.calculateVTailXCGTorenbeek(aircraft); 
				_yCG = LiftingSurfaceBalanceCalc.calculateVTailYCGTorenbeek(aircraft);
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case CANARD : {
			
			_cg = new CenterOfGravity();
			_cg.setLRForigin(
					aircraft.getCanard().getXApexConstructionAxes(),
					aircraft.getCanard().getYApexConstructionAxes(),
					aircraft.getCanard().getZApexConstructionAxes()
					);

			_cg.setXLRFref(aircraft.getCanard().getPanels().get(0).getChordRoot().to(SI.METER).times(0.4));
			_cg.setYLRFref(aircraft.getCanard().getSpan().to(SI.METER).times(0.5*0.4));
			_cg.setZLRFref(Amount.valueOf(0., SI.METER));
			
			switch(method) {

			case TORENBEEK_1982 : { 
				_methodsList.add(method);
				_xCG = LiftingSurfaceBalanceCalc.calculateCanardXCGTorenbeek(aircraft);
				_yCG = LiftingSurfaceBalanceCalc.calculateCanardYCGTorenbeek(aircraft);
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		default : {} break;

		}

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
