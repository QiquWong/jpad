package analyses.powerplant;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import calculators.balance.EngineBalanceCalc;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class EngineBalanceManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private List<CenterOfGravity> _cgList;
	private List<Amount<Length>> _xCGEstimatedList;
	private Map <MethodEnum, Amount<Length>> _xCGMap;
	private List<MethodEnum> _methodsList;  
	private double[] _percentDifference;
	private CenterOfGravity _totalCG;
	private Amount<Length> _totalXCGEstimated;
	private Map <MethodEnum, Amount<Length>> _totalXCGMap;
	private double[] _totalPercentDifference;
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public EngineBalanceManager () {
		
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		
		_cgList = new ArrayList<>();
		_xCGEstimatedList = new ArrayList<>();
		_xCGMap = new HashMap<>();
		_methodsList = new ArrayList<>();
		
	}
	
	public void calculateTotalCG(Aircraft theAircraft, Map<ComponentEnum, MethodEnum> methodsMapBalance) {

		_totalCG = new CenterOfGravity();
		_totalCG.setLRForigin(
				theAircraft.getPowerPlant().getEngineList().get(0).getXApexConstructionAxes(), 
				theAircraft.getPowerPlant().getEngineList().get(0).getYApexConstructionAxes(), 
				theAircraft.getPowerPlant().getEngineList().get(0).getZApexConstructionAxes()
				);
		_totalCG.setXLRFref(theAircraft.getPowerPlant().getEngineList().get(0).getLength().times(0.5));
		_totalCG.setYLRFref(theAircraft.getNacelles().getNacellesList().get(0).getDiameterMax().divide(2));
		_totalCG.setZLRFref(theAircraft.getNacelles().getNacellesList().get(0).getDiameterMax().divide(2));
		
		theAircraft.getPowerPlant().getEngineList().stream().forEach(eng -> {
			
			calculateCG(theAircraft, methodsMapBalance);
			
		});
		
		if(!methodsMapBalance.get(ComponentEnum.POWER_PLANT).equals(MethodEnum.AVERAGE)) {
			_totalPercentDifference =  new double[_totalXCGMap.size()];
			_totalXCGEstimated = _totalXCGMap.get(methodsMapBalance.get(ComponentEnum.POWER_PLANT)).to(SI.METER);
		}
		else {
			_totalPercentDifference =  new double[_totalXCGMap.size()];
			_totalXCGEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_totalCG.getXLRF(),
					_totalXCGMap,
					_totalPercentDifference,
					100.).getMean(), SI.METER);
		}
		
		_totalCG.setXLRF(_totalXCGEstimated);
		_totalCG.calculateCGinBRF(ComponentEnum.POWER_PLANT);
	}
	
	private void calculateCG (Aircraft theAircraft, Map<ComponentEnum, MethodEnum> methodsMapBalance) {
		
		calculateCG(theAircraft, MethodEnum.TORENBEEK_1976);
		
		if(!methodsMapBalance.get(ComponentEnum.POWER_PLANT).equals(MethodEnum.AVERAGE)) {
			_percentDifference = new double[_xCGMap.size()];
			_xCGEstimatedList.add(_xCGMap.get(methodsMapBalance.get(ComponentEnum.POWER_PLANT)));
		}
		else {
			_percentDifference = new double[_xCGMap.size()];
			_xCGEstimatedList.add(
					Amount.valueOf(JPADStaticWriteUtils.compareMethods(
							_cgList.get(0).getXLRFref(), 
							_xCGMap,
							_percentDifference,
							100.).getFilteredMean(), SI.METER)
					);
		}
		
	}
	
	private void calculateCG (Aircraft theAircraft, MethodEnum method) {
		
		CenterOfGravity cg = new CenterOfGravity();
		cg.setLRForigin(
				theAircraft.getPowerPlant().getEngineList().get(0).getXApexConstructionAxes(), 
				theAircraft.getPowerPlant().getEngineList().get(0).getYApexConstructionAxes(), 
				theAircraft.getPowerPlant().getEngineList().get(0).getZApexConstructionAxes()
				);
		cg.setXLRFref(theAircraft.getPowerPlant().getEngineList().get(0).getLength().times(0.5));
		cg.setYLRFref(theAircraft.getNacelles().getNacellesList().get(0).getDiameterMax().divide(2));
		cg.setZLRFref(theAircraft.getNacelles().getNacellesList().get(0).getDiameterMax().divide(2));
		
		switch (method) {
		case SFORZA:
			_methodsList.add(method);
			cg.setXLRF(EngineBalanceCalc.calculateEngineXCGSforza(theAircraft));
			_xCGMap.put(method, Amount.valueOf(round(cg.getXLRF().doubleValue(SI.METER)), SI.METER));
			break;
		default:
			break;
		}

		cg.calculateCGinBRF(ComponentEnum.POWER_PLANT);
		_cgList.add(cg);
		
	}
	
	//------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	//------------------------------------------------------------------------------
	
	public List<CenterOfGravity> getCGList() {
		return _cgList;
	}

	public void setCGList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}

	public List<Amount<Length>> getXCGEstimatedList() {
		return _xCGEstimatedList;
	}

	public void setXCGEstimatedList(List<Amount<Length>> _xCGEstimatedList) {
		this._xCGEstimatedList = _xCGEstimatedList;
	}

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

	public double[] getPercentDifference() {
		return _percentDifference;
	}

	public void setPercentDifference(double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	public CenterOfGravity getTotalCG() {
		return _totalCG;
	}

	public void setTotalCG(CenterOfGravity _totalCG) {
		this._totalCG = _totalCG;
	}

	public Amount<Length> getTotalXCGEstimated() {
		return _totalXCGEstimated;
	}

	public void setTotalXCGEstimated(Amount<Length> _totalXCGEstimated) {
		this._totalXCGEstimated = _totalXCGEstimated;
	}

	public Map<MethodEnum, Amount<Length>> getTotalXCGMap() {
		return _totalXCGMap;
	}

	public void setTotalXCGMap(Map<MethodEnum, Amount<Length>> _totalXCGMap) {
		this._totalXCGMap = _totalXCGMap;
	}

	public double[] getTotalPercentDifference() {
		return _totalPercentDifference;
	}

	public void setTotalPercentDifference(double[] _totalPercentDifference) {
		this._totalPercentDifference = _totalPercentDifference;
	}

}
