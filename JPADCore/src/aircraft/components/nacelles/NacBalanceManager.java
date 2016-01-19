package aircraft.components.nacelles;

import java.util.ArrayList;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class NacBalanceManager extends aircraft.componentmodel.componentcalcmanager.BalanceManager{

	private Nacelle _theNacelle;
	
	private Amount<Length> _xCG;
	private Double[] _percentDifferenceXCG;

	public NacBalanceManager(Nacelle nacelle) {
		super();
		_theNacelle = nacelle;
	}
	
	public void initializeDependentData() {
		_cg.setLRForigin(
				_theNacelle.get_X0(), 
				_theNacelle.get_Y0(), 
				_theNacelle.get_Z0());
		
		_cg.set_xLRFref(_theNacelle.get_length().times(0.4));
		_cg.set_yLRFref(_theNacelle.get_diameterMean().divide(2));
		_cg.set_zLRFref(_theNacelle.get_diameterMean().divide(2));
	}
	
	@Override
	public void initializeInnerCalculators() {
		
	}

	@Override
	public void calculateAll() {

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		torenbeek();
		
		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));
		
		_cg.calculateCGinBRF();

	}
	
	public void torenbeek() {
		_methodsList.add(MethodEnum.TORENBEEK_1982);
		_xCG = _theNacelle.get_length().times(0.4);
		_xCGMap.put(MethodEnum.TORENBEEK_1982, _xCG);	
	}

}
