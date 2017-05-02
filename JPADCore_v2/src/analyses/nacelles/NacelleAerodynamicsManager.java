package analyses.nacelles;

import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.Nacelles;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.MomentCalc;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;

public class NacelleAerodynamicsManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (From superclass and calculated)
	private Nacelles _theNacelles;
	private LiftingSurface _theWing;
	private OperatingConditions _theOperatingConditions;
	private Map<AerodynamicAndStabilityEnum, MethodEnum> _taskList;
	private Map<String, List<MethodEnum>> _plotList;
	private ConditionEnum _theCondition;
	private Double _reynolds;
	private Double _xTransition;
	private Double _cF;
	private Double _currentMach;
	private Amount<Length> _currentAltitude;
	private final double[] _positionOfC4ToNacelleLength = {.1,.2,.3,.4,.5,.6,.7};
	private final double[] _kF = {.115, .172, .344, .487, .688, .888, 1.146};

	// OUTPUT DATA
	private Map<MethodEnum, Double> _cD0Parasite;
	private Map<MethodEnum, Double> _cD0Base;
	private Map<MethodEnum, Double> _cD0Total;
	private Map<MethodEnum, Amount<?>> _cMAlpha;

	//------------------------------------------------------------------------------
	// BUILDER
	//------------------------------------------------------------------------------
	public NacelleAerodynamicsManager(
			Nacelles nacelles,
			LiftingSurface theWing,
			OperatingConditions operationConditions,
			ConditionEnum theCondition,
			Map<AerodynamicAndStabilityEnum, MethodEnum> taskList,
			Map<String, List<MethodEnum>> plotList
			) {
		
		_theNacelles = nacelles;
		_theWing = theWing;
		_theOperatingConditions = operationConditions;
		_theCondition = theCondition;
		_taskList = taskList;
		_plotList = plotList;

		initializeData();
		
		// TODO: COMPLETE INITIALIZE CALCULATORS 
		initializeCalculators();
	}
	
	//------------------------------------------------------------------------------
	// METHODS
	//------------------------------------------------------------------------------
	public void initializeData() {
		
		switch (_theCondition) {
		case TAKE_OFF:
			_currentMach = _theOperatingConditions.getMachTakeOff();
			_currentAltitude = _theOperatingConditions.getAltitudeTakeOff().to(SI.METER);
			break;
		case CLIMB:
			_currentMach = _theOperatingConditions.getMachClimb();
			_currentAltitude = _theOperatingConditions.getAltitudeClimb().to(SI.METER);
			break;
		case CRUISE:
			_currentMach = _theOperatingConditions.getMachCruise();
			_currentAltitude = _theOperatingConditions.getAltitudeCruise().to(SI.METER);
			break;
		case LANDING:
			_currentMach = _theOperatingConditions.getMachLanding();
			_currentAltitude = _theOperatingConditions.getAltitudeLanding().to(SI.METER);
			break;
		}
		
		_reynolds = AerodynamicCalc.calculateReynoldsEffective(
				_currentMach,
				0.3,
				_currentAltitude.doubleValue(SI.METER), 
				_theNacelles.getNacellesList().get(0).getLength().doubleValue(SI.METER),
				_theNacelles.getNacellesList().get(0).getRoughness().doubleValue(SI.METER)
				);
		_xTransition = 0.0; // TODO : Why ???
		_cF = AerodynamicCalc.calculateCf(_reynolds, _currentMach, _xTransition);
		
	}

	private void initializeCalculators() {
		
		// TODO !!
		
	}
	
	//............................................................................
	// Calc CD0 PARASITE INNER CLASS
	//............................................................................
	public class CalcCD0Parasite {
		
		public void semiempirical() {

			_cD0Parasite.put(
					MethodEnum.SEMPIEMPIRICAL, 
					DragCalc.calculateCD0Parasite(
							_theNacelles.getNacellesList().get(0).calculateFormFactor(), 
							_cF, 
							_theNacelles.getNacellesList().get(0).getSurfaceWetted().doubleValue(SI.SQUARE_METRE), 
							_theWing.getSurface().doubleValue(SI.SQUARE_METRE)
							)
					);
		}
		
	}
	//............................................................................
	// END Calc CD0 PARASITE INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CD0 BASE INNER CLASS
	//............................................................................
	public class CalcCD0Base {
		
		public void semiempirical() {
			
			if(_cD0Parasite.get(MethodEnum.SEMPIEMPIRICAL) == null) {
				CalcCD0Parasite calcCD0Parasite = new CalcCD0Parasite();
				calcCD0Parasite.semiempirical();
			}
			
			_cD0Base.put(
					MethodEnum.SEMPIEMPIRICAL,
					DragCalc.calculateCD0Base(
							MethodEnum.MATLAB, 
							_cD0Parasite.get(MethodEnum.SEMPIEMPIRICAL), 
							_theWing.getSurface().doubleValue(SI.SQUARE_METRE),
							_theNacelles.getNacellesList().get(0).getDiameterOutlet().doubleValue(SI.METER), 
							_theNacelles.getNacellesList().get(0).getDiameterMax().doubleValue(SI.METER)
							)
					);
			
		}
	}
	//............................................................................
	// END Calc CD0 BASE INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CD0 TOTAL INNER CLASS
	//............................................................................
	public class CalcCD0Total {
		
		public void semiempirical() {
			
			if(_cD0Base.get(MethodEnum.SEMPIEMPIRICAL) == null) {
				CalcCD0Base calcCD0Base = new CalcCD0Base();
				calcCD0Base.semiempirical();
			}
			
			_cD0Total.put(
					MethodEnum.SEMPIEMPIRICAL,
					((1 + _theNacelles.getKExcr())
							*_cD0Parasite.get(MethodEnum.SEMPIEMPIRICAL)) 
					+ _cD0Base.get(MethodEnum.SEMPIEMPIRICAL)
					);
			
		}
		
	}
	//............................................................................
	// END Calc CD0 TOTAL INNER CLASS
	//............................................................................

	//............................................................................
	// Calc CM_Alpha INNER CLASS
	//............................................................................
	public class CalcCMAlpha {
		
		public void gilruth() {
			
			_cMAlpha.put(
					MethodEnum.GILRUTH, 
					MomentCalc.calculateCMAlphaFuselageOrNacelleGilruth(
							_theNacelles.getNacellesList().get(0).getLength(),
							_theNacelles.getNacellesList().get(0).getDiameterMax(),
							_positionOfC4ToNacelleLength,
							_kF,
							_theWing.getSurface(), 
							_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChord(),
							_theWing.getXApexConstructionAxes(), 
							_theWing.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot()
							)
					);
			
		}
		
	}
	//............................................................................
	// END Calc CM_Alpha INNER CLASS
	//............................................................................
	
	//------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------
	
	public Nacelles getTheNacelles() {
		return _theNacelles;
	}

	public LiftingSurface getTheWing() {
		return _theWing;
	}

	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}

	public ConditionEnum getTheCondition() {
		return _theCondition;
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

	public Double getCurrentMach() {
		return _currentMach;
	}

	public Amount<Length> getCurrentAltitude() {
		return _currentAltitude;
	}

	public Map<MethodEnum, Double> getCD0Parasite() {
		return _cD0Parasite;
	}

	public Map<MethodEnum, Double> getCD0Base() {
		return _cD0Base;
	}

	public Map<MethodEnum, Double> getCD0Total() {
		return _cD0Total;
	}

	public Map<MethodEnum, Amount<?>> getCMAlpha() {
		return _cMAlpha;
	}

	public void setTheNacelles(Nacelles _theNacelles) {
		this._theNacelles = _theNacelles;
	}

	public void setTheWing(LiftingSurface _theWing) {
		this._theWing = _theWing;
	}

	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}

	public void setTheCondition(ConditionEnum _theCondition) {
		this._theCondition = _theCondition;
	}

	public void setReynolds(Double _reynolds) {
		this._reynolds = _reynolds;
	}

	public void setXTransition(Double _xTransition) {
		this._xTransition = _xTransition;
	}

	public void setCF(Double _cF) {
		this._cF = _cF;
	}

	public void setCurrentMach(Double _currentMach) {
		this._currentMach = _currentMach;
	}

	public void setCurrentAltitude(Amount<Length> _currentAltitude) {
		this._currentAltitude = _currentAltitude;
	}

	public void setCD0Parasite(Map<MethodEnum, Double> _cD0Parasite) {
		this._cD0Parasite = _cD0Parasite;
	}

	public void setCD0Base(Map<MethodEnum, Double> _cD0Base) {
		this._cD0Base = _cD0Base;
	}

	public void setCD0Total(Map<MethodEnum, Double> _cD0Total) {
		this._cD0Total = _cD0Total;
	}

	public void setCMAlpha(Map<MethodEnum, Amount<?>> _cMAlpha) {
		this._cMAlpha = _cMAlpha;
	}

	public double[] getPositionOfC4ToNacelleLength() {
		return _positionOfC4ToNacelleLength;
	}

	public double[] getKF() {
		return _kF;
	}

	public Map<AerodynamicAndStabilityEnum, MethodEnum> getTaskList() {
		return _taskList;
	}

	public void setTaskList(Map<AerodynamicAndStabilityEnum, MethodEnum> _taskList) {
		this._taskList = _taskList;
	}

	public Map<String, List<MethodEnum>> getPlotList() {
		return _plotList;
	}

	public void setPlotList(Map<String, List<MethodEnum>> _plotList) {
		this._plotList = _plotList;
	}

}
