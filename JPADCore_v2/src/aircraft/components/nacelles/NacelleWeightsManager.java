package aircraft.components.nacelles;

import static java.lang.Math.round;

import java.util.Map;

import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.componentmodel.InnerCalculator;
import aircraft.components.Aircraft;
import aircraft.components.nacelles.Nacelle.MountingPosition;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.JPADStaticWriteUtils;

public class NacelleWeightsManager extends aircraft.componentmodel.componentcalcmanager.WeightsManager{

	private Aircraft _theAircraft;
	private Nacelle _theNacelle;
	private MountingPosition _mountingPosition;
	private InnerCalculator calculator;
	private Turboprop turboprop;
	private Turbofan turbofan;
	private Piston piston;

	
	public NacelleWeightsManager(Aircraft aircraft, Nacelle nacelle) {
		_theAircraft = aircraft;
		_theNacelle = nacelle;

		initializeDependentData();
		initializeInnerCalculators();
	}


	@Override
	public void initializeDependentData() {
		_mass = Amount.valueOf(0., SI.KILOGRAM);
		_massEstimated = Amount.valueOf(0., SI.KILOGRAM);
//		_massReference = Amount.valueOf(0., SI.KILOGRAM);
		_massReference = _theAircraft.getNacelles().get_massReference();
	}
	
	
	@Override
	public void initializeInnerCalculators() {
		if (_theNacelle.get_theEngine().get_engineType().equals(EngineTypeEnum.TURBOPROP))
			calculator = new Turboprop();
		else if(_theNacelle.get_theEngine().get_engineType().equals(EngineTypeEnum.TURBOFAN))
			calculator = new Turbofan();
		else if(_theNacelle.get_theEngine().get_engineType().equals(EngineTypeEnum.PISTON))
			calculator = new Piston();	
	}
	
