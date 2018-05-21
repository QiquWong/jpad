package analyses.fuselage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.balance.FuselageBalanceCalc;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class FuselageBalanceManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Map <MethodEnum, Amount<Length>> _xCGMap;
	private List<MethodEnum> _methodsList;
	private Amount<Length> _xCG, _xCGReference, _xCGEstimated, _zCGEstimated;
	private CenterOfGravity _cg;
	private double[] _percentDifferenceXCG;
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public FuselageBalanceManager () {
		
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._xCGMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		
	}
	
	public void calculateCG(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMap) {
		calculateCG(aircraft, MethodEnum.SFORZA);
		calculateCG(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMap.get(ComponentEnum.FUSELAGE).equals(MethodEnum.AVERAGE)) 
			_cg.setXLRF(_xCGMap.get(methodsMap.get(ComponentEnum.FUSELAGE)));
		else {
			_percentDifferenceXCG = new double[_xCGMap.size()];
			_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getXLRFref(), 
					_xCGMap,
					_percentDifferenceXCG,
					30.).getFilteredMean(), SI.METER));
		}
		_cg.calculateCGinBRF(ComponentEnum.FUSELAGE);
	}
	
	public void calculateCG(
			Aircraft aircraft, 
			MethodEnum method) {

		_cg = new CenterOfGravity();
		
		_cg.setLRForigin(
				aircraft.getFuselage().getXApexConstructionAxes(), 
				aircraft.getFuselage().getYApexConstructionAxes(), 
				aircraft.getFuselage().getZApexConstructionAxes()
				);
		_cg.setXLRFref(aircraft.getFuselage().getFuselageLength().times(0.45));
		_cg.setYLRFref(Amount.valueOf(0., SI.METER));
		_cg.setZLRFref(aircraft.getFuselage().getZApexConstructionAxes().to(SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		switch(method) {

		case SFORZA : { 
			_methodsList.add(method);
			_xCG = FuselageBalanceCalc.calculateFuselageXCGSforza(aircraft);
			_xCGMap.put(method, _xCG);
		} break;

		// page 313 Torenbeek (1982)
		case TORENBEEK_1982 : { 
			_methodsList.add(method);
			_xCG = FuselageBalanceCalc.calculateFuselageXCGTorenbeek(aircraft);
			_xCGMap.put(method, _xCG);
		} break;

		default : break;

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

	public Amount<Length> getXCGReference() {
		return _xCGReference;
	}

	public void setXCGReference(Amount<Length> _xCGReference) {
		this._xCGReference = _xCGReference;
	}

	public Amount<Length> getXCGEstimated() {
		return _xCGEstimated;
	}

	public void setXCGEstimated(Amount<Length> _xCGEstimated) {
		this._xCGEstimated = _xCGEstimated;
	}

	public Amount<Length> getZCGEstimated() {
		return _zCGEstimated;
	}

	public void setZCGEstimated(Amount<Length> _zCGEstimated) {
		this._zCGEstimated = _zCGEstimated;
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
	
}
