package aircraft.components.nacelles;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class NacelleAerodynamicsManager extends aircraft.componentmodel.componentcalcmanager.AerodynamicsManager{

	private Amount<Length> _length;
	private Amount<Length> _roughness;
	private Double _reynolds;
	private Double _xTransition;
	private Double _cF;

	private Double _cd0Parasite;
	private Double _cd0Base;
	private Double _cd0Total;
	private Aircraft _theAircraft;
	private Nacelle _theNacelle;
	private Amount<Area> sW;

	public NacelleAerodynamicsManager(
			Aircraft aircraft, 
			Nacelle nacelle) {

		_theAircraft = aircraft;
		_theNacelle = nacelle;
		_length = _theNacelle.get_length();
		_roughness = _theNacelle.get_roughness();

		initializeDependentData();
	}
	
	@Override
	public void initializeDependentData() {
		//TODO: check this
		_mach = 0.45;
		_altitude = 6000.;
		sW = _theAircraft.getWing().get_surface();
	}

	@Override
	public void initializeInnerCalculators() {
		
	}
	
	@Override
	public void calculateAll() {

		JPADStaticWriteUtils.logToConsole("NEED TO CHANGE MACH AND ALTITUDE FOR NACELLE");
		_reynolds = AerodynamicCalc.calculateReynoldsEffective(_mach, 0.3, _altitude, 
				_length.doubleValue(SI.METER), _roughness.doubleValue(SI.METER));
		_xTransition = 0.0;
		_cF = AerodynamicCalc.calculateCf(_reynolds, _mach, _xTransition);

		double kExcr = DragCalc.calculateKExcrescences(_theAircraft.getSWetTotal()); 

		calculateCd0Parasite();
		calculateCd0Base();
		calculateCd0Total(kExcr);

	}

	public double calculateCd0Parasite() {

		_cd0Parasite = DragCalc.calculateCd0Parasite(
				_theNacelle.formFactor(), 
				_cF, 
				_theNacelle.get_surfaceWetted().getEstimatedValue(), 
				sW.getEstimatedValue());

		return _cd0Parasite;
	}

	public double calculateCd0Base() {
		_cd0Base = DragCalc.calculateCd0Base(
				MethodEnum.MATLAB, 
				_cd0Parasite, sW.getEstimatedValue(),
				_theNacelle.get_diameterOutlet().getEstimatedValue(), 
				_theNacelle.get_diameterMean().getEstimatedValue());

		return _cd0Base;
	}

	public double calculateCd0Total(double kExcr) {
		_cd0Total = ((1 + kExcr)*_cd0Parasite + _cd0Base); 
		return _cd0Total;
	}


	public Amount<Length> get_length() {
		return _length;
	}

	public Amount<Length> get_roughness() {
		return _roughness;
	}

	public Double get_reynolds() {
		return _reynolds;
	}

	public Double get_xTransition() {
		return _xTransition;
	}

	public Double get_cF() {
		return _cF;
	}

	public Double get_cd0Parasite() {
		return _cd0Parasite;
	}

	public Double get_cd0Base() {
		return _cd0Base;
	}

	public Double get_cd0Total() {
		return _cd0Total;
	}

	public void set_theNacelle(Nacelle _theNacelle) {
		this._theNacelle = _theNacelle;
	}

}
