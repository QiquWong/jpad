package analyses.fuselage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcAlpha0L;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager.CalcCLAlpha;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.geometry.FusNacGeometryCalc;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;

/*
 *******************************************************************************
 * @author Vittorio Trifari, Agostino De Marco
 *******************************************************************************
 */

public class FuselageAerodynamicsManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private Fuselage _theFuselage;
	private LiftingSurface _theWing;
	private LiftingSurfaceAerodynamicsManager _theWingAerodynamicManager;
	private OperatingConditions _theOperatingConditions;
	private List<Amount<Angle>> _alphaArray;
	private ConditionEnum _theCondition;
	private Double _fuselageXPercentPositionPole;
	
	//..............................................................................
	// DERIVED INPUT	
	private Amount<Length> _currentAltitude;
	private Amount<Temperature> _currentDeltaTemperature;
	private Double _currentMachNumber;
	private Double _cF;
	private Amount<Area> _fuselageFrontSurface;
	private Double _fuselageSurfaceRatio;
	private Amount<Length> _equivalentDiameterBase;
	private final double[] _positionOfC4ToFuselageLength = {.1,.2,.3,.4,.5,.6,.7};
	private final double[] _kF = {.115, .172, .344, .487, .688, .888, 1.146};
	
	//..............................................................................
	// OUTPUT
	//..............................................................................
	// DRAG
	private Map<MethodEnum, Double>	_cD0Parasite;
	private Map<MethodEnum, Double> _cD0Upsweep;
	private Map<MethodEnum, Double> _cD0Windshield;
	private Map<MethodEnum, Double> _cD0Base;
	private Map<MethodEnum, Double> _cD0Total;
	private Map<MethodEnum, Double> _cDInduced;
	private Map<MethodEnum, Double> _cDAtAlpha;
	private Map <MethodEnum, Double[]> _polar3DCurve;
	
	// PITCHING MOMENT
	private Map<MethodEnum, Double> _cM0;
	private Map<MethodEnum, Amount<?>> _cMAlpha;
	private Map<MethodEnum, Double> _cMAtAlpha;
	private Map<MethodEnum, Double[]> _moment3DCurve;
	
	//------------------------------------------------------------------------------
	// BUILDER:
	//------------------------------------------------------------------------------
	public FuselageAerodynamicsManager(
			Fuselage theFuselage,
			LiftingSurface theWing,
			LiftingSurfaceAerodynamicsManager theWingAerodynamicManager,
			OperatingConditions theOperatingConditions,
			List<Amount<Angle>> alphaArray,
			ConditionEnum theCondition,
			Double fuselageXPercentPositionPole
			) {
		
		this._theFuselage = theFuselage;
		this._theWing = theWing;
		this._theWingAerodynamicManager = theWingAerodynamicManager;
		this._theOperatingConditions = theOperatingConditions;
		this._alphaArray = alphaArray;
		this._theCondition = theCondition;
		this._fuselageXPercentPositionPole = fuselageXPercentPositionPole;
		
		if(_theWing == null) {
			System.err.println("ERROR : A WING MUST BE ASSIGNED FOR THE FUSELAGE AERODYNAMIC ANALYSIS !!");
			return;
		}
		
		if(_theWingAerodynamicManager == null) {
			System.err.println("ERROR : A WING ANALYSIS MUST BE ASSIGNED FOR THE FUSELAGE AERODYNAMIC ANALYSIS !!");
			return;
		}
		
		initializeVariables(_theCondition);
		initializeData();
		
	}
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeVariables(ConditionEnum theCondition) {
		
		switch (theCondition) {
		case TAKE_OFF:
			this._currentMachNumber = this._theOperatingConditions.getMachTakeOff();
			this._currentAltitude = this._theOperatingConditions.getAltitudeTakeOff();
			this._currentDeltaTemperature = this._theOperatingConditions.getDeltaTemperatureTakeOff();
			break;
		case CLIMB:
			this._currentMachNumber = this._theOperatingConditions.getMachClimb();
			this._currentAltitude = this._theOperatingConditions.getAltitudeClimb();
			this._currentDeltaTemperature = this._theOperatingConditions.getDeltaTemperatureClimb();
			break;
		case CRUISE:
			this._currentMachNumber = this._theOperatingConditions.getMachCruise();
			this._currentAltitude = this._theOperatingConditions.getAltitudeCruise();
			this._currentDeltaTemperature = this._theOperatingConditions.getDeltaTemperatureCruise();
			break;
		case LANDING:
			this._currentMachNumber = this._theOperatingConditions.getMachLanding();
			this._currentAltitude = this._theOperatingConditions.getAltitudeLanding();
			this._currentDeltaTemperature = this._theOperatingConditions.getDeltaTemperatureLanding();
			break;
		default:
			break;
		}
		
		_cD0Parasite = new HashMap<MethodEnum, Double>();
		_cD0Upsweep = new HashMap<MethodEnum, Double>();
		_cD0Base = new HashMap<MethodEnum, Double>();
		_cD0Windshield = new HashMap<MethodEnum, Double>();
		_cDInduced = new HashMap<MethodEnum, Double>();
		_cDAtAlpha = new HashMap<MethodEnum, Double>();
		_cD0Total = new HashMap<MethodEnum, Double>();
		_polar3DCurve = new HashMap<MethodEnum, Double[]>();
		
		_cM0 = new HashMap<MethodEnum, Double>();
		_cMAlpha = new HashMap<MethodEnum, Amount<?>>();
		_cMAtAlpha = new HashMap<MethodEnum, Double>();
		_moment3DCurve = new HashMap<MethodEnum, Double[]>();
		
	}
	
	private void initializeData() {
		
		_fuselageFrontSurface = 
				Amount.valueOf(
						Math.PI
						*Math.pow(_theFuselage.getEquivalentDiameterCylinderGM().doubleValue(SI.METER), 2)
						/4,
						SI.SQUARE_METRE
						);
		_cF = AerodynamicCalc.calculateCf(
				AerodynamicCalc.calculateReynolds(
						_currentAltitude,
						_currentDeltaTemperature,
						_currentMachNumber,
						_theFuselage.getFuselageLength()
						), 
				_currentMachNumber, 
				0.
				);
		_fuselageSurfaceRatio = 
				_fuselageFrontSurface.doubleValue(SI.SQUARE_METRE)/
				_theWing.getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
		
		// value chosen to match matlab file base drag --> lenF - dxTailCap;
		_equivalentDiameterBase = 
				Amount.valueOf(
						_theFuselage.getEquivalentDiameterAtX(
								_theFuselage.getFuselageLength()
								.minus(_theFuselage.getTailCapOffset())
								.doubleValue(SI.METER)
								),
						SI.METER
						);
		
		if(_theWingAerodynamicManager.getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
			CalcAlpha0L calcAlpha0L = _theWingAerodynamicManager.new CalcAlpha0L();
			calcAlpha0L.integralMeanWithTwist();
		}
		
	}
	
	//............................................................................
	// Calc CD0 PARASITE INNER CLASS
	//............................................................................
	public class CalcCD0Parasite {
		
		public void semiempirical() {
			
			_cD0Parasite.put(
					MethodEnum.SEMIEMPIRICAL, 
					DragCalc.calculateCD0Parasite(
							_theFuselage.getFormFactor(), 
							_cF,
							_theFuselage.getSWetTotal().doubleValue(SI.SQUARE_METRE),
							_theWing.getSurfacePlanform().doubleValue(SI.SQUARE_METRE)
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
							_theWing.getSurfacePlanform().doubleValue(SI.SQUARE_METRE), 
							_equivalentDiameterBase.doubleValue(SI.METER),
							_theFuselage.getEquivalentDiameterCylinderGM().doubleValue(SI.METER)
							)
					);
		}
		
	}
	//............................................................................
	// END Calc CD0 BASE INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CD0 UPSWEEP INNER CLASS
	//............................................................................
	public class CalcCD0Upsweep {
		
		public void semiempirical() {
			
			Amount<Length> zCamber75 = 
					Amount.valueOf(
							_theFuselage.getCamberAngleAtX(
									_theFuselage.getFuselageLength().doubleValue(SI.METER)
									- (0.25*_theFuselage.getTailLength().doubleValue(SI.METER))
									),
							SI.METER
							);

			_cD0Upsweep.put(
					MethodEnum.SEMIEMPIRICAL, 
					DragCalc.calculateCD0Upsweep(
							_theFuselage.getCylinderSectionArea(),
							_theWing.getSurfacePlanform(),
							_theFuselage.getTailLength(),
							zCamber75
							)
					);
		}
		
	}
	//............................................................................
	// END Calc CD0 UPSWEEP INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CD0 WINDSHIELD INNER CLASS
	//............................................................................
	public class CalcCD0Windshield {
		
		public void semiempirical() {
			
			_cD0Windshield.put(
					MethodEnum.SEMIEMPIRICAL, 
					DragCalc.calculateCD0Windshield(
							MethodEnum.SEMIEMPIRICAL,
							_theFuselage.getWindshieldType(),
							_theFuselage.getWindshieldArea(),
							_theFuselage.getCylinderSectionArea(),
							_theWing.getSurfacePlanform()
							)
					);
		}
		
	}
	//............................................................................
	// END Calc CD0 WINDSHIELD INNER CLASS
	//............................................................................
	
	//............................................................................
	// Calc CD0 TOTAL INNER CLASS
	//............................................................................
	public class CalcCD0Total {
		
		public void semiempirical() {
			
			if(_cD0Parasite.get(MethodEnum.SEMIEMPIRICAL) == null){
				CalcCD0Parasite calcCD0Parasite = new CalcCD0Parasite();
				calcCD0Parasite.semiempirical();
			}
			
			if(_cD0Base.get(MethodEnum.SEMIEMPIRICAL) == null){
				CalcCD0Base calcCD0Base = new CalcCD0Base();
				calcCD0Base.semiempirical();
			}
			
			if(_cD0Upsweep.get(MethodEnum.SEMIEMPIRICAL) == null){
				CalcCD0Upsweep calcCD0Upsweep = new CalcCD0Upsweep();
				calcCD0Upsweep.semiempirical();
			}
			
			if(_cD0Windshield.get(MethodEnum.SEMIEMPIRICAL) == null){
				CalcCD0Windshield calcCD0Windshield = new CalcCD0Windshield();
				calcCD0Windshield.semiempirical();
			}
			
			_cD0Total.put(
					MethodEnum.SEMIEMPIRICAL, 
					(_cD0Parasite.get(MethodEnum.SEMIEMPIRICAL)
					*(1 + _theFuselage.getKExcr())
					) 
					+ _cD0Upsweep.get(MethodEnum.SEMIEMPIRICAL) 
					+ _cD0Base.get(MethodEnum.SEMIEMPIRICAL)
					+ _cD0Windshield.get(MethodEnum.SEMIEMPIRICAL)
					);
		}
		
		public void fusDes() {
			
			Amount<Area> frontSurface = FusNacGeometryCalc.calculateSfront(
					_theFuselage.getEquivalentDiameterCylinderGM()
					).to(SI.SQUARE_METRE); 
			
			double surfaceRatio = 
					frontSurface.doubleValue(SI.SQUARE_METRE)
					/_theWing.getSurfacePlanform().doubleValue(SI.SQUARE_METRE);

			double cDFlatPlate = AerodynamicCalc.calculateCfTurb(
					AerodynamicCalc.calculateReynolds(
							_currentAltitude,
							_currentDeltaTemperature,
							_currentMachNumber,
							_theFuselage.getFuselageLength()
							), 
					_currentMachNumber
					);
			
			// Referred to wing surface
			_cD0Total.put(
					MethodEnum.FUSDES,
					DragCalc.dragFusDesCalc(
							_theFuselage.getFusDesDatabaseReader().get_Kn_vs_FRn(
									_theFuselage.getNoseFinenessRatio(),
									_theFuselage.getWindshieldAngle().doubleValue(NonSI.DEGREE_ANGLE)
									),
							_theFuselage.getFusDesDatabaseReader().get_Kc_vs_FR(
									_theFuselage.getFuselageFinenessRatio()
									),
							_theFuselage.getFusDesDatabaseReader().get_Kt_vs_FRt(
									_theFuselage.getTailFinenessRatio(),
									_theFuselage.getUpsweepAngle().doubleValue(NonSI.DEGREE_ANGLE)
									),
							_theFuselage.getSWetTotal().doubleValue(SI.SQUARE_METRE),
							_theFuselage.getSWetNose().doubleValue(SI.SQUARE_METRE),
							_theFuselage.getSWetCylinder().doubleValue(SI.SQUARE_METRE),
							_theFuselage.getSWetTail().doubleValue(SI.SQUARE_METRE),
							frontSurface.doubleValue(SI.SQUARE_METRE),
							cDFlatPlate)
					*surfaceRatio
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
		public void semiempirical(Amount<Angle> alphaBody, Double currentMach) {
			
			List<Amount<Length>> xStations = 
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									0,
									_theFuselage.getFuselageLength().doubleValue(SI.METER), 
									50
									),
							SI.METER
							);
			
			_cDInduced.put(
					MethodEnum.SEMIEMPIRICAL, 
					DragCalc.calculateCDInducedFuselageOrNacelle(
							xStations, 
							alphaBody, 
							currentMach,
							FusNacGeometryCalc.calculateFuselageVolume(
									_theFuselage.getFuselageLength(), 
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											xStations.stream()
											.map(x -> FusNacGeometryCalc.getWidthAtX(
													x.doubleValue(SI.METER),
													_theFuselage.getOutlineXYSideRCurveX(), 
													_theFuselage.getOutlineXYSideRCurveX()
													))
											.collect(Collectors.toList())
											)
									), 
							_theWing.getAeroDatabaseReader().get_C_m0_b_k2_minus_k1_vs_FFR(
									_theFuselage.getFuselageLength().doubleValue(SI.METER), 
									_theFuselage.getEquivalentDiameterGM().doubleValue(SI.METER)
									), 
							FusNacGeometryCalc.calculateMaxDiameter(
									xStations,
									_theFuselage.getOutlineXYSideRCurveX(),
									_theFuselage.getOutlineXYSideRCurveY()
									), 
							_theFuselage.getNoseLength(),                                    
							_theFuselage.getFuselageLength(), 
							_theWing.getSurfacePlanform(), 
							_theFuselage.getOutlineXZUpperCurveX(), 
							_theFuselage.getOutlineXZUpperCurveZ(), 
							_theFuselage.getOutlineXZLowerCurveX(),
							_theFuselage.getOutlineXZLowerCurveZ(),
							_theFuselage.getOutlineXYSideRCurveX(), 
							_theFuselage.getOutlineXYSideRCurveY()
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
		
		public double semiempirical(Amount<Angle> alphaBody, Double currentMach) {
			
			double cDActual = 0.0;
			
			if(_cD0Total.get(MethodEnum.SEMIEMPIRICAL) == null) {
				CalcCD0Total calcCD0Total = new CalcCD0Total(); 
				calcCD0Total.semiempirical();
			}
			
			List<Amount<Length>> xStations = 
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									0,
									_theFuselage.getFuselageLength().doubleValue(SI.METER), 
									50
									),
							SI.METER
							);
			
			double cDInduced = DragCalc.calculateCDInducedFuselageOrNacelle(
					xStations, 
					alphaBody, 
					currentMach,
					FusNacGeometryCalc.calculateFuselageVolume(
							_theFuselage.getFuselageLength(), 
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									xStations.stream()
									.map(x -> FusNacGeometryCalc.getWidthAtX(
											x.doubleValue(SI.METER),
											_theFuselage.getOutlineXYSideRCurveX(), 
											_theFuselage.getOutlineXYSideRCurveX()
											))
									.collect(Collectors.toList())
									)
							), 
					_theWing.getAeroDatabaseReader().get_C_m0_b_k2_minus_k1_vs_FFR(
							_theFuselage.getFuselageLength().doubleValue(SI.METER), 
							_theFuselage.getEquivalentDiameterGM().doubleValue(SI.METER)
							), 
					FusNacGeometryCalc.calculateMaxDiameter(
							xStations,
							_theFuselage.getOutlineXYSideRCurveX(),
							_theFuselage.getOutlineXYSideRCurveY()
							), 
					_theFuselage.getNoseLength(),                                   
					_theFuselage.getFuselageLength(), 
					_theWing.getSurfacePlanform(), 
					_theFuselage.getOutlineXZUpperCurveX(), 
					_theFuselage.getOutlineXZUpperCurveZ(), 
					_theFuselage.getOutlineXZLowerCurveX(),
					_theFuselage.getOutlineXZLowerCurveZ(),
					_theFuselage.getOutlineXYSideRCurveX(), 
					_theFuselage.getOutlineXYSideRCurveY()
					);
			
			_cDAtAlpha.put(
					MethodEnum.SEMIEMPIRICAL,
					_cD0Total.get(MethodEnum.SEMIEMPIRICAL)
					+ cDInduced
					);
			
			cDActual = _cD0Total.get(MethodEnum.SEMIEMPIRICAL)
					+ cDInduced;
			
			return cDActual;
		}
		
		public double fusDes(Amount<Angle> alphaBody, Double currentMach) {
			
			double cDActual = 0.0;
			
			if(_cD0Total.get(MethodEnum.FUSDES) == null) {
				CalcCD0Total calcCD0Total = new CalcCD0Total(); 
				calcCD0Total.fusDes();
			}
			
			List<Amount<Length>> xStations = 
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(
									0,
									_theFuselage.getFuselageLength().doubleValue(SI.METER), 
									50
									),
							SI.METER
							);
			
			double cDInduced = DragCalc.calculateCDInducedFuselageOrNacelle(
					xStations, 
					alphaBody, 
					currentMach,
					FusNacGeometryCalc.calculateFuselageVolume(
							_theFuselage.getFuselageLength(), 
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									xStations.stream()
									.map(x -> FusNacGeometryCalc.getWidthAtX(
											x.doubleValue(SI.METER),
											_theFuselage.getOutlineXYSideRCurveX(), 
											_theFuselage.getOutlineXYSideRCurveX()
											))
									.collect(Collectors.toList())
									)
							), 
					_theWing.getAeroDatabaseReader().get_C_m0_b_k2_minus_k1_vs_FFR(
							_theFuselage.getFuselageLength().doubleValue(SI.METER), 
							_theFuselage.getEquivalentDiameterGM().doubleValue(SI.METER)
							), 
					FusNacGeometryCalc.calculateMaxDiameter(
							xStations,
							_theFuselage.getOutlineXYSideRCurveX(),
							_theFuselage.getOutlineXYSideRCurveY()
							), 
					_theFuselage.getNoseLength(),                       
					_theFuselage.getFuselageLength(), 
					_theWing.getSurfacePlanform(), 
					_theFuselage.getOutlineXZUpperCurveX(), 
					_theFuselage.getOutlineXZUpperCurveZ(), 
					_theFuselage.getOutlineXZLowerCurveX(),
					_theFuselage.getOutlineXZLowerCurveZ(),
					_theFuselage.getOutlineXYSideRCurveX(), 
					_theFuselage.getOutlineXYSideRCurveY()
					);
			
			_cDAtAlpha.put(
					MethodEnum.FUSDES,
					_cD0Total.get(MethodEnum.FUSDES)
					+ cDInduced
					);
			
			cDActual = _cD0Total.get(MethodEnum.FUSDES)
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
		
		public void semiempirical(Double currentMach) {
			
			CalcCDAtAlpha calcCDAtAlpha = new CalcCDAtAlpha();
			
			Double[] cDArray = new Double[_alphaArray.size()];
			for(int i=0; i<_alphaArray.size(); i++) {
				cDArray[i] = calcCDAtAlpha.semiempirical(_alphaArray.get(i), currentMach);
			}
			
			_polar3DCurve.put(MethodEnum.SEMIEMPIRICAL, cDArray);
			
		}
		
		public void fusDes(Double currentMach) {

			CalcCDAtAlpha calcCDAtAlpha = new CalcCDAtAlpha();
			
			Double[] cDArray = new Double[_alphaArray.size()];
			for(int i=0; i<_alphaArray.size(); i++) {
				cDArray[i] = calcCDAtAlpha.fusDes(_alphaArray.get(i), currentMach);
			}
			
			_polar3DCurve.put(MethodEnum.FUSDES, cDArray);
			
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
			
			if(_theWingAerodynamicManager.getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST) == null) {
				CalcAlpha0L calcAlpha0L = _theWingAerodynamicManager.new CalcAlpha0L();
				calcAlpha0L.integralMeanWithTwist();
			}
			
			_cM0.put(
					MethodEnum.MULTHOPP, 
					MomentCalc.calculateCM0Multhopp(
							_theFuselage.getXApexConstructionAxes(),
							_theFuselage.getFuselageLength(), 
							_theWing.getAeroDatabaseReader().get_C_m0_b_k2_minus_k1_vs_FFR(
									_theFuselage.getFuselageLength().doubleValue(SI.METER), 
									_theFuselage.getEquivalentDiameterGM().doubleValue(SI.METER)
									),
							_theWing.getRiggingAngle(),
							_theWingAerodynamicManager.getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST),
							_theWing.getSurfacePlanform(), 
							_theWing.getPanels().get(0).getChordRoot(), 
							_theWing.getMeanAerodynamicChord(), 
							_theWing.getXApexConstructionAxes(),
							_theFuselage.getOutlineXYSideRCurveX(),
							_theFuselage.getOutlineXYSideRCurveY(),
							_theFuselage.getOutlineXZUpperCurveX(), 
							_theFuselage.getOutlineXZUpperCurveZ(), 
							_theFuselage.getOutlineXZLowerCurveX(),
							_theFuselage.getOutlineXZLowerCurveZ()
							)
					);
		}
		
		public void fusDes() {
			
			_cM0.put(
					MethodEnum.FUSDES,
					MomentCalc.calcCM0Fuselage(
							_theFuselage.getFusDesDatabaseReader().get_CM0_FR_vs_FR(
									_theFuselage.getFuselageFinenessRatio(), 
									_fuselageXPercentPositionPole
									),
							_theFuselage.getFusDesDatabaseReader().get_dCM_nose_vs_wshield(
									_theFuselage.getWindshieldAngle().doubleValue(NonSI.DEGREE_ANGLE),
									_theFuselage.getNoseFinenessRatio()
									),
							_theFuselage.getFusDesDatabaseReader().get_dCM_tail_vs_upsweep(
									_theFuselage.getUpsweepAngle().doubleValue(NonSI.DEGREE_ANGLE),
									_theFuselage.getTailFinenessRatio()
									)
							)
					*_fuselageSurfaceRatio
					*_theFuselage.getEquivalentDiameterCylinderGM().doubleValue(SI.METER)
					/_theWing.getMeanAerodynamicChord().doubleValue(SI.METRE)
					);
			
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
							_theFuselage.getXApexConstructionAxes(),
							_theFuselage.getFuselageLength(),
							downwashGradientRoskamConstant, 
							_theWing.getAspectRatio(),
							_theWing.getSurfacePlanform(), 
							_theWing.getPanels().get(0).getChordRoot(), 
							_theWing.getMeanAerodynamicChord(),
							_theWingAerodynamicManager.getCLAlpha().get(MethodEnum.NASA_BLACKWELL),
							_theWing.getXApexConstructionAxes(),
							wingTrailingEdgeToHTailQuarterChordDistance,
							_theWing.getAeroDatabaseReader(),
							_theFuselage.getOutlineXYSideRCurveX(),
							_theFuselage.getOutlineXYSideRCurveY()
							)
					);
		}
		
		public void gilruth() {
			
			_cMAlpha.put(
					MethodEnum.GILRUTH, 
					MomentCalc.calculateCMAlphaFuselageGilruth(
							_theFuselage.getFuselageLength(),
							_theFuselage.getSectionCylinderWidth(),
							_positionOfC4ToFuselageLength,
							_kF,
							_theWing.getSurfacePlanform(), 
							_theWing.getMeanAerodynamicChord(),
							_theWing.getXApexConstructionAxes(), 
							_theWing.getPanels().get(0).getChordRoot()
							)
					);
			
		}
		
		public void fusDes() {
			
			_cMAlpha.put(
					MethodEnum.FUSDES, 
					Amount.valueOf(
							MomentCalc.calcCMAlphaFuselage(
									_theFuselage.getFusDesDatabaseReader().get_CMa_FR_vs_FR(
											_theFuselage.getFuselageFinenessRatio(), 
											_fuselageXPercentPositionPole
											),
									_theFuselage.getFusDesDatabaseReader().get_dCMa_nose_vs_wshield(
											_theFuselage.getWindshieldAngle().doubleValue(NonSI.DEGREE_ANGLE),
											_theFuselage.getNoseFinenessRatio()
											),
									_theFuselage.getFusDesDatabaseReader().get_dCMa_tail_vs_upsweep(
											_theFuselage.getUpsweepAngle().doubleValue(NonSI.DEGREE_ANGLE),
											_theFuselage.getTailFinenessRatio()
											)
									)
							*_fuselageSurfaceRatio
							*_theFuselage.getEquivalentDiameterCylinderGM().doubleValue(SI.METER)
							/_theWing.getMeanAerodynamicChord().doubleValue(SI.METRE),
							NonSI.DEGREE_ANGLE.inverse()
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
		
		public void gilruth(Amount<Angle> alphaBody) {
			
			if(_cM0.get(MethodEnum.MULTHOPP) == null) {
				CalcCM0 calcCM0 = new CalcCM0();
				calcCM0.multhopp();
			}
			
			if(_cMAlpha.get(MethodEnum.GILRUTH) == null) {
				CalcCMAlpha calcCMAlpha = new CalcCMAlpha();
				calcCMAlpha.gilruth();
			}
			
			_cMAtAlpha.put(
					MethodEnum.GILRUTH, 
					MomentCalc.calculateCMAtAlphaFuselage(
							alphaBody, 
							_cMAlpha.get(MethodEnum.GILRUTH), 
							_cM0.get(MethodEnum.MULTHOPP)
							)
					);
		}
		
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
		
		public void fusDes(Amount<Angle> alphaBody) {
			
			if(_cM0.get(MethodEnum.FUSDES) == null) {
				CalcCM0 calcCM0 = new CalcCM0();
				calcCM0.fusDes();
			}
			
			if(_cMAlpha.get(MethodEnum.FUSDES) == null) {
				CalcCMAlpha calcCMAlpha = new CalcCMAlpha();
				calcCMAlpha.fusDes();
			}
			
			_cMAtAlpha.put(
					MethodEnum.FUSDES, 
					MomentCalc.calculateCMAtAlphaFuselage(
							alphaBody, 
							_cMAlpha.get(MethodEnum.FUSDES), 
							_cM0.get(MethodEnum.FUSDES)
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
		
		public void gilruth() {
			
			if(_cM0.get(MethodEnum.MULTHOPP) == null) {
				CalcCM0 calcCM0 = new CalcCM0();
				calcCM0.multhopp();
			}
			
			if(_cMAlpha.get(MethodEnum.GILRUTH) == null) {
				CalcCMAlpha calcCMAlpha = new CalcCMAlpha();
				calcCMAlpha.gilruth();
			}
			
			_moment3DCurve.put(
					MethodEnum.GILRUTH, 
					MyArrayUtils.convertListOfDoubleToDoubleArray(
							_alphaArray.stream()
							.map(a -> MomentCalc.calculateCMAtAlphaFuselage(
									a, 
									_cMAlpha.get(MethodEnum.GILRUTH), 
									_cM0.get(MethodEnum.MULTHOPP))
									)
							.collect(Collectors.toList())
							)
					);
		}
		
		public void fusDes() {
			
			if(_cM0.get(MethodEnum.FUSDES) == null) {
				CalcCM0 calcCM0 = new CalcCM0();
				calcCM0.fusDes();
			}
			
			if(_cMAlpha.get(MethodEnum.FUSDES) == null) {
				CalcCMAlpha calcCMAlpha = new CalcCMAlpha();
				calcCMAlpha.fusDes();
			}
			
			_moment3DCurve.put(
					MethodEnum.FUSDES, 
					MyArrayUtils.convertListOfDoubleToDoubleArray(
							_alphaArray.stream()
							.map(a -> MomentCalc.calculateCMAtAlphaFuselage(
									a, 
									_cMAlpha.get(MethodEnum.FUSDES), 
									_cM0.get(MethodEnum.FUSDES))
									)
							.collect(Collectors.toList())
							)
					);
			
		}
		
	}
	//............................................................................
	// END Calc CM@Alpha TOTAL INNER CLASS
	//............................................................................
	
	//............................................................................
	// GETTERS & SETTERS:
	//............................................................................
	public Fuselage getTheFuselage() {
		return _theFuselage;
	}

	public LiftingSurface getTheWing() {
		return _theWing;
	}

	public OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}

	public List<Amount<Angle>> getAlphaArray() {
		return _alphaArray;
	}

	public ConditionEnum getTheCondition() {
		return _theCondition;
	}

	public Double getFuselageXPercentPositionPole() {
		return _fuselageXPercentPositionPole;
	}

	public Amount<Length> getCurrentAltitude() {
		return _currentAltitude;
	}

	public Double getCurrentMachNumber() {
		return _currentMachNumber;
	}

	public Amount<Temperature> getCurrentDeltaTemperature() {
		return _currentDeltaTemperature;
	}

	public void setCurrentDeltaTemperature(Amount<Temperature> _currentDeltaTemperature) {
		this._currentDeltaTemperature = _currentDeltaTemperature;
	}

	public Double getCF() {
		return _cF;
	}

	public Amount<Area> getFuselageFrontSurface() {
		return _fuselageFrontSurface;
	}

	public Double getFuselageSurfaceRatio() {
		return _fuselageSurfaceRatio;
	}

	public Map<MethodEnum, Double> getCD0Parasite() {
		return _cD0Parasite;
	}

	public Map<MethodEnum, Double> getCD0Upsweep() {
		return _cD0Upsweep;
	}

	public Map<MethodEnum, Double> getCD0Base() {
		return _cD0Base;
	}

	public Map<MethodEnum, Double> getCD0Total() {
		return _cD0Total;
	}

	public Map<MethodEnum, Double[]> getPolar3DCurve() {
		return _polar3DCurve;
	}

	public Map<MethodEnum, Double> getCM0() {
		return _cM0;
	}

	public Map<MethodEnum, Amount<?>> getCMAlpha() {
		return _cMAlpha;
	}

	public Map<MethodEnum, Double> getCMAtAlpha() {
		return _cMAtAlpha;
	}

	public Map<MethodEnum, Double[]> getMoment3DCurve() {
		return _moment3DCurve;
	}

	public void setTheFuselage(Fuselage _theFuselage) {
		this._theFuselage = _theFuselage;
	}

	public void setTheWing(LiftingSurface _theWing) {
		this._theWing = _theWing;
	}

	public void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		this._theOperatingConditions = _theOperatingConditions;
	}

	public void setAlphaArray(List<Amount<Angle>> _alphaArray) {
		this._alphaArray = _alphaArray;
	}

	public void setTheCondition(ConditionEnum _theCondition) {
		this._theCondition = _theCondition;
	}

	public void setFuselageXPercentPositionPole(Double _fuselageXPercentPositionPole) {
		this._fuselageXPercentPositionPole = _fuselageXPercentPositionPole;
	}

	public void setCurrentAltitude(Amount<Length> _currentAltitude) {
		this._currentAltitude = _currentAltitude;
	}

	public void setCurrentMachNumber(Double _currentMachNumber) {
		this._currentMachNumber = _currentMachNumber;
	}

	public void setCF(Double _cF) {
		this._cF = _cF;
	}

	public void setFuselageFrontSurface(Amount<Area> _fuselageFrontSurface) {
		this._fuselageFrontSurface = _fuselageFrontSurface;
	}

	public void setFuselageSurfaceRatio(Double _fuselageSurfaceRatio) {
		this._fuselageSurfaceRatio = _fuselageSurfaceRatio;
	}

	public void setCD0Parasite(Map<MethodEnum, Double> _cD0Parasite) {
		this._cD0Parasite = _cD0Parasite;
	}

	public void setCD0Upsweep(Map<MethodEnum, Double> _cD0Upsweep) {
		this._cD0Upsweep = _cD0Upsweep;
	}

	public Map<MethodEnum, Double> getCD0Windshield() {
		return _cD0Windshield;
	}

	public void setCD0Windshield(Map<MethodEnum, Double> _cD0Windshield) {
		this._cD0Windshield = _cD0Windshield;
	}

	public void setCD0Base(Map<MethodEnum, Double> _cD0Base) {
		this._cD0Base = _cD0Base;
	}

	public void setCD0Total(Map<MethodEnum, Double> _cD0Total) {
		this._cD0Total = _cD0Total;
	}

	public void setPolar3DCurve(Map<MethodEnum, Double[]> _polar3DCurve) {
		this._polar3DCurve = _polar3DCurve;
	}

	public void setCM0(Map<MethodEnum, Double> _cM0) {
		this._cM0 = _cM0;
	}

	public void setCMAlpha(Map<MethodEnum, Amount<?>> _cMAlpha) {
		this._cMAlpha = _cMAlpha;
	}

	public void setCMAtAlpha(Map<MethodEnum, Double> _cMAtAlpha) {
		this._cMAtAlpha = _cMAtAlpha;
	}

	public void setMoment3DCurve(Map<MethodEnum, Double[]> _moment3DCurve) {
		this._moment3DCurve = _moment3DCurve;
	}

	public Amount<Length> getEquivalentDiameterBase() {
		return _equivalentDiameterBase;
	}

	public void setEquivalentDiameterBase(Amount<Length> _equivalentDiameterBase) {
		this._equivalentDiameterBase = _equivalentDiameterBase;
	}

	public double[] getPositionOfC4ToFuselageLength() {
		return _positionOfC4ToFuselageLength;
	}

	public double[] getKF() {
		return _kF;
	}

	public LiftingSurfaceAerodynamicsManager getTheWingAerodynamicManager() {
		return _theWingAerodynamicManager;
	}

	public void setTheWingAerodynamicManager(LiftingSurfaceAerodynamicsManager _theWingAerodynamicManager) {
		this._theWingAerodynamicManager = _theWingAerodynamicManager;
	}

	public Map<MethodEnum, Double> getCDAtAlpha() {
		return _cDAtAlpha;
	}

	public void setCDAtAlpha(Map<MethodEnum, Double> _cDAtAlpha) {
		this._cDAtAlpha = _cDAtAlpha;
	}

	public Map<MethodEnum, Double> getCDInduced() {
		return _cDInduced;
	}

	public void setCDInduced(Map<MethodEnum, Double> _cDInduced) {
		this._cDInduced = _cDInduced;
	}
}
