package analyses.powerplant;

import static java.lang.Math.round;

import java.util.Map;

import javax.measure.quantity.Force;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.Engine;
import analyses.analysismodel.InnerCalculator;
import analyses.analysismodel.analysiscalcmanager.WeightsManager;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.JPADStaticWriteUtils;

public class EngineWeightsManager extends WeightsManager{

	private Engine _theEngine;
	private EngineTypeEnum _engineType;

	private Amount<Mass> _totalMass,
	_massEstimated,
	_dryMassPublicDomain;
	private Amount<Force> _t0;

	public EngineWeightsManager(Engine engine) {
		_theEngine = engine;
		
		initializeDependentData();
		initializeInnerCalculators();
	}

	@Override
	public void initializeDependentData() {
		_t0 = _theEngine.getT0();
		_dryMassPublicDomain = _theEngine.getDryMassPublicDomain();
		_engineType = _theEngine.getEngineType();
	}
	
	@Override
	public void initializeInnerCalculators() {
		if (_engineType.equals(EngineTypeEnum.TURBOPROP))
			calculator = new Turboprop();
		else if(_engineType.equals(EngineTypeEnum.TURBOFAN))
			calculator = new Turbofan();
		else if(_engineType.equals(EngineTypeEnum.PISTON))
			calculator = new Piston();		
	}
	
	/** 
	 * Evaluate a single engine (dry) mass
	 * 
	 * @author Lorenzo Attanasio
	 * @param method
	 */
	@Override
	public void calculateAll() {
		calculator.allMethods();

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_dryMassPublicDomain, 
				_massMap,
				_percentDifference,
				20.).getFilteredMean(), SI.KILOGRAM);

		calculateTotalMass();

	}

	public class Turbofan extends InnerCalculator{

		//page 434 Stanford
		public Amount<Mass> harris() {
			if (_t0.to(NonSI.POUND_FORCE).getEstimatedValue() < 10000) {
				_massEstimated = Amount.valueOf(
						Math.pow(0.4054 * _t0.to(NonSI.POUND_FORCE).getEstimatedValue(),0.9255), 
						NonSI.POUND).to(SI.KILOGRAM);

			} else {
				_massEstimated = Amount.valueOf(
						Math.pow(0.616 * _t0.to(NonSI.POUND_FORCE).getEstimatedValue(),0.886), 
						NonSI.POUND).to(SI.KILOGRAM);
			}

			_methodsList.add(MethodEnum.HARRIS);
			_massMap.put(MethodEnum.HARRIS, Amount.valueOf(round(_massEstimated.getEstimatedValue()), SI.KILOGRAM));
			return _massEstimated;
		}

		@Override
		public void allMethods() {
			harris();			
		} 
	}


	public class Turboprop extends InnerCalculator{

		// page 434 Stanford		
		public Amount<Mass> harris() {
			if (_t0.to(NonSI.POUND_FORCE).getEstimatedValue() < 10000) {
				_massEstimated = Amount.valueOf(
						Math.pow(0.4054 * _t0.to(NonSI.POUND_FORCE).getEstimatedValue(),0.9255), 
						NonSI.POUND).to(SI.KILOGRAM);

			} else {
				_massEstimated = Amount.valueOf(
						Math.pow(0.616 * _t0.to(NonSI.POUND_FORCE).getEstimatedValue(),0.886), 
						NonSI.POUND).to(SI.KILOGRAM);
			}
			_methodsList.add(MethodEnum.HARRIS);
			_massMap.put(MethodEnum.HARRIS, Amount.valueOf(round(_massEstimated.getEstimatedValue()), SI.KILOGRAM));
			return _massEstimated;
		}

		@Override
		public void allMethods() {
			harris();			
		}
	}


	public class Piston extends InnerCalculator {

		@Override
		public void allMethods() {
			// TODO Auto-generated method stub
		}

	}

	/**
	 * Calculate engine mass only if public domain data
	 * about engine is not available
	 */
	public void calculateTotalMass() {

		if (_dryMassPublicDomain != null) {
			_massEstimated = Amount.valueOf(_dryMassPublicDomain.getEstimatedValue(), SI.KILOGRAM);
		}

		// TORENBEEK_1982 method gives better results for 50000 < MTOM < 200000
		if (_engineType.equals(EngineTypeEnum.TURBOPROP) | ( 
				_theAircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().getEstimatedValue() < 50000 |
				_theAircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().getEstimatedValue() > 200000)) {
			calculateTotalMass(MethodEnum.TORENBEEK_1982);

		} else {
			calculateTotalMass(MethodEnum.TORENBEEK_2013);
		}

	}

	/**
	 * Calculate total engine mass
	 * 
	 * @author Lorenzo Attanasio
	 * @param method
	 * @return 
	 */
	public Amount<Mass> calculateTotalMass(MethodEnum method) {

		switch(method){

		case TORENBEEK_1982 : {
			_totalMass = _massEstimated.times(1.377);
		} break;

		case TORENBEEK_2013 : {
			_totalMass = Amount.valueOf((
					_t0.times(0.25).getEstimatedValue() + 
					8000)/AtmosphereCalc.g0.getEstimatedValue(), SI.KILOGRAM);
		} break;

		default : break;
		}

		return _totalMass;
	}

	public Amount<Mass> getDryMassPublicDomain() {
		return _dryMassPublicDomain;
	}

	public void setDryMassPublicDomain(Amount<Mass> _dryMassPublicDomain) {
		this._dryMassPublicDomain = _dryMassPublicDomain;
	}

	public Amount<Mass> getTotalMass() {
		return _totalMass;
	}

	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	public Amount<Mass> getMass() {
		return _mass;
	}

	public void setMass(Amount<Mass> _mass) {
		this._mass = _mass;
	}
	
	public Double[] getPercentDifference() {
		return _percentDifference;
	}
	
	public void setPercentDifference(Double[] _percentDifference) {
		this._percentDifference =_percentDifference;
	}

}