	@Override
	public void calculateAll() {
		
		calculator.allMethods();
		
		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				100.).getFilteredMean(), SI.KILOGRAM);
		
	}

	/** 
	 * Evaluate turbofan nacelle mass
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class Turbofan extends InnerCalculator{

		public Turbofan() { }

		// page 150 Jenkinson - Civil Jet Aircraft Design
		public Amount<Mass> jenkinson() {

			if (_theNacelle.get_theEngine().get_t0().getEstimatedValue() < 600000.){
				_mass = Amount.valueOf(
						_theNacelle.get_theEngine().get_t0().divide(1000.).times(6.8).getEstimatedValue(), 
						SI.KILOGRAM);
			
			} else {
				_mass = Amount.valueOf(2760 + 
						(2.2*_theNacelle.get_theEngine().get_t0().divide(1000.).getEstimatedValue()), 
						SI.KILOGRAM);
			}
			
			_methodsList.add(MethodEnum.JENKINSON);
			_massMap.put(MethodEnum.JENKINSON, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		}

		/*
		 
		public Amount<Mass> kundu(){ // page 287 Kundu - Aircraft Design. Mass in lb???
			
			if(_theNacelle.get_theEngine().get_bpr()>4.) {
				_mass = _theAircraft.get_powerPlant().
						get_T0Total().divide(AtmosphereCalc.g0).to(SI.KILOGRAM).times(6.7);
			} else {
				_mass = _theAircraft.get_powerPlant().
						get_T0Total().divide(AtmosphereCalc.g0).to(SI.KILOGRAM).times(6.2);
			}
			
			_methodsList.add(MethodEnum.KUNDU);
			_massMap.put(MethodEnum.KUNDU, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		} 
		*/
		
		/*
		 *  It includes The engine structural section, or nacelle group, and the propulsion group which includes the engines, engine exhaust,
			reverser, starting, controls, lubricating, and fuel systems are handled together as the total propulsion
			weight. Therefore is NOT applicable!
		 
		public Amount<Mass> kroo() { // page 434 Stanford
			_methodsList.add(MethodEnum.KUNDU);
			_mass = _theAircraft.get_powerPlant().get_dryMassPublicDomain().times(1.6);
			_massMap.put(MethodEnum.KROO, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		};
		*/

		public Amount<Mass> torenbeek1976 () {
			_mass = Amount.valueOf(0.405*
					Math.sqrt(_theAircraft.getThePerformance().getVDiveEAS().getEstimatedValue())*
					Math.pow(_theNacelle.get_surfaceWetted().getEstimatedValue(), 1.3), SI.KILOGRAM);
			_methodsList.add(MethodEnum.TORENBEEK_1976);
			_massMap.put(MethodEnum.TORENBEEK_1976, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		}

		public Amount<Mass> torenbeek1982() {

			_mass = Amount.valueOf(
					0.055*_theNacelle.get_theEngine().get_t0().to(NonSI.POUND_FORCE).getEstimatedValue(),
					NonSI.POUND).
					to(SI.KILOGRAM);
			_methodsList.add(MethodEnum.TORENBEEK_1982);
			_massMap.put(MethodEnum.TORENBEEK_1982, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		}

		@Override
		public void allMethods() {
			jenkinson();
			torenbeek1976();
			torenbeek1982();
		}
	}

	/** 
	 * Evaluate turboprop nacelle mass
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class Turboprop extends InnerCalculator{

		public Turboprop() { }

		public Amount<Mass> jenkinson() { // page 150 Jenkinson - Civil Jet _theAircraft Design
			if (_theNacelle.get_theEngine().get_t0().getEstimatedValue() < 600000.){
				_mass = Amount.valueOf(
						_theNacelle.get_theEngine().get_t0().divide(1000).times(6.8).getEstimatedValue(), 
						SI.KILOGRAM);
			
			} else {
				_mass = Amount.valueOf(2760 + 
						(2.2*_theNacelle.get_theEngine().get_t0().divide(1000).getEstimatedValue()), 
						SI.KILOGRAM);
			}
			_methodsList.add(MethodEnum.JENKINSON);
			_massMap.put(MethodEnum.JENKINSON, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		}

		//TODO: TO CHECK!!! UNITS OF MASS????
		/*
	case KUNDU : { // page 287 Kundu - _theAircraft Design. Mass in lb???
		_methodsList.add(method);
		if (_mountingPosition == MountingPosition.WING) {
			_mass = Amount.valueOf(
					_theAircraft.get_powerPlant().
					get_P0().to(NonSI.HORSEPOWER).times(6.5).getEstimatedValue(),
					NonSI.POUND_FORCE).to(SI.NEWTON).divide(MyOperatingConditions.g0).to(SI.KILOGRAM);

		} else if (_mountingPosition == MountingPosition.UNDERCARRIAGE_HOUSING) {
			_mass = Amount.valueOf(
					_theAircraft.get_powerPlant().
					get_P0().to(NonSI.HORSEPOWER).times(8.).getEstimatedValue(),
					NonSI.POUND_FORCE).to(SI.NEWTON).divide(MyOperatingConditions.g0).to(SI.KILOGRAM);
		} else {
			_mass = Amount.valueOf(
					_theAircraft.get_powerPlant().
					get_P0().to(NonSI.HORSEPOWER).times(28.).getEstimatedValue(),
					NonSI.POUND_FORCE).to(SI.NEWTON).divide(MyOperatingConditions.g0).to(SI.KILOGRAM);
		}
		_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
	} break;
		 */
		//TODO: TO CHECK!!! Mass is overestimated
		//			case KROO : {
		//				_methodsList.add(method);
		//				_mass = _theAircraft.get_powerPlant().get_massDryEngineReference().times(0.6);
		//				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		//			} break;
		//			

		public Amount<Mass> torenbeek1976() {
			_mass = Amount.valueOf(0.405*
					Math.sqrt(_theAircraft.getThePerformance().getVDiveEAS().getEstimatedValue())*
					Math.pow(_theNacelle.get_surfaceWetted().getEstimatedValue()*2, 1.3), SI.KILOGRAM);
			_methodsList.add(MethodEnum.TORENBEEK_1976);
			_massMap.put(MethodEnum.TORENBEEK_1976, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		}

		public Amount<Mass> torenbeek1982() {
			_mass = Amount.valueOf(
					0.055*_theNacelle.get_theEngine().get_t0().to(NonSI.POUND_FORCE).getEstimatedValue(),
					NonSI.POUND).
					to(SI.KILOGRAM);
			_methodsList.add(MethodEnum.TORENBEEK_1982);
			_massMap.put(MethodEnum.TORENBEEK_1982, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		} 

		@Override
		public void allMethods() {
			jenkinson();
			torenbeek1976();
			torenbeek1982();
		}
	} 

	public class Piston extends InnerCalculator{

		public Amount<Mass> kundu() {
			if (_mountingPosition == MountingPosition.WING) {
				_mass = Amount.valueOf(
						_theNacelle.get_theEngine().get_p0()
						.to(NonSI.HORSEPOWER).times(0.4).getEstimatedValue(),
						NonSI.POUND_FORCE).to(SI.NEWTON).divide(AtmosphereCalc.g0).to(SI.KILOGRAM);
			} else if (_mountingPosition == MountingPosition.FUSELAGE) {
				_mass = Amount.valueOf(
						_theNacelle.get_theEngine().get_p0().to(NonSI.HORSEPOWER).times(0.5).getEstimatedValue(),
						NonSI.POUND_FORCE).to(SI.NEWTON).divide(AtmosphereCalc.g0).to(SI.KILOGRAM);
			}
			_methodsList.add(MethodEnum.KUNDU);
			_massMap.put(MethodEnum.KUNDU, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			return _mass;
		}

		@Override
		public void allMethods() {
			kundu();
		}
	}

	public void set_theNacelle(Nacelle _theNacelle) {
		this._theNacelle = _theNacelle;
	}

	public Map<MethodEnum, Amount<Mass>> get_massMap() {
		return _massMap;
	}

	public Amount<Mass> get_massEstimated() {
		return _massEstimated;
	}

	public Turboprop getTurboprop() {
		return turboprop;
	}

	public Turbofan getTurbofan() {
		return turbofan;
	}

	public Piston getPiston() {
		return piston;
	}

	public Amount<Mass> get_massReference() {
		return _massReference;
	}

	public void set_massReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	public Amount<Mass> get_mass() {
		return _mass;
	}

	public void set_mass(Amount<Mass> _mass) {
		this._mass = _mass;
	}
	
	public Double[] get_percentDifference() {
		return _percentDifference;
	}
	
	public void set_percentDifference(Double[] _percentDifference) {
		this._percentDifference =_percentDifference;
	}
	


}
