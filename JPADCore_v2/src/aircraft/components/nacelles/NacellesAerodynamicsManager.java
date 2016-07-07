package aircraft.components.nacelles;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.componentmodel.componentcalcmanager.AerodynamicsManager;
import aircraft.components.Aircraft;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import configuration.enumerations.MethodEnum;
import writers.JPADStaticWriteUtils;

public class NacellesAerodynamicsManager extends AerodynamicsManager{

	private Amount<Length> _length;
	private Amount<Length> _roughness;
	private Double _reynolds;
	private Double _xTransition;
	private Double _cF;

	private Double _cd0Parasite;
	private Double _cd0Base;
	private Double _cd0Total;
	private Aircraft _theAircraft;
	private NacelleCreator _theNacelle;
	private Amount<Area> _wingSurface;

	private OperatingConditions _theOperatingConditions;
	
	public NacellesAerodynamicsManager(
			Aircraft aircraft, 
			NacelleCreator nacelle,
			OperatingConditions operationConditions) {

		_theAircraft = aircraft;
		_theOperatingConditions = operationConditions;
		_theNacelle = nacelle;
		_length = _theNacelle.getLength();
		_roughness = _theNacelle.getRoughness();

		initializeDependentData();
	}
	
	@Override
	public void initializeDependentData() {
		_mach = _theOperatingConditions.get_machCurrent();
		_altitude = _theOperatingConditions.get_altitude().doubleValue(SI.METER);
		_wingSurface = _theAircraft.getWing().getSurface();
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

		double kExcr = DragCalc.calculateKExcrescences(_theAircraft.getSWetTotal().doubleValue(SI.SQUARE_METRE)); 

		calculateCd0Parasite();
		calculateCd0Base();
		calculateCd0Total(kExcr);

	}

	public double calculateCd0Parasite() {

		_cd0Parasite = DragCalc.calculateCd0Parasite(
				_theNacelle.calculateFormFactor(), 
				_cF, 
				_theNacelle.getSurfaceWetted().getEstimatedValue(), 
				_wingSurface.getEstimatedValue());

		return _cd0Parasite;
	}

	public double calculateCd0Base() {
		_cd0Base = DragCalc.calculateCd0Base(
				MethodEnum.MATLAB, 
				_cd0Parasite, _wingSurface.getEstimatedValue(),
				_theNacelle.getDiameterOutlet().getEstimatedValue(), 
				_theNacelle.getDiameterMean().getEstimatedValue());

		return _cd0Base;
	}

	public double calculateCd0Total(double kExcr) {
		_cd0Total = ((1 + kExcr)*_cd0Parasite + _cd0Base); 
		return _cd0Total;
	}


	public Amount<Length> getLength() {
		return _length;
	}

	public Amount<Length> getRoughness() {
		return _roughness;
	}

	public Double getReynolds() {
		return _reynolds;
	}

	public Double getXTransition() {
		return _xTransition;
	}

	public Double getCF() {
		return _cF;
	}

	public Double getCd0Parasite() {
		return _cd0Parasite;
	}

	public Double getCd0Base() {
		return _cd0Base;
	}

	public Double getCd0Total() {
		return _cd0Total;
	}

	public void setTheNacelle(NacelleCreator _theNacelle) {
		this._theNacelle = _theNacelle;
	}

}
