package analyses.powerplant;

import java.util.ArrayList;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.Engine;
import analyses.analysismodel.analysiscalcmanager.BalanceManager;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class EngineBalanceManager extends BalanceManager{

	private Engine _theEngine; 
	
	private Amount<Length> _xCG;
	private Double[] _percentDifferenceXCG;
	
	
	public EngineBalanceManager(Engine engine) {
		super();
		
		_theEngine = engine;
		initializeDependentData();
		initializeInnerCalculators();
		initializeDependentData();
		calculateAll();
	}
	
	
	public void initializeDependentData() {
		_cg.setLRForigin(
				_theEngine.getXApexConstructionAxes(), 
				_theEngine.getYApexConstructionAxes(), 
				_theEngine.getZApexConstructionAxes());
		
		_cg.set_xLRFref(_theEngine.getLength().divide(2));
		_cg.set_yLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_zLRFref(Amount.valueOf(0., SI.METER));
	}
	
	@Override
	public void initializeInnerCalculators() {
		
	}
	
	@Override
	public void calculateAll() {

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		sforza();
		
		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];

		_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.getXLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF(ComponentEnum.ENGINE);

	}
	
	public void sforza() {
		_methodsList.add(MethodEnum.SFORZA);
		_xCG = _theEngine.getLength().divide(2.);
		_xCGMap.put(MethodEnum.SFORZA, _xCG);
	}
	
}
