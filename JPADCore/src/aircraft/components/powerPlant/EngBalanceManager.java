package aircraft.components.powerPlant;

import java.util.ArrayList;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class EngBalanceManager extends aircraft.componentmodel.componentcalcmanager.BalanceManager{

	private Engine _theEngine; 
	
	private Amount<Length> _xCG;
	private Double[] _percentDifferenceXCG;
	
	
	public EngBalanceManager(Engine engine) {
		super();
		
		_theEngine = engine;
		initializeDependentData();
		initializeInnerCalculators();
	}
	
	
	public void initializeDependentData() {
		_cg.setLRForigin(
				_theEngine.get_X0(), 
				_theEngine.get_Y0(), 
				_theEngine.get_Z0());
		
		_cg.set_xLRFref(_theEngine.get_length().divide(2));
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

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

	}
	
	public void sforza() {
		_methodsList.add(MethodEnum.SFORZA);
		_xCG = _theEngine.get_length().divide(2.);
		_xCGMap.put(MethodEnum.SFORZA, _xCG);
	}
	
}
