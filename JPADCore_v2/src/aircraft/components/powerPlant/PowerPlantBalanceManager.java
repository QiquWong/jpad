package aircraft.components.powerPlant;

import java.util.ArrayList;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.componentmodel.componentcalcmanager.BalanceManager;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class PowerPlantBalanceManager extends BalanceManager{

	private PowerPlant _thePowerPlant; 
	
	private Amount<Length> _xCG;
	private Double[] _percentDifferenceXCG;
	
	
	public PowerPlantBalanceManager(PowerPlant powerPlant) {
		super();
		
		_thePowerPlant = powerPlant;
		initializeDependentData();
		initializeInnerCalculators();
		initializeDependentData();
		calculateAll();
	}
	
	
	public void initializeDependentData() {
		_cg.setLRForigin(
				_thePowerPlant.getXApexConstructionAxes(), 
				_thePowerPlant.getYApexConstructionAxes(), 
				_thePowerPlant.getZApexConstructionAxes());
		
		_cg.set_xLRFref(_thePowerPlant.get_engineList().get(0).getLength().divide(2));
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
		_xCG = _thePowerPlant.get_engineList().get(0).getLength().divide(2.);
		_xCGMap.put(MethodEnum.SFORZA, _xCG);
	}
	
}
