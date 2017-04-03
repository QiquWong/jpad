package analyses.nacelles;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.nacelles.NacelleCreator;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;

public class NacelleAerodynamicsManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (From superclass and calculated)
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private ConditionEnum _theCondition;
	private NacelleCreator _theNacelle;
	private Double _reynolds;
	private Double _xTransition;
	private Double _cF;
	private Double _mach;
	private Amount<Length> _altitude;

	// OUTPUT DATA
	private Double _cD0Parasite;
	private Double _cD0Base;
	private Double _cD0Total;

	//------------------------------------------------------------------------------
	// BUILDER
	//------------------------------------------------------------------------------
	public NacelleAerodynamicsManager(
			Aircraft aircraft, 
			NacelleCreator nacelle,
			OperatingConditions operationConditions,
			ConditionEnum theCondition) {

		_theAircraft = aircraft;
		_theOperatingConditions = operationConditions;
		_theNacelle = nacelle;
		_theCondition = theCondition;

		initializeDependentData();
	}
	
	//------------------------------------------------------------------------------
	// METHODS
	//------------------------------------------------------------------------------
	public void initializeDependentData() {
		
		switch (_theCondition) {
		case TAKE_OFF:
			_mach = _theOperatingConditions.getMachTakeOff();
			_altitude = _theOperatingConditions.getAltitudeTakeOff().to(SI.METER);
			break;
		case CLIMB:
			_mach = _theOperatingConditions.getMachClimb();
			_altitude = _theOperatingConditions.getAltitudeClimb().to(SI.METER);
			break;
		case CRUISE:
			_mach = _theOperatingConditions.getMachCruise();
			_altitude = _theOperatingConditions.getAltitudeCruise().to(SI.METER);
			break;
		case LANDING:
			_mach = _theOperatingConditions.getMachLanding();
			_altitude = _theOperatingConditions.getAltitudeLanding().to(SI.METER);
			break;
		}
	}

	public void calculateAll() {

		_reynolds = AerodynamicCalc.calculateReynoldsEffective(
				_mach,
				0.3,
				_altitude.doubleValue(SI.METER), 
				_theNacelle.getLength().doubleValue(SI.METER),
				_theNacelle.getRoughness().doubleValue(SI.METER)
				);
		_xTransition = 0.0;
		_cF = AerodynamicCalc.calculateCf(_reynolds, _mach, _xTransition);

		double kExcr = DragCalc.calculateKExcrescences(_theAircraft.getSWetTotal().doubleValue(SI.SQUARE_METRE)); 

		calculateCD0Parasite();
		calculateCD0Base();
		calculateCD0Total(kExcr);

	}

	public double calculateCD0Parasite() {

		_cD0Parasite = DragCalc.calculateCD0Parasite(
				_theNacelle.calculateFormFactor(), 
				_cF, 
				_theNacelle.getSurfaceWetted().doubleValue(SI.SQUARE_METRE), 
				_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE));

		return _cD0Parasite;
	}

	public double calculateCD0Base() {
		_cD0Base = DragCalc.calculateCd0Base(
				MethodEnum.MATLAB, 
				_cD0Parasite, 
				_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
				_theNacelle.getDiameterOutlet().doubleValue(SI.METER), 
				_theNacelle.getDiameterMax().doubleValue(SI.METER)
				);

		return _cD0Base;
	}

	public double calculateCD0Total(double kExcr) {
		_cD0Total = ((1 + kExcr)*_cD0Parasite + _cD0Base); 
		return _cD0Total;
	}

	//------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------
	
	public Double getReynolds() {
		return _reynolds;
	}

	public Double getXTransition() {
		return _xTransition;
	}

	public Double getCF() {
		return _cF;
	}

	public Double getCD0Parasite() {
		return _cD0Parasite;
	}

	public Double getCD0Base() {
		return _cD0Base;
	}

	public Double getCD0Total() {
		return _cD0Total;
	}

	public void setTheNacelle(NacelleCreator _theNacelle) {
		this._theNacelle = _theNacelle;
	}

	public ConditionEnum getTheCondition() {
		return _theCondition;
	}

	public void setTheCondition(ConditionEnum _theCondition) {
		this._theCondition = _theCondition;
	}

}
