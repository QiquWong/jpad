package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Base;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Parasite;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Total;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Upsweep;
import analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Windshield;
import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager.CalcAlpha0L;
import analyses.liftingsurface.LSAerodynamicsManager.CalcAlphaStall;
import analyses.liftingsurface.LSAerodynamicsManager.CalcAlphaStar;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCD0;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCDAtAlpha;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCDInduced;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCDWave;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCL0;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLAlpha;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLAtAlpha;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLAtAlphaHighLift;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLStar;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLmax;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCMAlpha;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCMAtAlpha;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCMac;
import analyses.liftingsurface.LSAerodynamicsManager.CalcDragDistributions;
import analyses.liftingsurface.LSAerodynamicsManager.CalcHighLiftCurve;
import analyses.liftingsurface.LSAerodynamicsManager.CalcHighLiftDevicesEffects;
import analyses.liftingsurface.LSAerodynamicsManager.CalcLiftCurve;
import analyses.liftingsurface.LSAerodynamicsManager.CalcLiftDistributions;
import analyses.liftingsurface.LSAerodynamicsManager.CalcMachCr;
import analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve;
import analyses.liftingsurface.LSAerodynamicsManager.CalcMomentDistribution;
import analyses.liftingsurface.LSAerodynamicsManager.CalcOswaldFactor;
import analyses.liftingsurface.LSAerodynamicsManager.CalcPolar;
import analyses.liftingsurface.LSAerodynamicsManager.CalcXAC;
import analyses.nacelles.NacelleAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.stability.StabilityCalculators;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXLSUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

/**
 * Evaluate and store aerodynamic parameters relative to the whole aircraft.
 * Calculations are handled through static libraries which are properly called in this class;
 * other methods are instead used to get the aerodynamic parameters from each component
 * in order to obtain quantities relative to the whole aircraft.
 */ 

public class ACAerodynamicCalculator {

	/*
	 *******************************************************************************
	 * THIS CLASS IS A PROTOTYPE OF THE NEW ACAerodynamicsManager (WORK IN PROGRESS)
	 * 
	 * @author Vittorio Trifari, Manuela Ruocco, Agostino De Marco
	 *******************************************************************************
	 */

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	IACAerodynamicCalculator _theAerodynamicBuilderInterface;

	//..............................................................................
	// DERIVED INPUT	
	private Double _currentMachNumber;
	private Amount<Length> _currentAltitude;
	private Amount<Angle> _alphaBodyCurrent;
	private Amount<Angle> _alphaWingCurrent;
	private Amount<Angle> _alphaHTailCurrent;
	private Amount<Angle> _betaVTailCurrent;
	private Amount<Angle> _alphaNacelleCurrent;
	Double _landingGearUsedDrag = null;
	private List<Amount<Angle>> _deltaEForEquilibrium = new ArrayList<>();
	private List<Double> _horizontalTailEquilibriumCoefficient= new ArrayList<>();
	
	// for downwash estimation
	private Amount<Length> _zACRootWing;
	private Amount<Length> _horizontalDistanceQuarterChordWingHTail;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailPARTIAL;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE;

	// for discretized airfoil Cl along sempispan
	private List<List<Double>> _discretizedWingAirfoilsCl;

	// for discretized airfoil Cd along sempispan
	private List<List<Double>> _discretizedWingAirfoilsCd;

	// for discretized airfoil Cm along sempispan
	private List<List<Double>> _discretizedWingAirfoilsCm;

	// for discretized airfoil Cl along sempispan
	private List<List<Double>> _discretizedHTailAirfoilsCl;

	// for discretized airfoil Cd along sempispan
	private List<List<Double>> _discretizedHTailAirfoilsCd;

	// Global
	private List<Amount<Angle>> _alphaBodyList;
	private List<Amount<Angle>> _alphaWingList;
	private List<Amount<Angle>> _alphaHTailList;
	private List<Amount<Angle>> _betaList;

	//..............................................................................
	// INNER CALCULATORS
	private Map<ComponentEnum, LSAerodynamicsManager> _liftingSurfaceAerodynamicManagers;
	private Map<ComponentEnum, FuselageAerodynamicsManager> _fuselageAerodynamicManagers;
	private Map<ComponentEnum, NacelleAerodynamicsManager>_nacelleAerodynamicManagers;

	//..............................................................................
	// OUTPUT
	private Map<Amount<Angle>, List<Double>> _hTail3DLiftCurvesElevator;
	
	// Methods are always the same used for the wing Lift curve. If the wing lift curve is not required, the use method is Nasa Blackwell
	private Amount<?> _clAlphaWingFuselage;
	private Double _clZeroWingFuselage;
	private Double _clMaxWingFuselage;
	private Double _clStarWingFuselage;
	private Amount<Angle> _alphaStarWingFuselage;
	private Amount<Angle> _alphaStallWingFuselage;
	private Amount<Angle> _alphaZeroLiftWingFuselage;

	private List<Double> _current3DWingLiftCurve;
	private List<Double> _current3DWingPolarCurve;
	private List<Double> _current3DWingMomentCurve;
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailLiftCurve;
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailPolarCurve;
	private List<Double> _current3DHorizontalTailMomentCurve;
	private Double _current3DVerticalTailDragCoefficient;
	private List<Double> _deltaCDElevatorList;
	private Map<Amount<Angle>, List<Double>> _3DHorizontalTailPolarCurveForElevatorDeflection;

	Map<MethodEnum, List<Amount<Length>>> _verticalDistanceZeroLiftDirectionWingHTailVariable;
	private Map<Boolean, Map<MethodEnum, List<Double>>> _downwashGradientMap;
	private Map<Boolean, Map<MethodEnum, List<Amount<Angle>>>> _downwashAngleMap;
	private List<Tuple3<MethodEnum, Double, Double>> _buffetBarrierCurve = new ArrayList<>();
	private Map<MethodEnum, List<Tuple2<Double, Double>>> _cNbFuselage = new HashMap<>();
	private Map<MethodEnum, List<Tuple2<Double, Double>>> _cNbVertical = new HashMap<>();
	private Map<MethodEnum, List<Tuple2<Double, Double>>> _cNbWing = new HashMap<>();
	private Map<MethodEnum, List<Tuple2<Double, Double>>> _cNbTotal = new HashMap<>();
	private Map<MethodEnum, Map<Amount<Angle>, List<Tuple2<Double, Double>>>> _cNdr = new HashMap<>();
	private Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNFuselage = new HashMap<>();
	private Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNVertical = new HashMap<>();
	private Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNWing = new HashMap<>();
	private Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNTotal = new HashMap<>();
	private Map<MethodEnum, Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>>> _cNDueToDeltaRudder = new HashMap<>();
	private Map<MethodEnum, Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>>> _betaOfEquilibrium = new HashMap<>();

	//Longitudinal Static Stability Output

	private Map<Double, Map<Amount<Angle>, List<Double>>> _totalMomentCoefficient = new HashMap<>(); //xcg, delta e , CM
	private Map<Amount<Angle>, List<Double>> _totalLiftCoefficient = new HashMap<>(); //delta e, CL
	private Map<Amount<Angle>, List<Double>> _totalDragCoefficient = new HashMap<>(); //delta e, CD
	private Map<Double, List<Double>> _horizontalTailEquilibriumLiftCoefficient = new HashMap<>(); //xcg, CLh
	private Map<Double, List<Double>> _totalEquilibriumLiftCoefficient = new HashMap<>(); //xcg, CL
	private Map<Double, List<Double>> _totalEquilibriumDragCoefficient = new HashMap<>(); //xcg, CL
	private Map<Double, List<Amount<Angle>>> _deltaEEquilibrium = new HashMap<>(); //xcg

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeAnalysis() {

		_downwashGradientMap = new HashMap<>();
		_downwashAngleMap = new HashMap<>();
		_verticalDistanceZeroLiftDirectionWingHTailVariable = new HashMap<>();
		_discretizedWingAirfoilsCl = new ArrayList<List<Double>>();
		_discretizedWingAirfoilsCd = new ArrayList<List<Double>>();
		_discretizedWingAirfoilsCm = new ArrayList<List<Double>>();
		_discretizedHTailAirfoilsCl = new ArrayList<List<Double>>();
		_discretizedHTailAirfoilsCd = new ArrayList<List<Double>>();

		//set current Mach number

		switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
		case TAKE_OFF:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachTakeOff();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeTakeOff();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCurrentTakeOff();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaCurrentTakeOff();
			break;
		case CLIMB:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachClimb();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeClimb();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCurrentClimb();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaCurrentClimb();
			break;
		case CRUISE:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachCruise();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeCruise();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCurrentCruise();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaCurrentCruise();
			break;
		case LANDING:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachLanding();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeLanding();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCurrentLanding();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaCurrentLanding();
			break;
		default:
			break;
		}

