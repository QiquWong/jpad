package analyses.nacelles;

import java.util.ArrayList;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.nacelles.NacelleCreator;
import analyses.analysismodel.analysiscalcmanager.BalanceManager;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class NacelleBalanceManager extends BalanceManager{

	private NacelleCreator _theNacelle;
	
	private Amount<Length> _xCG;
	private Double[] _percentDifferenceXCG;

	public NacelleBalanceManager(NacelleCreator nacelle) {
		super();
		_theNacelle = nacelle;
		initializeDependentData();
		calculateAll();
	}
	
	public void initializeDependentData() {
		_cg.setLRForigin(
				_theNacelle.getXApexConstructionAxes(), 
				_theNacelle.getYApexConstructionAxes(), 
				_theNacelle.getZApexConstructionAxes());
		
		_cg.set_xLRFref(_theNacelle.getLength().times(0.4));
		_cg.set_yLRFref(_theNacelle.getDiameterMax().divide(2));
		_cg.set_zLRFref(_theNacelle.getDiameterMax().divide(2));
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

		_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.getXLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));
		
		_cg.calculateCGinBRF(ComponentEnum.NACELLE);

	}
	
	public void torenbeek() {
		_methodsList.add(MethodEnum.TORENBEEK_1982);
		_xCG = _theNacelle.getLength().times(0.4);
		_xCGMap.put(MethodEnum.TORENBEEK_1982, _xCG);	
	}

}
