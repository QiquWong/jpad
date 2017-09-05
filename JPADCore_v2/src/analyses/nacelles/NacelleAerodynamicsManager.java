package analyses.nacelles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.Nacelles;
import analyses.OperatingConditions;
import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager.CalcAlpha0L;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLAlpha;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.geometry.FusNacGeometryCalc;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;

public class NacelleAerodynamicsManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (From superclass and calculated)
	private Nacelles _theNacelles;
	private LiftingSurface _theWing;
	private OperatingConditions _theOperatingConditions;
	private LSAerodynamicsManager _theWingAerodynamicManager;
	private List<Amount<Angle>> _alphaArray;
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
	private Map<MethodEnum, Double> _cDInduced;
	private Map<MethodEnum, Double> _cDAtAlpha;
	private Map <MethodEnum, Double[]> _polar3DCurve;
	
	private Map<MethodEnum, Double> _cM0;
	private Map<MethodEnum, Amount<?>> _cMAlpha;
	private Map<MethodEnum, Double> _cMAtAlpha;
	private Map<MethodEnum, Double[]> _moment3DCurve;

	//------------------------------------------------------------------------------
	// BUILDER
	//------------------------------------------------------------------------------
	public NacelleAerodynamicsManager(
			Nacelles nacelles,
			LiftingSurface theWing,
			LSAerodynamicsManager theWingAerodynamicManager,
			OperatingConditions operationConditions,
			ConditionEnum theCondition,
			List<Amount<Angle>> alphaArray
			) {
		
		_theNacelles = nacelles;
		_theWing = theWing;
		_theWingAerodynamicManager = theWingAerodynamicManager;
		_theOperatingConditions = operationConditions;
		_theCondition = theCondition;
		_alphaArray = alphaArray;
		
		initializeData();
		
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
		
		_cD0Parasite = new HashMap<MethodEnum, Double>();
		_cD0Base = new HashMap<MethodEnum, Double>();
		_cDInduced = new HashMap<MethodEnum, Double>();
		_cDAtAlpha = new HashMap<MethodEnum, Double>();
		_cD0Total = new HashMap<MethodEnum, Double>();
		_polar3DCurve = new HashMap<MethodEnum, Double[]>();
		
		_cM0 = new HashMap<MethodEnum, Double>();
		_cMAlpha = new HashMap<MethodEnum, Amount<?>>();
		_cMAtAlpha = new HashMap<MethodEnum, Double>();
		_moment3DCurve = new HashMap<MethodEnum, Double[]>();
		
	}

	//............................................................................
	// Calc CD0 PARASITE INNER CLASS
	//............................................................................
	public class CalcCD0Parasite {
		
		public void semiempirical() {

			_cD0Parasite.put(
					MethodEnum.SEMIEMPIRICAL, 
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
			
			if(_cD0Parasite.get(MethodEnum.SEMIEMPIRICAL) == null) {
				CalcCD0Parasite calcCD0Parasite = new CalcCD0Parasite();
				calcCD0Parasite.semiempirical();
			}
			
			_cD0Base.put(
					MethodEnum.SEMIEMPIRICAL,
					DragCalc.calculateCD0Base(
							MethodEnum.MATLAB, 
							_cD0Parasite.get(MethodEnum.SEMIEMPIRICAL), 
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
			
			if(_cD0Base.get(MethodEnum.SEMIEMPIRICAL) == null) {
				CalcCD0Base calcCD0Base = new CalcCD0Base();
				calcCD0Base.semiempirical();
			}
			
			_cD0Total.put(
					MethodEnum.SEMIEMPIRICAL,
					((1 + _theNacelles.getKExcr())
							*_cD0Parasite.get(MethodEnum.SEMIEMPIRICAL)) 
					+ _cD0Base.get(MethodEnum.SEMIEMPIRICAL)
					);
			
		}
		
	}
	//............................................................................
	// END Calc CD0 TOTAL INNER CLASS
	//............................................................................

	//............................................................................
	// Calc CDInduced INNER CLASS
	//............................................................................
	public class CalcCDInduced {
		
		//@see NASA TN D-6800 (pag.47 pdf)
		public void semiempirical(Amount<Angle> alphaBody) {
			
			List<Amount<Length>> xStations = 
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									0,
									_theNacelles.getNacellesList().get(0).getLength().doubleValue(SI.METER), 
									50
									),
							SI.METER
							);
			
			_cDInduced.put(
					MethodEnum.SEMIEMPIRICAL, 
					DragCalc.calculateCDInducedFuselageOrNacelle(
							xStations, 
							alphaBody, 
							_theNacelles.getNacellesList().get(0).getSurfaceWetted(), 
							FusNacGeometryCalc.calculateFuselageVolume(
									_theNacelles.getNacellesList().get(0).getLength(), 
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											xStations.stream()
											.map(x -> FusNacGeometryCalc.getWidthAtX(
													x.doubleValue(SI.METER),
													_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()), 
													_theNacelles.getNacellesList().get(0).getYCoordinatesOutlineXYRight().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList())
													))
											.collect(Collectors.toList())
											)
									), 
							_theWing.getAerodynamicDatabaseReader().get_C_m0_b_k2_minus_k1_vs_FFR(
									_theNacelles.getNacellesList().get(0).getLength().doubleValue(SI.METER), 
									_theNacelles.getNacellesList().get(0).getDiameterMax().doubleValue(SI.METER)
									), 
							_theNacelles.getNacellesList().get(0).getDiameterMax(), 
							_theNacelles.getNacellesList().get(0).getLength(), 
							_theWing.getSurface(), 
							_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()), 
							_theNacelles.getNacellesList().get(0).getZCoordinatesOutlineXZUpper().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()),
							_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()),
							_theNacelles.getNacellesList().get(0).getZCoordinatesOutlineXZLower().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()),
							_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()),
							_theNacelles.getNacellesList().get(0).getYCoordinatesOutlineXYRight().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList())
							)
					);
			
		}
		
	}
	//............................................................................
	// END Calc CDInduced INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CD@Alpha INNER CLASS
	//............................................................................
	public class CalcCDAtAlpha {
		
		public double semiempirical(Amount<Angle> alphaBody) {
			
			double cDActual = 0.0;
			
			if(_cD0Total.get(MethodEnum.SEMIEMPIRICAL) == null) {
				CalcCD0Total calcCD0Total = new CalcCD0Total(); 
				calcCD0Total.semiempirical();
			}
			
			List<Amount<Length>> xStations = 
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									0,
									_theNacelles.getNacellesList().get(0).getLength().doubleValue(SI.METER), 
									50
									),
							SI.METER
							);
			
			double cDInduced = DragCalc.calculateCDInducedFuselageOrNacelle(
					xStations, 
					alphaBody, 
					_theNacelles.getNacellesList().get(0).getSurfaceWetted(), 
					FusNacGeometryCalc.calculateFuselageVolume(
							_theNacelles.getNacellesList().get(0).getLength(), 
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									xStations.stream()
									.map(x -> FusNacGeometryCalc.getWidthAtX(
											x.doubleValue(SI.METER),
											_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()), 
											_theNacelles.getNacellesList().get(0).getYCoordinatesOutlineXYRight().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList())
											))
									.collect(Collectors.toList())
									)
							), 
					_theWing.getAerodynamicDatabaseReader().get_C_m0_b_k2_minus_k1_vs_FFR(
							_theNacelles.getNacellesList().get(0).getLength().doubleValue(SI.METER), 
							_theNacelles.getNacellesList().get(0).getDiameterMax().doubleValue(SI.METER)
							), 
					_theNacelles.getNacellesList().get(0).getDiameterMax(), 
					_theNacelles.getNacellesList().get(0).getLength(), 
					_theWing.getSurface(), 
					_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()), 
					_theNacelles.getNacellesList().get(0).getZCoordinatesOutlineXZUpper().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()),
					_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()),
					_theNacelles.getNacellesList().get(0).getZCoordinatesOutlineXZLower().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()),
					_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList()),
					_theNacelles.getNacellesList().get(0).getYCoordinatesOutlineXYRight().stream().map(p -> p.doubleValue(SI.METER)).collect(Collectors.toList())
					);
			
			getCDAtAlpha().put(
					MethodEnum.SEMIEMPIRICAL,
					_cD0Total.get(MethodEnum.SEMIEMPIRICAL)
					+ cDInduced
					);
			
			cDActual = _cD0Total.get(MethodEnum.SEMIEMPIRICAL)
					+ cDInduced;
			
			return cDActual;
		}
		
	}
	//............................................................................
	// END Calc CD@Alpha INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc Polar Curve INNER CLASS
	//............................................................................
	public class CalcPolar {
		
		public void semiempirical() {
			
			CalcCDAtAlpha calcCDAtAlpha = new CalcCDAtAlpha();
			
			Double[] cDArray = new Double[getAlphaArray().size()];
			for(int i=0; i<getAlphaArray().size(); i++) {
				cDArray[i] = calcCDAtAlpha.semiempirical(getAlphaArray().get(i));
			}
			
			getPolar3DCurve().put(MethodEnum.SEMIEMPIRICAL, cDArray);
			
		}
		
	}
	//............................................................................
	// END Calc Polar Curve INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CM0 INNER CLASS
	//............................................................................
	public class CalcCM0 {
		
		public void multhopp() {
			
			switch (_theNacelles.getNacellesList().get(0).getMountingPosition()) {
			case WING:
				
				if(_theWingAerodynamicManager.getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
					CalcAlpha0L calcAlpha0L = _theWingAerodynamicManager.new CalcAlpha0L();
					calcAlpha0L.integralMeanWithTwist();
				}
				
				getCM0().put(
						MethodEnum.MULTHOPP, 
						MomentCalc.calculateCM0NacelleMulthopp(
								_theNacelles.getNacellesList().get(0).getLength(),
								_theWing.getAerodynamicDatabaseReader().get_C_m0_b_k2_minus_k1_vs_FFR(
										_theNacelles.getNacellesList().get(0).getLength().doubleValue(SI.METER), 
										_theNacelles.getNacellesList().get(0).getDiameterMax().doubleValue(SI.METER)
										),
								_theWing.getSurface(), 
								_theWing.getRiggingAngle(),
								_theWingAerodynamicManager.getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST),
								_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChord(), 
								_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList()),
								_theNacelles.getNacellesList().get(0).getZCoordinatesOutlineXZUpper().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList()),
								_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList()),
								_theNacelles.getNacellesList().get(0).getZCoordinatesOutlineXZLower().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList()),
								_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList()),
								_theNacelles.getNacellesList().get(0).getYCoordinatesOutlineXYRight().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList())
								)
						);
				break;
			case FUSELAGE:
				// TODO : FIND OUT HOW TO CALCULATE
				getCM0().put(
						MethodEnum.MULTHOPP,
						0.0
						);
			case HTAIL:
				// TODO : FIND OUT HOW TO CALCULATE
				getCM0().put(
						MethodEnum.MULTHOPP,
						0.0
						);
			case UNDERCARRIAGE_HOUSING:
				// TODO : FIND OUT HOW TO CALCULATE
				getCM0().put(
						MethodEnum.MULTHOPP,
						0.0
						);
			default:
				break;
			}
			
		}
		
	}
	//............................................................................
	// END Calc CM0 INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CM_Alpha INNER CLASS
	//............................................................................
	public class CalcCMAlpha {
		
		public void multhopp(
				Amount<Length> wingTrailingEdgeToHTailQuarterChordDistance,
				Double downwashGradientRoskamConstant
				) {
			
			if(_theWingAerodynamicManager.getCLAlpha().get(MethodEnum.NASA_BLACKWELL) == null) {
				CalcCLAlpha calcCLAlpha = _theWingAerodynamicManager.new CalcCLAlpha();
				calcCLAlpha.nasaBlackwell();
			}
			
			_cMAlpha.put(
					MethodEnum.MULTHOPP, 
					MomentCalc.calculateCMAlphaFuselageOrNacelleMulthopp(
							_theNacelles.getNacellesList().get(0).getXApexConstructionAxes(),
							_theNacelles.getNacellesList().get(0).getLength(),
							downwashGradientRoskamConstant, 
							_theWing.getAspectRatio(),
							_theWing.getSurface(), 
							_theWing.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot(), 
							_theWing.getLiftingSurfaceCreator().getMeanAerodynamicChord(),
							_theWingAerodynamicManager.getCLAlpha().get(MethodEnum.NASA_BLACKWELL),
							_theWing.getXApexConstructionAxes(),
							wingTrailingEdgeToHTailQuarterChordDistance,
							_theWing.getAerodynamicDatabaseReader(),
							_theNacelles.getNacellesList().get(0).getXCoordinatesOutline().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList()),
							_theNacelles.getNacellesList().get(0).getYCoordinatesOutlineXYRight().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList())
							)
					);
			
		}
	}
	//............................................................................
	// END Calc CM_Alpha INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CM@Alpha INNER CLASS
	//............................................................................
	public class CalcCMAtAlpha {
		
		public void multhopp(
				Amount<Angle> alphaBody,
				Amount<Length> wingTrailingEdgeToHTailQuarterChordDistance,
				Double downwashGradientRoskamConstant
				) {
			
			if(_cM0.get(MethodEnum.MULTHOPP) == null) {
				CalcCM0 calcCM0 = new CalcCM0();
				calcCM0.multhopp();
			}
			
			if(_cMAlpha.get(MethodEnum.MULTHOPP) == null) {
				CalcCMAlpha calcCMAlpha = new CalcCMAlpha();
				calcCMAlpha.multhopp(
						wingTrailingEdgeToHTailQuarterChordDistance, 
						downwashGradientRoskamConstant
						);
			}
			
			_cMAtAlpha.put(
					MethodEnum.MULTHOPP, 
					MomentCalc.calculateCMAtAlphaFuselage(
							alphaBody, 
							_cMAlpha.get(MethodEnum.MULTHOPP), 
							_cM0.get(MethodEnum.MULTHOPP)
							)
					);
		}
		
	}
	//............................................................................
	// END Calc CM@Alpha INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc Moment Curve INNER CLASS
	//............................................................................
	public class CalcMomentCurve {
		
		public void multhopp(
				Amount<Length> wingTrailingEdgeToHTailQuarterChordDistance,
				Double downwashGradientRoskamConstant
				) {
			
			if(_cM0.get(MethodEnum.MULTHOPP) == null) {
				CalcCM0 calcCM0 = new CalcCM0();
				calcCM0.multhopp();
			}
			
			if(_cMAlpha.get(MethodEnum.MULTHOPP) == null) {
				CalcCMAlpha calcCMAlpha = new CalcCMAlpha();
				calcCMAlpha.multhopp(
						wingTrailingEdgeToHTailQuarterChordDistance,
						downwashGradientRoskamConstant
						);
			}
			
			_moment3DCurve.put(
					MethodEnum.MULTHOPP, 
					MyArrayUtils.convertListOfDoubleToDoubleArray(
							_alphaArray.stream()
							.map(a -> MomentCalc.calculateCMAtAlphaFuselage(
									a, 
									_cMAlpha.get(MethodEnum.MULTHOPP), 
									_cM0.get(MethodEnum.MULTHOPP))
									)
							.collect(Collectors.toList())
							)
					);
		}
		
	}
	//............................................................................
	// END Calc CM@Alpha TOTAL INNER CLASS
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

	public Map<MethodEnum, Double> getCDInduced() {
		return _cDInduced;
	}

	public void setCDInduced(Map<MethodEnum, Double> _cDInduced) {
		this._cDInduced = _cDInduced;
	}

	public Map<MethodEnum, Double> getCDAtAlpha() {
		return _cDAtAlpha;
	}

	public void setCDAtAlpha(Map<MethodEnum, Double> _cDAtAlpha) {
		this._cDAtAlpha = _cDAtAlpha;
	}

	public Map <MethodEnum, Double[]> getPolar3DCurve() {
		return _polar3DCurve;
	}

	public void setPolar3DCurve(Map <MethodEnum, Double[]> _polar3DCurve) {
		this._polar3DCurve = _polar3DCurve;
	}

	public List<Amount<Angle>> getAlphaArray() {
		return _alphaArray;
	}

	public void setAlphaArray(List<Amount<Angle>> _alphaArray) {
		this._alphaArray = _alphaArray;
	}

	public Map<MethodEnum, Double> getCM0() {
		return _cM0;
	}

	public void setCM0(Map<MethodEnum, Double> _cM0) {
		this._cM0 = _cM0;
	}

	public Map<MethodEnum, Double> getCMAtAlpha() {
		return _cMAtAlpha;
	}

	public void setCMAtAlpha(Map<MethodEnum, Double> _cMAtAlpha) {
		this._cMAtAlpha = _cMAtAlpha;
	}

	public Map<MethodEnum, Double[]> getMoment3DCurve() {
		return _moment3DCurve;
	}

	public void setMoment3DCurve(Map<MethodEnum, Double[]> _moment3DCurve) {
		this._moment3DCurve = _moment3DCurve;
	}

	public LSAerodynamicsManager getTheWingAerodynamicManager() {
		return _theWingAerodynamicManager;
	}

	public void setTheWingAerodynamicManager(LSAerodynamicsManager _theWingAerodynamicManager) {
		this._theWingAerodynamicManager = _theWingAerodynamicManager;
	}

	
}