		calculateComponentsData();
		initializeData();
		initializeArrays();
		calculateWingHorizontalTailAndNacelleCurrentAlpha();
		
	}

	private void initializeData() {

		//...................................................................................
		// DISTANCE BETWEEN WING VORTEX PLANE AND THE AERODYNAMIC CENTER OF THE HTAIL
		//...................................................................................
		switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
		case TAKE_OFF:
			_landingGearUsedDrag = _theAerodynamicBuilderInterface.getLandingGearDragCoefficient();
			break;
		case CLIMB:
			_landingGearUsedDrag = 0.0;
			break;
		case CRUISE:
			_landingGearUsedDrag = 0.0;
			break;
		case LANDING:
			_landingGearUsedDrag = _theAerodynamicBuilderInterface.getLandingGearDragCoefficient();
			break;
		}
		
		_zACRootWing = Amount.valueOf(
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				- (
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXAcAirfoilVsY().get(0)*
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(0).doubleValue(SI.METER)*
						Math.tan(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN))
						),
				SI.METER
				);

		//Horizontal and vertical distance
		_horizontalDistanceQuarterChordWingHTail = Amount.valueOf(
				(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER)
						+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(0).doubleValue(SI.METER)/4) - 
				(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)
						+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(0).doubleValue(SI.METER)/4),
				SI.METER
				);

		if ( (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) 
				|| (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) ) {

			this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
					_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
					- this._zACRootWing.doubleValue(SI.METER),
					SI.METER
					);

		}

		if ( (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) 
				|| (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) ) { // different sides

			if(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ){

				this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
						+ Math.abs(this._zACRootWing.doubleValue(SI.METER)),
						SI.METER
						);

			}

			if(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ){
				this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
						-( Math.abs(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER)) 
								+ this._zACRootWing.doubleValue(SI.METER)
								),
						SI.METER
						);	
			}
		}

		// the horizontal distance is always the same, the vertical changes in function of the angle of attack.

		if (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				< _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER)
				){

			_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = 
					Amount.valueOf(
							_verticalDistanceZeroLiftDirectionWingHTailPARTIAL.doubleValue(SI.METER) + (
									(_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
											Math.tan(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN) -
													_theAerodynamicBuilderInterface.getTheAircraft().getWing()
													.getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition())
													.getAlphaZeroLift().get(
															_theAerodynamicBuilderInterface.getComponentTaskList()
															.get(ComponentEnum.WING)
															.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
															)
													.doubleValue(SI.RADIAN)
													)
											)
									),
							SI.METER
							);
		}

		if (_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				> _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
				) {

			this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = Amount.valueOf(
					this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL.doubleValue(SI.METER) - (
							(this._horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
									Math.tan(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN) -
											_theAerodynamicBuilderInterface.getTheAircraft().getWing()
											.getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition())
											.getAlphaZeroLift().get(
													_theAerodynamicBuilderInterface.getComponentTaskList()
													.get(ComponentEnum.WING)
													.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
													)
											.doubleValue(SI.RADIAN)
											)
									)
							),
					SI.METER);
		}


		this._verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE = Amount.valueOf(
				this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE.doubleValue(SI.METER) * 
				Math.cos(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(SI.RADIAN) -
						_theAerodynamicBuilderInterface.getTheAircraft().getWing()
						.getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition())
						.getAlphaZeroLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								)
						.doubleValue(SI.RADIAN)
						),
				SI.METER);
		
		_deltaEForEquilibrium = MyArrayUtils.convertDoubleArrayToListOfAmount((MyArrayUtils.linspaceDouble(-45, 10, 20)), NonSI.DEGREE_ANGLE); 
	}

	private void initializeArrays() {

		/////////////////////////////////////////////////////////////////////////////////////
		// ALPHA BODY ARRAY
		_alphaBodyList = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						_theAerodynamicBuilderInterface.getAlphaBodyInitial().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getAlphaBodyFinal().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getNumberOfAlphasBody()),
				NonSI.DEGREE_ANGLE
				);

		/////////////////////////////////////////////////////////////////////////////////////
		// ALPHA WING ARRAY CLEAN
		_alphaWingList = _alphaBodyList.stream()
				.map(x -> x.to(NonSI.DEGREE_ANGLE).plus(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE))
						)
				.collect(Collectors.toList());  

		/////////////////////////////////////////////////////////////////////////////////////
		// DOWNWASH ARRAY 
		//...................................................................................
		// ROSKAM (constant gradient)
		//...................................................................................		
		// calculate cl alpha at M=0
		Amount<Length> altitude = Amount.valueOf(0.0, SI.METER);
		if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
			altitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeTakeOff();
		else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.CLIMB))
			altitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeClimb();
		else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.CRUISE))
			altitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeCruise();
		else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
			altitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeLanding();

		double cLAlphaMachZero = LiftCalc.calculateCLAlphaAtMachNasaBlackwell(
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition()).getYStationDistribution(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition()).getChordDistribution(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition()).getXLEDistribution(), 
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition()).getDihedralDistribution(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition()).getTwistDistribution(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition()).getAlphaZeroLiftDistribution(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTheAerodynamicsCalculatorMap().get(_theAerodynamicBuilderInterface.getCurrentCondition()).getVortexSemiSpanToSemiSpanRatio(),
				0.0,
				altitude
				);

		// Roskam method
		List<Double> downwashGradientConstantList = new ArrayList<>();
		for(int i=0; i<_alphaBodyList.size(); i++)
			downwashGradientConstantList.add(
					AerodynamicCalc.calculateDownwashRoskamWithMachEffect(
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTaperRatioEquivalent(), 
							_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) / _theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER), 
							_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE.doubleValue(SI.METER) / _theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER), 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
							cLAlphaMachZero, 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing()
							.getTheAerodynamicsCalculatorMap()
							.get(_theAerodynamicBuilderInterface.getCurrentCondition())
							.getCLAlpha().get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.CL_ALPHA)
									).to(SI.RADIAN.inverse()).getEstimatedValue()
							)
					);

		Map<MethodEnum, List<Double>> downwashGradientConstant = new HashMap<>();
		downwashGradientConstant.put(
				MethodEnum.ROSKAM,
				downwashGradientConstantList
				);

		double epsilonZeroRoskam = - _downwashGradientMap.get(Boolean.TRUE).get(MethodEnum.ROSKAM).get(0) 
				* _theAerodynamicBuilderInterface.getTheAircraft().getWing()
				.getTheAerodynamicsCalculatorMap()
				.get(_theAerodynamicBuilderInterface.getCurrentCondition())
				.getAlphaZeroLift()
				.get(
						_theAerodynamicBuilderInterface.getComponentTaskList()
						.get(ComponentEnum.WING)
						.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
						).doubleValue(NonSI.DEGREE_ANGLE);

		List<Amount<Angle>> downwashAngleConstantList = new ArrayList<>();
		for (int i=0; i<this._theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++)
			downwashAngleConstantList.add(
					Amount.valueOf(
							epsilonZeroRoskam 
							+ _downwashGradientMap.get(Boolean.TRUE).get(MethodEnum.ROSKAM).get(0)
							* _alphaWingList.get(i).doubleValue(NonSI.DEGREE_ANGLE),
							NonSI.DEGREE_ANGLE
							)	
					);

		Map<MethodEnum, List<Amount<Angle>>> downwashAngleConstant = new HashMap<>();
		downwashAngleConstant.put(
				MethodEnum.ROSKAM,
				downwashAngleConstantList
				);

		//...................................................................................
		// SLINGERLAND (constant gradient)
		//...................................................................................
		for (int i=0; i<this._theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++){
			double cl = 
					_theAerodynamicBuilderInterface.getTheAircraft().getWing()
					.getTheAerodynamicsCalculatorMap()
					.get(_theAerodynamicBuilderInterface.getCurrentCondition())
					.getCLAlpha()
					.get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.WING)
							.get(AerodynamicAndStabilityEnum.CL_ALPHA)
							)
					.to(NonSI.DEGREE_ANGLE.inverse())
					.getEstimatedValue() 
					* _alphaWingList.get(i).doubleValue(NonSI.DEGREE_ANGLE) 
					+ _theAerodynamicBuilderInterface.getTheAircraft().getWing()
					.getTheAerodynamicsCalculatorMap()
					.get(_theAerodynamicBuilderInterface.getCurrentCondition())
					.getCLZero()
					.get(
							_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.WING)
							.get(AerodynamicAndStabilityEnum.CL_ZERO)
							);

			downwashAngleConstantList.clear();
			downwashAngleConstantList.add(
					AerodynamicCalc.calculateDownwashAngleLinearSlingerland(
							_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER), 
							_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE.doubleValue(SI.METER), 
							cl, 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan()
							).to(NonSI.DEGREE_ANGLE)
					);
		}

		downwashAngleConstant.put(
				MethodEnum.SLINGERLAND,
				downwashAngleConstantList
				);


		downwashGradientConstant.put(
				MethodEnum.SLINGERLAND,
				MyArrayUtils.convertDoubleArrayToListDouble(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyMathUtils.calculateArrayFirstDerivative(
										MyArrayUtils.convertListOfAmountTodoubleArray(_alphaBodyList),
										MyArrayUtils.convertListOfAmountTodoubleArray(
												downwashAngleConstant
												.get(MethodEnum.SLINGERLAND)
												)
										)
								)
						)
				);

		//.....................................................................................
		// Filling the global maps ...
		_downwashGradientMap.put(Boolean.TRUE, downwashGradientConstant);
		_downwashAngleMap.put(Boolean.TRUE, downwashAngleConstant);

		//...................................................................................
		// ROSKAM (non linear gradient)
		//...................................................................................		

		Map<MethodEnum, List<Double>> downwashGradientNonLinear = new HashMap<>();

		downwashGradientNonLinear.put(
				MethodEnum.ROSKAM,
				AerodynamicCalc.calculateVariableDownwashGradientRoskamWithMachEffect(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTaperRatioEquivalent(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing()
						.getTheAerodynamicsCalculatorMap()
						.get(_theAerodynamicBuilderInterface.getCurrentCondition())
						.getAlphaZeroLift()
						.get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								),
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
						cLAlphaMachZero,
						_theAerodynamicBuilderInterface.getTheAircraft().getWing()
						.getTheAerodynamicsCalculatorMap()
						.get(_theAerodynamicBuilderInterface.getCurrentCondition())
						.getCLAlpha()
						.get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.CL_ALPHA)
								)
						.to(SI.RADIAN.inverse())
						.getEstimatedValue(), 
						_alphaBodyList
						)
				);

		Map<MethodEnum, List<Amount<Angle>>> downwashAngleNonLinear = new HashMap<>();

		downwashAngleNonLinear.put(
				MethodEnum.ROSKAM,
				AerodynamicCalc.calculateDownwashAngleFromDownwashGradient(
						downwashGradientNonLinear.get(MethodEnum.ROSKAM),
						_alphaBodyList,
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing()
						.getTheAerodynamicsCalculatorMap()
						.get(_theAerodynamicBuilderInterface.getCurrentCondition())
						.getAlphaZeroLift()
						.get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								)
						)
				);

		_verticalDistanceZeroLiftDirectionWingHTailVariable.put(
				MethodEnum.ROSKAM,
				AerodynamicCalc.calculateVortexPlaneHorizontalTailVerticalDistance(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing()
						.getTheAerodynamicsCalculatorMap()
						.get(_theAerodynamicBuilderInterface.getCurrentCondition())
						.getAlphaZeroLift()
						.get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								),
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						_alphaBodyList, 
						downwashAngleNonLinear.get(MethodEnum.ROSKAM)
						)	
				);

		//...................................................................................
		// SLINGERLAND (non linear gradient)
		//...................................................................................
		downwashAngleNonLinear.put(
				MethodEnum.SLINGERLAND,
				AerodynamicCalc.calculateDownwashAngleNonLinearSlingerland(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing()
						.getTheAerodynamicsCalculatorMap()
						.get(_theAerodynamicBuilderInterface.getCurrentCondition())
						.getAlphaZeroLift()
						.get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(), 
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						MyArrayUtils.convertToDoublePrimitive(
								_theAerodynamicBuilderInterface.getTheAircraft().getWing()
								.getTheAerodynamicsCalculatorMap()
								.get(_theAerodynamicBuilderInterface.getCurrentCondition())
								.getLiftCoefficient3DCurve()
								.get(
										_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.WING)
										.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
										)
								),
						MyArrayUtils.convertListOfAmountTodoubleArray(
								_alphaWingList.stream()
								.map(x -> x.to(NonSI.DEGREE_ANGLE))
								.collect(Collectors.toList())
								),
						MyArrayUtils.convertToDoublePrimitive(MyArrayUtils.convertListOfAmountToDoubleArray(
								_alphaBodyList.stream()
								.map(x -> x.to(NonSI.DEGREE_ANGLE))
								.collect(Collectors.toList())
								))
						)
				);

		downwashGradientNonLinear.put(
				MethodEnum.SLINGERLAND,
				MyArrayUtils.convertDoubleArrayToListDouble(
						MyArrayUtils.convertFromDoubleToPrimitive(
								MyMathUtils.calculateArrayFirstDerivative(
										MyArrayUtils.convertListOfAmountTodoubleArray(_alphaBodyList),
										MyArrayUtils.convertListOfAmountTodoubleArray(
												downwashAngleNonLinear
												.get(MethodEnum.SLINGERLAND)
												)
										)
								)
						)
				);

		_verticalDistanceZeroLiftDirectionWingHTailVariable.put(
				MethodEnum.SLINGERLAND,
				AerodynamicCalc.calculateVortexPlaneHorizontalTailVerticalDistance(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing()
						.getTheAerodynamicsCalculatorMap()
						.get(_theAerodynamicBuilderInterface.getCurrentCondition())
						.getAlphaZeroLift()
						.get(
								_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								),
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						_alphaBodyList, 
						downwashAngleNonLinear.get(MethodEnum.SLINGERLAND)
						)	
				);

		// Filling the global maps ...
		_downwashGradientMap.put(Boolean.FALSE, downwashGradientNonLinear);
		_downwashAngleMap.put(Boolean.FALSE, downwashAngleNonLinear);

		/////////////////////////////////////////////////////////////////////////////////////
		// ALPHA HTAIL ARRAY 
		if (_theAerodynamicBuilderInterface.getDownwashConstant() == Boolean.TRUE){
			_alphaHTailList = new ArrayList<>();
			for (int i=0; i<_theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++){
				_alphaHTailList.add(
						Amount.valueOf(
								_alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
								- _downwashAngleMap
								.get(Boolean.TRUE)
								.get(
										_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.AIRCRAFT)
										.get(AerodynamicAndStabilityEnum.DOWNWASH)
										)
								.get(i)
								.doubleValue(NonSI.DEGREE_ANGLE)
								+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
								NonSI.DEGREE_ANGLE
								)
						);
			}
		}
		if (_theAerodynamicBuilderInterface.getDownwashConstant() == Boolean.FALSE){
			_alphaHTailList = new ArrayList<>();
			for (int i=0; i<_theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++){
				_alphaHTailList.add(
						Amount.valueOf(
								_alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
								- _downwashAngleMap
								.get(Boolean.FALSE)
								.get(
										_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.AIRCRAFT)
										.get(AerodynamicAndStabilityEnum.DOWNWASH)
										)
								.get(i)
								.doubleValue(NonSI.DEGREE_ANGLE)
								+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
								NonSI.DEGREE_ANGLE
								)
						);
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////
		// BETA ARRAY
		_betaList = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						_theAerodynamicBuilderInterface.getBetaInitial().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getBetaFinal().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getNumberOfBeta()),
				NonSI.DEGREE_ANGLE
				);
	}

	private void calculateComponentsData() {

		/*
		 * THIS WILL CREATE ALL THE COMPONENTS MANAGERS 
		 * AND RUN ALL THE REQUIRED COMPONENTS CALCULATIONS
		 * 
		 * TODO: HAVE A LOOK ON THE WING LIFT CURVE CHECKS AND IMPLEMENT THE POSSIBILITY TO RECOGNIZE IF 
		 *       THE CURVE HAS BEEN ASSIGNED FROM THE INPUT OR NOT. (THE SAME FOR HTAIL, VTAIL, FUSELAGE AND 
		 *       NACELLE CURVES OF CL, CD OR CM).
		 *       THE CHECK SHOULD BE PERFORMED UPON THE EXISTENCE OR NOT OF THE SPECIFIC INTERPOLATING FUNCTION 
		 */

		//========================================================================================================================
		// WING
		if(_theAerodynamicBuilderInterface.getTheAircraft().getWing() != null) {

			_liftingSurfaceAerodynamicManagers.put(
					ComponentEnum.WING,
					new LSAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getWing(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(),
							_theAerodynamicBuilderInterface.getWingNumberOfPointSemiSpanWise(),
							_alphaWingList, 
							_theAerodynamicBuilderInterface.getAlphaWingForDistribution(),
							_theAerodynamicBuilderInterface.getWingMomentumPole()
							)
					);

			//.........................................................................................................................
			//	CRITICAL_MACH
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {

				CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAtAlpha();

				CalcMachCr calcMachCr = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcMachCr();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
				case KORN_MASON:
					calcMachCr.kornMason(calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaWingCurrent));
					break;
				case KROO:
					calcMachCr.kroo(calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaWingCurrent));
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	AERODYNAMIC_CENTER
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {

				CalcXAC calcXAC = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcXAC();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {
				case QUARTER:
					calcXAC.atQuarterMAC();
					break;
				case DEYOUNG_HARPER:
					calcXAC.deYoungHarper();
					break;
				case NAPOLITANO_DATCOM:
					calcXAC.datcomNapolitano();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CL_AT_ALPHA
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {

				CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
				case LINEAR_DLR:
					calcCLAtAlpha.linearDLR(getAlphaWingCurrent());
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCLAtAlpha.linearAndersonCompressibleSubsonic(getAlphaWingCurrent());
					break;
				case LINEAR_NASA_BLACKWELL:
					calcCLAtAlpha.nasaBlackwellLinear(getAlphaWingCurrent());
					break;
				case NASA_BLACKWELL:
					calcCLAtAlpha.nasaBlackwellCompleteCurve(getAlphaWingCurrent());
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CL_ALPHA
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {

				CalcCLAlpha calcCLAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ALPHA)) {
				case INTEGRAL_MEAN:
					calcCLAlpha.integralMean2D();
					break;
				case POLHAMUS:
					calcCLAlpha.polhamus();
					break;
				case NASA_BLACKWELL:
					calcCLAlpha.nasaBlackwell();
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCLAlpha.andersonSweptCompressibleSubsonic();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CL_ZERO
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {

				CalcCL0 calcCL0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCL0();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ZERO)) {
				case NASA_BLACKWELL:
					calcCL0.nasaBlackwell();
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCL0.andersonSweptCompressibleSubsonic();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CL_STAR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_STAR)) {

				CalcCLStar calcCLStar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLStar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_STAR)) {
				case NASA_BLACKWELL:
					calcCLStar.nasaBlackwell();
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCLStar.andersonSweptCompressibleSubsonic();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CL_MAX
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_MAX)) {

				CalcCLmax calcCLmax = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLmax();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_MAX)) {
				case NASA_BLACKWELL:
					calcCLmax.nasaBlackwell();
					break;
				case PHILLIPS_ALLEY:
					calcCLmax.phillipsAndAlley();
					break;
				case ROSKAM:
					calcCLmax.roskam();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	ALPHA_ZERO_LIFT
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {

				CalcAlpha0L calcAlpha0L = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcAlpha0L();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
				case INTEGRAL_MEAN_NO_TWIST:
					calcAlpha0L.integralMeanNoTwist();
					break;
				case INTEGRAL_MEAN_TWIST:
					calcAlpha0L.integralMeanWithTwist();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	ALPHA_STAR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_STAR)) {

				CalcAlphaStar calcAlphaStar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcAlphaStar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_STAR)) {
				case MEAN_AIRFOIL_INFLUENCE_AREAS:
					calcAlphaStar.meanAirfoilWithInfluenceAreas();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	ALPHA_STALL
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_STALL)) {

				CalcAlphaStall calcAlphaStall = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcAlphaStall();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_STALL)) {
				case PHILLIPS_ALLEY:
					calcAlphaStall.fromCLmaxPhillipsAndAlley();
					break;
				case NASA_BLACKWELL:
					calcAlphaStall.fromAlphaMaxLinearNasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	LIFT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

				CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
				case PHILLIPS_ALLEY:
					calcLiftCurve.fromCLmaxPhillipsAndAlley();
					break;
				case NASA_BLACKWELL:
					calcLiftCurve.nasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	LIFT_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

				CalcLiftDistributions calcLiftDistributions = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftDistributions();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {
				case SCHRENK:
					calcLiftDistributions.schrenk();
					break;
				case NASA_BLACKWELL:
					calcLiftDistributions.nasaBlackwell();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD0
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD0)) {

				CalcCD0 calcCD0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCD0();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD0)) {
				case SEMIEMPIRICAL:
					calcCD0.semiempirical(_currentMachNumber, _currentAltitude);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_INDUCED
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {

				CalcCDInduced calcCDInduced = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCDInduced();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {
				case GROSU:
					calcCDInduced.grosu(_alphaWingCurrent);
					break;
				case HOWE:
					calcCDInduced.howe(_alphaWingCurrent);
					break;
				case RAYMER:
					calcCDInduced.raymer(_alphaWingCurrent);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_WAVE
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_WAVE)) {

				CalcCDWave calcCDWave = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCDWave();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_WAVE)) {
				case LOCK_KORN_WITH_KORN_MASON:
					calcCDWave.lockKornWithKornMason();
					break;
				case LOCK_KORN_WITH_KROO:
					calcCDWave.lockKornWithKroo();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	OSWALD_FACTOR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {

				CalcOswaldFactor calcOswaldFactor = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcOswaldFactor();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {
				case GROSU:
					calcOswaldFactor.grosu(_alphaWingCurrent);
					break;
				case HOWE:
					calcOswaldFactor.howe();
					break;
				case RAYMER:
					calcOswaldFactor.raymer();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	POLAR_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				analyses.liftingsurface.LSAerodynamicsManager.CalcPolar calcPolar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcPolar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {
				case SEMIEMPIRICAL:
					calcPolar.semiempirical(_currentMachNumber, _currentAltitude);
					break;
				case AIRFOIL_DISTRIBUTION:
					calcPolar.fromCdDistribution(_currentMachNumber, _currentAltitude);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	DRAG_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

				CalcDragDistributions calcDragDistributions = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcDragDistributions();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {
				case NASA_BLACKWELL:
					calcDragDistributions.nasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {

				CalcCDAtAlpha calcCDAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCDAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCDAtAlpha.fromCdDistribution(
							_alphaWingCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				case SEMIEMPIRICAL:
					calcCDAtAlpha.semiempirical(
							_alphaWingCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.................................................................................................................................
			if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF) 
					|| _theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING)) {

				//.........................................................................................................................
				//	HIGH_LIFT_DEVICES_EFFECTS
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {

					CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
					case SEMIEMPIRICAL:
						if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
							calcHighLiftDevicesEffects.semiempirical(
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
									_currentMachNumber
									);
						else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
							calcHighLiftDevicesEffects.semiempirical(
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
									_currentMachNumber
									);
						break;
					default:
						break;
					}
				}

				//.........................................................................................................................
				//	HIGH_LIFT_CURVE_3D

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

					CalcHighLiftCurve calcHighLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftCurve();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {
					case SEMIEMPIRICAL:
						if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
							calcHighLiftCurve.semiempirical(
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
									_currentMachNumber, 
									_currentAltitude
									);
						else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
							calcHighLiftCurve.semiempirical(
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
									_currentMachNumber, 
									_currentAltitude
									);
						break;
					default:
						break;
					}
				}

				//.........................................................................................................................
				//	CL_AT_ALPHA_HIGH_LIFT 
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {

					CalcCLAtAlphaHighLift calcCLAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAtAlphaHighLift();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {
					case SEMIEMPIRICAL:
						if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
							calcCLAtAlphaHighLift.semiempirical(
									_alphaWingCurrent,
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
									_currentMachNumber, 
									_currentAltitude
									);
						else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
							calcCLAtAlphaHighLift.semiempirical(
									_alphaWingCurrent,
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
									_currentMachNumber, 
									_currentAltitude
									);
						break;
					default:
						break;
					}
				}

				//.........................................................................................................................
				//	CD_AT_ALPHA_HIGH_LIFT 
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {

					/*
					 * CD@Alpha + Delta CD0 from High Lift Devices Effects
					 */

					CalcCDAtAlpha calcCDAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCDAtAlpha();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
					case AIRFOIL_DISTRIBUTION:
						calcCDAtAlpha.fromCdDistribution(
								_alphaWingCurrent, 
								_currentMachNumber,
								_currentAltitude
								);
						break;
					case SEMIEMPIRICAL:
						calcCDAtAlpha.semiempirical(
								_alphaWingCurrent, 
								_currentMachNumber,
								_currentAltitude
								);
						break;
					default:
						break;
					}

					CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
					case SEMIEMPIRICAL:
						if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
							calcHighLiftDevicesEffects.semiempirical(
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
									_currentMachNumber
									);
						else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
							calcHighLiftDevicesEffects.semiempirical(
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
									_currentMachNumber
									);
						break;
					default:
						break;
					}

				}

				//.........................................................................................................................
				//	CM_AT_ALPHA_HIGH_LIFT 
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {

					/*
					 * CM@Alpha + Delta CMac from High Lift Devices Effects
					 */

					CalcCMAtAlpha calcCMAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCMAtAlpha();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
					case AIRFOIL_DISTRIBUTION:
						calcCMAtAlpha.fromAirfoilDistribution(_alphaWingCurrent);
						break;
					default:
						break;
					}

					CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
					case SEMIEMPIRICAL:
						if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
							calcHighLiftDevicesEffects.semiempirical(
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
									_currentMachNumber
									);
						else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
							calcHighLiftDevicesEffects.semiempirical(
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
									_currentMachNumber
									);
						break;
					default:
						break;
					}

				}

			}
			//.........................................................................................................................
			//	CM_AC
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {

				CalcCMac calcCMac = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCMac();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {
				case BASIC_AND_ADDITIONAL:
					calcCMac.basicAndAdditionalContribution();
					break;
				case AIRFOIL_DISTRIBUTION:
					calcCMac.fromAirfoilDistribution();
					break;
				case INTEGRAL_MEAN:
					calcCMac.integralMean();
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_ALPHA
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {

				CalcCMAlpha calcCMAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCMAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCMAlpha.andersonSweptCompressibleSubsonic();
					break;
				case POLHAMUS:
					calcCMAlpha.polhamus();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_AT_ALPHA  
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {

				CalcCMAtAlpha calcCMAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCMAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCMAtAlpha.fromAirfoilDistribution(_alphaWingCurrent);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcMomentCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcMomentCurve.fromAirfoilDistribution();
					break;
				default:
					break;
				}
			}
			//.........................................................................................................................
			//	MOMENT_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

				CalcMomentDistribution calcMomentDistribution = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcMomentDistribution();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcMomentDistribution.fromAirfoilDistribution();
					break;
				default:
					break;
				}
			}
		}

		//============================================================================
		// HTAIL
		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {
			_liftingSurfaceAerodynamicManagers.put(
					ComponentEnum.HORIZONTAL_TAIL,
					new LSAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getHTail(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(),
							_theAerodynamicBuilderInterface.getHTailNumberOfPointSemiSpanWise(),
							_alphaHTailList, 
							_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution(),
							_theAerodynamicBuilderInterface.getHTailMomentumPole()
							)
					);

			//.........................................................................................................................
			//	CRITICAL_MACH 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {

				CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLAtAlpha();

				CalcMachCr calcMachCr = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMachCr();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
				case KORN_MASON:
					calcMachCr.kornMason(calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaHTailCurrent));
					break;
				case KROO:
					calcMachCr.kroo(calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaHTailCurrent));
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	AERODYNAMIC_CENTER
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {

				CalcXAC calcXAC = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcXAC();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {
				case QUARTER:
					calcXAC.atQuarterMAC();
					break;
				case DEYOUNG_HARPER:
					calcXAC.deYoungHarper();
					break;
				case NAPOLITANO_DATCOM:
					calcXAC.datcomNapolitano();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_AT_ALPHA  
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {

				CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
				case LINEAR_DLR:
					calcCLAtAlpha.linearDLR(_alphaHTailCurrent);
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCLAtAlpha.linearAndersonCompressibleSubsonic(_alphaHTailCurrent);
					break;
				case LINEAR_NASA_BLACKWELL:
					calcCLAtAlpha.nasaBlackwellLinear(_alphaHTailCurrent);
					break;
				case NASA_BLACKWELL:
					calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaHTailCurrent);
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_ALPHA
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {

				CalcCLAlpha calcCLAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ALPHA)) {
				case INTEGRAL_MEAN:
					calcCLAlpha.integralMean2D();
					break;
				case POLHAMUS:
					calcCLAlpha.polhamus();
					break;
				case NASA_BLACKWELL:
					calcCLAlpha.nasaBlackwell();
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCLAlpha.andersonSweptCompressibleSubsonic();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_ZERO
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {

				CalcCL0 calcCL0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCL0();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ZERO)) {
				case NASA_BLACKWELL:
					calcCL0.nasaBlackwell();
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCL0.andersonSweptCompressibleSubsonic();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_STAR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_STAR)) {

				CalcCLStar calcCLStar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLStar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_STAR)) {
				case NASA_BLACKWELL:
					calcCLStar.nasaBlackwell();
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCLStar.andersonSweptCompressibleSubsonic();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_MAX
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_MAX)) {

				CalcCLmax calcCLmax = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLmax();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)) {
				case NASA_BLACKWELL:
					calcCLmax.nasaBlackwell();
					break;
				case PHILLIPS_ALLEY:
					calcCLmax.phillipsAndAlley();
					break;
				case ROSKAM:
					calcCLmax.roskam();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	ALPHA_ZERO_LIFT
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {

				CalcAlpha0L calcAlpha0L = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcAlpha0L();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
				case INTEGRAL_MEAN_NO_TWIST:
					calcAlpha0L.integralMeanNoTwist();
					break;
				case INTEGRAL_MEAN_TWIST:
					calcAlpha0L.integralMeanWithTwist();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	ALPHA_STAR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_STAR)) {

				CalcAlphaStar calcAlphaStar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcAlphaStar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)) {
				case MEAN_AIRFOIL_INFLUENCE_AREAS:
					calcAlphaStar.meanAirfoilWithInfluenceAreas();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	ALPHA_STALL
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_STALL)) {

				CalcAlphaStall calcAlphaStall = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcAlphaStall();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)) {
				case PHILLIPS_ALLEY:
					calcAlphaStall.fromCLmaxPhillipsAndAlley();
					break;
				case NASA_BLACKWELL:
					calcAlphaStall.fromAlphaMaxLinearNasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	LIFT_CURVE_3D 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

				CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcLiftCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
				case PHILLIPS_ALLEY:
					calcLiftCurve.fromCLmaxPhillipsAndAlley();
					break;
				case NASA_BLACKWELL:
					calcLiftCurve.nasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	LIFT_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

				CalcLiftDistributions calcLiftDistributions = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcLiftDistributions();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {
				case SCHRENK:
					calcLiftDistributions.schrenk();
					break;
				case NASA_BLACKWELL:
					calcLiftDistributions.nasaBlackwell();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD0
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD0)) {

				CalcCD0 calcCD0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCD0();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD0)) {
				case SEMIEMPIRICAL:
					calcCD0.semiempirical(_currentMachNumber, _currentAltitude);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_INDUCED
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {

				CalcCDInduced calcCDInduced = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCDInduced();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {
				case GROSU:
					calcCDInduced.grosu(_alphaHTailCurrent);
					break;
				case HOWE:
					calcCDInduced.howe(_alphaHTailCurrent);
					break;
				case RAYMER:
					calcCDInduced.raymer(_alphaHTailCurrent);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_WAVE 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_WAVE)) {

				CalcCDWave calcCDWave = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCDWave();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_WAVE)) {
				case LOCK_KORN_WITH_KORN_MASON:
					calcCDWave.lockKornWithKornMason();
					break;
				case LOCK_KORN_WITH_KROO:
					calcCDWave.lockKornWithKroo();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	OSWALD_FACTOR 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {

				CalcOswaldFactor calcOswaldFactor = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcOswaldFactor();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {
				case GROSU:
					calcOswaldFactor.grosu(_alphaHTailCurrent);
					break;
				case HOWE:
					calcOswaldFactor.howe();
					break;
				case RAYMER:
					calcOswaldFactor.raymer();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	POLAR_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				analyses.liftingsurface.LSAerodynamicsManager.CalcPolar calcPolar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcPolar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {
				case SEMIEMPIRICAL:
					calcPolar.semiempirical(_currentMachNumber, _currentAltitude);
					break;
				case AIRFOIL_DISTRIBUTION:
					calcPolar.fromCdDistribution(_currentMachNumber, _currentAltitude);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	DRAG_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

				CalcDragDistributions calcDragDistributions = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcDragDistributions();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {
				case NASA_BLACKWELL:
					calcDragDistributions.nasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {

				CalcCDAtAlpha calcCDAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCDAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCDAtAlpha.fromCdDistribution(
							_alphaHTailCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				case SEMIEMPIRICAL:
					calcCDAtAlpha.semiempirical(
							_alphaHTailCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	ELEVATOR_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftDevicesEffects();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
				case SEMIEMPIRICAL:
					calcHighLiftDevicesEffects.semiempirical(
							_theAerodynamicBuilderInterface.getDeltaElevatorList(), 
							null, 
							_currentMachNumber
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	ELEVATOR_LIFT_CURVE_3D

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

				CalcHighLiftCurve calcHighLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {
				case SEMIEMPIRICAL:
					calcHighLiftCurve.semiempirical(
							_theAerodynamicBuilderInterface.getDeltaElevatorList(), 
							null, 
							_currentMachNumber, 
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CL_AT_ALPHA_ELEVATOR 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {

				CalcCLAtAlphaHighLift calcCLAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLAtAlphaHighLift();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {
				case SEMIEMPIRICAL:
					calcCLAtAlphaHighLift.semiempirical(
							_alphaHTailCurrent,
							_theAerodynamicBuilderInterface.getDeltaElevatorList(), 
							null, 
							_currentMachNumber, 
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_AT_ALPHA_ELEVATOR 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {

				/*
				 * CD@Alpha + Delta CD0 from High Lift Devices Effects
				 */

				CalcCDAtAlpha calcCDAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCDAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCDAtAlpha.fromCdDistribution(
							_alphaHTailCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				case SEMIEMPIRICAL:
					calcCDAtAlpha.semiempirical(
							_alphaHTailCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				default:
					break;
				}

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftDevicesEffects();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
				case SEMIEMPIRICAL:
						calcHighLiftDevicesEffects.semiempirical(
								_theAerodynamicBuilderInterface.getDeltaElevatorList(), 
								null, 
								_currentMachNumber
								);
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CM_AT_ALPHA_ELEVATOR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {

				/*
				 * CM@Alpha + Delta CMac from High Lift Devices Effects
				 */

				CalcCMAtAlpha calcCMAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCMAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCMAtAlpha.fromAirfoilDistribution(_alphaHTailCurrent);
					break;
				default:
					break;
				}

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftDevicesEffects();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
				case SEMIEMPIRICAL:
					calcHighLiftDevicesEffects.semiempirical(
							_theAerodynamicBuilderInterface.getDeltaElevatorList(), 
							null, 
							_currentMachNumber
							);
				default:
					break;
				}
			}
			//.........................................................................................................................
			//	CM_AC
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {

				CalcCMac calcCMac = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCMac();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {
				case BASIC_AND_ADDITIONAL:
					calcCMac.basicAndAdditionalContribution();
					break;
				case AIRFOIL_DISTRIBUTION:
					calcCMac.fromAirfoilDistribution();
					break;
				case INTEGRAL_MEAN:
					calcCMac.integralMean();
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_ALPHA
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {

				CalcCMAlpha calcCMAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCMAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCMAlpha.andersonSweptCompressibleSubsonic();
					break;
				case POLHAMUS:
					calcCMAlpha.polhamus();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_AT_ALPHA  
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {

				CalcCMAtAlpha calcCMAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCMAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCMAtAlpha.fromAirfoilDistribution(_alphaHTailCurrent);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMomentCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcMomentCurve.fromAirfoilDistribution();
					break;
				default:
					break;
				}
			}
			//.........................................................................................................................
			//	MOMENT_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

				CalcMomentDistribution calcMomentDistribution = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMomentDistribution();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcMomentDistribution.fromAirfoilDistribution();
					break;
				default:
					break;
				}
			}
		}

		//============================================================================
		// VTAIL 
		if(_theAerodynamicBuilderInterface.getTheAircraft().getVTail() != null) {
			_liftingSurfaceAerodynamicManagers.put(
					ComponentEnum.VERTICAL_TAIL,
					new LSAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getVTail(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(),
							_theAerodynamicBuilderInterface.getVTailNumberOfPointSemiSpanWise(), 
							_betaList, // Alpha for VTail is Beta
							_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution(),
							_theAerodynamicBuilderInterface.getVTailMomentumPole()
							)
					);

			//.........................................................................................................................
			//	CRITICAL_MACH 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {

				CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCLAtAlpha();

				CalcMachCr calcMachCr = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcMachCr();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
				case KORN_MASON:
					calcMachCr.kornMason(calcCLAtAlpha.nasaBlackwellCompleteCurve(_betaVTailCurrent));
					break;
				case KROO:
					calcMachCr.kroo(calcCLAtAlpha.nasaBlackwellCompleteCurve(_betaVTailCurrent));
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	AERODYNAMIC_CENTER
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {

				CalcXAC calcXAC = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcXAC();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {
				case QUARTER:
					calcXAC.atQuarterMAC();
					break;
				case DEYOUNG_HARPER:
					calcXAC.deYoungHarper();
					break;
				case NAPOLITANO_DATCOM:
					calcXAC.datcomNapolitano();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_AT_ALPHA  
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {

				CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCLAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
				case LINEAR_DLR:
					calcCLAtAlpha.linearDLR(_betaVTailCurrent);
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCLAtAlpha.linearAndersonCompressibleSubsonic(_betaVTailCurrent);
					break;
				case LINEAR_NASA_BLACKWELL:
					calcCLAtAlpha.nasaBlackwellLinear(_betaVTailCurrent);
					break;
				case NASA_BLACKWELL:
					calcCLAtAlpha.nasaBlackwellCompleteCurve(_betaVTailCurrent);
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_ALPHA
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {

				CalcCLAlpha calcCLAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCLAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ALPHA)) {
				case HELMBOLD_DIEDERICH:
					calcCLAlpha.helmboldDiederich(_currentMachNumber);
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_ZERO
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {

				CalcCL0 calcCL0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCL0();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ZERO)) {
				case NASA_BLACKWELL:
					calcCL0.nasaBlackwell();
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCL0.andersonSweptCompressibleSubsonic();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_STAR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_STAR)) {

				CalcCLStar calcCLStar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCLStar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_STAR)) {
				case NASA_BLACKWELL:
					calcCLStar.nasaBlackwell();
					break;
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCLStar.andersonSweptCompressibleSubsonic();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CL_MAX
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_MAX)) {

				CalcCLmax calcCLmax = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCLmax();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)) {
				case NASA_BLACKWELL:
					calcCLmax.nasaBlackwell();
					break;
				case PHILLIPS_ALLEY:
					calcCLmax.phillipsAndAlley();
					break;
				case ROSKAM:
					calcCLmax.roskam();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	ALPHA_ZERO_LIFT
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {

				CalcAlpha0L calcAlpha0L = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcAlpha0L();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
				case INTEGRAL_MEAN_NO_TWIST:
					calcAlpha0L.integralMeanNoTwist();
					break;
				case INTEGRAL_MEAN_TWIST:
					calcAlpha0L.integralMeanWithTwist();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	ALPHA_STAR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_STAR)) {

				CalcAlphaStar calcAlphaStar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcAlphaStar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)) {
				case MEAN_AIRFOIL_INFLUENCE_AREAS:
					calcAlphaStar.meanAirfoilWithInfluenceAreas();
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	ALPHA_STALL
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_STALL)) {

				CalcAlphaStall calcAlphaStall = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcAlphaStall();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)) {
				case PHILLIPS_ALLEY:
					calcAlphaStall.fromCLmaxPhillipsAndAlley();
					break;
				case NASA_BLACKWELL:
					calcAlphaStall.fromAlphaMaxLinearNasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	LIFT_CURVE_3D 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

				CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcLiftCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
				case PHILLIPS_ALLEY:
					calcLiftCurve.fromCLmaxPhillipsAndAlley();
					break;
				case NASA_BLACKWELL:
					calcLiftCurve.nasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	LIFT_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

				CalcLiftDistributions calcLiftDistributions = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcLiftDistributions();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {
				case SCHRENK:
					calcLiftDistributions.schrenk();
					break;
				case NASA_BLACKWELL:
					calcLiftDistributions.nasaBlackwell();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD0
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD0)) {

				CalcCD0 calcCD0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCD0();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD0)) {
				case SEMIEMPIRICAL:
					calcCD0.semiempirical(_currentMachNumber, _currentAltitude);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_INDUCED
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {

				CalcCDInduced calcCDInduced = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCDInduced();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {
				case GROSU:
					calcCDInduced.grosu(_betaVTailCurrent);
					break;
				case HOWE:
					calcCDInduced.howe(_betaVTailCurrent);
					break;
				case RAYMER:
					calcCDInduced.raymer(_betaVTailCurrent);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_WAVE 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_WAVE)) {

				CalcCDWave calcCDWave = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCDWave();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_WAVE)) {
				case LOCK_KORN_WITH_KORN_MASON:
					calcCDWave.lockKornWithKornMason();
					break;
				case LOCK_KORN_WITH_KROO:
					calcCDWave.lockKornWithKroo();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	OSWALD_FACTOR 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {

				CalcOswaldFactor calcOswaldFactor = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcOswaldFactor();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {
				case GROSU:
					calcOswaldFactor.grosu(_betaVTailCurrent);
					break;
				case HOWE:
					calcOswaldFactor.howe();
					break;
				case RAYMER:
					calcOswaldFactor.raymer();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	POLAR_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				analyses.liftingsurface.LSAerodynamicsManager.CalcPolar calcPolar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcPolar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {
				case SEMIEMPIRICAL:
					calcPolar.semiempirical(_currentMachNumber, _currentAltitude);
					break;
				case AIRFOIL_DISTRIBUTION:
					calcPolar.fromCdDistribution(_currentMachNumber, _currentAltitude);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	DRAG_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

				CalcDragDistributions calcDragDistributions = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcDragDistributions();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {
				case NASA_BLACKWELL:
					calcDragDistributions.nasaBlackwell(_currentMachNumber);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {

				CalcCDAtAlpha calcCDAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCDAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCDAtAlpha.fromCdDistribution(
							_betaVTailCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				case SEMIEMPIRICAL:
					calcCDAtAlpha.semiempirical(
							_betaVTailCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	RUDDER_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcHighLiftDevicesEffects();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
				case SEMIEMPIRICAL:
					calcHighLiftDevicesEffects.semiempirical(
							_theAerodynamicBuilderInterface.getDeltaRudderList(), 
							null, 
							_currentMachNumber
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	RUDDER_LIFT_CURVE_3D

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

				CalcHighLiftCurve calcHighLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcHighLiftCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {
				case SEMIEMPIRICAL:
					calcHighLiftCurve.semiempirical(
							_theAerodynamicBuilderInterface.getDeltaRudderList(), 
							null, 
							_currentMachNumber, 
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CL_AT_ALPHA_RUDDER 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {

				CalcCLAtAlphaHighLift calcCLAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCLAtAlphaHighLift();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {
				case SEMIEMPIRICAL:
					calcCLAtAlphaHighLift.semiempirical(
							_betaVTailCurrent,
							_theAerodynamicBuilderInterface.getDeltaRudderList(), 
							null, 
							_currentMachNumber, 
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_AT_ALPHA_RUDDER 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {

				/*
				 * CD@Alpha + Delta CD0 from High Lift Devices Effects
				 */

				CalcCDAtAlpha calcCDAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCDAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCDAtAlpha.fromCdDistribution(
							_betaVTailCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				case SEMIEMPIRICAL:
					calcCDAtAlpha.semiempirical(
							_betaVTailCurrent, 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				default:
					break;
				}

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcHighLiftDevicesEffects();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
				case SEMIEMPIRICAL:
						calcHighLiftDevicesEffects.semiempirical(
								_theAerodynamicBuilderInterface.getDeltaRudderList(), 
								null, 
								_currentMachNumber
								);
					break;
				default:
					break;
				}

			}

			//.........................................................................................................................
			//	CM_AT_ALPHA_RUDDER
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {

				/*
				 * CM@Alpha + Delta CMac from High Lift Devices Effects
				 */

				CalcCMAtAlpha calcCMAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCMAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCMAtAlpha.fromAirfoilDistribution(_betaVTailCurrent);
					break;
				default:
					break;
				}

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcHighLiftDevicesEffects();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
				case SEMIEMPIRICAL:
					calcHighLiftDevicesEffects.semiempirical(
							_theAerodynamicBuilderInterface.getDeltaRudderList(), 
							null, 
							_currentMachNumber
							);
				default:
					break;
				}
			}
			//.........................................................................................................................
			//	CM_AC
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {

				CalcCMac calcCMac = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCMac();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {
				case BASIC_AND_ADDITIONAL:
					calcCMac.basicAndAdditionalContribution();
					break;
				case AIRFOIL_DISTRIBUTION:
					calcCMac.fromAirfoilDistribution();
					break;
				case INTEGRAL_MEAN:
					calcCMac.integralMean();
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_ALPHA
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {

				CalcCMAlpha calcCMAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCMAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {
				case ANDERSON_COMPRESSIBLE_SUBSONIC:
					calcCMAlpha.andersonSweptCompressibleSubsonic();
					break;
				case POLHAMUS:
					calcCMAlpha.polhamus();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_AT_ALPHA  
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {

				CalcCMAtAlpha calcCMAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCMAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcCMAtAlpha.fromAirfoilDistribution(_betaVTailCurrent);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcMomentCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcMomentCurve.fromAirfoilDistribution();
					break;
				default:
					break;
				}
			}
			//.........................................................................................................................
			//	MOMENT_DISTRIBUTION
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

				CalcMomentDistribution calcMomentDistribution = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcMomentDistribution();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {
				case AIRFOIL_DISTRIBUTION:
					calcMomentDistribution.fromAirfoilDistribution();
					break;
				default:
					break;
				}
			}
		}

		//============================================================================
		// FUSELAGE
		if(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage() != null) {
			_fuselageAerodynamicManagers.put(
					ComponentEnum.FUSELAGE,
					new FuselageAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getFuselage(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing(), 
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_alphaBodyList, 
							_theAerodynamicBuilderInterface.getCurrentCondition(), 
							_theAerodynamicBuilderInterface.getAdimensionalFuselageMomentumPole()
							)
					);

			//.........................................................................................................................
			//	CD0_PARASITE
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_PARASITE_FUSELAGE)) {

				CalcCD0Parasite calcCD0Parasite = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCD0Parasite();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_PARASITE_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcCD0Parasite.semiempirical();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD0_BASE
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_BASE_FUSELAGE)) {

				CalcCD0Base calcCD0Base = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCD0Base();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_BASE_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcCD0Base.semiempirical();
					break;
				default:
					break;
				}
			}
			//.........................................................................................................................
			//	CD0_UPSWEEP
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_UPSWEEP_FUSELAGE)) {

				CalcCD0Upsweep calcCD0Upsweep = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCD0Upsweep();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_UPSWEEP_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcCD0Upsweep.semiempirical();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD0_WINDSHIELD
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_WINDSHIELD_FUSELAGE)) {

				CalcCD0Windshield calcCD0Windshield = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCD0Windshield();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_WINDSHIELD_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcCD0Windshield.semiempirical();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD0_TOTAL
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_TOTAL_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcCD0Total calcCD0Total = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCD0Total();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_TOTAL_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcCD0Total.semiempirical();
					break;
				case FUSDES:
					calcCD0Total.fusDes();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_INDUCED
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcCDInduced calcCDInduced = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCDInduced();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)) {
				case SEMIEMPIRICAL:  
					calcCDInduced.semiempirical(_alphaBodyCurrent);
					break;
				default:
					break;
				}
			}
			//.........................................................................................................................
			//	POLAR_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcPolar calcPolar = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcPolar();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcPolar.semiempirical();
					break;
				case FUSDES:
					calcPolar.fusDes();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CD_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcCDAtAlpha calcCDAtAlpha = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCDAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE)) {
				case SEMIEMPIRICAL: 
					calcCDAtAlpha.semiempirical(_alphaBodyCurrent);
					break;
				case FUSDES: 
					calcCDAtAlpha.fusDes(_alphaBodyCurrent);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM0
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM0_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcCM0 calcCM0 = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCM0();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM0_FUSELAGE)) {
				case MULTHOPP:
					calcCM0.multhopp();
					break;
				case FUSDES:
					calcCM0.fusDes();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_ALPHA
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcCMAlpha calcCMAlpha = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCMAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM_ALPHA_FUSELAGE)) {
				case GILRUTH:
					calcCMAlpha.gilruth();
					break;
				case FUSDES:
					calcCMAlpha.fusDes();
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcCMAtAlpha calcCMAtAlpha = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCMAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcCMAtAlpha.semiempirical(_alphaBodyCurrent);
					break;
				case FUSDES:
					calcCMAtAlpha.fusDes(_alphaBodyCurrent);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcMomentCurve calcMomentCurve = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcMomentCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcMomentCurve.semiempirical();
					break;
				case FUSDES:
					calcMomentCurve.fusDes();
					break;
				default:
					break;
				}
			}
		}
		//============================================================================
		// NACELLE

		List<Amount<Angle>> alphaNacelleList = new ArrayList<>();
		switch (_theAerodynamicBuilderInterface.getTheAircraft().getNacelles().getMountingPositionNacelles()) {
		case WING:
			alphaNacelleList = _alphaWingList;
			break;
		case FUSELAGE:
			alphaNacelleList = _alphaHTailList;
			break;
		case HTAIL:
			alphaNacelleList = _alphaHTailList;
			break;
		default:
			break;
		}

		if(_theAerodynamicBuilderInterface.getTheAircraft().getNacelles() != null) {
			_nacelleAerodynamicManagers.put(
					ComponentEnum.NACELLE,
					new NacelleAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getNacelles(),
							_theAerodynamicBuilderInterface.getTheAircraft().getWing(), 
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(), 
							alphaNacelleList 
							)
					);

		//.........................................................................................................................
		//	CD0_PARASITE
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_PARASITE_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcCD0Parasite calcCD0Parasite = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCD0Parasite();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_PARASITE_NACELLE)) {
			case SEMIEMPIRICAL:
				calcCD0Parasite.semiempirical();
				break;
			default:
				break;
			}
		}

		//.........................................................................................................................
		//	CD0_BASE
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_BASE_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcCD0Base calcCD0Base = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCD0Base();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_BASE_NACELLE)) {
			case SEMIEMPIRICAL:
				calcCD0Base.semiempirical();
				break;
			default:
				break;
			}
		}

		//.........................................................................................................................
		//	CD0_TOTAL
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_TOTAL_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcCD0Total calcCD0Total = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCD0Total();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_TOTAL_NACELLE)) {
			case SEMIEMPIRICAL:
				calcCD0Total.semiempirical();
				break;
			default:
				break;
			}
		}

		//.........................................................................................................................
		//	CD_INDUCED
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcCDInduced calcCDInduced = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCDInduced();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)) {
			case SEMIEMPIRICAL:
				calcCDInduced.semiempirical(_alphaNacelleCurrent);
				break;
			default:
				break;
			}
		}

		//.........................................................................................................................
		//	POLAR_CURVE_3D
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcPolar calcPolar = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcPolar();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)) {
			case SEMIEMPIRICAL:
				calcPolar.semiempirical();
				break;
			default:
				break;
			}
		}

		//.........................................................................................................................
		//	CD_AT_ALPHA 
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcCDInduced calcCDInduced = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCDInduced();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)) {
			case SEMIEMPIRICAL:
				calcCDInduced.semiempirical(_alphaNacelleCurrent);
				break;
			default:
				break;
			}
		}

		//.........................................................................................................................
		//	CM0
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM0_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcCM0 calcCM0 = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCM0();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM0_NACELLE)) {
			case MULTHOPP:
				calcCM0.multhopp();
				break;
			default:
				break;
			}
		}

		//.........................................................................................................................
		//	CM_ALPHA
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcCMAlpha calcCMAlpha = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCMAlpha();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM_ALPHA_NACELLE)) {
			case GILRUTH:
				calcCMAlpha.gilruth();
				break;
			default:
				break;
			}
		}
		//.........................................................................................................................
		//	CM_AT_ALPHA 
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcCMAtAlpha calcCMAtAlpha = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCMAtAlpha();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE)) {
			case SEMIEMPIRICAL:
				calcCMAtAlpha.semiempirical(_alphaNacelleCurrent);
				break;
			default:
				break;
			}
		}

		//.........................................................................................................................
		//	MOMENT_CURVE_3D
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)) {

			analyses.nacelles.NacelleAerodynamicsManager.CalcMomentCurve calcMomentCurve = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcMomentCurve();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)) {
			case SEMIEMPIRICAL:
				calcMomentCurve.semiempirical();
				break;
			default:
				break;
			}
		}
	}
		//--------------------------AIRCRAFT----------------------------------------
		//.........................................................................................................................
		//	WING AERODYNAMIC_CENTER

		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){
				CalcXAC calcXAC = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcXAC();
				calcXAC.deYoungHarper();
				if(_theAerodynamicBuilderInterface.getWingMomentumPole() == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacLRF().get(MethodEnum.DEYOUNG_HARPER)
							);

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER,MethodEnum.DEYOUNG_HARPER);
			}
		}

		//.........................................................................................................................
		//	HORIZONTAL TAIL AERODYNAMIC_CENTER

		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){
				CalcXAC calcXAC = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcXAC();
				calcXAC.deYoungHarper();
				if(_theAerodynamicBuilderInterface.getHTailMomentumPole() == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacLRF().get(MethodEnum.DEYOUNG_HARPER)
							);

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER,MethodEnum.DEYOUNG_HARPER);
			}
		}

		//.........................................................................................................................
		//	WING LIFT_CURVE_3D (EVENTUALLY WITH HIGH LIFT DEVICES AND WITH FUSELAGE EFFECTS)
		List<Double> temporaryLiftCurve = new ArrayList<>();


		switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
		case TAKE_OFF:

			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL) ||
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

					CalcHighLiftCurve calcHighLiftCurveTakeOff = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftCurve();
					calcHighLiftCurveTakeOff.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
							_currentMachNumber, 
							_currentAltitude);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D,MethodEnum.SEMIEMPIRICAL);
					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getLiftCoefficient3DCurveHighLift()
							.get(MethodEnum.SEMIEMPIRICAL));
				}
			}
			else {
				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getLiftCoefficient3DCurveHighLift()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
								)
						);
			}

			if(_theAerodynamicBuilderInterface.getFuselageEffectOnWingLiftCurve()){

				//CL ALPHA
				_clAlphaWingFuselage =
						LiftCalc.calculateCLAlphaFuselage(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlphaHighLift().get(
										MethodEnum.SEMIEMPIRICAL),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
								Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
										SI.METER)
								);

				//CL ZERO
				_clZeroWingFuselage =
						-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZeroHighLift()
						.get(MethodEnum.SEMIEMPIRICAL);

				//CL MAX
				_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING).getCLMaxHighLift().get(MethodEnum.SEMIEMPIRICAL);

				//CL STAR
				_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING).getCLStarHighLift()
						.get(MethodEnum.SEMIEMPIRICAL);

				//ALPHA STAR
				_alphaStarWingFuselage = Amount.valueOf(
						(_clStarWingFuselage - _clZeroWingFuselage)/
						_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
						NonSI.DEGREE_ANGLE);

				//ALPHA stall
				double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
						_liftingSurfaceAerodynamicManagers.get(
								ComponentEnum.WING).getAlphaStallHighLift()
						.get(MethodEnum.SEMIEMPIRICAL)
						.doubleValue(NonSI.DEGREE_ANGLE);

				_alphaStallWingFuselage = Amount.valueOf(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING).getAlphaStallHighLift()
						.get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
						NonSI.DEGREE_ANGLE);


				//CURVE
				temporaryLiftCurve = new ArrayList<>();
				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(LiftCalc.calculateCLvsAlphaArray(
						this._clZeroWingFuselage,
						this._clMaxWingFuselage,
						this._alphaStarWingFuselage,
						this._alphaStallWingFuselage,
						this._clAlphaWingFuselage,
						MyArrayUtils.convertListOfAmountToDoubleArray(_alphaWingList)
						));
			}
			break;
		case CLIMB:
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL) ||
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

					CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
					calcLiftCurve.nasaBlackwell(_currentMachNumber);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D,MethodEnum.NASA_BLACKWELL);
					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getLiftCoefficient3DCurve()
							.get(MethodEnum.NASA_BLACKWELL));
				}
			}
			else {
				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getLiftCoefficient3DCurve()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
								)
						);
			}
			if(_theAerodynamicBuilderInterface.getFuselageEffectOnWingLiftCurve()){
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.PHILLIPS_ALLEY)){
					//CL ALPHA
					_clAlphaWingFuselage =
							LiftCalc.calculateCLAlphaFuselage(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
											MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
									Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
											SI.METER)
									);

					//CL ZERO
					_clZeroWingFuselage =
							-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero()
							.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC);

					//CL MAX
					_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getCLMax().get(MethodEnum.PHILLIPS_ALLEY);

					//CL STAR
					_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getCLStar()
							.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC);

					//ALPHA STAR
					_alphaStarWingFuselage = Amount.valueOf(
							(_clStarWingFuselage - _clZeroWingFuselage)/
							_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
							NonSI.DEGREE_ANGLE);

					//ALPHA stall
					double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
							_liftingSurfaceAerodynamicManagers.get(
									ComponentEnum.WING).getAlphaStar()
							.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS)
							.doubleValue(NonSI.DEGREE_ANGLE);

					_alphaStallWingFuselage = Amount.valueOf(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getAlphaStall()
							.get(MethodEnum.PHILLIPS_ALLEY).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
							NonSI.DEGREE_ANGLE);

				}
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.NASA_BLACKWELL)){
					//CL ALPHA
					_clAlphaWingFuselage =
							LiftCalc.calculateCLAlphaFuselage(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
											MethodEnum.NASA_BLACKWELL),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
									Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
											SI.METER)
									);


					//CL ZERO
					_clZeroWingFuselage =
							-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero()
							.get(MethodEnum.NASA_BLACKWELL);

					//CL MAX
					_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getCLMax().get(MethodEnum.NASA_BLACKWELL);

					//CL STAR
					_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getCLStar()
							.get(MethodEnum.NASA_BLACKWELL);

					//ALPHA STAR
					_alphaStarWingFuselage = Amount.valueOf(
							(_clStarWingFuselage - _clZeroWingFuselage)/
							_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
							NonSI.DEGREE_ANGLE);

					//ALPHA stall
					double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
							_liftingSurfaceAerodynamicManagers.get(
									ComponentEnum.WING).getAlphaStar()
							.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS)
							.doubleValue(NonSI.DEGREE_ANGLE);

					_alphaStallWingFuselage = Amount.valueOf(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getAlphaStall()
							.get(MethodEnum.NASA_BLACKWELL).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
							NonSI.DEGREE_ANGLE);
				}

				//CURVE
				temporaryLiftCurve = new ArrayList<>();
				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(LiftCalc.calculateCLvsAlphaArray(
						this._clZeroWingFuselage,
						this._clMaxWingFuselage,
						this._alphaStarWingFuselage,
						this._alphaStallWingFuselage,
						this._clAlphaWingFuselage,
						MyArrayUtils.convertListOfAmountToDoubleArray(_alphaWingList)
						));
			}
			break;
		case CRUISE:
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL) ||
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

					CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
					calcLiftCurve.nasaBlackwell(_currentMachNumber);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D,MethodEnum.NASA_BLACKWELL);
					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getLiftCoefficient3DCurve()
							.get(MethodEnum.NASA_BLACKWELL));
				}
			}
			else {
				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getLiftCoefficient3DCurve()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
								)
						);
			}
			if(_theAerodynamicBuilderInterface.getFuselageEffectOnWingLiftCurve()){
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.PHILLIPS_ALLEY)){
					//CL ALPHA
					_clAlphaWingFuselage =
							LiftCalc.calculateCLAlphaFuselage(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
											MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
									Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
											SI.METER)
									);

					//CL ZERO
					_clZeroWingFuselage =
							-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero()
							.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC);

					//CL MAX
					_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getCLMax().get(MethodEnum.PHILLIPS_ALLEY);

					//CL STAR
					_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getCLStar()
							.get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC);

					//ALPHA STAR
					_alphaStarWingFuselage = Amount.valueOf(
							(_clStarWingFuselage - _clZeroWingFuselage)/
							_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
							NonSI.DEGREE_ANGLE);

					//ALPHA stall
					double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
							_liftingSurfaceAerodynamicManagers.get(
									ComponentEnum.WING).getAlphaStar()
							.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS)
							.doubleValue(NonSI.DEGREE_ANGLE);

					_alphaStallWingFuselage = Amount.valueOf(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getAlphaStall()
							.get(MethodEnum.PHILLIPS_ALLEY).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
							NonSI.DEGREE_ANGLE);

				}
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.NASA_BLACKWELL)){
					//CL ALPHA
					_clAlphaWingFuselage =
							LiftCalc.calculateCLAlphaFuselage(
									_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
											MethodEnum.NASA_BLACKWELL),
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
									Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
											SI.METER)
									);


					//CL ZERO
					_clZeroWingFuselage =
							-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero()
							.get(MethodEnum.NASA_BLACKWELL);

					//CL MAX
					_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getCLMax().get(MethodEnum.NASA_BLACKWELL);

					//CL STAR
					_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getCLStar()
							.get(MethodEnum.NASA_BLACKWELL);

					//ALPHA STAR
					_alphaStarWingFuselage = Amount.valueOf(
							(_clStarWingFuselage - _clZeroWingFuselage)/
							_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
							NonSI.DEGREE_ANGLE);

					//ALPHA stall
					double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
							_liftingSurfaceAerodynamicManagers.get(
									ComponentEnum.WING).getAlphaStar()
							.get(MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS)
							.doubleValue(NonSI.DEGREE_ANGLE);

					_alphaStallWingFuselage = Amount.valueOf(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING).getAlphaStall()
							.get(MethodEnum.NASA_BLACKWELL).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
							NonSI.DEGREE_ANGLE);
				}

				//CURVE
				temporaryLiftCurve = new ArrayList<>();
				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(LiftCalc.calculateCLvsAlphaArray(
						this._clZeroWingFuselage,
						this._clMaxWingFuselage,
						this._alphaStarWingFuselage,
						this._alphaStallWingFuselage,
						this._clAlphaWingFuselage,
						MyArrayUtils.convertListOfAmountToDoubleArray(_alphaWingList)
						));
			}
			break;
		case LANDING:
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL) ||
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

					CalcHighLiftCurve calcHighLiftCurveTakeOff = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftCurve();
					calcHighLiftCurveTakeOff.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
							_currentMachNumber, 
							_currentAltitude);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D,MethodEnum.SEMIEMPIRICAL);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D,MethodEnum.NASA_BLACKWELL);
					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getLiftCoefficient3DCurveHighLift()
							.get(MethodEnum.SEMIEMPIRICAL));
				}
			}
			else {
				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getLiftCoefficient3DCurveHighLift()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
								)
						);
			}
			if(_theAerodynamicBuilderInterface.getFuselageEffectOnWingLiftCurve()){

				//CL ALPHA
				_clAlphaWingFuselage =
						LiftCalc.calculateCLAlphaFuselage(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlphaHighLift().get(
										MethodEnum.SEMIEMPIRICAL),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
								Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
										SI.METER)
								);

				//CL ZERO
				_clZeroWingFuselage =
						-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZeroHighLift()
						.get(MethodEnum.SEMIEMPIRICAL);

				//CL MAX
				_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING).getCLMaxHighLift().get(MethodEnum.SEMIEMPIRICAL);

				//CL STAR
				_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING).getCLStarHighLift()
						.get(MethodEnum.SEMIEMPIRICAL);

				//ALPHA STAR
				_alphaStarWingFuselage = Amount.valueOf(
						(_clStarWingFuselage - _clZeroWingFuselage)/
						_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
						NonSI.DEGREE_ANGLE);

				//ALPHA stall
				double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
						_liftingSurfaceAerodynamicManagers.get(
								ComponentEnum.WING).getAlphaStallHighLift()
						.get(MethodEnum.SEMIEMPIRICAL)
						.doubleValue(NonSI.DEGREE_ANGLE);

				_alphaStallWingFuselage = Amount.valueOf(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING).getAlphaStallHighLift()
						.get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
						NonSI.DEGREE_ANGLE);


				//CURVE
				temporaryLiftCurve = new ArrayList<>();
				temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(LiftCalc.calculateCLvsAlphaArray(
						this._clZeroWingFuselage,
						this._clMaxWingFuselage,
						this._alphaStarWingFuselage,
						this._alphaStallWingFuselage,
						this._clAlphaWingFuselage,
						MyArrayUtils.convertListOfAmountToDoubleArray(_alphaWingList)
						));
			}
			break;
		}
		_current3DWingLiftCurve = temporaryLiftCurve;


		//.........................................................................................................................
		//	WING POLAR_CURVE_3D

		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

				CalcPolar calcPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcPolar();

				calcPolarCurve.fromCdDistribution(_currentMachNumber, _currentAltitude);

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,MethodEnum.AIRFOIL_DISTRIBUTION);
			}
		}

		switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
		case TAKE_OFF:
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS) &&
					!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)){
				if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD().get(MethodEnum.INPUT) == null){
					CalcHighLiftDevicesEffects calcHighLiftEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
					calcHighLiftEffects.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
							_currentMachNumber
							);

					MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getPolar3DCurve()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)))
					.stream().forEach(cd -> {
						_current3DWingPolarCurve.add(
								cd + 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getDeltaCD()
								.get(MethodEnum.SEMIEMPIRICAL));
					});
				}
				else{
					MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getPolar3DCurve()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)))
					.stream().forEach(cd -> {
						_current3DWingPolarCurve.add(
								cd + 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getDeltaCD()
								.get(MethodEnum.INPUT));
					});
				}
			}

			break;
		case CLIMB:
			_current3DWingPolarCurve = MyArrayUtils.convertDoubleArrayToListDouble(
					_liftingSurfaceAerodynamicManagers
					.get(ComponentEnum.WING)
					.getPolar3DCurve()
					.get(_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.WING)
							.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)));
			break;
		case CRUISE:
			_current3DWingPolarCurve = MyArrayUtils.convertDoubleArrayToListDouble(
					_liftingSurfaceAerodynamicManagers
					.get(ComponentEnum.WING)
					.getPolar3DCurve()
					.get(_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.WING)
							.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)));
			break;
		case LANDING:
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS) &&
					!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)){
				if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD().get(MethodEnum.INPUT) == null){
					CalcHighLiftDevicesEffects calcHighLiftEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
					calcHighLiftEffects.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(),
							_currentMachNumber
							);

					MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getPolar3DCurve()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)))
					.stream().forEach(cd -> {
						_current3DWingPolarCurve.add(
								cd + 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getDeltaCD()
								.get(MethodEnum.SEMIEMPIRICAL));
					});
				}
				else{
					MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getPolar3DCurve()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)))
					.stream().forEach(cd -> {
						_current3DWingPolarCurve.add(
								cd + 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getDeltaCD()
								.get(MethodEnum.INPUT));
					});
				}
			}

			break;
		}

		//.........................................................................................................................
		//	WING MOMENT CURVE

		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

				CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcMomentCurve();

				calcMomentCurve.fromAirfoilDistribution();

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE,MethodEnum.AIRFOIL_DISTRIBUTION);
			}
		}

		switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
		case TAKE_OFF:
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS) &&
					!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)){
				if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD().get(MethodEnum.INPUT) == null){
					CalcHighLiftDevicesEffects calcHighLiftEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
					calcHighLiftEffects.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
							_currentMachNumber
							);

					MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getMoment3DCurve()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)))
					.stream().forEach(cm -> {
						_current3DWingMomentCurve.add(
								cm + 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getDeltaCMc4()
								.get(MethodEnum.SEMIEMPIRICAL));
					});
				}
				else{
					MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getMoment3DCurve()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)))
					.stream().forEach(cm -> {
						_current3DWingMomentCurve.add(
								cm + 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getDeltaCMc4()
								.get(MethodEnum.INPUT));
					});
				}
			}

			break;
		case CLIMB:
			_current3DWingMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
					_liftingSurfaceAerodynamicManagers
					.get(ComponentEnum.WING)
					.getMoment3DCurve()
					.get(_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.WING)
							.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));
			break;
		case CRUISE:
			_current3DWingMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
					_liftingSurfaceAerodynamicManagers
					.get(ComponentEnum.WING)
					.getMoment3DCurve()
					.get(_theAerodynamicBuilderInterface.getComponentTaskList()
							.get(ComponentEnum.WING)
							.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)));
			break;
		case LANDING:
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS) &&
					!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)){
				if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD().get(MethodEnum.INPUT) == null){
					CalcHighLiftDevicesEffects calcHighLiftEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
					calcHighLiftEffects.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(),
							_currentMachNumber
							);

					MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getMoment3DCurve()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)))
					.stream().forEach(cm -> {
						_current3DWingMomentCurve.add(
								cm + 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getDeltaCMc4()
								.get(MethodEnum.SEMIEMPIRICAL));
					});
				}
				else{
					MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getMoment3DCurve()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)))
					.stream().forEach(cm -> {
						_current3DWingMomentCurve.add(
								cm + 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getDeltaCMc4()
								.get(MethodEnum.INPUT));
					});
				}
			}

			break;
		}



		//.........................................................................................................................
		//	FUSELAGE POLAR_CURVE_3D

		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)) {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

				analyses.fuselage.FuselageAerodynamicsManager.CalcPolar calcFuselagePolarCurve = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcPolar();

				calcFuselagePolarCurve.fusDes();

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE,MethodEnum.FUSDES);
			}
		}

		//.........................................................................................................................
		//	FUSELAGE MOMENT_CURVE_3D

		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

				analyses.fuselage.FuselageAerodynamicsManager.CalcMomentCurve calcFuselageMomentCurve = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcMomentCurve();

				calcFuselageMomentCurve.fusDes();

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE,MethodEnum.FUSDES);
			}
		}


		//.........................................................................................................................
		//	HORIZONTAL TAIL LIFT_CURVES_3D

		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

				CalcHighLiftCurve calcHTailHighLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftCurve();

				_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach(de -> {
					List<Double> temporaryLiftHorizontalTail = new ArrayList<>();
					List<Amount<Angle>> temporaryDeList = new ArrayList<>();
					temporaryDeList.add(de);

					calcHTailHighLiftCurve.semiempirical(
							temporaryDeList, 
							null, 
							_currentMachNumber, 
							_currentAltitude
							);
					temporaryLiftHorizontalTail = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(MethodEnum.SEMIEMPIRICAL));

					_current3DHorizontalTailLiftCurve.put(
							de, 
							temporaryLiftHorizontalTail);

				});

			}
			_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D,MethodEnum.SEMIEMPIRICAL);
		}


		//.........................................................................................................................
		//	HORIZONTAL TAIL POLAR_CURVES_3D

		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

				CalcPolar calcBaselinePolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcPolar();

				calcBaselinePolarCurve.fromCdDistribution(_currentMachNumber, _currentAltitude);

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,MethodEnum.AIRFOIL_DISTRIBUTION);

				_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach(de -> {

					int i = _theAerodynamicBuilderInterface.getDeltaElevatorList().indexOf(de);
					_deltaCDElevatorList.add(0.0000156*
							Math.pow(de.doubleValue(NonSI.DEGREE_ANGLE),2) + 
							0.000002 * de.doubleValue(NonSI.DEGREE_ANGLE));

					List<Double> temporaryDragCurve = new ArrayList<>();
					MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL)
							.getPolar3DCurve().get(_theAerodynamicBuilderInterface
									.getComponentTaskList()
									.get(ComponentEnum.HORIZONTAL_TAIL)
									.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)))
					.stream()
					.forEach(cd -> {
						temporaryDragCurve.add(cd + _deltaCDElevatorList.get(i));
					});

					_current3DHorizontalTailPolarCurve.put(
							de, 
							temporaryDragCurve
							);
				});
			}

		}


		//.........................................................................................................................
		//	HORIZONTAL TAIL MOMENT_CURVES_3D


		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

				CalcMomentCurve calcBaselineMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMomentCurve();

				calcBaselineMomentCurve.fromAirfoilDistribution();
				_current3DHorizontalTailMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(MethodEnum.AIRFOIL_DISTRIBUTION)
						);
			}
			_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE,MethodEnum.AIRFOIL_DISTRIBUTION);
		}

		//.........................................................................................................................
		//	VERTICAL TAIL POLAR_CURVES_3D
		CalcCDAtAlpha calcCDAtBetaVerticalTail = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCDAtAlpha();
		_current3DVerticalTailDragCoefficient = calcCDAtBetaVerticalTail
				.fromCdDistribution(
						Amount.valueOf(0.0, NonSI.DEGREE_ANGLE),
						_currentMachNumber,
						_currentAltitude
						);
	}

	private void calculateWingHorizontalTailAndNacelleCurrentAlpha() {
		
		//...................................................................................
		// CALCULATION OF THE CURRENT ALPHA FOR WING AND HTAIL
		//...................................................................................
		this._alphaWingCurrent = this._alphaBodyCurrent.to(NonSI.DEGREE_ANGLE)
				.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE));
		
		//...................................................................................
		Amount<Angle> currentDownwashAngle = null;
		if (_theAerodynamicBuilderInterface.getDownwashConstant() == Boolean.TRUE){
			currentDownwashAngle = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_downwashAngleMap
									.get(Boolean.TRUE)
									.get(_theAerodynamicBuilderInterface.getComponentTaskList()
											.get(ComponentEnum.AIRCRAFT)
											.get(AerodynamicAndStabilityEnum.DOWNWASH)
											)
									),
							MyArrayUtils.convertListOfAmountTodoubleArray(_alphaBodyList),
							_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
							),
					NonSI.DEGREE_ANGLE
					);	
		}
		if (_theAerodynamicBuilderInterface.getDownwashConstant() == Boolean.FALSE){
			currentDownwashAngle = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_downwashAngleMap
									.get(Boolean.FALSE)
									.get(_theAerodynamicBuilderInterface.getComponentTaskList()
											.get(ComponentEnum.AIRCRAFT)
											.get(AerodynamicAndStabilityEnum.DOWNWASH)
											)
									),
							MyArrayUtils.convertListOfAmountTodoubleArray(_alphaBodyList),
							_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
							),
					NonSI.DEGREE_ANGLE
					);	
		}			
		
		this._alphaHTailCurrent = Amount.valueOf(
				_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
				- _theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE)
				- currentDownwashAngle.doubleValue(NonSI.DEGREE_ANGLE)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
				NonSI.DEGREE_ANGLE
				);
		
		//...................................................................................
		switch (_theAerodynamicBuilderInterface.getTheAircraft().getNacelles().getMountingPositionNacelles()) {
		case WING:
			_alphaNacelleCurrent = _alphaWingCurrent.to(NonSI.DEGREE_ANGLE); //TODO calculate upwash nacelles
			break;
		case FUSELAGE:
			_alphaNacelleCurrent = Amount.valueOf(
					_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
					- _theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE)
					- currentDownwashAngle.doubleValue(NonSI.DEGREE_ANGLE),
					NonSI.DEGREE_ANGLE
					);
			break;
		case HTAIL:
			_alphaNacelleCurrent = Amount.valueOf(
					_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
					- _theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE)
					- currentDownwashAngle.doubleValue(NonSI.DEGREE_ANGLE)
					+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
					NonSI.DEGREE_ANGLE
					);
			break;
		default:
			break;
		}
		//...................................................................................
	}
	
	public void calculate(String resultsFolderPath) {

		String aerodynamicAndStabilityFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "AERODYNAMIC_AND_STABILITY"
				+ File.separator
				);
		
		initializeAnalysis();

		//=================================================================================================
		// ANALYSES
		//=================================================================================================
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL)) {

			CalcTotalLiftCoefficient calcTotalLiftCoefficient = new CalcTotalLiftCoefficient();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.CL_TOTAL)) {
			case FROM_BALANCE_EQUATION:
				calcTotalLiftCoefficient.fromAircraftComponents();
				break;
			default:
				break;
			}
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL)) {

			CalcTotalDragCoefficient calcTotalDragCoefficient = new CalcTotalDragCoefficient();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.CD_TOTAL)) {
			case FROM_BALANCE_EQUATION:
				calcTotalDragCoefficient.fromAircraftComponents();
				break;
			case ROSKAM:
				calcTotalDragCoefficient.fromRoskam();
				break;
			default:
				break;
			}
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {

			CalcTotalMomentCoefficient calcTotalMomentCoefficient = new CalcTotalMomentCoefficient();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.CM_TOTAL)) {
			case FROM_BALANCE_EQUATION:
				calcTotalMomentCoefficient.fromAircraftComponents();
				break;
			default:
				break;
			}
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

			CalcLongitudinalStability calcLongitudinalStability = new CalcLongitudinalStability();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {
			case FROM_BALANCE_EQUATION:
				calcLongitudinalStability.fromForceBalanceEquation();
				break;
			default:
				break;
			}
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY)) {

			CalcDirectionalStability calcDirectionalStability = new CalcDirectionalStability();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY)) {
			case VEDSC_SIMPLIFIED_WING:
				calcDirectionalStability.vedscSimplifiedWing(_currentMachNumber); 
				break;
			case VEDSC_USAFDATCOM_WING:
				calcDirectionalStability.vedscUsafDatcomWing(_currentMachNumber); 
				break;
			default:
				break;
			}
		}
		
		//=================================================================================================
		// PLOTS
		//=================================================================================================
		
		// TODO : FILL ME !!
		
		//------------------------------------------------------------------------------------------------------------------------------------
		try {
			toXLSFile(aerodynamicAndStabilityFolderPath + "Aerodynamic_and_Stability");
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("resource")
	public static ACAerodynamicCalculator importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			ConditionEnum theCondition
			) throws IOException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading aerodynamic ansd stability analysis data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");

		Boolean readBalanceFromXLSFlag;
		
		String readBalanceFromXLSString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@balance_from_xls_file");
		
		if(readBalanceFromXLSString.equalsIgnoreCase("true"))
			readBalanceFromXLSFlag = Boolean.TRUE;
		else
			readBalanceFromXLSFlag = Boolean.FALSE;
		
		String fileBalanceXLS = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@file_balance");
		
		//===============================================================
		// READING BALANCE DATA
		//===============================================================
		List<Double> xCGAdimensionalPositions = new ArrayList<>();
		List<Double> zCGAdimensionalPositions = new ArrayList<>();
		Amount<Length> zCGLandingGears = null;
		
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */
		if(readBalanceFromXLSFlag == Boolean.TRUE) {

			File balanceFile = new File(
					MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
					+ theAircraft.getId() 
					+ File.separator
					+ "WEIGHTS"
					+ File.separator
					+ fileBalanceXLS);
			if(balanceFile.exists()) {

				FileInputStream readerXLS = new FileInputStream(balanceFile);
				Workbook workbook;
				if (balanceFile.getAbsolutePath().endsWith(".xls")) {
					workbook = new HSSFWorkbook(readerXLS);
				}
				else if (balanceFile.getAbsolutePath().endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(readerXLS);
				}
				else {
					throw new IllegalArgumentException("I don't know how to create that kind of new file");
				}

				//---------------------------------------------------------------
				// XCG POSITIONS
				Sheet sheetGlobalData = MyXLSUtils.findSheet(workbook, "GLOBAL RESULTS");
				Double xCGAtMaxTakeOffWeight = 0.0;
				Double xCGMaxForward = 0.0;
				Double xCGMaxAfterward = 0.0;
				
				if(sheetGlobalData != null) {
					//...............................................................
					// Xcg AT MAX TAKE-OFF WEIGHT
					Cell xCGAtMaxTakeOffWeightCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Xcg maximum take-off mass MAC").get(0)).getCell(2);
					if(xCGAtMaxTakeOffWeightCell != null)
						xCGAtMaxTakeOffWeight = xCGAtMaxTakeOffWeightCell.getNumericCellValue()/100;
					//...............................................................
					// MAX FORWARD Xcg
					Cell xCGMaxForwardCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Max forward Xcg MAC").get(0)).getCell(2);
					if(xCGMaxForwardCell != null)
						xCGMaxForward = xCGMaxForwardCell.getNumericCellValue()/100;
					//...............................................................
					// MAX AFTERWARD Xcg
					Cell xCGMaxAfterwardCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Max aft Xcg MAC").get(0)).getCell(2);
					if(xCGMaxAfterwardCell != null)
						xCGMaxAfterward = xCGMaxAfterwardCell.getNumericCellValue()/100;
				}
				
				xCGAdimensionalPositions.add(xCGMaxForward);
				xCGAdimensionalPositions.add(xCGAtMaxTakeOffWeight);
				xCGAdimensionalPositions.add(xCGMaxAfterward);
				
				//---------------------------------------------------------------
				// ZCG POSITIONS
				Double zCGAtMaxTakeOffWeight = 0.0;
				
				if(sheetGlobalData != null) {
					//...............................................................
					// Zcg AT MAX TAKE-OFF WEIGHT
					Cell zCGAtMaxTakeOffWeightCell = sheetGlobalData.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Zcg maximum take-off mass MAC").get(0)).getCell(2);
					if(zCGAtMaxTakeOffWeightCell != null)
						zCGAtMaxTakeOffWeight = zCGAtMaxTakeOffWeightCell.getNumericCellValue()/100;
				}
				
				zCGAdimensionalPositions.add(zCGAtMaxTakeOffWeight);
				zCGAdimensionalPositions.add(zCGAtMaxTakeOffWeight);
				zCGAdimensionalPositions.add(zCGAtMaxTakeOffWeight);
				
				//---------------------------------------------------------------
				// ZCG POSITIONS LANDING GEAR
				Sheet sheetLandingGear = MyXLSUtils.findSheet(workbook, "LANDING GEARS");
				
				if(sheetLandingGear != null) {
					//...............................................................
					// Zcg LANDING GEAR
					Cell zCGLandingGearCell = sheetLandingGear.getRow(MyXLSUtils.findRowIndex(sheetGlobalData, "Zcg BRF").get(0)).getCell(2);
					if(zCGLandingGearCell != null)
						zCGLandingGears = Amount.valueOf(zCGLandingGearCell.getNumericCellValue(), SI.METER);
				}
			}
			else {
				System.err.println("FILE '" + balanceFile.getAbsolutePath() + "' NOT FOUND!! \n\treturning...");
				return null;
			}
		}
		else {
		
			//---------------------------------------------------------------
			// XCG POSITIONS
			String xCGAdimensionalPositionsProperty = reader.getXMLPropertyByPath("//balance/adimensional_center_of_gravity_x_position_list");
			if(xCGAdimensionalPositionsProperty != null)
				xCGAdimensionalPositions = reader.readArrayDoubleFromXML("//balance/adimensional_center_of_gravity_x_position_list");
			
			//---------------------------------------------------------------
			// ZCG POSITIONS
			String zCGAdimensionalPositionsProperty = reader.getXMLPropertyByPath("//balance/adimensional_center_of_gravity_z_position_list");
			if(zCGAdimensionalPositionsProperty != null)
				zCGAdimensionalPositions = reader.readArrayDoubleFromXML("//balance/adimensional_center_of_gravity_z_position_list");
			
			//---------------------------------------------------------------
			// ZCG POSITIONS LANDING GEAR
			String zCGLandingGearsProperty = reader.getXMLPropertyByPath("//balance/rear_landing_gear_dimensional_center_of_gravity_z_position");
			if(zCGLandingGearsProperty != null)
				zCGLandingGears = reader.getXMLAmountLengthByPath("//balance/rear_landing_gear_dimensional_center_of_gravity_z_position");
		}
		
		//===============================================================
		// READING GLOBAL DATA
		//===============================================================
		Amount<Angle> alphaBodyInitial = null;
		Amount<Angle> alphaBodyFinal = null;
		Integer numberOfAlphaBody = null;
		Amount<Angle> betaInitial = null;
		Amount<Angle> betaFinal = null;
		Integer numberOfBeta = null;
		List<Amount<Angle>> alphaWingArrayForDistributions = new ArrayList<>();
		List<Amount<Angle>> alphaHorizontalTailArrayForDistributions = new ArrayList<>();
		List<Amount<Angle>> betaVerticalTailArrayForDistributions = new ArrayList<>();
		Integer numberOfPointsSemispanwiseWing = null;
		Integer numberOfPointsSemispanwiseHTail = null;
		Integer numberOfPointsSemispanwiseVTail = null;
		List<Amount<Angle>> deltaElevatorList = new ArrayList<>();
		List<Amount<Angle>> deltaRudderList = new ArrayList<>();
		Double adimensionalWingMomentumPole = null;
		Double adimensionalHTailMomentumPole = null;
		Double adimensionalVTailMomentumPole = null;
		Double adimensionalFuselageMomentumPole = null;
		Double horizontalTailDynamicPressureRatio = null;
		
		MyInterpolatingFunction tauElevator = new MyInterpolatingFunction();
		List<Double> tauElevatorFunction = new ArrayList<>();
		List<Amount<Angle>> tauElevatorFunctionDeltaElevator = new ArrayList<>();
		
		MyInterpolatingFunction tauRudder = new MyInterpolatingFunction();
		List<Double> tauRudderFunction = new ArrayList<>();
		List<Amount<Angle>> tauRudderFunctionDeltaRudder = new ArrayList<>();
		
		//---------------------------------------------------------------
		// ALPHA BODY INITIAL
		String alphaBodyInitialProperty = reader.getXMLPropertyByPath("//global_data/alpha_body_initial");
		if(alphaBodyInitialProperty != null)
			alphaBodyInitial = reader.getXMLAmountAngleByPath("//global_data/alpha_body_initial");
		
		//---------------------------------------------------------------
		// ALPHA BODY FINAL
		String alphaBodyFinalProperty = reader.getXMLPropertyByPath("//global_data/alpha_body_final");
		if(alphaBodyFinalProperty != null)
			alphaBodyFinal = reader.getXMLAmountAngleByPath("//global_data/alpha_body_final");
		
		//---------------------------------------------------------------
		// NUMBER OF ALPHA BODY
		String numberOfAlphaBodyProperty = reader.getXMLPropertyByPath("//global_data/number_of_alpha_body");
		if(numberOfAlphaBodyProperty != null)
			numberOfAlphaBody = Integer.valueOf(numberOfAlphaBodyProperty);
		
		//---------------------------------------------------------------
		// BETA INITIAL
		String betaInitialProperty = reader.getXMLPropertyByPath("//global_data/beta_initial");
		if(betaInitialProperty != null)
			betaInitial = reader.getXMLAmountAngleByPath("//global_data/beta_initial");
		
		//---------------------------------------------------------------
		// BETA FINAL
		String betaFinalProperty = reader.getXMLPropertyByPath("//global_data/beta_final");
		if(betaFinalProperty != null)
			betaFinal = reader.getXMLAmountAngleByPath("//global_data/beta_final");
		
		//---------------------------------------------------------------
		// NUMBER OF BETA 
		String numberOfBetaProperty = reader.getXMLPropertyByPath("//global_data/number_of_beta");
		if(numberOfBetaProperty != null)
			numberOfBeta = Integer.valueOf(numberOfBetaProperty);
		
		//---------------------------------------------------------------
		// ALPHA WING FOR DISTRIBUTION
		String alphaWingArrayForDistributionsProperty = reader.getXMLPropertyByPath("//global_data/alpha_wing_array_for_distributions");
		if(alphaWingArrayForDistributionsProperty != null)
			alphaWingArrayForDistributions = reader.readArrayofAmountFromXML("//global_data/alpha_wing_array_for_distributions");
		
		//---------------------------------------------------------------
		// ALPHA HORIZONTAL TAIL FOR DISTRIBUTION
		String alphaHorizontalTailArrayForDistributionsProperty = reader.getXMLPropertyByPath("//global_data/alpha_horizontal_tail_array_for_distributions");
		if(alphaHorizontalTailArrayForDistributionsProperty != null)
			alphaHorizontalTailArrayForDistributions = reader.readArrayofAmountFromXML("//global_data/alpha_horizontal_tail_array_for_distributions");
		
		//---------------------------------------------------------------
		// BETA VERTICAL TAIL FOR DISTRIBUTION
		String betaVerticalTailArrayForDistributionsProperty = reader.getXMLPropertyByPath("//global_data/beta_vertical_tail_array_for_distributions");
		if(betaVerticalTailArrayForDistributionsProperty != null)
			betaVerticalTailArrayForDistributions = reader.readArrayofAmountFromXML("//global_data/beta_vertical_tail_array_for_distributions");
		
		//---------------------------------------------------------------
		// NUMBER OF POINTS SEMISPANWISE WING
		String numberOfPointsSemispanwiseWingProperty = reader.getXMLPropertyByPath("//global_data/number_of_points_semispawise_wing");
		if(numberOfPointsSemispanwiseWingProperty != null)
			numberOfPointsSemispanwiseWing = Integer.valueOf(numberOfPointsSemispanwiseWingProperty);
		
		//---------------------------------------------------------------
		// NUMBER OF POINTS SEMISPANWISE HTAIL
		String numberOfPointsSemispanwiseHTailProperty = reader.getXMLPropertyByPath("//global_data/number_of_points_semispawise_horizontal_tail");
		if(numberOfPointsSemispanwiseHTailProperty != null)
			numberOfPointsSemispanwiseHTail = Integer.valueOf(numberOfPointsSemispanwiseHTailProperty);
		
		//---------------------------------------------------------------
		// NUMBER OF POINTS SEMISPANWISE VTAIL
		String numberOfPointsSemispanwiseVTailProperty = reader.getXMLPropertyByPath("//global_data/number_of_points_semispawise_vertical_tail");
		if(numberOfPointsSemispanwiseVTailProperty != null)
			numberOfPointsSemispanwiseVTail = Integer.valueOf(numberOfPointsSemispanwiseVTailProperty);
		
		//---------------------------------------------------------------
		// DELTA ELEVATOR LIST
		String deltaElevatorListProperty = reader.getXMLPropertyByPath("//global_data/delta_elevator_array");
		if(deltaElevatorListProperty != null)
			deltaElevatorList = reader.readArrayofAmountFromXML("//global_data/delta_elevator_array");
		
		//---------------------------------------------------------------
		// DELTA RUDDER LIST
		String deltaRudderListProperty = reader.getXMLPropertyByPath("//global_data/delta_rudder_array");
		if(deltaRudderListProperty != null)
			deltaRudderList = reader.readArrayofAmountFromXML("//global_data/delta_rudder_array");
		
		//---------------------------------------------------------------
		// WING MOMENTUM POLE
		String adimensionalWingMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_wing_momentum_pole");
		if(adimensionalWingMomentumPoleProperty != null)
			adimensionalWingMomentumPole = Double.valueOf(adimensionalWingMomentumPoleProperty);
		
		Amount<Length> wingMomentumPole =
				Amount.valueOf(
						adimensionalWingMomentumPole
						* theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)
						+ theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
						SI.METER
						);
 		
		//---------------------------------------------------------------
		// HTAIL MOMENTUM POLE
		String adimensionalHTailMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_horizontal_tail_momentum_pole");
		if(adimensionalHTailMomentumPoleProperty != null)
			adimensionalHTailMomentumPole = Double.valueOf(adimensionalHTailMomentumPoleProperty);
		
		Amount<Length> hTailMomentumPole =
				Amount.valueOf(
						adimensionalHTailMomentumPole
						* theAircraft.getHTail().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)
						+ theAircraft.getHTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
						SI.METER
						);
		
		//---------------------------------------------------------------
		// VTAIL MOMENTUM POLE
		String adimensionalVTailMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_vertical_tail_momentum_pole");
		if(adimensionalVTailMomentumPoleProperty != null)
			adimensionalVTailMomentumPole = Double.valueOf(adimensionalVTailMomentumPoleProperty);
		
		Amount<Length> vTailMomentumPole =
				Amount.valueOf(
						adimensionalVTailMomentumPole
						* theAircraft.getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)
						+ theAircraft.getVTail().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
						SI.METER
						);
		
		//---------------------------------------------------------------
		// FUSELAGE MOMENTUM POLE
		String adimensionalFuselageMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_fuselage_momentum_pole");
		if(adimensionalFuselageMomentumPoleProperty != null)
			adimensionalFuselageMomentumPole = Double.valueOf(adimensionalFuselageMomentumPoleProperty);
		
		//---------------------------------------------------------------
		// DYNAMIC PRESSURE RATIO
		String dynamicPressureRatioFromFileString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/@dynamic_pressure_ratio_from_file");
		
		if(dynamicPressureRatioFromFileString.equalsIgnoreCase("FALSE")){
			horizontalTailDynamicPressureRatio = 
					AerodynamicCalc.calculateDynamicPressureRatio(
							theAircraft.getHTail().getPositionRelativeToAttachment()
							);
		}
		else {
			String horizontalTailDynamicPressureRatioProperty = reader.getXMLPropertyByPath("//global_data/horizontal_tail_dynamic_pressure_ratio");
			if(horizontalTailDynamicPressureRatioProperty != null)
				horizontalTailDynamicPressureRatio = Double.valueOf(horizontalTailDynamicPressureRatioProperty);
		}
		
		//---------------------------------------------------------------
		// TAU ELEVATOR
		String tauElevatorFromFileString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/@tau_elevator_from_file");
		
		if(tauElevatorFromFileString.equalsIgnoreCase("FALSE")){
			double[] deltaElevatorArray = MyArrayUtils.linspace(-25, 5, 31);
 			for(int i=0; i<deltaElevatorArray.length; i++)
				tauElevatorFunction.add(
						LiftCalc.calculateTauIndexElevator(
								theAircraft.getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(), 
								theAircraft.getHTail().getLiftingSurfaceCreator().getAspectRatio(), 
								theAircraft.getHTail().getHighLiftDatabaseReader(), 
								theAircraft.getHTail().getAerodynamicDatabaseReader(), 
								Amount.valueOf(deltaElevatorArray[i], NonSI.DEGREE_ANGLE)
								)
						);
 			
			tauElevator.interpolateLinear(
					deltaElevatorArray,
					MyArrayUtils.convertToDoublePrimitive(tauElevatorFunction)
					);
		}
		else {
			String tauElevatorFunctionProperty = reader.getXMLPropertyByPath("//global_data/tau_elevator_function/tau");
			if(tauElevatorFunctionProperty != null)
				tauElevatorFunction = reader.readArrayDoubleFromXML("//global_data/tau_elevator_function/tau"); 
			String tauElevatorFunctionDeltaElevatorProperty = reader.getXMLPropertyByPath("//global_data/tau_elevator_function/delta_elevator");
			if(tauElevatorFunctionDeltaElevatorProperty != null)
				tauElevatorFunctionDeltaElevator = reader.readArrayofAmountFromXML("//global_data/tau_elevator_function/delta_elevator");
			
			if(tauElevatorFunction.size() > 1)
				if(tauElevatorFunction.size() != tauElevatorFunctionDeltaElevator.size())
				{
					System.err.println("TAU ELEVATOR ARRAY AND THE RELATED DELTA ELEVATOR ARRAY MUST HAVE THE SAME LENGTH !");
					System.exit(1);
				}
			if(tauElevatorFunction.size() == 1) {
				tauElevatorFunction.add(tauElevatorFunction.get(0));
				tauElevatorFunctionDeltaElevator.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				tauElevatorFunctionDeltaElevator.add(Amount.valueOf(360.0, NonSI.DEGREE_ANGLE));
			}
			
			tauElevator.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							tauElevatorFunctionDeltaElevator.stream()
							.map(f -> f.to(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(tauElevatorFunction)
					);
		}
		
		//---------------------------------------------------------------
		// TAU RUDDER
		String tauRudderFromFileString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/@tau_rudder_from_file");
		
		if(tauRudderFromFileString.equalsIgnoreCase("FALSE")){
			double[] deltaRudderArray = MyArrayUtils.linspace(-25, 25, 51);
 			for(int i=0; i<deltaRudderArray.length; i++)
				tauRudderFunction.add(
						LiftCalc.calculateTauIndexElevator(
								theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(), 
								theAircraft.getVTail().getLiftingSurfaceCreator().getAspectRatio(), 
								theAircraft.getVTail().getHighLiftDatabaseReader(), 
								theAircraft.getVTail().getAerodynamicDatabaseReader(), 
								Amount.valueOf(deltaRudderArray[i], NonSI.DEGREE_ANGLE)
								)
						);
 			
 			tauRudder.interpolateLinear(
					deltaRudderArray,
					MyArrayUtils.convertToDoublePrimitive(tauRudderFunction)
					);
		}
		else {
			String tauRudderFunctionProperty = reader.getXMLPropertyByPath("//global_data/tau_rudder_function/tau");
			if(tauRudderFunctionProperty != null)
				tauRudderFunction = reader.readArrayDoubleFromXML("//global_data/tau_rudder_function/tau"); 
			String tauRudderFunctionDeltaRudderProperty = reader.getXMLPropertyByPath("//global_data/tau_rudder_function/delta_rudder");
			if(tauRudderFunctionDeltaRudderProperty != null)
				tauRudderFunctionDeltaRudder = reader.readArrayofAmountFromXML("//global_data/tau_rudder_function/delta_rudder");
			
			if(tauRudderFunction.size() > 1)
				if(tauRudderFunction.size() != tauRudderFunctionDeltaRudder.size())
				{
					System.err.println("TAU RUDDER ARRAY AND THE RELATED DELTA RUDDER ARRAY MUST HAVE THE SAME LENGTH !");
					System.exit(1);
				}
			if(tauRudderFunction.size() == 1) {
				tauRudderFunction.add(tauRudderFunction.get(0));
				tauRudderFunctionDeltaRudder.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
				tauRudderFunctionDeltaRudder.add(Amount.valueOf(360.0, NonSI.DEGREE_ANGLE));
			}
			
			tauRudder.interpolateLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							tauRudderFunctionDeltaRudder.stream()
							.map(f -> f.to(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList())
							),
					MyArrayUtils.convertToDoublePrimitive(tauRudderFunction)
					);
		}
		
		//===============================================================
		// READING COMPONENTS TASK LIST DATA
		//===============================================================		
		// WING:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> wingTaskList = new HashMap<>();
		MyInterpolatingFunction wingLiftCurveFunction = null;
		MyInterpolatingFunction wingPolarCurveFunction = null;
		MyInterpolatingFunction wingMomentCurveFunction = null;
		MethodEnum wingCriticalMachMethod = null;
		MethodEnum wingAerodynamicCenterMethod = null;
		MethodEnum wingCLAtAlphaMethod = null;
		MethodEnum wingCLZeroMethod = null;
		MethodEnum wingAlphaZeroLiftMethod = null;
		MethodEnum wingCLStarMethod = null;
		MethodEnum wingAlphaStarMethod = null;
		MethodEnum wingCLAlphaMethod = null;
		MethodEnum wingCLMaxMethod = null;
		MethodEnum wingAlphaStallMethod = null;
		MethodEnum wingLiftCurveMethod = null;
		MethodEnum wingLiftDistributionMethod = null;
		MethodEnum wingCD0Method = null;
		MethodEnum wingCDInducedMethod = null;
		MethodEnum wingCDWaveMethod = null;
		MethodEnum wingOswaldFactorMethod = null;
		MethodEnum wingPolarCurveMethod = null;
		MethodEnum wingDragDistributionMethod = null;
		MethodEnum wingCDAtAlphaMethod = null;
		MethodEnum wingHighLiftDevicedEffectsMethod = null;
		MethodEnum wingHighLiftCurveMethod = null;
		MethodEnum wingCLAtAlphaHighLiftMethod = null;
		MethodEnum wingCDAtAlphaHighLiftMethod = null;
		MethodEnum wingCMAtAlphaHighLiftMethod = null;
		MethodEnum wingCMacMethod = null;
		MethodEnum wingCMAlphaMethod = null;
		MethodEnum wingCMAtAlphaMethod = null;
		MethodEnum wingMomentCurveMethod = null;
		MethodEnum wingMomentDistributionMethod = null;
		
		//---------------------------------------------------------------
		// CRITICAL MACH
		String wingCriticalMachPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/critical_mach/@perform");
		
		if(wingCriticalMachPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCriticalMachMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/critical_mach/@method");
			
			if(wingCriticalMachMethodString != null) {
				
				if(wingCriticalMachMethodString.equalsIgnoreCase("KORN_MASON")) 
					wingCriticalMachMethod = MethodEnum.KORN_MASON;
				
				if(wingCriticalMachMethodString.equalsIgnoreCase("KROO"))  
					wingCriticalMachMethod = MethodEnum.KROO;
					
				wingTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, wingCriticalMachMethod);
			}
		}
		
		
		//---------------------------------------------------------------
		// AERODYNAMIC CENTER
		String wingAerodynamicCenterPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/aerodynamic_center/@perform");
		
		if(wingAerodynamicCenterPerformString.equalsIgnoreCase("TRUE")){
			
			String wingAerodynamicCenterMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/aerodynamic_center/@method");
			
			if(wingAerodynamicCenterMethodString != null) {
				
				if(wingAerodynamicCenterMethodString.equalsIgnoreCase("QUARTER")) 
					wingAerodynamicCenterMethod = MethodEnum.QUARTER;
				
				if(wingAerodynamicCenterMethodString.equalsIgnoreCase("DEYOUNG_HARPER"))  
					wingAerodynamicCenterMethod = MethodEnum.DEYOUNG_HARPER;
				
				if(wingAerodynamicCenterMethodString.equalsIgnoreCase("NAPOLITANO_DATCOM"))  
					wingAerodynamicCenterMethod = MethodEnum.NAPOLITANO_DATCOM;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, wingAerodynamicCenterMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL ALPHA
		String wingCLAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/cL_alpha/@perform");
		
		if(wingCLAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCLAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/cL_alpha/@method");
			
			if(wingCLAlphaMethodString != null) {
				
				if(wingCLAlphaMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					wingCLAlphaMethod = MethodEnum.NASA_BLACKWELL;
				
				if(wingCLAlphaMethodString.equalsIgnoreCase("POLHAMUS"))  
					wingCLAlphaMethod = MethodEnum.POLHAMUS;
				
				if(wingCLAlphaMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC"))  
					wingCLAlphaMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(wingCLAlphaMethodString.equalsIgnoreCase("INTEGRAL_MEAN"))  
					wingCLAlphaMethod = MethodEnum.INTEGRAL_MEAN;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CL_ALPHA, wingCLAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL ZERO
		String wingCLZeroPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/cL_zero/@perform");
		
		if(wingCLZeroPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCLZeroMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/cL_zero/@method");
			
			if(wingCLZeroMethodString != null) {
				
				if(wingCLZeroMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					wingCLZeroMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(wingCLZeroMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					wingCLZeroMethod = MethodEnum.NASA_BLACKWELL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CL_ZERO, wingCLZeroMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL STAR
		String wingCLStarPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/cL_star/@perform");
		
		if(wingCLStarPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCLStarMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/cL_star/@method");
			
			if(wingCLStarMethodString != null) {
				
				if(wingCLStarMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					wingCLStarMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(wingCLStarMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					wingCLStarMethod = MethodEnum.NASA_BLACKWELL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CL_STAR, wingCLStarMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL MAX
		String wingCLMaxPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/cL_max/@perform");
		
		if(wingCLMaxPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCLMaxMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/cL_max/@method");
			
			if(wingCLMaxMethodString != null) {
				
				if(wingCLMaxMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
					wingCLMaxMethod = MethodEnum.PHILLIPS_ALLEY;
				
				if(wingCLMaxMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					wingCLMaxMethod = MethodEnum.NASA_BLACKWELL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CL_MAX, wingCLMaxMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA ZERO LIFT
		String wingAlphaZeroLiftPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/alpha_zero_lift/@perform");
		
		if(wingAlphaZeroLiftPerformString.equalsIgnoreCase("TRUE")){
			
			String wingAlphaZeroLiftMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/alpha_zero_lift/@method");
			
			if(wingAlphaZeroLiftMethodString != null) {
				
				if(wingAlphaZeroLiftMethodString.equalsIgnoreCase("INTEGRAL_MEAN_NO_TWIST")) 
					wingAlphaZeroLiftMethod = MethodEnum.INTEGRAL_MEAN_NO_TWIST;
				
				if(wingAlphaZeroLiftMethodString.equalsIgnoreCase("INTEGRAL_MEAN_TWIST"))  
					wingAlphaZeroLiftMethod = MethodEnum.INTEGRAL_MEAN_TWIST;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, wingAlphaZeroLiftMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA STAR
		String wingAlphaStarPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/alpha_star/@perform");
		
		if(wingAlphaStarPerformString.equalsIgnoreCase("TRUE")){
			
			String wingAlphaStarMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/alpha_star/@method");
			
			if(wingAlphaStarMethodString != null) {
				
				if(wingAlphaStarMethodString.equalsIgnoreCase("MEAN_AIRFOIL_INFLUENCE_AREAS")) 
					wingAlphaStarMethod = MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STAR, wingAlphaStarMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA STALL
		String wingAlphaStallPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/alpha_stall/@perform");
		
		if(wingAlphaStallPerformString.equalsIgnoreCase("TRUE")){
			
			String wingAlphaStallMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/alpha_stall/@method");
			
			if(wingAlphaStallMethodString != null) {
				
				if(wingAlphaStallMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
					wingAlphaStallMethod = MethodEnum.PHILLIPS_ALLEY;
				
				if(wingAlphaStallMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					wingAlphaStallMethod = MethodEnum.NASA_BLACKWELL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STALL, wingAlphaStallMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// LIFT CURVE
		String wingLiftCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/cL_vs_alpha/@perform");
		
		if(wingLiftCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String wingLiftCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/cL_vs_alpha/@method");
			
			if(wingLiftCurveMethodString != null) {
				
				if(wingLiftCurveMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
					wingLiftCurveMethod = MethodEnum.PHILLIPS_ALLEY;
				
				if(wingLiftCurveMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					wingLiftCurveMethod = MethodEnum.NASA_BLACKWELL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, wingLiftCurveMethod);
				
			}
		}
		else if(wingLiftCurvePerformString.equalsIgnoreCase("FALSE")){
			
			wingLiftCurveFunction = new MyInterpolatingFunction();
			List<Amount<Angle>> wingLiftCurveAlpha = reader.readArrayofAmountFromXML("//wing_analyses/lift/cL_vs_alpha/alpha");
			List<Double> wingLiftCurveCL = reader.readArrayDoubleFromXML("//wing_analyses/lift/cL_vs_alpha/cL");
			wingLiftCurveFunction.interpolate(
					MyArrayUtils.convertListOfAmountTodoubleArray(
							wingLiftCurveAlpha.stream()
							.map(a -> a.to(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList())
							), 
					MyArrayUtils.convertToDoublePrimitive(wingLiftCurveCL)
					);
			
		}
		
		//---------------------------------------------------------------
		// LIFT DISTRIBUTION
		String wingLiftDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/lift_distributions/@perform");

		if(wingLiftDistributionPerformString.equalsIgnoreCase("TRUE")){

			String wingLiftDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/lift_distributions/@method");

			if(wingLiftDistributionMethodString != null) {

				if(wingLiftDistributionMethodString.equalsIgnoreCase("SCHRENK")) 
					wingLiftDistributionMethod = MethodEnum.SCHRENK;

				if(wingLiftDistributionMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					wingLiftDistributionMethod = MethodEnum.NASA_BLACKWELL;

				wingTaskList.put(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION, wingLiftDistributionMethod);

			}
		}

		//---------------------------------------------------------------
		// CL AT ALPHA
		String wingCLAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/lift/cL_at_alpha_current/@perform");

		if(wingCLAtAlphaPerformString.equalsIgnoreCase("TRUE")){

			String wingCLAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/cL_at_alpha_current/@method");

			if(wingCLAtAlphaMethodString != null) {

				if(wingCLAtAlphaMethodString.equalsIgnoreCase("LINEAR_DLR")) 
					wingCLAtAlphaMethod = MethodEnum.LINEAR_DLR;

				if(wingCLAtAlphaMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					wingCLAtAlphaMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(wingCLAtAlphaMethodString.equalsIgnoreCase("LINEAR_NASA_BLACKWELL")) 
					wingCLAtAlphaMethod = MethodEnum.LINEAR_NASA_BLACKWELL;
				
				if(wingCLAtAlphaMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					wingCLAtAlphaMethod = MethodEnum.NASA_BLACKWELL;

				wingTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, wingCLAtAlphaMethod);

			}
		}
		
		/*
		 * TODO: FILL ME (create a Map<AerodynamicAndStabilityEnum, MethodEnum> 
		 *       with all the methods different from "null")
		 */


		//...............................................................
		// HORIZONTAL TAIL:
		//...............................................................
		/*
		 * TODO: FILL ME (create a Map<AerodynamicAndStabilityEnum, MethodEnum> 
		 *       with all the methods different from "null")
		 */
		
		//...............................................................
		// VERTICAL TAIL:
		//...............................................................
		/*
		 * TODO: FILL ME (create a Map<AerodynamicAndStabilityEnum, MethodEnum> 
		 *       with all the methods different from "null")
		 */
		
		//...............................................................
		// FUSELAGE:
		//...............................................................
		/*
		 * TODO: FILL ME (create a Map<AerodynamicAndStabilityEnum, MethodEnum> 
		 *       with all the methods different from "null")
		 */
		
		//...............................................................
		// NACELLES:
		//...............................................................
		/*
		 * TODO: FILL ME (create a Map<AerodynamicAndStabilityEnum, MethodEnum> 
		 *       with all the methods different from "null")
		 */
		
		//...............................................................
		// AIRCRAFT:
		//...............................................................
		/*
		 * TODO: FILL ME (create a Map<AerodynamicAndStabilityEnum, MethodEnum> 
		 *       with all the methods different from "null")
		 */
		
		//===============================================================
		// READING PLOT DATA
		//===============================================================
		
		// TODO: FILL ME !!
		
		/********************************************************************************************
		 * Once the data are ready, it's possible to create the manager object can be created
		 * using the builder pattern.
		 */
		IACAerodynamicCalculator theAerodynamicAndStabilityBuilderInterface = new IACAerodynamicCalculator.Builder()
				.setId(id)
				.setTheAircraft(theAircraft)
				.setTheOperatingConditions(theOperatingConditions)
				.setCurrentCondition(theCondition)
				.addAllXCGAircraft(xCGAdimensionalPositions)
				.addAllZCGAircraft(zCGAdimensionalPositions)
				.setZCGLandingGear(zCGLandingGears)
				.setAlphaBodyInitial(alphaBodyInitial)
				.setAlphaBodyFinal(alphaBodyFinal)
				.setNumberOfAlphasBody(numberOfAlphaBody)
				.setBetaInitial(betaInitial)
				.setBetaFinal(betaFinal)
				.setNumberOfBeta(numberOfBeta)
				.addAllAlphaWingForDistribution(alphaWingArrayForDistributions)
				.addAllAlphaHorizontalTailForDistribution(alphaHorizontalTailArrayForDistributions)
				.addAllBetaVerticalTailForDistribution(betaVerticalTailArrayForDistributions)
				.setWingNumberOfPointSemiSpanWise(numberOfPointsSemispanwiseWing)
				.setHTailNumberOfPointSemiSpanWise(numberOfPointsSemispanwiseHTail)
				.setVTailNumberOfPointSemiSpanWise(numberOfPointsSemispanwiseVTail)
				.addAllDeltaElevatorList(deltaElevatorList)
				.addAllDeltaRudderList(deltaRudderList)
				.setWingMomentumPole(wingMomentumPole)
				.setHTailMomentumPole(hTailMomentumPole)
				.setVTailMomentumPole(vTailMomentumPole)
				.setAdimensionalFuselageMomentumPole(adimensionalFuselageMomentumPole)
				.setDynamicPressureRatio(horizontalTailDynamicPressureRatio)
				.setTauElevatorFunction(tauElevator)
				.setTauRudderFunction(tauRudder)
				.putComponentTaskList(ComponentEnum.WING, wingTaskList)
				.setWingLiftCurveFunction(wingLiftCurveFunction)
				// TODO: CONTINUE
				.buildPartial();
		
		ACAerodynamicCalculator theAerodynamicAndStabilityManager = new ACAerodynamicCalculator();
		theAerodynamicAndStabilityManager.setTheAerodynamicBuilderInterface(theAerodynamicAndStabilityBuilderInterface);
		
		return theAerodynamicAndStabilityManager;

	}

	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException {

		// TODO : FILL ME !!

	}

	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\n\n\t-------------------------------------\n")
				.append("\tAerodynamic and Stability Analysis\n")
				.append("\t-------------------------------------\n")
				;

		// TODO : FILL ME !!

		return sb.toString();

	}

	//............................................................................
	// BUFFET BARRIER INNER CLASS
	//............................................................................
	public class CalcBuffetBarrier {

		public void kroo() {

			// TODO: see Aircraft Design Course (xlsx files)
			
		}

	}
	//............................................................................
	// END BUFFET BARRIER INNER CLASS
	//............................................................................

	//............................................................................
	// Total Lift Coefficient INNER CLASS
	//............................................................................
	public class CalcTotalLiftCoefficient {

		public void fromAircraftComponents() {
			_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> 
			_totalLiftCoefficient.put(
					de,
					LiftCalc.calculateCLTotalCurveWithEquation(
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurface(),  
							_current3DWingLiftCurve,
							_current3DHorizontalTailLiftCurve.get(de),
							_theAerodynamicBuilderInterface.getDynamicPressureRatio(), 
							_alphaBodyList)

					)
					);
		}
	}
	//............................................................................
	// END Total Lift Coefficient INNER CLASS
	//............................................................................

	//............................................................................
	// Total Drag Coefficient INNER CLASS
	//............................................................................
	public class CalcTotalDragCoefficient {

		public void fromAircraftComponents() {
			
			_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> 
			_totalDragCoefficient.put(
					de,
					DragCalc.calculateTotalPolarFromEquation(
							_current3DWingPolarCurve, 
							_current3DHorizontalTailPolarCurve.get(de), 
							_current3DVerticalTailDragCoefficient,
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurface(),
							_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurface(),
							MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
									.get(ComponentEnum.FUSELAGE)
									.getPolar3DCurve()
									.get(_theAerodynamicBuilderInterface.getComponentTaskList()
											.get(ComponentEnum.FUSELAGE)
											.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE))),
							_landingGearUsedDrag,
							_theAerodynamicBuilderInterface.getDeltaCD0Miscellaneous(),  // FIX THIS
							_theAerodynamicBuilderInterface.getDynamicPressureRatio(), 
							_alphaBodyList)
					)
					);

		}
		
		public void fromRoskam(){
			
			if(_totalLiftCoefficient.isEmpty()){
				CalcTotalLiftCoefficient calculateTotalLiftCoefficient = new CalcTotalLiftCoefficient();
				calculateTotalLiftCoefficient.fromAircraftComponents();
			}
			
			CalcCD0Total calcCD0TotalFuselage = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCD0Total();
			calcCD0TotalFuselage.semiempirical();
			
			analyses.nacelles.NacelleAerodynamicsManager.CalcCD0Total calcCD0TotalNacelle =
					_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCD0Total();
			calcCD0TotalNacelle.semiempirical();
			
			CalcCD0 calcCD0TotalWing = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCD0();
			calcCD0TotalWing.semiempirical(_currentMachNumber, _currentAltitude);

			Double cD0ParasiteWing = DragCalc.calculateCD0ParasiteLiftingSurface(
					_theAerodynamicBuilderInterface.getTheAircraft().getWing(),
					_theAerodynamicBuilderInterface.getTheOperatingConditions().getMachTransonicThreshold(),
					_currentMachNumber,
					_currentAltitude
					);
			
			CalcCD0 calcCD0TotalHTail = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCD0();
			calcCD0TotalHTail.semiempirical(_currentMachNumber, _currentAltitude);

			Double cD0ParasiteHTail = DragCalc.calculateCD0ParasiteLiftingSurface(
					_theAerodynamicBuilderInterface.getTheAircraft().getHTail(),
					_theAerodynamicBuilderInterface.getTheOperatingConditions().getMachTransonicThreshold(),
					_currentMachNumber,
					_currentAltitude
					);
			
			CalcCD0 calcCD0TotalVTail = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCD0();
			calcCD0TotalVTail.semiempirical(_currentMachNumber, _currentAltitude);

			Double cD0ParasiteVTail = DragCalc.calculateCD0ParasiteLiftingSurface(
					_theAerodynamicBuilderInterface.getTheAircraft().getVTail(),
					_theAerodynamicBuilderInterface.getTheOperatingConditions().getMachTransonicThreshold(),
					_currentMachNumber,
					_currentAltitude
					);
			
			Double cD0TotalAircraft = DragCalc.calculateCD0Total(
					_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
					_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL), 
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
					cD0ParasiteWing,
					_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCD0Total().get(MethodEnum.SEMIEMPIRICAL), 
					_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCD0Parasite().get(MethodEnum.SEMIEMPIRICAL),
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
					cD0ParasiteHTail, 
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCD0().get(MethodEnum.SEMIEMPIRICAL), 
					cD0ParasiteVTail
					);
			
			Double oswaldFactorTotalAircraft = AerodynamicCalc.calculateOswaldHowe(
					_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio(),
					_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getAspectRatio(),
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMeanAirfoil().getAirfoilCreator().getThicknessToChordRatio(),
					_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN),
					_theAerodynamicBuilderInterface.getTheAircraft().getWing().getNumberOfEngineOverTheWing(),
					_currentMachNumber
					);
			
			Double kDragPolarAircraft = 1/(
					_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio()
					*Math.PI
					*oswaldFactorTotalAircraft			
					);
			
			CalcCDWave calcCDWave = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCDWave();
			calcCDWave.lockKornWithKroo();
			
			Double cDWave = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDWave().get(MethodEnum.LOCK_KORN_WITH_KROO);
			
			_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach(de -> {
				_totalDragCoefficient.put(
						de, 
						_totalLiftCoefficient.get(de).stream().map(cL -> 
						cD0TotalAircraft + (Math.pow(cL, 2)*kDragPolarAircraft) + cDWave)
						.collect(Collectors.toList())
						);
			});
		}
	}
	//............................................................................
	// END Total Drag Coefficient INNER CLASS
	//............................................................................

	//............................................................................
	// Total Moment Coefficient INNER CLASS
	//............................................................................
	public class CalcTotalMomentCoefficient {

		public void fromAircraftComponents() {
			
			_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {

				int i = _theAerodynamicBuilderInterface.getXCGAircraft().indexOf(xcg);
				Map<Amount<Angle>, List<Double>> momentMap = new HashMap<>();
				_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach( de -> 
				momentMap.put(
						de,
						MomentCalc.calculateCMTotalCurveWithBalanceEquation(
								Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getX0().doubleValue(SI.METER), SI.METER),
								Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getZ0().doubleValue(SI.METER), SI.METER),
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getXacLRF()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.WING)
										.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes()), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(), 
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.getXacLRF()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.HORIZONTAL_TAIL)
										.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes()), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
								_theAerodynamicBuilderInterface.getZCGLandingGear(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getLiftingSurfaceCreator().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurface(), 
								_current3DWingLiftCurve,
								_current3DWingPolarCurve,
								_current3DWingMomentCurve,
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.FUSELAGE)
										.getMoment3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.FUSELAGE)
												.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE))),
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.FUSELAGE)
										.getPolar3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.FUSELAGE)
												.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE))),
								_current3DHorizontalTailLiftCurve.get(de),
							    _current3DHorizontalTailPolarCurve.get(de),
								_current3DHorizontalTailMomentCurve,
								_landingGearUsedDrag,
								_theAerodynamicBuilderInterface.getDynamicPressureRatio(), 
								_alphaBodyList, 
								_theAerodynamicBuilderInterface.getWingPendularStability())						
						)
						);
				_totalMomentCoefficient.put(
						xcg,
						momentMap
						);

			});
		}
	}
	//............................................................................
	// END Total Moment Coefficient INNER CLASS
	//............................................................................

	//............................................................................
	// Longitudinal Stability INNER CLASS
	//............................................................................
	public class CalcLongitudinalStability {

		public void fromForceBalanceEquation() { 

			//=======================================================================================
			// Calculating horizontal tail equilibrium lift coefficient ... CLh_e
			//=======================================================================================

			_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {

				int i = _theAerodynamicBuilderInterface.getXCGAircraft().indexOf(xcg);
	
				_horizontalTailEquilibriumLiftCoefficient.put(
						xcg,
						LiftCalc.calculateHorizontalTailEquilibriumLiftCoefficient(
								Amount.valueOf((xcg*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getX0().doubleValue(SI.METER), SI.METER), 
								Amount.valueOf((_theAerodynamicBuilderInterface.getZCGAircraft().get(i)*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeZ().doubleValue(SI.METER)+
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getZ0().doubleValue(SI.METER), SI.METER),
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getXacLRF()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.WING)
										.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes()),  
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),  
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.HORIZONTAL_TAIL)
								.getXacLRF()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.HORIZONTAL_TAIL)
										.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)).plus(_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getXApexConstructionAxes()), 
								_theAerodynamicBuilderInterface.getZCGLandingGear(),
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(),
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurface(), 
								_current3DWingLiftCurve,
								_current3DWingPolarCurve,
								_current3DWingMomentCurve,
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.FUSELAGE)
										.getMoment3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.FUSELAGE)
												.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE))),
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.FUSELAGE)
										.getPolar3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.FUSELAGE)
												.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE))),
								_landingGearUsedDrag,
								_theAerodynamicBuilderInterface.getDynamicPressureRatio(), 
								_alphaBodyList, 
								_theAerodynamicBuilderInterface.getWingPendularStability()
								));
						
			});

			//=======================================================================================
			// Calculating total equilibrium lift coefficient ... CLtot_e
			//=======================================================================================

			
			_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {
			_totalEquilibriumLiftCoefficient.put(
					xcg,
					LiftCalc.calculateCLTotalCurveWithEquation(
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurface(),  
							_current3DWingLiftCurve,
							_horizontalTailEquilibriumLiftCoefficient.get(xcg),
							_theAerodynamicBuilderInterface.getDynamicPressureRatio(), 
							_alphaBodyList));

			});
			
			//=======================================================================================
			// Calculating delta e equilibrium ... deltae_e
			//=======================================================================================

			Map<Amount<Angle>, List<Double>> liftCoefficientHorizontalTailForEquilibrium = new HashMap<>();
			
			CalcHighLiftCurve calcHTailHighLiftCurveForDEEquilibrium = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftCurve();

			_deltaEForEquilibrium.stream().forEach(de -> {
				List<Double> temporaryLiftHorizontalTail = new ArrayList<>();
				List<Amount<Angle>> temporaryDeList = new ArrayList<>();
				temporaryDeList.add(de);
				
				calcHTailHighLiftCurveForDEEquilibrium.semiempirical(
						temporaryDeList, 
						null, 
						_currentMachNumber, 
						_currentAltitude
						);
				
				temporaryLiftHorizontalTail = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(MethodEnum.SEMIEMPIRICAL));

				liftCoefficientHorizontalTailForEquilibrium.put(
						de, 
						temporaryLiftHorizontalTail);
				

			});
			
			_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {
			_deltaEEquilibrium.put(xcg, 
					AerodynamicCalc.calculateDeltaEEquilibrium(
					liftCoefficientHorizontalTailForEquilibrium, 
					_deltaEForEquilibrium, 
					_horizontalTailEquilibriumLiftCoefficient.get(xcg), 
					_alphaBodyList
					));
			});
			
			//=======================================================================================
			// Calculating total equilibrium Drag coefficient ... CDtot_e
			//=======================================================================================
			
			_deltaEForEquilibrium.stream().forEach(de -> {
		
				int i = _theAerodynamicBuilderInterface.getDeltaElevatorList().indexOf(de);
			_deltaCDElevatorList.add(0.0000156*
					Math.pow(de.doubleValue(NonSI.DEGREE_ANGLE),2) + 
					0.000002 * de.doubleValue(NonSI.DEGREE_ANGLE));
			
			List<Double> temporaryDragCurveForElevatorDeflection = new ArrayList<>();
			MyArrayUtils.convertDoubleArrayToListDouble(
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL)
					.getPolar3DCurve().get(_theAerodynamicBuilderInterface
					.getComponentTaskList()
					.get(ComponentEnum.HORIZONTAL_TAIL)
					.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)))
			.stream()
			.forEach(cd -> {
				temporaryDragCurveForElevatorDeflection.add(cd + _deltaCDElevatorList.get(i));
			});
			_3DHorizontalTailPolarCurveForElevatorDeflection.put(
					de, 
					temporaryDragCurveForElevatorDeflection
					);
			});

			
			_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(xcg -> {	
				_horizontalTailEquilibriumCoefficient = new ArrayList<>();
				
				_horizontalTailEquilibriumCoefficient = 
					DragCalc.calculateTrimmedPolar(
							_3DHorizontalTailPolarCurveForElevatorDeflection,
							_deltaEEquilibrium.get(xcg), 
							_deltaEForEquilibrium, 
							_alphaBodyList);
		
				_totalEquilibriumDragCoefficient.put(xcg, 
						DragCalc.calculateTotalPolarFromEquation(
						_current3DWingPolarCurve, 
						_horizontalTailEquilibriumCoefficient, 
						_current3DVerticalTailDragCoefficient,
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(), 
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getSurface(),
						_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurface(),
						MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.FUSELAGE)
								.getPolar3DCurve()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.FUSELAGE)
										.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE))),
						_landingGearUsedDrag,
						_theAerodynamicBuilderInterface.getDeltaCD0Miscellaneous(),  // FIX THIS
						_theAerodynamicBuilderInterface.getDynamicPressureRatio(), 
						_alphaBodyList));
			});
			
			
			
			
			
			
		}
	}
	//............................................................................
	// END Longitudinal Stability INNER CLASS
	//............................................................................


	//............................................................................
	// Directional Stability INNER CLASS
	//............................................................................
	public class CalcDirectionalStability {

		public void vedscSimplifiedWing(Double mach) {

			//=======================================================================================
			// Calculating stability derivatives for each component ...
			//=======================================================================================
			_cNbFuselage.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaFuselage(
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFusDesDatabaseReader(), 
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getVEDSCDatabaseReader(),
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getLambdaF(), 
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getLambdaN(), 
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getLambdaT(), 
											((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
													+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
													+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
											/_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getLength().doubleValue(SI.METER),
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterGM(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSpan(),
											Amount.valueOf(
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER)
															),
													SI.METER
													), 
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
											/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2),
											_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getPositionRelativeToAttachment(),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment()
											)
									)
							)
					.collect(Collectors.toList())
					);



			_cNbWing.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaWing(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord())
									)
							)
					.collect(Collectors.toList())
					);


			_cNbVertical.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNbetaVerticalTail(
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(), 
											Math.abs(
													(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER))
													- ((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
															+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
															+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
													),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurface().doubleValue(SI.SQUARE_METRE),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMeanAirfoil().getAirfoilCreator().getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
											mach, 
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVEDSCDatabaseReader().get_KFv_vs_bv_over_dfv(
													_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER), 
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER)
															), 
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2)
													),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVEDSCDatabaseReader().get_KWv_vs_zw_over_rf(
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment(),
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2)),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVEDSCDatabaseReader().get_KHv_vs_zh_over_bv1(
													_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getPositionRelativeToAttachment(),
													_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2), 
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment())

											)
									)
							).collect(Collectors.toList())
					);


			_cNbTotal.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									_cNbVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
									._2()
									+ _cNbWing.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
									._2()
									+ _cNbFuselage.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
									._2()
									)
							).collect(Collectors.toList())
					);

			//=======================================================================================
			// Calculating control derivatives ...
			//=======================================================================================
			Map<Amount<Angle>, List<Tuple2<Double, Double>>> cNdrMap = new HashMap<>();

			_theAerodynamicBuilderInterface.getDeltaRudderList().stream().forEach(
					dr -> cNdrMap.put(
							dr,
							_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
									x -> Tuple.of(
											x,
											MomentCalc.calcCNdr(
													_cNbVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
													.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
													._2(),
													dr, 
													_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
													_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getAspectRatio(),
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAerodynamicDatabaseReader(), 
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getHighLiftDatabaseReader()
													)
											)
									)
							.collect(Collectors.toList())
							)
					);	

			_cNdr.put(MethodEnum.VEDSC_SIMPLIFIED_WING, cNdrMap);

			//=======================================================================================
			// Calculating yawing coefficient breakdown ...
			//=======================================================================================
			_cNFuselage.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcNonLinearCNFuselage(
											_cNbFuselage.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_betaList
											)
									)
							).collect(Collectors.toList())
					);

			_cNWing.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNWing(
											_cNbWing.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_betaList
											)
									)
							).collect(Collectors.toList())
					);

			_cNVertical.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcNonLinearCNVTail(
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAerodynamicDatabaseReader(),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSweepLEEquivalent(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
											.getMeanAirfoil().getAirfoilCreator().getThicknessToChordRatio(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
											.getMeanAirfoil().getAirfoilCreator().getFamily(),
											_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLMax().get(
													_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)
													)*Math.cos(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStall().get(
																	_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
																	).doubleValue(SI.RADIAN)
															),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurface(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(),
											Amount.valueOf(
													Math.abs(
															(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
																	.getXacLRF().get(
																			_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																			).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
																	.doubleValue(SI.METER))
															- ((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
																	+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
																	+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
															),
													SI.METER),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStar().get(
													_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
													),
											_betaList
											)
									)
							).collect(Collectors.toList())
					);

			_cNTotal.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcTotalCN(
											_cNFuselage.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_cNWing.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_cNVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2()
											)
									)
							).collect(Collectors.toList())
					);

			Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> cNDueToDeltaRudderMap = new HashMap<>();

			List<Double> tauRudderList = new ArrayList<>();
			if(_theAerodynamicBuilderInterface.getTauRudderFunction() == null)
				_theAerodynamicBuilderInterface.getDeltaRudderList().stream()
				.forEach(dr -> tauRudderList.add(
						StabilityCalculators.calculateTauIndex(
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAerodynamicDatabaseReader(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getHighLiftDatabaseReader(), 
								dr
								)
						));
			else
				_theAerodynamicBuilderInterface.getDeltaRudderList().stream()
				.forEach(dr -> tauRudderList.add(
						_theAerodynamicBuilderInterface.getTauRudderFunction().value(dr.doubleValue(NonSI.DEGREE_ANGLE))
						));

			_theAerodynamicBuilderInterface.getDeltaRudderList().stream().forEach(
					dr -> cNDueToDeltaRudderMap.put(
							dr,
							_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
									x -> Tuple.of(
											x,
											MomentCalc.calcCNDueToDeltaRudder(
													_betaList,
													_cNVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2,
													_cNbVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2,
													dr, 
													tauRudderList.get(_theAerodynamicBuilderInterface.getDeltaRudderList().indexOf(dr))
													)
											)
									)
							.collect(Collectors.toList())
							)
					);	

			_cNDueToDeltaRudder.put(MethodEnum.VEDSC_SIMPLIFIED_WING, cNDueToDeltaRudderMap);

			//=======================================================================================
			// Calculating dr_equilibrium for each beta ...
			//=======================================================================================
			Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>> betaOfEquilibriumListAtCG = new HashMap<>();

			_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(
					x -> betaOfEquilibriumListAtCG.put(
							x, 
							_theAerodynamicBuilderInterface.getDeltaRudderList().stream().map(
									dr -> Tuple.of( 
											Amount.valueOf(
													MyMathUtils.getIntersectionXAndY(
															MyArrayUtils.convertListOfAmountTodoubleArray(_betaList),
															MyArrayUtils.convertToDoublePrimitive(
																	_cNDueToDeltaRudder.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
																	.get(dr)
																	.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
																	._2()
																	),
															MyArrayUtils.linspace(
																	0.0,
																	0.0,
																	_betaList.size()
																	)
															).get(0)._1(),
													NonSI.DEGREE_ANGLE),
											dr)
									)
							.collect(Collectors.toList())
							)
					);

			_betaOfEquilibrium.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING, 
					betaOfEquilibriumListAtCG
					);
		}

		public void vedscUsafDatcomWing(Double mach) {

			//=======================================================================================
			// Calculating stability derivatives for each component ...
			//=======================================================================================
			_cNbFuselage.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaFuselage(
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFusDesDatabaseReader(), 
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getVEDSCDatabaseReader(),
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getLambdaF(), 
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getLambdaN(), 
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getLambdaT(), 
											((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
													+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
													+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
											/_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getLength().doubleValue(SI.METER),
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterGM(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSpan(),
											Amount.valueOf(
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER)
															),
													SI.METER
													), 
											_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
											/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2),
											_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getPositionRelativeToAttachment(),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment()
											)
									)
							)
					.collect(Collectors.toList())
					);

			CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAtAlpha();

			_cNbWing.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaWing(
											calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaBodyCurrent),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
											_liftingSurfaceAerodynamicManagers
											.get(ComponentEnum.WING)
											.getXacMRF().get(
													_theAerodynamicBuilderInterface.getComponentTaskList()
													.get(ComponentEnum.WING)	
													.get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
													), 
											x
											)
									)
							)
					.collect(Collectors.toList())
					);


			_cNbVertical.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNbetaVerticalTail(
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(), 
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(), 
											Math.abs(
													(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER))
													- ((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
															+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
															+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
													),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurface().doubleValue(SI.SQUARE_METRE),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMeanAirfoil().getAirfoilCreator().getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
											mach, 
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVEDSCDatabaseReader().get_KFv_vs_bv_over_dfv(
													_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER), 
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER)
															), 
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2)
													),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVEDSCDatabaseReader().get_KWv_vs_zw_over_rf(
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment(),
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2)),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getVEDSCDatabaseReader().get_KHv_vs_zh_over_bv1(
													_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getPositionRelativeToAttachment(),
													_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
													_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2), 
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getPositionRelativeToAttachment())

											)
									)
							).collect(Collectors.toList())
					);


			_cNbTotal.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
									._2()
									+ _cNbWing.get(MethodEnum.VEDSC_USAFDATCOM_WING)
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
									._2()
									+ _cNbFuselage.get(MethodEnum.VEDSC_USAFDATCOM_WING)
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
									._2()
									)
							).collect(Collectors.toList())
					);

			//=======================================================================================
			// Calculating control derivatives ...
			//=======================================================================================
			Map<Amount<Angle>, List<Tuple2<Double, Double>>> cNdrMap = new HashMap<>();

			_theAerodynamicBuilderInterface.getDeltaRudderList().stream().forEach(
					dr -> cNdrMap.put(
							dr,
							_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
									x -> Tuple.of(
											x,
											MomentCalc.calcCNdr(
													_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
													.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
													._2(),
													dr, 
													_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
													_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getAspectRatio(),
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAerodynamicDatabaseReader(), 
													_theAerodynamicBuilderInterface.getTheAircraft().getWing().getHighLiftDatabaseReader()
													)
											)
									)
							.collect(Collectors.toList())
							)
					);	

			_cNdr.put(MethodEnum.VEDSC_USAFDATCOM_WING, cNdrMap);		

			//=======================================================================================
			// Calculating yawing coefficient breakdown ...
			//=======================================================================================
			_cNFuselage.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcNonLinearCNFuselage(
											_cNbFuselage.get(MethodEnum.VEDSC_USAFDATCOM_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_betaList
											)
									)
							).collect(Collectors.toList())
					);

			_cNWing.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNWing(
											_cNbWing.get(MethodEnum.VEDSC_USAFDATCOM_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_betaList
											)
									)
							).collect(Collectors.toList())
					);

			_cNVertical.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcNonLinearCNVTail(
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAerodynamicDatabaseReader(),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSweepLEEquivalent(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
											.getMeanAirfoil().getAirfoilCreator().getThicknessToChordRatio(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
											.getMeanAirfoil().getAirfoilCreator().getFamily(),
											_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLMax().get(
													_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)
													)*Math.cos(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStall().get(
																	_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
																	).doubleValue(SI.RADIAN)
															),
											_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getSurface(),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(),
											Amount.valueOf(
													Math.abs(
															(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
																	.getXacLRF().get(
																			_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																			).plus(_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getXApexConstructionAxes())
																	.doubleValue(SI.METER))
															- ((x*_theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
																	+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
																	+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER))
															),
													SI.METER),
											_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStar().get(
													_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
													),
											_betaList
											)
									)
							).collect(Collectors.toList())
					);

			_cNTotal.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcTotalCN(
											_cNFuselage.get(MethodEnum.VEDSC_USAFDATCOM_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_cNWing.get(MethodEnum.VEDSC_USAFDATCOM_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2(),
											_cNVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
											.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
											._2()
											)
									)
							).collect(Collectors.toList())
					);

			Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> cNDueToDeltaRudderMap = new HashMap<>();

			List<Double> tauRudderList = new ArrayList<>();
			if(_theAerodynamicBuilderInterface.getTauRudderFunction() == null)
				_theAerodynamicBuilderInterface.getDeltaRudderList().stream()
				.forEach(dr -> tauRudderList.add(
						StabilityCalculators.calculateTauIndex(
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAspectRatio(),
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getAerodynamicDatabaseReader(), 
								_theAerodynamicBuilderInterface.getTheAircraft().getVTail().getHighLiftDatabaseReader(), 
								dr
								)
						));
			else
				_theAerodynamicBuilderInterface.getDeltaRudderList().stream()
				.forEach(dr -> tauRudderList.add(
						_theAerodynamicBuilderInterface.getTauRudderFunction().value(dr.doubleValue(NonSI.DEGREE_ANGLE))
						));

			_theAerodynamicBuilderInterface.getDeltaRudderList().stream().forEach(
					dr -> cNDueToDeltaRudderMap.put(
							dr,
							_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
									x -> Tuple.of(
											x,
											MomentCalc.calcCNDueToDeltaRudder(
													_betaList,
													_cNVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING).get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2,
													_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING).get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))._2,
													dr, 
													tauRudderList.get(_theAerodynamicBuilderInterface.getDeltaRudderList().indexOf(dr))
													)
											)
									)
							.collect(Collectors.toList())
							)
					);	

			_cNDueToDeltaRudder.put(MethodEnum.VEDSC_USAFDATCOM_WING, cNDueToDeltaRudderMap);

			//=======================================================================================
			// Calculating dr_equilibrium for each beta ...
			//=======================================================================================
			Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>> betaOfEquilibriumListAtCG = new HashMap<>();

			_theAerodynamicBuilderInterface.getXCGAircraft().stream().forEach(
					x -> betaOfEquilibriumListAtCG.put(
							x, 
							_theAerodynamicBuilderInterface.getDeltaRudderList().stream().map(
									dr -> Tuple.of( 
											Amount.valueOf(
													MyMathUtils.getIntersectionXAndY(
															MyArrayUtils.convertListOfAmountTodoubleArray(_betaList),
															MyArrayUtils.convertToDoublePrimitive(
																	_cNDueToDeltaRudder.get(MethodEnum.VEDSC_USAFDATCOM_WING)
																	.get(dr)
																	.get(_theAerodynamicBuilderInterface.getXCGAircraft().indexOf(x))
																	._2()
																	),
															MyArrayUtils.linspace(
																	0.0,
																	0.0,
																	_betaList.size()
																	)
															).get(0)._1(),
													NonSI.DEGREE_ANGLE),
											dr)
									)
							.collect(Collectors.toList())
							)
					);

			_betaOfEquilibrium.put(
					MethodEnum.VEDSC_USAFDATCOM_WING, 
					betaOfEquilibriumListAtCG
					);
		}

	}
	//............................................................................
	// END Directional Stability INNER CLASS
	//............................................................................

	//............................................................................
	// GETTERS & SETTERS:
	//............................................................................
	public IACAerodynamicCalculator getTheAerodynamicBuilderInterface() {
		return _theAerodynamicBuilderInterface;
	}

	public void setTheAerodynamicBuilderInterface(IACAerodynamicCalculator _theAerodynamicBuilderInterface) {
		this._theAerodynamicBuilderInterface = _theAerodynamicBuilderInterface;
	}

	public Double getCurrentMachNumber() {
		return _currentMachNumber;
	}

	public void setCurrentMachNumber(Double _currentMachNumber) {
		this._currentMachNumber = _currentMachNumber;
	}

	public Amount<Length> getCurrentAltitude() {
		return _currentAltitude;
	}

	public void setCurrentAltitude(Amount<Length> _currentAltitude) {
		this._currentAltitude = _currentAltitude;
	}

	public Amount<Length> getZACRootWing() {
		return _zACRootWing;
	}

	public void setZACRootWing(Amount<Length> _zACRootWing) {
		this._zACRootWing = _zACRootWing;
	}

	public Amount<Length> getHorizontalDistanceQuarterChordWingHTail() {
		return _horizontalDistanceQuarterChordWingHTail;
	}

	public void setHorizontalDistanceQuarterChordWingHTail(Amount<Length> _horizontalDistanceQuarterChordWingHTail) {
		this._horizontalDistanceQuarterChordWingHTail = _horizontalDistanceQuarterChordWingHTail;
	}

	public Amount<Length> getVerticalDistanceZeroLiftDirectionWingHTailPARTIAL() {
		return _verticalDistanceZeroLiftDirectionWingHTailPARTIAL;
	}

	public void setVerticalDistanceZeroLiftDirectionWingHTailPARTIAL(
			Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailPARTIAL) {
		this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = _verticalDistanceZeroLiftDirectionWingHTailPARTIAL;
	}

	public Amount<Length> getVerticalDistanceZeroLiftDirectionWingHTailCOMPLETE() {
		return _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	}

	public void setVerticalDistanceZeroLiftDirectionWingHTailCOMPLETE(
			Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE) {
		this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	}

	public Amount<Length> getVerticalDistanceZeroLiftDirectionWingHTailEFFECTIVE() {
		return _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE;
	}

	public void setVerticalDistanceZeroLiftDirectionWingHTailEFFECTIVE(
			Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE) {
		this._verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE = _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE;
	}

	public List<List<Double>> getDiscretizedWingAirfoilsCl() {
		return _discretizedWingAirfoilsCl;
	}

	public void setDiscretizedWingAirfoilsCl(List<List<Double>> _discretizedWingAirfoilsCl) {
		this._discretizedWingAirfoilsCl = _discretizedWingAirfoilsCl;
	}

	public List<List<Double>> getDiscretizedWingAirfoilsCd() {
		return _discretizedWingAirfoilsCd;
	}

	public void setDiscretizedWingAirfoilsCd(List<List<Double>> _discretizedWingAirfoilsCd) {
		this._discretizedWingAirfoilsCd = _discretizedWingAirfoilsCd;
	}

	public List<List<Double>> getDiscretizedWingAirfoilsCm() {
		return _discretizedWingAirfoilsCm;
	}

	public void setDiscretizedWingAirfoilsCm(List<List<Double>> _discretizedWingAirfoilsCm) {
		this._discretizedWingAirfoilsCm = _discretizedWingAirfoilsCm;
	}

	public List<List<Double>> getDiscretizedHTailAirfoilsCl() {
		return _discretizedHTailAirfoilsCl;
	}

	public void setDiscretizedHTailAirfoilsCl(List<List<Double>> _discretizedHTailAirfoilsCl) {
		this._discretizedHTailAirfoilsCl = _discretizedHTailAirfoilsCl;
	}

	public List<List<Double>> getDiscretizedHTailAirfoilsCd() {
		return _discretizedHTailAirfoilsCd;
	}

	public void setDiscretizedHTailAirfoilsCd(List<List<Double>> _discretizedHTailAirfoilsCd) {
		this._discretizedHTailAirfoilsCd = _discretizedHTailAirfoilsCd;
	}

	public List<Amount<Angle>> getAlphaBodyList() {
		return _alphaBodyList;
	}

	public void setAlphaBodyList(List<Amount<Angle>> _alphaBodyList) {
		this._alphaBodyList = _alphaBodyList;
	}

	public List<Amount<Angle>> getAlphaWingList() {
		return _alphaWingList;
	}

	public void setAlphaWingList(List<Amount<Angle>> _alphaWingList) {
		this._alphaWingList = _alphaWingList;
	}

	public List<Amount<Angle>> getAlphaHTailList() {
		return _alphaHTailList;
	}

	public void setAlphaHTailList(List<Amount<Angle>> _alphaHTailList) {
		this._alphaHTailList = _alphaHTailList;
	}

	public List<Amount<Angle>> getBetaList() {
		return _betaList;
	}

	public void setBetaList(List<Amount<Angle>> _betaList) {
		this._betaList = _betaList;
	}

	public Map<ComponentEnum, LSAerodynamicsManager> getLiftingSurfaceAerodynamicManagers() {
		return _liftingSurfaceAerodynamicManagers;
	}

	public void setLiftingSurfaceAerodynamicManagers(
			Map<ComponentEnum, LSAerodynamicsManager> _liftingSurfaceAerodynamicManagers) {
		this._liftingSurfaceAerodynamicManagers = _liftingSurfaceAerodynamicManagers;
	}

	public Map<ComponentEnum, FuselageAerodynamicsManager> getFuselageAerodynamicManagers() {
		return _fuselageAerodynamicManagers;
	}

	public void setFuselageAerodynamicManagers(
			Map<ComponentEnum, FuselageAerodynamicsManager> _fuselageAerodynamicManagers) {
		this._fuselageAerodynamicManagers = _fuselageAerodynamicManagers;
	}

	public Map<ComponentEnum, NacelleAerodynamicsManager> getNacelleAerodynamicManagers() {
		return _nacelleAerodynamicManagers;
	}

	public void setNacelleAerodynamicManagers(Map<ComponentEnum, NacelleAerodynamicsManager> _nacelleAerodynamicManagers) {
		this._nacelleAerodynamicManagers = _nacelleAerodynamicManagers;
	}


	public Amount<?> getClAlphaWingFuselage() {
		return _clAlphaWingFuselage;
	}

	public void setClAlphaWingFuselage(Amount<?> _clAlphaWingFuselage) {
		this._clAlphaWingFuselage = _clAlphaWingFuselage;
	}

	public Double getClZeroWingFuselage() {
		return _clZeroWingFuselage;
	}

	public void setClZeroWingFuselage(Double _clZeroWingFuselage) {
		this._clZeroWingFuselage = _clZeroWingFuselage;
	}

	public Double getClMaxWingFuselage() {
		return _clMaxWingFuselage;
	}

	public void setClMaxWingFuselage(Double _clMaxWingFuselage) {
		this._clMaxWingFuselage = _clMaxWingFuselage;
	}

	public Double getClStarWingFuselage() {
		return _clStarWingFuselage;
	}

	public void setClStarWingFuselage(Double _clStarWingFuselage) {
		this._clStarWingFuselage = _clStarWingFuselage;
	}

	public Amount<Angle> getAlphaStarWingFuselage() {
		return _alphaStarWingFuselage;
	}

	public void setAlphaStarWingFuselage(Amount<Angle> _alphaStarWingFuselage) {
		this._alphaStarWingFuselage = _alphaStarWingFuselage;
	}

	public Amount<Angle> getAlphaStallWingFuselage() {
		return _alphaStallWingFuselage;
	}

	public void setAlphaStallWingFuselage(Amount<Angle> _alphaStallWingFuselage) {
		this._alphaStallWingFuselage = _alphaStallWingFuselage;
	}

	public Amount<Angle> getAlphaZeroLiftWingFuselage() {
		return _alphaZeroLiftWingFuselage;
	}

	public void setAlphaZeroLiftWingFuselage(Amount<Angle> _alphaZeroLiftWingFuselage) {
		this._alphaZeroLiftWingFuselage = _alphaZeroLiftWingFuselage;
	}

	public Map<MethodEnum, List<Amount<Length>>> getVerticalDistanceZeroLiftDirectionWingHTailVariable() {
		return _verticalDistanceZeroLiftDirectionWingHTailVariable;
	}

	public void setVerticalDistanceZeroLiftDirectionWingHTailVariable(
			Map<MethodEnum, List<Amount<Length>>> _verticalDistanceZeroLiftDirectionWingHTailVariable) {
		this._verticalDistanceZeroLiftDirectionWingHTailVariable = _verticalDistanceZeroLiftDirectionWingHTailVariable;
	}

	public Map<Boolean, Map<MethodEnum, List<Double>>> getDownwashGradientMap() {
		return _downwashGradientMap;
	}

	public void setDownwashGradientMap(Map<Boolean, Map<MethodEnum, List<Double>>> _downwashGradientMap) {
		this._downwashGradientMap = _downwashGradientMap;
	}

	public Map<Boolean, Map<MethodEnum, List<Amount<Angle>>>> getDownwashAngleMap() {
		return _downwashAngleMap;
	}

	public void setDownwashAngleMap(Map<Boolean, Map<MethodEnum, List<Amount<Angle>>>> _downwashAngleMap) {
		this._downwashAngleMap = _downwashAngleMap;
	}

	public List<Tuple3<MethodEnum, Double, Double>> getBuffetBarrierCurve() {
		return _buffetBarrierCurve;
	}

	public void setBuffetBarrierCurve(List<Tuple3<MethodEnum, Double, Double>> buffetBarrierCurve) {
		this._buffetBarrierCurve = buffetBarrierCurve;
	}

	public Map<MethodEnum, List<Tuple2<Double, Double>>> getCNbFuselage() {
		return _cNbFuselage;
	}

	public void setCNbFuselage(Map<MethodEnum, List<Tuple2<Double, Double>>> _cNbFuselage) {
		this._cNbFuselage = _cNbFuselage;
	}

	public Map<MethodEnum, List<Tuple2<Double, Double>>> getCNbVertical() {
		return _cNbVertical;
	}

	public void setCNbVertical(Map<MethodEnum, List<Tuple2<Double, Double>>> _cNbVertical) {
		this._cNbVertical = _cNbVertical;
	}

	public Map<MethodEnum, List<Tuple2<Double, Double>>> getCNbWing() {
		return _cNbWing;
	}

	public void setCNbWing(Map<MethodEnum, List<Tuple2<Double, Double>>> _cNbWing) {
		this._cNbWing = _cNbWing;
	}

	public Map<MethodEnum, List<Tuple2<Double, Double>>> getCNbTotal() {
		return _cNbTotal;
	}

	public void setCNbTotal(Map<MethodEnum, List<Tuple2<Double, Double>>> _cNbTotal) {
		this._cNbTotal = _cNbTotal;
	}

	public Map<MethodEnum, Map<Amount<Angle>, List<Tuple2<Double, Double>>>> getCNdr() {
		return _cNdr;
	}

	public void setCNdr(Map<MethodEnum, Map<Amount<Angle>, List<Tuple2<Double, Double>>>> _cNdr) {
		this._cNdr = _cNdr;
	}

	public Map<MethodEnum, List<Tuple2<Double, List<Double>>>> getCNFuselage() {
		return _cNFuselage;
	}

	public void setCNFuselage(Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNFuselage) {
		this._cNFuselage = _cNFuselage;
	}

	public Map<MethodEnum, List<Tuple2<Double, List<Double>>>> getCNVertical() {
		return _cNVertical;
	}

	public void setCNVertical(Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNVertical) {
		this._cNVertical = _cNVertical;
	}

	public Map<MethodEnum, List<Tuple2<Double, List<Double>>>> getCNWing() {
		return _cNWing;
	}

	public void setCNWing(Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNWing) {
		this._cNWing = _cNWing;
	}

	public Map<MethodEnum, List<Tuple2<Double, List<Double>>>> getCNTotal() {
		return _cNTotal;
	}

	public void setCNTotal(Map<MethodEnum, List<Tuple2<Double, List<Double>>>> _cNTotal) {
		this._cNTotal = _cNTotal;
	}

	public Map<MethodEnum, Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>>> getCNDueToDeltaRudder() {
		return _cNDueToDeltaRudder;
	}

	public void setCNDueToDeltaRudder(
			Map<MethodEnum, Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>>> _cNDueToDeltaRudder) {
		this._cNDueToDeltaRudder = _cNDueToDeltaRudder;
	}

	public Map<MethodEnum, Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>>> getBetaOfEquilibrium() {
		return _betaOfEquilibrium;
	}

	public void setBetaOfEquilibrium(
			Map<MethodEnum, Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>>> _betaOfEquilibrium) {
		this._betaOfEquilibrium = _betaOfEquilibrium;
	}

	public Map<Double, Map<Amount<Angle>, List<Double>>> getTotalMomentCoefficient() {
		return _totalMomentCoefficient;
	}

	public void setTotalMomentCoefficient(Map<Double, Map<Amount<Angle>, List<Double>>> _totalMomentCoefficient) {
		this._totalMomentCoefficient = _totalMomentCoefficient;
	}

	public Map<Amount<Angle>, List<Double>> getTotalLiftCoefficient() {
		return _totalLiftCoefficient;
	}

	public void setTotalLiftCoefficient(Map<Amount<Angle>, List<Double>> _totalLiftCoefficient) {
		this._totalLiftCoefficient = _totalLiftCoefficient;
	}

	public Map<Amount<Angle>, List<Double>> getTotalDragCoefficient() {
		return _totalDragCoefficient;
	}

	public void setTotalDragCoefficient(Map<Amount<Angle>, List<Double>> _totalDragCoefficient) {
		this._totalDragCoefficient = _totalDragCoefficient;
	}

	public Map<Double, List<Double>> getHorizontalTailEquilibriumLiftCoefficient() {
		return _horizontalTailEquilibriumLiftCoefficient;
	}

	public void setHorizontalTailEquilibriumLiftCoefficient(
			Map<Double, List<Double>> _horizontalTailEquilibriumLiftCoefficient) {
		this._horizontalTailEquilibriumLiftCoefficient = _horizontalTailEquilibriumLiftCoefficient;
	}

	public Map<Double, List<Double>> getTotalEquilibriumLiftCoefficient() {
		return _totalEquilibriumLiftCoefficient;
	}

	public void setTotalEquilibriumLiftCoefficient(Map<Double, List<Double>> _totalEquilibriumLiftCoefficient) {
		this._totalEquilibriumLiftCoefficient = _totalEquilibriumLiftCoefficient;
	}

	public Map<Double, List<Double>> getTotalEquilibriumDragCoefficient() {
		return _totalEquilibriumDragCoefficient;
	}

	public void setTotalEquilibriumDragCoefficient(Map<Double, List<Double>> _totalEquilibriumDragCoefficient) {
		this._totalEquilibriumDragCoefficient = _totalEquilibriumDragCoefficient;
	}

	public Double getCurrent3DVerticalTailDragCoefficient() {
		return _current3DVerticalTailDragCoefficient;
	}

	public void setCurrent3DVerticalTailDragCoefficient(Double _current3DVerticalTailDragCoefficient) {
		this._current3DVerticalTailDragCoefficient = _current3DVerticalTailDragCoefficient;
	}

	public Amount<Angle> getAlphaBodyCurrent() {
		return _alphaBodyCurrent;
	}

	public void setAlphaBodyCurrent(Amount<Angle> _alphaCurrent) {
		this._alphaBodyCurrent = _alphaCurrent;
	}

	public Map<Amount<Angle>, List<Double>> getHTail3DLiftCurvesElevator() {
		return _hTail3DLiftCurvesElevator;
	}

	public void setHTail3DLiftCurvesElevator(Map<Amount<Angle>, List<Double>> _hTail3DLiftCurvesElevator) {
		this._hTail3DLiftCurvesElevator = _hTail3DLiftCurvesElevator;
	}

	public Amount<Angle> getAlphaWingCurrent() {
		return _alphaWingCurrent;
	}

	public void setAlphaWingCurrent(Amount<Angle> _alphaWingCurrent) {
		this._alphaWingCurrent = _alphaWingCurrent;
	}

	public Amount<Angle> getAlphaHTailCurrent() {
		return _alphaHTailCurrent;
	}

	public void setAlphaHTailCurrent(Amount<Angle> _alphaHTailCurrent) {
		this._alphaHTailCurrent = _alphaHTailCurrent;
	}

	public Amount<Angle> getAlphaNacelleCurrent() {
		return _alphaNacelleCurrent;
	}

	public void setAlphaNacelleCurrent(Amount<Angle> _alphaNacelleCurrent) {
		this._alphaNacelleCurrent = _alphaNacelleCurrent;
	}

	public Amount<Angle> getBetaVTailCurrent() {
		return _betaVTailCurrent;
	}

	public void setBetaVTailCurrent(Amount<Angle> _betaVTailCurrent) {
		this._betaVTailCurrent = _betaVTailCurrent;
	}
}
