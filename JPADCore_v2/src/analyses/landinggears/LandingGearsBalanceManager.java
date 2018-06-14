package analyses.landinggears;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.balance.LandingGearsBalanceCalc;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class LandingGearsBalanceManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Map <MethodEnum, Amount<Length>> _xCGMap;
	private Map <MethodEnum, Amount<Length>> _zCGMap;
	private List<MethodEnum> _methodsList;
	private Amount<Length> _xCG, _zCG;
	private CenterOfGravity _cg;
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public LandingGearsBalanceManager () {
		
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		this._xCGMap = new HashMap<>();
		this._zCGMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		
	}
	
	public void calculateCG(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMap) {
		
		calculateCG(aircraft, MethodEnum.SFORZA);
		
		if(!methodsMap.get(ComponentEnum.LANDING_GEAR).equals(MethodEnum.AVERAGE)) { 
			_cg.setXLRF(_xCGMap.get(methodsMap.get(ComponentEnum.LANDING_GEAR)));
			_cg.setZLRF(_zCGMap.get(methodsMap.get(ComponentEnum.LANDING_GEAR)));
		}
		else {
			_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getXLRFref(), 
					_xCGMap,
					new double[_xCGMap.size()],
					100.).getFilteredMean(), SI.METER));
			_cg.setZLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getZLRFref(), 
					_zCGMap,
					new double[_zCGMap.size()],
					100.).getFilteredMean(), SI.METER));
		}
		_cg.calculateCGinBRF(ComponentEnum.LANDING_GEAR);
	}
	
	public void calculateCG(
			Aircraft aircraft, 
			MethodEnum method) {

		_cg = new CenterOfGravity();
		
		_cg.setLRForigin(
				aircraft.getFuselage().getXApexConstructionAxes(), 
				aircraft.getFuselage().getYApexConstructionAxes(), 
				aircraft.getLandingGears().getZApexConstructionAxesMainGear()
				);
		_cg.setXLRFref(LandingGearsBalanceCalc.calculateXCGLandingGears(aircraft));
		_cg.setYLRFref(Amount.valueOf(0., SI.METER));
		_cg.setZLRFref(LandingGearsBalanceCalc.calculateZCGLandingGears(aircraft));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		switch(method) {

		case SFORZA : { 
			_methodsList.add(method);
			_xCG = LandingGearsBalanceCalc.calculateXCGLandingGears(aircraft);
			_xCGMap.put(method, _xCG);
			_zCG = LandingGearsBalanceCalc.calculateZCGLandingGears(aircraft);
			_zCGMap.put(method, _zCG);
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

	public CenterOfGravity getCG() {
		return _cg;
	}

	public void setCG(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	public Amount<Length> getZCG() {
		return _zCG;
	}

	public void setZCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	public Map <MethodEnum, Amount<Length>> getZCGMap() {
		return _zCGMap;
	}

	public void setZCGMap(Map <MethodEnum, Amount<Length>> _zCGMap) {
		this._zCGMap = _zCGMap;
	}

}
