package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
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
import analyses.liftingsurface.LSAerodynamicsManager.CalcCDAtAlphaHighLift;
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
import analyses.liftingsurface.LSAerodynamicsManager.CalcCMAtAlphaHighLift;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCMac;
import analyses.liftingsurface.LSAerodynamicsManager.CalcDragDistributions;
import analyses.liftingsurface.LSAerodynamicsManager.CalcHighLiftCurve;
import analyses.liftingsurface.LSAerodynamicsManager.CalcHighLiftDevicesEffects;
import analyses.liftingsurface.LSAerodynamicsManager.CalcHighLiftMomentCurve;
import analyses.liftingsurface.LSAerodynamicsManager.CalcHighLiftPolarCurve;
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
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
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

public class ACAerodynamicAndStabilityManager {

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
	IACAerodynamicAndStabilityManager _theAerodynamicBuilderInterface;

	//..............................................................................
	// OUTPUT CHECK VARIABLES
	private Boolean _writeWing = Boolean.FALSE;
	private Boolean _writeHTail = Boolean.FALSE;
	private Boolean _writeVTail = Boolean.FALSE;
	private Boolean _writeFuselage = Boolean.FALSE;
	private Boolean _writeNacelle = Boolean.FALSE;
	
	// TODO: CONTINUE WITH ALL AIRCRAFT SHEETS
	
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
	private List<Amount<Angle>> _alphaNacelleList;
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
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailMomentCurve;
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

		_liftingSurfaceAerodynamicManagers = new HashMap<>();
		_fuselageAerodynamicManagers = new HashMap<>();
		_nacelleAerodynamicManagers = new HashMap<>();
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
		
	}

	private void initializeDataForDownwash() {

		//...................................................................................
		// PRELIMINARY CHECKS
		//...................................................................................
		/*
		 * Check on ALPHA ZERO LIFT in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
			
			CalcAlpha0L calcAlpha0L = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcAlpha0L();
			calcAlpha0L.integralMeanWithTwist();
			
			_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
					AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, 
					MethodEnum.INTEGRAL_MEAN_TWIST
					);
			
		}
		
		//...................................................................................
		// DISTANCE BETWEEN WING VORTEX PLANE AND THE AERODYNAMIC CENTER OF THE HTAIL
		//...................................................................................
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
													_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING)
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
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING)
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
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING)
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

	private void calculateDownwash() {

		//...................................................................................
		// PRELIMINARY CHECKS
		//...................................................................................
		Amount<?> cLAlphaWingCurrent = null;
		Amount<Angle> alphaZeroLiftWingCurrent = null;
		Double cL0WingCurrent = null;
		Double[] liftCurveWingCurrent = null;
		
		/*
		 * Check on CL ALPHA in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {
			
			CalcCLAlpha calcCLAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAlpha();
			calcCLAlpha.nasaBlackwell();
			cLAlphaWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(MethodEnum.NASA_BLACKWELL);
			
		}
		else
			cLAlphaWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ALPHA)
					);
		
		/*
		 * Check on ALPHA ZERO LIFT in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
			
			CalcAlpha0L calcAlpha0L = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcAlpha0L();
			calcAlpha0L.integralMeanWithTwist();
			alphaZeroLiftWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(MethodEnum.INTEGRAL_MEAN_TWIST);
			
		}
		else
			alphaZeroLiftWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
					);
		
		/*
		 * Check on CL0 in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {
			
			CalcCL0 calcCL0 = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCL0();
			calcCL0.nasaBlackwell();
			cL0WingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().get(MethodEnum.NASA_BLACKWELL);
			
		}
		else
			cL0WingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ZERO)
					);
		
		/*
		 * Check on the LIFT CURVE in the LSAerodynamicsManager of the wing. If this is not present in the task list, it will be calculated
		 * using a defaul method. 
		 */
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
			
			CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
			calcLiftCurve.nasaBlackwell(_currentMachNumber);
			liftCurveWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL);
			
		}
		else
			liftCurveWingCurrent = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
					);
		
		/////////////////////////////////////////////////////////////////////////////////////
		// DOWNWASH ARRAY 
		//...................................................................................
		// ROSKAM (constant gradient)
		//...................................................................................		
		// calculate cl alpha at M=0
		double cLAlphaMachZero = LiftCalc.calculateCLAlphaAtMachNasaBlackwell(
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(),
				_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSurface(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getChordDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXLEDistribution(), 
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDihedralDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getTwistDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLiftDistribution(),
				_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getVortexSemiSpanToSemiSpanRatio(),
				0.0,
				_currentAltitude
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
							cLAlphaWingCurrent.to(SI.RADIAN.inverse()).getEstimatedValue()
							)
					);

		Map<MethodEnum, List<Double>> downwashGradientConstant = new HashMap<>();
		downwashGradientConstant.put(
				MethodEnum.ROSKAM,
				downwashGradientConstantList
				);

		double epsilonZeroRoskam = - downwashGradientConstant.get(MethodEnum.ROSKAM).get(0)
				* alphaZeroLiftWingCurrent.doubleValue(NonSI.DEGREE_ANGLE);

		List<Amount<Angle>> downwashAngleConstantList = new ArrayList<>();
		for (int i=0; i<this._theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++)
			downwashAngleConstantList.add(
					Amount.valueOf(
							epsilonZeroRoskam 
							+ downwashGradientConstant.get(MethodEnum.ROSKAM).get(0)
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
		downwashAngleConstantList.clear();
		for (int i=0; i<this._theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++){
			double cl = 
					cLAlphaWingCurrent.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue() 
					* _alphaWingList.get(i).doubleValue(NonSI.DEGREE_ANGLE) 
					+ cL0WingCurrent;

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

		/* FIXME */
		downwashGradientNonLinear.put(
				MethodEnum.ROSKAM,
				AerodynamicCalc.calculateVariableDownwashGradientRoskamWithMachEffect(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getTaperRatioEquivalent(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getZApexConstructionAxes(),
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getZApexConstructionAxes(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						alphaZeroLiftWingCurrent,
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
						cLAlphaMachZero,
						cLAlphaWingCurrent.to(SI.RADIAN.inverse()).getEstimatedValue(), 
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
						alphaZeroLiftWingCurrent
						)
				);

		_verticalDistanceZeroLiftDirectionWingHTailVariable.put(
				MethodEnum.ROSKAM,
				AerodynamicCalc.calculateVortexPlaneHorizontalTailVerticalDistance(
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
						alphaZeroLiftWingCurrent,
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
						alphaZeroLiftWingCurrent,
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSweepQuarterChordEquivalent(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getAspectRatio(),
						_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSemiSpan(), 
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						MyArrayUtils.convertToDoublePrimitive(liftCurveWingCurrent),
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
						alphaZeroLiftWingCurrent,
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						_alphaBodyList, 
						downwashAngleNonLinear.get(MethodEnum.SLINGERLAND)
						)	
				);

		//...................................................................................
		// FROM INPUT (non linear downwash gradient assigned by the user)
		//...................................................................................
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DOWNWASH))
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.DOWNWASH).equals(MethodEnum.INPUT)) {
				
				downwashGradientNonLinear.put(
						MethodEnum.INPUT,
						_alphaBodyList.stream()
						.map(ab -> _theAerodynamicBuilderInterface.getAircraftDownwashGradientFunction().value(ab.doubleValue(NonSI.DEGREE_ANGLE)))
						.collect(Collectors.toList())
						);
				
				downwashAngleNonLinear.put(
						MethodEnum.INPUT,
						AerodynamicCalc.calculateDownwashAngleFromDownwashGradient(
								downwashGradientNonLinear.get(MethodEnum.INPUT),
								_alphaBodyList,
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle(),
								alphaZeroLiftWingCurrent
								)
						);
				
			}
		
		// Filling the global maps ...
		_downwashGradientMap.put(Boolean.FALSE, downwashGradientNonLinear);
		_downwashAngleMap.put(Boolean.FALSE, downwashAngleNonLinear);
		
	}

	private void calculateComponentsData() {

		/*
		 * THIS WILL CREATE ALL THE COMPONENTS MANAGERS 
		 * AND RUN ALL THE REQUIRED COMPONENTS CALCULATIONS
		 */ 
		
		_alphaBodyList = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						_theAerodynamicBuilderInterface.getAlphaBodyInitial().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getAlphaBodyFinal().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getNumberOfAlphasBody()),
				NonSI.DEGREE_ANGLE
				);
		
		//========================================================================================================================
		// WING
		if(_theAerodynamicBuilderInterface.getTheAircraft().getWing() != null) {

			_alphaWingList = _alphaBodyList.stream()
					.map(x -> x.to(NonSI.DEGREE_ANGLE).plus(
							_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE))
							)
					.collect(Collectors.toList()); 

			_alphaWingCurrent = _alphaBodyCurrent.to(NonSI.DEGREE_ANGLE)
					.plus(_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE));
			
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

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.INPUT)) {					
					
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									_alphaWingList.stream()
									.map(aw -> _theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
					
				}
				else {
					
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.INPUT))
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlpha().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(getAlphaWingCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CL AT ALPHA REQUIRES THAT THE LIFT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {					

					List<Double> currentLiftCurve = new ArrayList<>();

					if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

						CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
						calcLiftCurve.nasaBlackwell(_currentMachNumber);

						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
								AerodynamicAndStabilityEnum.LIFT_CURVE_3D, 
								MethodEnum.NASA_BLACKWELL
								);
						
						Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL))
						.forEach(cL -> currentLiftCurve.add(cL));

					}
					else {

						switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
						case PHILLIPS_ALLEY:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(MethodEnum.PHILLIPS_ALLEY))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						case NASA_BLACKWELL:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						case INPUT:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(MethodEnum.INPUT))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						default:
							break;
						}

					}

					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									currentLiftCurve.stream()
									.map(cL -> _theAerodynamicBuilderInterface.getWingPolarCurveFunction().value(cL))
									.collect(Collectors.toList())
									)
							);
				}
				else {
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {
						if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
							
							CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAtAlpha();
							calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaWingCurrent);
							
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
									AerodynamicAndStabilityEnum.CL_AT_ALPHA,
									MethodEnum.NASA_BLACKWELL
									);
							
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDAtAlpha().put(
									MethodEnum.INPUT, 
									_theAerodynamicBuilderInterface.getWingPolarCurveFunction().value(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlpha().get(MethodEnum.NASA_BLACKWELL)
											)
									);
						}
						else {
							
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDAtAlpha().put(
									MethodEnum.INPUT, 
									_theAerodynamicBuilderInterface.getWingPolarCurveFunction().value(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlpha().get(
													_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
													)
											)
									);
							
						}
					}
					else {
						System.err.println("THE METHOD 'INPUT' FOR CD AT ALPHA REQUIRES THAT THE POLAR CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
					break;
				default:
					break;
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
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {					
					
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									_alphaWingList.stream()
									.map(aw -> _theAerodynamicBuilderInterface.getWingMomentCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
				}
				else {
					analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcMomentCurve();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
					case AIRFOIL_DISTRIBUTION:
						calcMomentCurve.fromAirfoilDistribution();
						break;
					default:
						break;
					}
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT))
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMAtAlphaHighLift().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getWingMomentCurveFunction().value(getAlphaWingCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CM AT ALPHA REQUIRES THAT THE MOMENT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D).equals(MethodEnum.INPUT)) {					
						
						if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
						
							CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
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
							
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
									AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS,
									MethodEnum.SEMIEMPIRICAL
									);
							
						}
						
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).setAlphaArrayPlotHighLift(
								MyArrayUtils.linspaceDouble(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLiftHighLift().get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE)-2,
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStallHighLift().get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE) + 3,
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getNumberOfAlphasPlot()
										)
								);

						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().put(
								MethodEnum.INPUT,
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayPlotHighLift())
										.map(aw -> _theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(aw))
										.collect(Collectors.toList())
										)
								);
					}
					else {
						
						CalcHighLiftCurve calcHighLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftCurve();
						switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {
						case SEMIEMPIRICAL:
							if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
								calcHighLiftCurve.semiempirical(
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
										_currentMachNumber 
										);
							else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
								calcHighLiftCurve.semiempirical(
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
				//	HIGH_LIFT_POLAR_CURVE_3D
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D).equals(MethodEnum.INPUT)) {					
						
						List<Double> currentHighLiftCurve = new ArrayList<>();

						if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

							CalcHighLiftCurve calcHighLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftCurve();
							
							if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
								calcHighLiftCurve.semiempirical(
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
										_currentMachNumber
										);
							else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
								calcHighLiftCurve.semiempirical(
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
										_currentMachNumber
										);

							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
									AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, 
									MethodEnum.SEMIEMPIRICAL
									);
							
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL))
							.forEach(cL -> currentHighLiftCurve.add(cL));

						}
						else {

							switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {
							case SEMIEMPIRICAL:
								Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(MethodEnum.SEMIEMPIRICAL))
								.forEach(cL -> currentHighLiftCurve.add(cL));
								break;
							case INPUT:
								Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(MethodEnum.INPUT))
								.forEach(cL -> currentHighLiftCurve.add(cL));
								break;
							default:
								break;
							}

						}
						
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurveHighLift().put(
								MethodEnum.INPUT,
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										currentHighLiftCurve.stream()
										.map(cL -> _theAerodynamicBuilderInterface.getWingHighLiftPolarCurveFunction().value(cL))
										.collect(Collectors.toList())
										)
								);
						
					}
					else {
						CalcHighLiftPolarCurve calcHighLiftPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftPolarCurve();
						switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {
						case SEMIEMPIRICAL:
							
							if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {
								CalcPolar calcPolar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcPolar();
								calcPolar.fromCdDistribution(_currentMachNumber, _currentAltitude);
								
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
										AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,
										MethodEnum.AIRFOIL_DISTRIBUTION
										);
							}
							
							if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
								calcHighLiftPolarCurve.semiempirical(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
												),
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
										_currentMachNumber 
										);
							else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
								calcHighLiftPolarCurve.semiempirical(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
												),
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
				//	HIGH_LIFT_MOMENT_CURVE_3D
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D).equals(MethodEnum.INPUT)) {					
						
						if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
							
							CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftDevicesEffects();
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
							
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
									AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, 
									MethodEnum.SEMIEMPIRICAL
									);
							
						}
						
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).setAlphaArrayPlotHighLift(
								MyArrayUtils.linspaceDouble(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLiftHighLift().get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE)-2,
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStallHighLift().get(MethodEnum.SEMIEMPIRICAL).doubleValue(NonSI.DEGREE_ANGLE) + 3,
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getNumberOfAlphasPlot()
										)
								);

						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficient3DCurveHighLift().put(
								MethodEnum.INPUT,
								MyArrayUtils.convertListOfDoubleToDoubleArray(
										Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayPlotHighLift())
										.map(aw -> _theAerodynamicBuilderInterface.getWingHighLiftMomentCurveFunction().value(aw))
										.collect(Collectors.toList())
										)
								);
					}
					else {
						
						CalcHighLiftMomentCurve calcHighLiftMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftMomentCurve();
						switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {
						case SEMIEMPIRICAL:
							if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
								calcHighLiftMomentCurve.semiempirical(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
												),
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
										_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
										_currentMachNumber 
										);
							else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
								calcHighLiftMomentCurve.semiempirical(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
												),
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
					case INPUT: 
						if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D).equals(MethodEnum.INPUT))
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlphaHighLift().put(
									MethodEnum.INPUT, 
									_theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(getAlphaWingCurrent().doubleValue(NonSI.DEGREE_ANGLE))
									);
						else {
							System.err.println("THE METHOD 'INPUT' FOR CL AT ALPHA HIGH LIFT REQUIRES THAT THE HIGH LIFT CURVE HAS BEEN ASSIGNED BY THE USER !!");
							System.exit(1);
						}
						break;
					default:
						break;
					}
				}

				//.........................................................................................................................
				//	CD_AT_ALPHA_HIGH_LIFT 
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {

					CalcCDAtAlphaHighLift calcCDAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCDAtAlphaHighLift();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {
					case SEMIEMPIRICAL:
						if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
							calcCDAtAlphaHighLift.semiempirical(
									_alphaWingCurrent,
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
									_currentMachNumber, 
									_currentAltitude
									);
						else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
							calcCDAtAlphaHighLift.semiempirical(
									_alphaWingCurrent,
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
									_currentMachNumber, 
									_currentAltitude
									);
						break;
					case INPUT: 
						if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D).equals(MethodEnum.INPUT)) {
							if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {
								CalcCLAtAlphaHighLift calcCLAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAtAlphaHighLift();
								
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
									
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
										AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT,
										MethodEnum.SEMIEMPIRICAL
										);
								
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDAtAlphaHighLift().put(
										MethodEnum.INPUT, 
										_theAerodynamicBuilderInterface.getWingHighLiftPolarCurveFunction().value(
												_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlphaHighLift().get(MethodEnum.SEMIEMPIRICAL)
												)
										);
							}
							else {
								
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDAtAlphaHighLift().put(
										MethodEnum.INPUT, 
										_theAerodynamicBuilderInterface.getWingHighLiftPolarCurveFunction().value(
												_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlphaHighLift().get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)
														)
												)
										);
								
							}
						}
						else {
							System.err.println("THE METHOD 'INPUT' FOR CD AT ALPHA HIGH LIFT REQUIRES THAT THE POLAR CURVE HAS BEEN ASSIGNED BY THE USER !!");
							System.exit(1);
						}
						break;
					default:
						break;
					}
				}
				
				//.........................................................................................................................
				//	CM_AT_ALPHA_HIGH_LIFT 
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {

					CalcCMAtAlphaHighLift calcCMAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCMAtAlphaHighLift();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {
					case SEMIEMPIRICAL:
						if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF))
							calcCMAtAlphaHighLift.semiempirical(
									_alphaWingCurrent,
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
									_currentMachNumber
									);
						else if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING))
							calcCMAtAlphaHighLift.semiempirical(
									_alphaWingCurrent,
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
									_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
									_currentMachNumber 
									);
						break;
					case INPUT: 
						if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D).equals(MethodEnum.INPUT))
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMAtAlphaHighLift().put(
									MethodEnum.INPUT, 
									_theAerodynamicBuilderInterface.getWingHighLiftMomentCurveFunction().value(getAlphaWingCurrent().doubleValue(NonSI.DEGREE_ANGLE))
									);
						else {
							System.err.println("THE METHOD 'INPUT' FOR CM AT ALPHA HIGH LIFT REQUIRES THAT THE HIGH LIFT CURVE HAS BEEN ASSIGNED BY THE USER !!");
							System.exit(1);
						}
						break;
					default:
						break;
					}
				}
			}
		}

		//============================================================================
		// HTAIL
		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {
			
			initializeDataForDownwash();
			calculateDownwash();
			calculateHorizontalTailAndNacelleCurrentAlpha(); /* available only after the evaluation of the downwash. The nacelle angle is also evaluated here for simplicity */
			
			/////////////////////////////////////////////////////////////////////////////////////
			// ALPHA HTAIL ARRAY 
			_alphaHTailList = new ArrayList<>();
			for (int i=0; i<_theAerodynamicBuilderInterface.getNumberOfAlphasBody(); i++){
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DOWNWASH))
					_alphaHTailList.add(
							Amount.valueOf(
									_alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
									- _downwashAngleMap
									.get(_theAerodynamicBuilderInterface.getDownwashConstant())
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
				else /* ...a default method will be used (SLINGERLAND NON LINEAR) */
					_alphaHTailList.add(
							Amount.valueOf(
									_alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
									- _downwashAngleMap
									.get(Boolean.FALSE)
									.get(MethodEnum.SLINGERLAND)
									.get(i)
									.doubleValue(NonSI.DEGREE_ANGLE)
									+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
									NonSI.DEGREE_ANGLE
									)
							);
			}
			
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

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.INPUT)) {					
					
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									_alphaHTailList.stream()
									.map(ah -> _theAerodynamicBuilderInterface.getHTailLiftCurveFunction().value(ah.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
					
				}
				else {
					
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.INPUT))
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAtAlpha().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getHTailLiftCurveFunction().value(getAlphaHTailCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CL AT ALPHA (HTAIL) REQUIRES THAT THE LIFT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {					

					List<Double> currentLiftCurve = new ArrayList<>();

					if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

						CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcLiftCurve();
						calcLiftCurve.nasaBlackwell(_currentMachNumber);

						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(
								AerodynamicAndStabilityEnum.LIFT_CURVE_3D, 
								MethodEnum.NASA_BLACKWELL
								);
						
						Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL))
						.forEach(cL -> currentLiftCurve.add(cL));

					}
					else {

						switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
						case PHILLIPS_ALLEY:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(MethodEnum.PHILLIPS_ALLEY))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						case NASA_BLACKWELL:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						case INPUT:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(MethodEnum.INPUT))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						default:
							break;
						}

					}

					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									currentLiftCurve.stream()
									.map(cL -> _theAerodynamicBuilderInterface.getHTailPolarCurveFunction().value(cL))
									.collect(Collectors.toList())
									)
							);
				}
				else {
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {
						if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
							
							CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCLAtAlpha();
							calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaHTailCurrent);
							
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(
									AerodynamicAndStabilityEnum.CL_AT_ALPHA,
									MethodEnum.NASA_BLACKWELL
									);
							
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCDAtAlpha().put(
									MethodEnum.INPUT, 
									_theAerodynamicBuilderInterface.getHTailPolarCurveFunction().value(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAtAlpha().get(MethodEnum.NASA_BLACKWELL)
											)
									);
						}
						else {
							
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCDAtAlpha().put(
									MethodEnum.INPUT, 
									_theAerodynamicBuilderInterface.getHTailPolarCurveFunction().value(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAtAlpha().get(
													_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
													)
											)
									);
							
						}
					}
					else {
						System.err.println("THE METHOD 'INPUT' FOR CD AT ALPHA (HTAIL) REQUIRES THAT THE POLAR CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
					break;
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
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {					
					
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									_alphaHTailList.stream()
									.map(ah -> _theAerodynamicBuilderInterface.getHTailMomentCurveFunction().value(ah.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
				}
				else {
					analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMomentCurve();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
					case AIRFOIL_DISTRIBUTION:
						calcMomentCurve.fromAirfoilDistribution();
						break;
					default:
						break;
					}
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT))
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCMAtAlphaHighLift().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getHTailMomentCurveFunction().value(getAlphaHTailCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CM AT ALPHA (HTAIL) REQUIRES THAT THE MOMENT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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
			
			//.........................................................................................................................
			//	ELEVATOR_EFFECTS
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftDevicesEffects();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
				case SEMIEMPIRICAL:
					List<Amount<Angle>> elevatorDeflection = new ArrayList<>();
					elevatorDeflection.add(_theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcHighLiftDevicesEffects.semiempirical(
							elevatorDeflection, 
							new ArrayList<>(), 
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
					List<Amount<Angle>> elevatorDeflection = new ArrayList<>();
					elevatorDeflection.add(_theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcHighLiftCurve.semiempirical(
							elevatorDeflection, 
							new ArrayList<>(), 
							_currentMachNumber 
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	ELEVATOR_POLAR_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

				CalcHighLiftPolarCurve calcHighLiftPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftPolarCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {
				case SEMIEMPIRICAL:
					
					if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {
						analyses.liftingsurface.LSAerodynamicsManager.CalcPolar calcPolar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcPolar();
						calcPolar.fromCdDistribution(_currentMachNumber, _currentAltitude);
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(
								AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,
								MethodEnum.AIRFOIL_DISTRIBUTION
								);
					}
					
					List<Amount<Angle>> elevatorDeflection = new ArrayList<>();
					elevatorDeflection.add(_theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcHighLiftPolarCurve.semiempirical(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
									),
							elevatorDeflection, 
							new ArrayList<>(), 
							_currentMachNumber 
							);
					break;
				default:
					break;
				}
			}
			
			//.........................................................................................................................
			//	ELEVATOR_MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

				CalcHighLiftMomentCurve calcHighLiftMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftMomentCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {
				case SEMIEMPIRICAL:
					
					if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
						analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMomentCurve();
						calcMomentCurve.fromAirfoilDistribution();
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(
								AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE,
								MethodEnum.AIRFOIL_DISTRIBUTION
								);
					}
					
					List<Amount<Angle>> elevatorDeflection = new ArrayList<>();
					elevatorDeflection.add(_theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcHighLiftMomentCurve.semiempirical(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
									),
							elevatorDeflection, 
							new ArrayList<>(), 
							_currentMachNumber 
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
					List<Amount<Angle>> elevatorDeflection = new ArrayList<>();
					elevatorDeflection.add(_theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcCLAtAlphaHighLift.semiempirical(
							_alphaHTailCurrent,
							elevatorDeflection, 
							new ArrayList<>(), 
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

				CalcCDAtAlphaHighLift calcCDAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCDAtAlphaHighLift();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {
				case SEMIEMPIRICAL:
					List<Amount<Angle>> elevatorDeflection = new ArrayList<>();
					elevatorDeflection.add(_theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcCDAtAlphaHighLift.semiempirical(
							_alphaHTailCurrent,
							elevatorDeflection, 
							new ArrayList<>(), 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_AT_ALPHA_ELEVATOR
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {

				CalcCMAtAlphaHighLift calcCMAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcCMAtAlphaHighLift();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {
				case SEMIEMPIRICAL:
					List<Amount<Angle>> elevatorDeflection = new ArrayList<>();
					elevatorDeflection.add(_theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcCMAtAlphaHighLift.semiempirical(
							_alphaHTailCurrent,
							elevatorDeflection, 
							new ArrayList<>(), 
							_currentMachNumber
							);
					break;
				default:
					break;
				}
			}
		}

		//============================================================================
		// VTAIL 
		if(_theAerodynamicBuilderInterface.getTheAircraft().getVTail() != null) {
			
			/////////////////////////////////////////////////////////////////////////////////////
			// BETA ARRAY
			_betaList = MyArrayUtils.convertDoubleArrayToListOfAmount(
					MyArrayUtils.linspace(
							_theAerodynamicBuilderInterface.getBetaInitial().doubleValue(NonSI.DEGREE_ANGLE),
							_theAerodynamicBuilderInterface.getBetaFinal().doubleValue(NonSI.DEGREE_ANGLE),
							_theAerodynamicBuilderInterface.getNumberOfBeta()),
					NonSI.DEGREE_ANGLE
					);
			
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

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.INPUT)) {					
					
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									_betaList.stream()
									.map(b -> _theAerodynamicBuilderInterface.getVTailLiftCurveFunction().value(b.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
					
				}
				else {
					
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.INPUT))
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAtAlpha().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getVTailLiftCurveFunction().value(getBetaVTailCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CL AT ALPHA (VTAIL) REQUIRES THAT THE LIFT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {					

					List<Double> currentLiftCurve = new ArrayList<>();

					if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

						CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcLiftCurve();
						calcLiftCurve.nasaBlackwell(_currentMachNumber);

						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).put(
								AerodynamicAndStabilityEnum.LIFT_CURVE_3D, 
								MethodEnum.NASA_BLACKWELL
								);
						
						Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL))
						.forEach(cL -> currentLiftCurve.add(cL));

					}
					else {

						switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {
						case PHILLIPS_ALLEY:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(MethodEnum.PHILLIPS_ALLEY))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						case NASA_BLACKWELL:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(MethodEnum.NASA_BLACKWELL))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						case INPUT:
							Arrays.stream(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(MethodEnum.INPUT))
							.forEach(cL -> currentLiftCurve.add(cL));
							break;
						default:
							break;
						}

					}

					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getPolar3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									currentLiftCurve.stream()
									.map(cL -> _theAerodynamicBuilderInterface.getVTailPolarCurveFunction().value(cL))
									.collect(Collectors.toList())
									)
							);
				}
				else {
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {
						if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
							
							CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCLAtAlpha();
							calcCLAtAlpha.nasaBlackwellCompleteCurve(_betaVTailCurrent);
							
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).put(
									AerodynamicAndStabilityEnum.CL_AT_ALPHA, 
									MethodEnum.NASA_BLACKWELL
									);
							
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCDAtAlpha().put(
									MethodEnum.INPUT, 
									_theAerodynamicBuilderInterface.getVTailPolarCurveFunction().value(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAtAlpha().get(MethodEnum.NASA_BLACKWELL)
											)
									);
						}
						else {
							
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCDAtAlpha().put(
									MethodEnum.INPUT, 
									_theAerodynamicBuilderInterface.getVTailPolarCurveFunction().value(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAtAlpha().get(
													_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
													)
											)
									);
							
						}
					}
					else {
						System.err.println("THE METHOD 'INPUT' FOR CD AT ALPHA (VTAIL) REQUIRES THAT THE POLAR CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
					break;
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
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT)) {					
					
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMoment3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									_betaList.stream()
									.map(b -> _theAerodynamicBuilderInterface.getVTailMomentCurveFunction().value(b.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
				}
				else {
					analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcMomentCurve();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
					case AIRFOIL_DISTRIBUTION:
						calcMomentCurve.fromAirfoilDistribution();
						break;
					default:
						break;
					}
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
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE).equals(MethodEnum.INPUT))
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCMAtAlphaHighLift().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getVTailMomentCurveFunction().value(getBetaVTailCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CM AT ALPHA (VTAIL) REQUIRES THAT THE MOMENT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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
			
			//.........................................................................................................................
			//	RUDDER_EFFECTS
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {

				CalcHighLiftDevicesEffects calcHighLiftDevicesEffects = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcHighLiftDevicesEffects();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {
				case SEMIEMPIRICAL:
					List<Amount<Angle>> rudderDeflection = new ArrayList<>();
					rudderDeflection.add(_theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcHighLiftDevicesEffects.semiempirical(
							rudderDeflection, 
							new ArrayList<>(), 
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
					List<Amount<Angle>> rudderDeflection = new ArrayList<>();
					rudderDeflection.add(_theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcHighLiftCurve.semiempirical(
							rudderDeflection, 
							new ArrayList<>(), 
							_currentMachNumber 
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	RUDDER_POLAR_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

				CalcHighLiftPolarCurve calcHighLiftPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcHighLiftPolarCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {
				case SEMIEMPIRICAL:
					
					if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {
						analyses.liftingsurface.LSAerodynamicsManager.CalcPolar calcPolar = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcPolar();
						calcPolar.fromCdDistribution(_currentMachNumber, _currentAltitude);
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).put(
								AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,
								MethodEnum.AIRFOIL_DISTRIBUTION
								);
					}
					
					List<Amount<Angle>> rudderDeflection = new ArrayList<>();
					rudderDeflection.add(_theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcHighLiftPolarCurve.semiempirical(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
									),
							rudderDeflection, 
							new ArrayList<>(), 
							_currentMachNumber 
							);
					break;
				default:
					break;
				}
			}
			
			//.........................................................................................................................
			//	RUDDER_MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

				CalcHighLiftMomentCurve calcHighLiftMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcHighLiftMomentCurve();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {
				case SEMIEMPIRICAL:
					
					if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
						analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcMomentCurve();
						calcMomentCurve.fromAirfoilDistribution();
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).put(
								AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE,
								MethodEnum.AIRFOIL_DISTRIBUTION
								);
					}
					
					List<Amount<Angle>> rudderDeflection = new ArrayList<>();
					rudderDeflection.add(_theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcHighLiftMomentCurve.semiempirical(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
									),
							rudderDeflection, 
							new ArrayList<>(), 
							_currentMachNumber 
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
					List<Amount<Angle>> rudderDeflection = new ArrayList<>();
					rudderDeflection.add(_theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcCLAtAlphaHighLift.semiempirical(
							_betaVTailCurrent,
							rudderDeflection, 
							new ArrayList<>(), 
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

				CalcCDAtAlphaHighLift calcCDAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCDAtAlphaHighLift();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {
				case SEMIEMPIRICAL:
					List<Amount<Angle>> rudderDeflection = new ArrayList<>();
					rudderDeflection.add(_theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcCDAtAlphaHighLift.semiempirical(
							_betaVTailCurrent,
							rudderDeflection, 
							new ArrayList<>(), 
							_currentMachNumber,
							_currentAltitude
							);
					break;
				default:
					break;
				}
			}

			//.........................................................................................................................
			//	CM_AT_ALPHA_RUDDER
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {

				CalcCMAtAlphaHighLift calcCMAtAlphaHighLift = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCMAtAlphaHighLift();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {
				case SEMIEMPIRICAL:
					List<Amount<Angle>> rudderDeflection = new ArrayList<>();
					rudderDeflection.add(_theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().to(NonSI.DEGREE_ANGLE));
					calcCMAtAlphaHighLift.semiempirical(
							_betaVTailCurrent,
							rudderDeflection, 
							new ArrayList<>(), 
							_currentMachNumber
							);
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
					calcCDInduced.semiempirical(_alphaBodyCurrent.to(NonSI.DEGREE_ANGLE));
					break;
				default:
					break;
				}
			}
			//.........................................................................................................................
			//	POLAR_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE).equals(MethodEnum.INPUT)) {					
					
					_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getMoment3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									_alphaBodyList.stream()
									.map(ab -> _theAerodynamicBuilderInterface.getFuselagePolarCurveFunction().value(ab.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
				}
				else {
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
			}
			
			//.........................................................................................................................
			//	CD_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcCDAtAlpha calcCDAtAlpha = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCDAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE)) {
				case SEMIEMPIRICAL: 
					calcCDAtAlpha.semiempirical(_alphaBodyCurrent.to(NonSI.DEGREE_ANGLE));
					break;
				case FUSDES: 
					calcCDAtAlpha.fusDes(_alphaBodyCurrent.to(NonSI.DEGREE_ANGLE));
					break;
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE).equals(MethodEnum.INPUT))
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCMAtAlpha().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getFuselagePolarCurveFunction().value(getAlphaBodyCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CD AT ALPHA (FUSELAGE) REQUIRES THAT THE MOMENT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE).equals(MethodEnum.INPUT)) {					
					
					_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getMoment3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									_alphaBodyList.stream()
									.map(ab -> _theAerodynamicBuilderInterface.getFuselageMomentCurveFunction().value(ab.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
				}
				else {
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
			
			//.........................................................................................................................
			//	CM_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcCMAtAlpha calcCMAtAlpha = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcCMAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE)) {
				case SEMIEMPIRICAL:
					calcCMAtAlpha.semiempirical(_alphaBodyCurrent.to(NonSI.DEGREE_ANGLE));
					break;
				case FUSDES:
					calcCMAtAlpha.fusDes(_alphaBodyCurrent.to(NonSI.DEGREE_ANGLE));
					break;
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE).equals(MethodEnum.INPUT))
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCMAtAlpha().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getFuselageMomentCurveFunction().value(getAlphaBodyCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CM AT ALPHA (FUSELAGE) REQUIRES THAT THE MOMENT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
					break;
				default:
					break;
				}
			}
		}
		
		//============================================================================
		// NACELLE

		_alphaNacelleList = new ArrayList<>();
		switch (_theAerodynamicBuilderInterface.getTheAircraft().getNacelles().getNacellesList().get(0).getMountingPosition()) {
		case WING:
			setAlphaNacelleList(_alphaWingList);
			break;
		case FUSELAGE:
			setAlphaNacelleList(_alphaHTailList);
			break;
		case HTAIL:
			setAlphaNacelleList(_alphaHTailList);
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
							getAlphaNacelleList() 
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

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE).equals(MethodEnum.INPUT)) {					
					
					_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getMoment3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									getAlphaNacelleList().stream()
									.map(an -> _theAerodynamicBuilderInterface.getNacellePolarCurveFunction().value(an.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
				}
				else {
					analyses.nacelles.NacelleAerodynamicsManager.CalcPolar calcPolar = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcPolar();
					switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)) {
					case SEMIEMPIRICAL:
						calcPolar.semiempirical();
						break;
					default:
						break;
					}
				}
			}

			//.........................................................................................................................
			//	CD_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE)) {

				analyses.nacelles.NacelleAerodynamicsManager.CalcCDAtAlpha calcCDAtAlpha = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCDAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE)) {
				case SEMIEMPIRICAL:
					calcCDAtAlpha.semiempirical(_alphaNacelleCurrent);
					break;
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE).equals(MethodEnum.INPUT))
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCMAtAlpha().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getNacellePolarCurveFunction().value(getAlphaNacelleCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CD AT ALPHA (NACELLE) REQUIRES THAT THE MOMENT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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
			//	MOMENT_CURVE_3D
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)) {

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE).equals(MethodEnum.INPUT)) {					
					
					_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getMoment3DCurve().put(
							MethodEnum.INPUT,
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									getAlphaNacelleList().stream()
									.map(an -> _theAerodynamicBuilderInterface.getNacelleMomentCurveFunction().value(an.doubleValue(NonSI.DEGREE_ANGLE)))
									.collect(Collectors.toList())
									)
							);
				}
				else {
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
			
			//.........................................................................................................................
			//	CM_AT_ALPHA 
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE)) {

				analyses.nacelles.NacelleAerodynamicsManager.CalcCMAtAlpha calcCMAtAlpha = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).new CalcCMAtAlpha();
				switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE)) {
				case SEMIEMPIRICAL:
					calcCMAtAlpha.semiempirical(_alphaNacelleCurrent);
					break;
				case INPUT: 
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE).equals(MethodEnum.INPUT))
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCMAtAlpha().put(
								MethodEnum.INPUT, 
								_theAerodynamicBuilderInterface.getNacelleMomentCurveFunction().value(getAlphaNacelleCurrent().doubleValue(NonSI.DEGREE_ANGLE))
								);
					else {
						System.err.println("THE METHOD 'INPUT' FOR CM AT ALPHA (NACELLE) REQUIRES THAT THE MOMENT CURVE HAS BEEN ASSIGNED BY THE USER !!");
						System.exit(1);
					}
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

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
						AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER,
						MethodEnum.DEYOUNG_HARPER
						);
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

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(
						AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER,
						MethodEnum.DEYOUNG_HARPER
						);
			}
		}

		//.........................................................................................................................
		//	WING LIFT_CURVE_3D (EVENTUALLY WITH HIGH LIFT DEVICES AND WITH FUSELAGE EFFECTS)
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

			List<Double> temporaryLiftCurve = new ArrayList<>();

			switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
			case TAKE_OFF:
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

					CalcHighLiftCurve calcHighLiftCurveTakeOff = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftCurve();
					calcHighLiftCurveTakeOff.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
							_currentMachNumber
							);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D,
							MethodEnum.SEMIEMPIRICAL
							);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.LIFT_CURVE_3D,
							MethodEnum.NASA_BLACKWELL
							);

					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getLiftCoefficient3DCurveHighLift()
							.get(MethodEnum.SEMIEMPIRICAL));
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

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D).equals(MethodEnum.SEMIEMPIRICAL)){

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
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D).equals(MethodEnum.INPUT)){

						//....................................................................................................
						// PRELIMINARY CHECKS (all the required parameters have to be retrieved from the assigned input curve)
						//....................................................................................................

						// CLalpha HIGH LIFT
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlphaHighLift().put(
								MethodEnum.INPUT,
								Amount.valueOf(
										_theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(1.0)
										- _theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(0.0),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						// CL0 HIGH LIFT
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZeroHighLift().put(
								MethodEnum.INPUT,
								_theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(0.0)
								);

						// CLstar HIGH LIFT /* TODO: CHECK THIS */
						List<Double> cLHighLiftList = _alphaWingList.stream()
								.map(aw -> _theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
								.collect(Collectors.toList());
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStarHighLift().put(
								MethodEnum.INPUT,
								LiftCalc.calculateCLStarFromCurve(
										_alphaWingList, 
										cLHighLiftList
										)
								);

						// CLmax HIGH LIFT
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLMaxHighLift().put(
								MethodEnum.INPUT,
								_alphaWingList.stream()
								.mapToDouble(aw -> _theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
								.max()
								.getAsDouble()
								);

						// ALPHA STALL HIGH LIFT
						int indexOfMaxCLHighLiftList = MyArrayUtils.getIndexOfMax(
								MyArrayUtils.convertToDoublePrimitive(cLHighLiftList)
								);
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStallHighLift().put(
								MethodEnum.INPUT,
								_alphaWingList.get(indexOfMaxCLHighLiftList)
								);

						//............................................................................................

						//CL ALPHA
						_clAlphaWingFuselage =
								LiftCalc.calculateCLAlphaFuselage(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlphaHighLift().get(
												MethodEnum.INPUT),
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
										Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
												SI.METER)
										);

						//CL ZERO
						_clZeroWingFuselage =
								-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZeroHighLift()
								.get(MethodEnum.INPUT);

						//CL MAX
						_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getCLMaxHighLift().get(MethodEnum.INPUT);

						//CL STAR
						_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getCLStarHighLift()
								.get(MethodEnum.INPUT);

						//ALPHA STAR
						_alphaStarWingFuselage = Amount.valueOf(
								(_clStarWingFuselage - _clZeroWingFuselage)/
								_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
								NonSI.DEGREE_ANGLE);

						//ALPHA stall
						double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
								_liftingSurfaceAerodynamicManagers.get(
										ComponentEnum.WING).getAlphaStallHighLift()
								.get(MethodEnum.INPUT)
								.doubleValue(NonSI.DEGREE_ANGLE);

						_alphaStallWingFuselage = Amount.valueOf(
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getAlphaStallHighLift()
								.get(MethodEnum.INPUT).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
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
				}
				break;
			case CLIMB:
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

					CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
					calcLiftCurve.nasaBlackwell(_currentMachNumber);
					
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.LIFT_CURVE_3D,
							MethodEnum.NASA_BLACKWELL
							);

					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getLiftCoefficient3DCurve()
							.get(MethodEnum.NASA_BLACKWELL));
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

						//.............................................................................
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

						//.............................................................................
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

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.INPUT)){

						//....................................................................................................
						// PRELIMINARY CHECKS (all the required parameters have to be retrieved from the assigned input curve)
						//....................................................................................................

						// CLalpha 
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().put(
								MethodEnum.INPUT,
								Amount.valueOf(
										_theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(1.0)
										- _theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(0.0),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						// CL0 
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().put(
								MethodEnum.INPUT,
								_theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(0.0)
								);

						// CLstar /* TODO: CHECK THIS */
						List<Double> cLList = _alphaWingList.stream()
								.map(aw -> _theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
								.collect(Collectors.toList());
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStar().put(
								MethodEnum.INPUT,
								LiftCalc.calculateCLStarFromCurve(
										_alphaWingList, 
										cLList
										)
								);

						// CLmax 
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLMax().put(
								MethodEnum.INPUT,
								_alphaWingList.stream()
								.mapToDouble(aw -> _theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
								.max()
								.getAsDouble()
								);

						// ALPHA STALL 
						int indexOfMaxCLList = MyArrayUtils.getIndexOfMax(
								MyArrayUtils.convertToDoublePrimitive(cLList)
								);
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStall().put(
								MethodEnum.INPUT,
								_alphaWingList.get(indexOfMaxCLList)
								);

						//............................................................................................

						//CL ALPHA
						_clAlphaWingFuselage =
								LiftCalc.calculateCLAlphaFuselage(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
												MethodEnum.INPUT),
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
										Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
												SI.METER)
										);


						//CL ZERO
						_clZeroWingFuselage =
								-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero()
								.get(MethodEnum.INPUT);

						//CL MAX
						_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getCLMax().get(MethodEnum.INPUT);

						//CL STAR
						_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getCLStar()
								.get(MethodEnum.INPUT);

						//ALPHA STAR
						_alphaStarWingFuselage = Amount.valueOf(
								(_clStarWingFuselage - _clZeroWingFuselage)/
								_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
								NonSI.DEGREE_ANGLE);

						//ALPHA stall
						double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
								_liftingSurfaceAerodynamicManagers.get(
										ComponentEnum.WING).getAlphaStar()
								.get(MethodEnum.INPUT)
								.doubleValue(NonSI.DEGREE_ANGLE);

						_alphaStallWingFuselage = Amount.valueOf(
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getAlphaStall()
								.get(MethodEnum.INPUT).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
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

					CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();
					calcLiftCurve.nasaBlackwell(_currentMachNumber);
					
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.LIFT_CURVE_3D,
							MethodEnum.NASA_BLACKWELL
							);

					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getLiftCoefficient3DCurve()
							.get(MethodEnum.NASA_BLACKWELL));
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

						//.............................................................................
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

						//.............................................................................
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
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.INPUT)){

						//....................................................................................................
						// PRELIMINARY CHECKS (all the required parameters have to be retrieved from the assigned input curve)
						//....................................................................................................

						// CLalpha 
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().put(
								MethodEnum.INPUT,
								Amount.valueOf(
										_theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(1.0)
										- _theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(0.0),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						// CL0 
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().put(
								MethodEnum.INPUT,
								_theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(0.0)
								);

						// CLstar /* TODO: CHECK THIS */
						List<Double> cLList = _alphaWingList.stream()
								.map(aw -> _theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
								.collect(Collectors.toList());
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStar().put(
								MethodEnum.INPUT,
								LiftCalc.calculateCLStarFromCurve(
										_alphaWingList, 
										cLList
										)
								);

						// CLmax 
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLMax().put(
								MethodEnum.INPUT,
								_alphaWingList.stream()
								.mapToDouble(aw -> _theAerodynamicBuilderInterface.getWingLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
								.max()
								.getAsDouble()
								);

						// ALPHA STALL 
						int indexOfMaxCLList = MyArrayUtils.getIndexOfMax(
								MyArrayUtils.convertToDoublePrimitive(cLList)
								);
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStall().put(
								MethodEnum.INPUT,
								_alphaWingList.get(indexOfMaxCLList)
								);

						//............................................................................................

						//CL ALPHA
						_clAlphaWingFuselage =
								LiftCalc.calculateCLAlphaFuselage(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
												MethodEnum.INPUT),
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
										Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
												SI.METER)
										);


						//CL ZERO
						_clZeroWingFuselage =
								-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero()
								.get(MethodEnum.INPUT);

						//CL MAX
						_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getCLMax().get(MethodEnum.INPUT);

						//CL STAR
						_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getCLStar()
								.get(MethodEnum.INPUT);

						//ALPHA STAR
						_alphaStarWingFuselage = Amount.valueOf(
								(_clStarWingFuselage - _clZeroWingFuselage)/
								_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
								NonSI.DEGREE_ANGLE);

						//ALPHA stall
						double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
								_liftingSurfaceAerodynamicManagers.get(
										ComponentEnum.WING).getAlphaStar()
								.get(MethodEnum.INPUT)
								.doubleValue(NonSI.DEGREE_ANGLE);

						_alphaStallWingFuselage = Amount.valueOf(
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getAlphaStall()
								.get(MethodEnum.INPUT).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
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

					CalcHighLiftCurve calcHighLiftCurveTakeOff = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftCurve();
					calcHighLiftCurveTakeOff.semiempirical(
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
							_currentMachNumber 
							);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D,
							MethodEnum.SEMIEMPIRICAL
							);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.LIFT_CURVE_3D,
							MethodEnum.NASA_BLACKWELL
							);

					temporaryLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getLiftCoefficient3DCurveHighLift()
							.get(MethodEnum.SEMIEMPIRICAL));
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

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D).equals(MethodEnum.SEMIEMPIRICAL)){

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
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D).equals(MethodEnum.INPUT)){

						//....................................................................................................
						// PRELIMINARY CHECKS (all the required parameters have to be retrieved from the assigned input curve)
						//....................................................................................................

						// CLalpha HIGH LIFT
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlphaHighLift().put(
								MethodEnum.INPUT,
								Amount.valueOf(
										_theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(1.0)
										- _theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(0.0),
										NonSI.DEGREE_ANGLE.inverse()
										)
								);

						// CL0 HIGH LIFT
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZeroHighLift().put(
								MethodEnum.INPUT,
								_theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(0.0)
								);

						// CLstar HIGH LIFT /* TODO: CHECK THIS */
						List<Double> cLHighLiftList = _alphaWingList.stream()
								.map(aw -> _theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
								.collect(Collectors.toList());
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStarHighLift().put(
								MethodEnum.INPUT,
								LiftCalc.calculateCLStarFromCurve(
										_alphaWingList, 
										cLHighLiftList
										)
								);

						// CLmax HIGH LIFT
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLMaxHighLift().put(
								MethodEnum.INPUT,
								_alphaWingList.stream()
								.mapToDouble(aw -> _theAerodynamicBuilderInterface.getWingHighLiftCurveFunction().value(aw.doubleValue(NonSI.DEGREE_ANGLE)))
								.max()
								.getAsDouble()
								);

						// ALPHA STALL HIGH LIFT
						int indexOfMaxCLHighLiftList = MyArrayUtils.getIndexOfMax(
								MyArrayUtils.convertToDoublePrimitive(cLHighLiftList)
								);
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStallHighLift().put(
								MethodEnum.INPUT,
								_alphaWingList.get(indexOfMaxCLHighLiftList)
								);

						//............................................................................................

						//CL ALPHA
						_clAlphaWingFuselage =
								LiftCalc.calculateCLAlphaFuselage(
										_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlphaHighLift().get(
												MethodEnum.INPUT),
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getSpan(), 
										Amount.valueOf(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
												_theAerodynamicBuilderInterface.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER)),
												SI.METER)
										);

						//CL ZERO
						_clZeroWingFuselage =
								-_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()*
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZeroHighLift()
								.get(MethodEnum.INPUT);

						//CL MAX
						_clMaxWingFuselage = _liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getCLMaxHighLift().get(MethodEnum.INPUT);

						//CL STAR
						_clStarWingFuselage = _liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getCLStarHighLift()
								.get(MethodEnum.INPUT);

						//ALPHA STAR
						_alphaStarWingFuselage = Amount.valueOf(
								(_clStarWingFuselage - _clZeroWingFuselage)/
								_clAlphaWingFuselage.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(), 
								NonSI.DEGREE_ANGLE);

						//ALPHA stall
						double deltaAlphaStarDeg = 	_alphaStarWingFuselage.doubleValue(NonSI.DEGREE_ANGLE) - 
								_liftingSurfaceAerodynamicManagers.get(
										ComponentEnum.WING).getAlphaStallHighLift()
								.get(MethodEnum.INPUT)
								.doubleValue(NonSI.DEGREE_ANGLE);

						_alphaStallWingFuselage = Amount.valueOf(
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING).getAlphaStallHighLift()
								.get(MethodEnum.INPUT).doubleValue(NonSI.DEGREE_ANGLE) - deltaAlphaStarDeg, 
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
				}
				break;
			}
			_current3DWingLiftCurve = temporaryLiftCurve;

		}

		//.........................................................................................................................
		//	WING POLAR_CURVE_3D
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				CalcPolar calcPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcPolar();

				calcPolarCurve.fromCdDistribution(_currentMachNumber, _currentAltitude);

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
						AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,
						MethodEnum.AIRFOIL_DISTRIBUTION
						);
			}

			switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
			case TAKE_OFF:
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

						CalcHighLiftPolarCurve calcHighLiftPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftPolarCurve();
						calcHighLiftPolarCurve.semiempirical(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
										), 
								_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
								_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
								_currentMachNumber
								);
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
								AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D, 
								MethodEnum.SEMIEMPIRICAL
								);
						
						_current3DWingPolarCurve = MyArrayUtils.convertDoubleArrayToListDouble(
								_liftingSurfaceAerodynamicManagers
								.get(ComponentEnum.WING)
								.getPolar3DCurveHighLift()
								.get(_theAerodynamicBuilderInterface.getComponentTaskList()
										.get(ComponentEnum.WING)
										.get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D))
								);
						
				}
				else {
					_current3DWingPolarCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getPolar3DCurveHighLift()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D))
							);
				}
			break;
			case CLIMB:
				_current3DWingPolarCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getPolar3DCurve()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE))
						);
				break;
			case CRUISE:
				_current3DWingPolarCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getPolar3DCurve()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE))
						);
				break;
			case LANDING:
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

					CalcHighLiftPolarCurve calcHighLiftPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftPolarCurve();
					calcHighLiftPolarCurve.semiempirical(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
									), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
							_currentMachNumber
							);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D, 
							MethodEnum.SEMIEMPIRICAL
							);
					
					_current3DWingPolarCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getPolar3DCurveHighLift()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D))
							);
					
			}
			else {
				_current3DWingPolarCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getPolar3DCurveHighLift()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D))
						);
			}
				break;
			}
		}

		//.........................................................................................................................
		//	WING MOMENT CURVE
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				CalcMomentCurve calcMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcMomentCurve();

				calcMomentCurve.fromAirfoilDistribution();

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
						AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE,
						MethodEnum.AIRFOIL_DISTRIBUTION
						);
			}

			switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
			case TAKE_OFF:
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

					CalcHighLiftMomentCurve calcHighLiftMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftMomentCurve();
					calcHighLiftMomentCurve.semiempirical(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
									), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionTakeOff(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionTakeOff(), 
							_currentMachNumber
							);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D, 
							MethodEnum.SEMIEMPIRICAL
							);
					
					_current3DWingMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getMomentCoefficient3DCurveHighLift()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D))
							);
					
			}
			else {
				_current3DWingMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getMomentCoefficient3DCurveHighLift()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D))
						);
			}
				break;
			case CLIMB:
				_current3DWingMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getMoment3DCurve()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE))
						);
				break;
			case CRUISE:
				_current3DWingMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getMoment3DCurve()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE))
						);
				break;
			case LANDING:
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

					CalcHighLiftMomentCurve calcHighLiftMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcHighLiftMomentCurve();
					calcHighLiftMomentCurve.semiempirical(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
									), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getFlapDeflectionLanding(), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions().getSlatDeflectionLanding(), 
							_currentMachNumber
							);
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(
							AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D, 
							MethodEnum.SEMIEMPIRICAL
							);
					
					_current3DWingMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
							_liftingSurfaceAerodynamicManagers
							.get(ComponentEnum.WING)
							.getMomentCoefficient3DCurveHighLift()
							.get(_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D))
							);
					
			}
			else {
				_current3DWingMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getMomentCoefficient3DCurveHighLift()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D))
						);
			}
				break;
			}
		}

		//.........................................................................................................................
		//	FUSELAGE POLAR_CURVE_3D
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcPolar calcFuselagePolarCurve = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcPolar();

				calcFuselagePolarCurve.fusDes();

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).put(
						AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE,
						MethodEnum.FUSDES
						);
			}
		}

		//.........................................................................................................................
		//	FUSELAGE MOMENT_CURVE_3D
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				analyses.fuselage.FuselageAerodynamicsManager.CalcMomentCurve calcFuselageMomentCurve = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcMomentCurve();

				calcFuselageMomentCurve.fusDes();

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).put(
						AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE,
						MethodEnum.FUSDES
						);
			}
		}

		//.........................................................................................................................
		//	HORIZONTAL TAIL LIFT_CURVES_3D
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
						_currentMachNumber 
						);
				temporaryLiftHorizontalTail = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL)
						.getLiftCoefficient3DCurveHighLift()
						.get(MethodEnum.SEMIEMPIRICAL)
						);

				_current3DHorizontalTailLiftCurve.put(
						de, 
						temporaryLiftHorizontalTail);

			});

			_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(
					AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D,
					MethodEnum.SEMIEMPIRICAL
					);
		}

		//.........................................................................................................................
		//	HORIZONTAL TAIL POLAR_CURVES_3D
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				CalcPolar calcBaselinePolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcPolar();

				calcBaselinePolarCurve.fromCdDistribution(_currentMachNumber, _currentAltitude);

				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(
						AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,
						MethodEnum.AIRFOIL_DISTRIBUTION
						);
			}
			
			_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach(de -> {

				int i = _theAerodynamicBuilderInterface.getDeltaElevatorList().indexOf(de);
				
				/*
				 * THIS EQUATION IS AN INTERPOLATING FUNCTION OBTAINED FROM CFD ANALYSES ON THE IRON AIRCRAFT
				 * SINCE THE SEMIEMPIRICAL METHOD ESTIMATES A DELTA_CD0 TOO HIGH FOR HTAIL 
				 */
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

		//.........................................................................................................................
		//	HORIZONTAL TAIL MOMENT_CURVES_3D
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL) ||
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)){

			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {
				
				CalcMomentCurve calcBaselineMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMomentCurve();

				calcBaselineMomentCurve.fromAirfoilDistribution();
				
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(
						AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE,
						MethodEnum.AIRFOIL_DISTRIBUTION
						);
				
			}
			_theAerodynamicBuilderInterface.getDeltaElevatorList().stream().forEach(de -> {

				List<Amount<Angle>> deltaElevatorList = new ArrayList<>();
				deltaElevatorList.add(de.to(NonSI.DEGREE_ANGLE));
				
				CalcHighLiftMomentCurve calcHighLiftMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcHighLiftMomentCurve();
				calcHighLiftMomentCurve.semiempirical(
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
								),
						deltaElevatorList,
						null,
						_currentMachNumber
						);
				
				List<Double> temporaryMomentCurve = new ArrayList<>();
				temporaryMomentCurve = MyArrayUtils.convertDoubleArrayToListDouble(
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL)
						.getMomentCoefficient3DCurveHighLift()
						.get(MethodEnum.SEMIEMPIRICAL)
						);

				_current3DHorizontalTailMomentCurve.put(
						de, 
						temporaryMomentCurve);
				
			});
		}

		//.........................................................................................................................
		//	VERTICAL TAIL DRAG COEFFICIENT AT BETA = 0 
		CalcCDAtAlpha calcCDAtBetaVerticalTail = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcCDAtAlpha();
		_current3DVerticalTailDragCoefficient = calcCDAtBetaVerticalTail
				.fromCdDistribution(
						Amount.valueOf(0.0, NonSI.DEGREE_ANGLE),
						_currentMachNumber,
						_currentAltitude
						);
	}

	private void calculateHorizontalTailAndNacelleCurrentAlpha() {
		
		//...................................................................................
		// CALCULATION OF THE CURRENT ALPHA FOR WING AND HTAIL
		//...................................................................................
		
		//...................................................................................
		Amount<Angle> currentDownwashAngle = null;
		
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DOWNWASH))
		
			currentDownwashAngle = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_downwashAngleMap
									.get(_theAerodynamicBuilderInterface.getDownwashConstant())
									.get(_theAerodynamicBuilderInterface.getComponentTaskList()
											.get(ComponentEnum.AIRCRAFT)
											.get(AerodynamicAndStabilityEnum.DOWNWASH)
											)
									.subList(
											0,
											MyArrayUtils.getIndexOfMax(
													MyArrayUtils.convertListOfAmountToDoubleArray(
															_downwashAngleMap
															.get(_theAerodynamicBuilderInterface.getDownwashConstant())
															.get(_theAerodynamicBuilderInterface.getComponentTaskList()
																	.get(ComponentEnum.AIRCRAFT)
																	.get(AerodynamicAndStabilityEnum.DOWNWASH)
																	)
															)
													)
											)
									),
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_alphaBodyList.subList(
											0,
											MyArrayUtils.getIndexOfMax(
													MyArrayUtils.convertListOfAmountToDoubleArray(
															_downwashAngleMap
															.get(_theAerodynamicBuilderInterface.getDownwashConstant())
															.get(_theAerodynamicBuilderInterface.getComponentTaskList()
																	.get(ComponentEnum.AIRCRAFT)
																	.get(AerodynamicAndStabilityEnum.DOWNWASH)
																	)
															)
													)
											)
									),
							_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
							),
					NonSI.DEGREE_ANGLE
					);	
		
		else /* ...a default method will be used (SLINGERLAND NON LINEAR) */
			
			currentDownwashAngle = Amount.valueOf(
					MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_downwashAngleMap
									.get(Boolean.FALSE)
									.get(MethodEnum.SLINGERLAND)
									.subList(
											0,
											MyArrayUtils.getIndexOfMax(
													MyArrayUtils.convertListOfAmountToDoubleArray(
															_downwashAngleMap
															.get(Boolean.FALSE)
															.get(MethodEnum.SLINGERLAND)
															)
													)
											)
									),
							MyArrayUtils.convertListOfAmountTodoubleArray(
									_alphaBodyList.subList(
											0,
											MyArrayUtils.getIndexOfMax(
													MyArrayUtils.convertListOfAmountToDoubleArray(
															_downwashAngleMap
															.get(Boolean.FALSE)
															.get(MethodEnum.SLINGERLAND)
															)
													)
											)
									),
							_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
							),
					NonSI.DEGREE_ANGLE
					);
		
		this._alphaHTailCurrent = Amount.valueOf(
				_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
				- _theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE)
				- currentDownwashAngle.doubleValue(NonSI.DEGREE_ANGLE)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
				NonSI.DEGREE_ANGLE
				);
		
		//...................................................................................
		switch (_theAerodynamicBuilderInterface.getTheAircraft().getNacelles().getNacellesList().get(0).getMountingPosition()) {
		case WING:
			_alphaNacelleCurrent = _alphaWingCurrent.to(NonSI.DEGREE_ANGLE); //TODO: calculate upwash nacelles
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
				+ "AERODYNAMIC_AND_STABILITY_" + _theAerodynamicBuilderInterface.getCurrentCondition().toString()
				+ File.separator
				);
		
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).isEmpty())
			_writeWing = Boolean.TRUE;
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).isEmpty())
			_writeHTail = Boolean.TRUE;
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).isEmpty())
			_writeVTail = Boolean.TRUE;
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).isEmpty())
			_writeFuselage = Boolean.TRUE;
		if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).isEmpty())
			_writeNacelle = Boolean.TRUE;
		
		// TODO: CONTINUE WITH ALL OTHER WRITE CHEKS
		
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
		
		/*
		 * TODO : FILL ME !!
		 *        REMEMBER TO PERFORM ALL THE NEEDED CHECKS FOR EACH PLOT !!
		 */
		
		//------------------------------------------------------------------------------------------------------------------------------------
		try {
			toXLSFile(aerodynamicAndStabilityFolderPath + "Aerodynamic_and_Stability_" + _theAerodynamicBuilderInterface.getCurrentCondition().toString());
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("resource")
	public static ACAerodynamicAndStabilityManager importFromXML (
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
		MyInterpolatingFunction wingLiftCurveFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction wingHighLiftCurveFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction wingHighLiftPolarCurveFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction wingHighLiftMomentCurveFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction wingPolarCurveFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction wingMomentCurveFunction = new MyInterpolatingFunction();
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
		MethodEnum wingHighLiftDevicesEffectsMethod = null;
		MethodEnum wingHighLiftCurveMethod = null;
		MethodEnum wingHighLiftPolarCurveMethod = null;
		MethodEnum wingHighLiftMomentCurveMethod = null;
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
			
			String wingLiftCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/lift/cL_vs_alpha/@user_defined");
			
			if(wingLiftCurveUserDefinedString != null) {
				
				if(wingLiftCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
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
				
					wingTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.INPUT);
					
				}
				else if(wingLiftCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
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
			}
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

				if(wingCLAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					wingCLAtAlphaMethod = MethodEnum.INPUT;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, wingCLAtAlphaMethod);

			}
		}
		
		//---------------------------------------------------------------
		// HIGH LIFT DEVICES EFFECTS
		String wingHighLiftDevicesEffectsPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/high_lift_devices/high_lift_devices_effects/@perform");
		
		if(wingHighLiftDevicesEffectsPerformString.equalsIgnoreCase("TRUE")){
			
			String wingHighLiftDevicesEffectsMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/high_lift_devices/high_lift_devices_effects/@method");
			
			if(wingHighLiftDevicesEffectsMethodString != null) {
				
				if(wingHighLiftDevicesEffectsMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					wingHighLiftDevicesEffectsMethod = MethodEnum.SEMIEMPIRICAL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, wingHighLiftDevicesEffectsMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// HIGH LIFT CURVE
		String wingHighLiftCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/high_lift_devices/cL_vs_alpha/@perform");
		
		if(wingHighLiftCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String wingHighLiftCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/high_lift_devices/cL_vs_alpha/@user_defined");
			
			if(wingHighLiftCurveUserDefinedString != null) {
				
				if(wingHighLiftCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					wingHighLiftCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> wingHighLiftCurveAlpha = reader.readArrayofAmountFromXML("//wing_analyses/high_lift_devices/cL_vs_alpha/alpha");
					List<Double> wingHighLiftCurveCL = reader.readArrayDoubleFromXML("//wing_analyses/high_lift_devices/cL_vs_alpha/cL");
					wingHighLiftCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									wingHighLiftCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(wingHighLiftCurveCL)
							);
					
					wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, MethodEnum.INPUT);
					
				}
				else if(wingHighLiftCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String wingHighLiftCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//wing_analyses/high_lift_devices/cL_vs_alpha/@method");
					
					if(wingHighLiftCurveMethodString != null) {
						
						if(wingHighLiftCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							wingHighLiftCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, wingHighLiftCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// HIGH LIFT POLAR CURVE
		String wingHighLiftPolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/high_lift_devices/cD_vs_cL/@perform");
		
		if(wingHighLiftPolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String wingHighLiftPolarCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/high_lift_devices/cD_vs_cL/@user_defined");
			
			if(wingHighLiftPolarCurveUserDefinedString != null) {
				
				if(wingHighLiftPolarCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					wingHighLiftPolarCurveFunction = new MyInterpolatingFunction();
					List<Double> wingHighLiftPolarCurveCL = reader.readArrayDoubleFromXML("//wing_analyses/high_lift_devices/cD_vs_cL/cL");
					List<Double> wingHighLiftPolarCurveCD = reader.readArrayDoubleFromXML("//wing_analyses/high_lift_devices/cD_vs_cL/cD");
					wingHighLiftCurveFunction.interpolate(
							MyArrayUtils.convertToDoublePrimitive(wingHighLiftPolarCurveCL), 
							MyArrayUtils.convertToDoublePrimitive(wingHighLiftPolarCurveCD)
							);
					
					wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D, MethodEnum.INPUT);
					
				}
				else if(wingHighLiftPolarCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String wingHighLiftPolarCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//wing_analyses/high_lift_devices/cD_vs_cL/@method");
					
					if(wingHighLiftPolarCurveMethodString != null) {
						
						if(wingHighLiftPolarCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							wingHighLiftPolarCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D, wingHighLiftPolarCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// HIGH LIFT MOMENT CURVE
		String wingHighLiftMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/high_lift_devices/cM_vs_alpha/@perform");
		
		if(wingHighLiftMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String wingHighLiftMomentCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/high_lift_devices/cM_vs_alpha/@user_defined");
			
			if(wingHighLiftMomentCurveUserDefinedString != null) {
				
				if(wingHighLiftMomentCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					wingHighLiftMomentCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> wingHighLiftMomentCurveAlpha = reader.readArrayofAmountFromXML("//wing_analyses/high_lift_devices/cM_vs_alpha/alpha");
					List<Double> wingHighLiftMomentCurveCM = reader.readArrayDoubleFromXML("//wing_analyses/high_lift_devices/cM_vs_alpha/cM");
					wingHighLiftCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									wingHighLiftMomentCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(wingHighLiftMomentCurveCM)
							);
					
					wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D, MethodEnum.INPUT);
					
				}
				else if(wingHighLiftMomentCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String wingHighLiftMomentCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//wing_analyses/high_lift_devices/cM_vs_alpha/@method");
					
					if(wingHighLiftMomentCurveMethodString != null) {
						
						if(wingHighLiftMomentCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							wingHighLiftMomentCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D, wingHighLiftMomentCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// CL AT ALPHA HIGH LIFT
		String wingCLAtAlphaHighLiftPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/high_lift_devices/cL_at_alpha_current/@perform");
		
		if(wingCLAtAlphaHighLiftPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCLAtAlphaHighLiftMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/high_lift_devices/cL_at_alpha_current/@method");
			
			if(wingCLAtAlphaHighLiftMethodString != null) {
				
				if(wingCLAtAlphaHighLiftMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					wingCLAtAlphaHighLiftMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(wingCLAtAlphaHighLiftMethodString.equalsIgnoreCase("INPUT")) 
					wingCLAtAlphaHighLiftMethod = MethodEnum.INPUT;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT, wingCLAtAlphaHighLiftMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD AT ALPHA HIGH LIFT
		String wingCDAtAlphaHighLiftPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/high_lift_devices/cD_at_alpha_current/@perform");
		
		if(wingCDAtAlphaHighLiftPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCDAtAlphaHighLiftMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/high_lift_devices/cD_at_alpha_current/@method");
			
			if(wingCDAtAlphaHighLiftMethodString != null) {
				
				if(wingCDAtAlphaHighLiftMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					wingCDAtAlphaHighLiftMethod = MethodEnum.SEMIEMPIRICAL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT, wingCDAtAlphaHighLiftMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM AT ALPHA HIGH LIFT
		String wingCMAtAlphaHighLiftPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/high_lift_devices/cM_at_alpha_current/@perform");
		
		if(wingCMAtAlphaHighLiftPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCMAtAlphaHighLiftMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/high_lift_devices/cM_at_alpha_current/@method");
			
			if(wingCMAtAlphaHighLiftMethodString != null) {
				
				if(wingCMAtAlphaHighLiftMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					wingCMAtAlphaHighLiftMethod = MethodEnum.SEMIEMPIRICAL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT, wingCMAtAlphaHighLiftMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0
		String wingCD0PerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/drag/cD_Zero/@perform");
		
		if(wingCD0PerformString.equalsIgnoreCase("TRUE")){
			
			String wingCD0MethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/drag/cD_Zero/@method");
			
			if(wingCD0MethodString != null) {
				
				if(wingCD0MethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					wingCD0Method = MethodEnum.SEMIEMPIRICAL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CD0, wingCD0Method);
				
			}
		}
		
		//---------------------------------------------------------------
		// OSWALD FACTOR
		String wingOswaldFactorPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/drag/oswald_factor/@perform");
		
		if(wingOswaldFactorPerformString.equalsIgnoreCase("TRUE")){
			
			String wingOswaldFactorMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/drag/oswald_factor/@method");
			
			if(wingOswaldFactorMethodString != null) {
				
				if(wingOswaldFactorMethodString.equalsIgnoreCase("GROSU")) 
					wingOswaldFactorMethod = MethodEnum.GROSU;
				
				if(wingOswaldFactorMethodString.equalsIgnoreCase("HOWE")) 
					wingOswaldFactorMethod = MethodEnum.HOWE;
				
				if(wingOswaldFactorMethodString.equalsIgnoreCase("RAYMER")) 
					wingOswaldFactorMethod = MethodEnum.RAYMER;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.OSWALD_FACTOR, wingOswaldFactorMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CDi
		String wingCDInducedPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/drag/cD_induced/@perform");
		
		if(wingCDInducedPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCDInducedMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/drag/cD_induced/@method");
			
			if(wingCDInducedMethodString != null) {
				
				if(wingCDInducedMethodString.equalsIgnoreCase("GROSU")) 
					wingCDInducedMethod = MethodEnum.GROSU;
				
				if(wingCDInducedMethodString.equalsIgnoreCase("HOWE")) 
					wingCDInducedMethod = MethodEnum.HOWE;
				
				if(wingCDInducedMethodString.equalsIgnoreCase("RAYMER")) 
					wingCDInducedMethod = MethodEnum.RAYMER;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE, wingCDInducedMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD WAVE
		String wingCDWavePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/drag/cD_wave/@perform");
		
		if(wingCDWavePerformString.equalsIgnoreCase("TRUE")){
			
			String wingCDWaveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/drag/cD_wave/@method");
			
			if(wingCDWaveMethodString != null) {
				
				if(wingCDWaveMethodString.equalsIgnoreCase("LOCK_KORN_WITH_KORN_MASON")) 
					wingCDWaveMethod = MethodEnum.LOCK_KORN_WITH_KORN_MASON;
				
				if(wingCDWaveMethodString.equalsIgnoreCase("LOCK_KORN_WITH_KROO")) 
					wingCDWaveMethod = MethodEnum.LOCK_KORN_WITH_KROO;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CD_WAVE, wingCDWaveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// POLAR CURVE
		String wingPolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/drag/cD_vs_cL/@perform");
		
		if(wingPolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String wingPolarCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/drag/cD_vs_cL/@user_defined");
			
			if(wingPolarCurveUserDefinedString != null) {
				
				if(wingPolarCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					wingPolarCurveFunction = new MyInterpolatingFunction();
					List<Double> wingPolarCurveCL = reader.readArrayDoubleFromXML("//wing_analyses/drag/cD_vs_cL/cL");
					List<Double> wingPolarCurveCD = reader.readArrayDoubleFromXML("//wing_analyses/drag/cD_vs_cL/cD");
					wingPolarCurveFunction.interpolate(
							MyArrayUtils.convertToDoublePrimitive(wingPolarCurveCL), 
							MyArrayUtils.convertToDoublePrimitive(wingPolarCurveCD)
							);
					
					wingTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE, MethodEnum.INPUT);
					
				}
				else if(wingPolarCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String wingPolarCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//wing_analyses/drag/cD_vs_cL/@method");
					
					if(wingPolarCurveMethodString != null) {
						
						if(wingPolarCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							wingPolarCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						if(wingPolarCurveMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
							wingPolarCurveMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
						
						wingTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE, wingPolarCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// DRAG DISTRIBUTION
		String wingDragDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/drag/drag_distributions/@perform");
		
		if(wingDragDistributionPerformString.equalsIgnoreCase("TRUE")){
			
			String wingDragDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/drag/drag_distributions/@method");
			
			if(wingDragDistributionMethodString != null) {
				
				if(wingDragDistributionMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					wingDragDistributionMethod = MethodEnum.NASA_BLACKWELL;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION, wingDragDistributionMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD AT ALPHA
		String wingCDAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/drag/cD_at_alpha_current/@perform");
		
		if(wingCDAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCDAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/drag/cD_at_alpha_current/@method");
			
			if(wingCDAtAlphaMethodString != null) {
				
				if(wingCDAtAlphaMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					wingCDAtAlphaMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(wingCDAtAlphaMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					wingCDAtAlphaMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(wingCDAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					wingCDAtAlphaMethod = MethodEnum.INPUT;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE, wingCDAtAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM_ac
		String wingCMacPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/pitching_moment/cM_ac/@perform");
		
		if(wingCMacPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCMacMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/pitching_moment/cM_ac/@method");
			
			if(wingCMacMethodString != null) {
				
				if(wingCMacMethodString.equalsIgnoreCase("BASIC_AND_ADDITIONAL")) 
					wingCMacMethod = MethodEnum.BASIC_AND_ADDITIONAL;
				
				if(wingCMacMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					wingCMacMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(wingCMacMethodString.equalsIgnoreCase("INTEGRAL_MEAN")) 
					wingCMacMethod = MethodEnum.INTEGRAL_MEAN;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE, wingCMacMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM ALPHA
		String wingCMAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/pitching_moment/cM_alpha/@perform");
		
		if(wingCMAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCMAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/pitching_moment/cM_alpha/@method");
			
			if(wingCMAlphaMethodString != null) {
				
				if(wingCMAlphaMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					wingCMAlphaMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(wingCMAlphaMethodString.equalsIgnoreCase("POLHAMUS")) 
					wingCMAlphaMethod = MethodEnum.POLHAMUS;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE, wingCMAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// MOMENT CURVE
		String wingMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/pitching_moment/cM_vs_alpha/@perform");
		
		if(wingMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String wingMomentCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/pitching_moment/cM_vs_alpha/@user_defined");
			
			if(wingMomentCurveUserDefinedString != null) {
				
				if(wingMomentCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					wingMomentCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> wingMomentCurveAlpha = reader.readArrayofAmountFromXML("//wing_analyses/pitching_moment/cM_vs_alpha/alpha");
					List<Double> wingMomentCurveCM = reader.readArrayDoubleFromXML("//wing_analyses/pitching_moment/cM_vs_alpha/cM");
					wingMomentCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									wingMomentCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(wingMomentCurveCM)
							);
					
					wingTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, MethodEnum.INPUT);
					
				}
				else if(wingMomentCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String wingMomentCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//wing_analyses/pitching_moment/cM_vs_alpha/@method");
					
					if(wingMomentCurveMethodString != null) {
						
						if(wingMomentCurveMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
							wingMomentCurveMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
						
						wingTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, wingMomentCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// CM AT ALPHA
		String wingCMAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/pitching_moment/cM_at_alpha_current/@perform");
		
		if(wingCMAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String wingCMAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/pitching_moment/cM_at_alpha_current/@method");
			
			if(wingCMAtAlphaMethodString != null) {
				
				if(wingCMAtAlphaMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					wingCMAtAlphaMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(wingCMAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					wingCMAtAlphaMethod = MethodEnum.INPUT;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE, wingCMAtAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// MOMENT DISTRIBUTION
		String wingMomentDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//wing_analyses/pitching_moment/moment_distributions/@perform");
		
		if(wingMomentDistributionPerformString.equalsIgnoreCase("TRUE")){
			
			String wingMomentDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//wing_analyses/pitching_moment/moment_distributions/@method");
			
			if(wingMomentDistributionMethodString != null) {
				
				if(wingMomentDistributionMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					wingMomentDistributionMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				wingTaskList.put(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE, wingMomentDistributionMethod);
				
			}
		}
		
		//...............................................................
		// HORIZONTAL TAIL:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> hTailTaskList = new HashMap<>();
		MyInterpolatingFunction hTailLiftCurveFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction hTailPolarCurveFunction = new MyInterpolatingFunction();
		MyInterpolatingFunction hTailMomentCurveFunction = new MyInterpolatingFunction();
		Amount<Angle> elevatorDeflectionForAnalysis = null;
		MethodEnum hTailCriticalMachMethod = null;
		MethodEnum hTailAerodynamicCenterMethod = null;
		MethodEnum hTailCLAtAlphaMethod = null;
		MethodEnum hTailCLZeroMethod = null;
		MethodEnum hTailAlphaZeroLiftMethod = null;
		MethodEnum hTailCLStarMethod = null;
		MethodEnum hTailAlphaStarMethod = null;
		MethodEnum hTailCLAlphaMethod = null;
		MethodEnum hTailCLMaxMethod = null;
		MethodEnum hTailAlphaStallMethod = null;
		MethodEnum hTailLiftCurveMethod = null;
		MethodEnum hTailLiftDistributionMethod = null;
		MethodEnum hTailCD0Method = null;
		MethodEnum hTailCDInducedMethod = null;
		MethodEnum hTailCDWaveMethod = null;
		MethodEnum hTailOswaldFactorMethod = null;
		MethodEnum hTailPolarCurveMethod = null;
		MethodEnum hTailDragDistributionMethod = null;
		MethodEnum hTailCDAtAlphaMethod = null;
		MethodEnum hTailElevatorEffectsMethod = null;
		MethodEnum hTailElevatorLiftCurveMethod = null;
		MethodEnum hTailElevatorPolarCurveMethod = null;
		MethodEnum hTailElevatorMomentCurveMethod = null;
		MethodEnum hTailCLAtAlphaElevatorMethod = null;
		MethodEnum hTailCDAtAlphaElevatorMethod = null;
		MethodEnum hTailCMAtAlphaElevatorMethod = null;
		MethodEnum hTailCMacMethod = null;
		MethodEnum hTailCMAlphaMethod = null;
		MethodEnum hTailCMAtAlphaMethod = null;
		MethodEnum hTailMomentCurveMethod = null;
		MethodEnum hTailMomentDistributionMethod = null;
		
		//---------------------------------------------------------------
		// CRITICAL MACH
		String hTailCriticalMachPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/critical_mach/@perform");
		
		if(hTailCriticalMachPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCriticalMachMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/critical_mach/@method");
			
			if(hTailCriticalMachMethodString != null) {
				
				if(hTailCriticalMachMethodString.equalsIgnoreCase("KORN_MASON")) 
					hTailCriticalMachMethod = MethodEnum.KORN_MASON;
				
				if(hTailCriticalMachMethodString.equalsIgnoreCase("KROO"))  
					hTailCriticalMachMethod = MethodEnum.KROO;
					
				hTailTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, hTailCriticalMachMethod);
			}
		}
		
		
		//---------------------------------------------------------------
		// AERODYNAMIC CENTER
		String hTailAerodynamicCenterPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/aerodynamic_center/@perform");
		
		if(hTailAerodynamicCenterPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailAerodynamicCenterMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/aerodynamic_center/@method");
			
			if(hTailAerodynamicCenterMethodString != null) {
				
				if(hTailAerodynamicCenterMethodString.equalsIgnoreCase("QUARTER")) 
					hTailAerodynamicCenterMethod = MethodEnum.QUARTER;
				
				if(hTailAerodynamicCenterMethodString.equalsIgnoreCase("DEYOUNG_HARPER"))  
					hTailAerodynamicCenterMethod = MethodEnum.DEYOUNG_HARPER;
				
				if(hTailAerodynamicCenterMethodString.equalsIgnoreCase("NAPOLITANO_DATCOM"))  
					hTailAerodynamicCenterMethod = MethodEnum.NAPOLITANO_DATCOM;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, hTailAerodynamicCenterMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL ALPHA
		String hTailCLAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/cL_alpha/@perform");
		
		if(hTailCLAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCLAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/cL_alpha/@method");
			
			if(hTailCLAlphaMethodString != null) {
				
				if(hTailCLAlphaMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					hTailCLAlphaMethod = MethodEnum.NASA_BLACKWELL;
				
				if(hTailCLAlphaMethodString.equalsIgnoreCase("POLHAMUS"))  
					hTailCLAlphaMethod = MethodEnum.POLHAMUS;
				
				if(hTailCLAlphaMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC"))  
					hTailCLAlphaMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(hTailCLAlphaMethodString.equalsIgnoreCase("INTEGRAL_MEAN"))  
					hTailCLAlphaMethod = MethodEnum.INTEGRAL_MEAN;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CL_ALPHA, hTailCLAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL ZERO
		String hTailCLZeroPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/cL_zero/@perform");
		
		if(hTailCLZeroPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCLZeroMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/cL_zero/@method");
			
			if(hTailCLZeroMethodString != null) {
				
				if(hTailCLZeroMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					hTailCLZeroMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(hTailCLZeroMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					hTailCLZeroMethod = MethodEnum.NASA_BLACKWELL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CL_ZERO, hTailCLZeroMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL STAR
		String hTailCLStarPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/cL_star/@perform");
		
		if(hTailCLStarPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCLStarMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/cL_star/@method");
			
			if(hTailCLStarMethodString != null) {
				
				if(hTailCLStarMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					hTailCLStarMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(hTailCLStarMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					hTailCLStarMethod = MethodEnum.NASA_BLACKWELL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CL_STAR, hTailCLStarMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL MAX
		String hTailCLMaxPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/cL_max/@perform");
		
		if(hTailCLMaxPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCLMaxMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/cL_max/@method");
			
			if(hTailCLMaxMethodString != null) {
				
				if(hTailCLMaxMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
					hTailCLMaxMethod = MethodEnum.PHILLIPS_ALLEY;
				
				if(hTailCLMaxMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					hTailCLMaxMethod = MethodEnum.NASA_BLACKWELL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CL_MAX, hTailCLMaxMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA ZERO LIFT
		String hTailAlphaZeroLiftPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/alpha_zero_lift/@perform");
		
		if(hTailAlphaZeroLiftPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailAlphaZeroLiftMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/alpha_zero_lift/@method");
			
			if(hTailAlphaZeroLiftMethodString != null) {
				
				if(hTailAlphaZeroLiftMethodString.equalsIgnoreCase("INTEGRAL_MEAN_NO_TWIST")) 
					hTailAlphaZeroLiftMethod = MethodEnum.INTEGRAL_MEAN_NO_TWIST;
				
				if(hTailAlphaZeroLiftMethodString.equalsIgnoreCase("INTEGRAL_MEAN_TWIST"))  
					hTailAlphaZeroLiftMethod = MethodEnum.INTEGRAL_MEAN_TWIST;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, hTailAlphaZeroLiftMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA STAR
		String hTailAlphaStarPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/alpha_star/@perform");
		
		if(hTailAlphaStarPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailAlphaStarMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/alpha_star/@method");
			
			if(hTailAlphaStarMethodString != null) {
				
				if(hTailAlphaStarMethodString.equalsIgnoreCase("MEAN_AIRFOIL_INFLUENCE_AREAS")) 
					hTailAlphaStarMethod = MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STAR, hTailAlphaStarMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA STALL
		String hTailAlphaStallPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/alpha_stall/@perform");
		
		if(hTailAlphaStallPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailAlphaStallMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/alpha_stall/@method");
			
			if(hTailAlphaStallMethodString != null) {
				
				if(hTailAlphaStallMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
					hTailAlphaStallMethod = MethodEnum.PHILLIPS_ALLEY;
				
				if(hTailAlphaStallMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					hTailAlphaStallMethod = MethodEnum.NASA_BLACKWELL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STALL, hTailAlphaStallMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// LIFT CURVE
		String hTailLiftCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/cL_vs_alpha/@perform");
		
		if(hTailLiftCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String hTailLiftCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/cL_vs_alpha/@user_defined");
			
			if(hTailLiftCurveUserDefinedString != null) {
				
				if(hTailLiftCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					hTailLiftCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> hTailLiftCurveAlpha = reader.readArrayofAmountFromXML("//horizontal_tail_analyses/lift/cL_vs_alpha/alpha");
					List<Double> hTailLiftCurveCL = reader.readArrayDoubleFromXML("//horizontal_tail_analyses/lift/cL_vs_alpha/cL");
					hTailLiftCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									hTailLiftCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(hTailLiftCurveCL)
							);
				
					hTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.INPUT);
					
				}
				else if(hTailLiftCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String hTailLiftCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//horizontal_tail_analyses/lift/cL_vs_alpha/@method");
					
					if(hTailLiftCurveMethodString != null) {
						
						if(hTailLiftCurveMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
							hTailLiftCurveMethod = MethodEnum.PHILLIPS_ALLEY;
						
						if(hTailLiftCurveMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
							hTailLiftCurveMethod = MethodEnum.NASA_BLACKWELL;
						
						hTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, hTailLiftCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// LIFT DISTRIBUTION
		String hTailLiftDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/lift_distributions/@perform");

		if(hTailLiftDistributionPerformString.equalsIgnoreCase("TRUE")){

			String hTailLiftDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/lift_distributions/@method");

			if(hTailLiftDistributionMethodString != null) {

				if(hTailLiftDistributionMethodString.equalsIgnoreCase("SCHRENK")) 
					hTailLiftDistributionMethod = MethodEnum.SCHRENK;

				if(hTailLiftDistributionMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					hTailLiftDistributionMethod = MethodEnum.NASA_BLACKWELL;

				hTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION, hTailLiftDistributionMethod);

			}
		}

		//---------------------------------------------------------------
		// CL AT ALPHA
		String hTailCLAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/lift/cL_at_alpha_current/@perform");

		if(hTailCLAtAlphaPerformString.equalsIgnoreCase("TRUE")){

			String hTailCLAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/lift/cL_at_alpha_current/@method");

			if(hTailCLAtAlphaMethodString != null) {

				if(hTailCLAtAlphaMethodString.equalsIgnoreCase("LINEAR_DLR")) 
					hTailCLAtAlphaMethod = MethodEnum.LINEAR_DLR;

				if(hTailCLAtAlphaMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					hTailCLAtAlphaMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(hTailCLAtAlphaMethodString.equalsIgnoreCase("LINEAR_NASA_BLACKWELL")) 
					hTailCLAtAlphaMethod = MethodEnum.LINEAR_NASA_BLACKWELL;
				
				if(hTailCLAtAlphaMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					hTailCLAtAlphaMethod = MethodEnum.NASA_BLACKWELL;

				if(hTailCLAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					hTailCLAtAlphaMethod = MethodEnum.INPUT;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, hTailCLAtAlphaMethod);

			}
		}
		
		//---------------------------------------------------------------
		// ELEVATOR DEFLECTION FOR ANALYSIS
		String elevatorDeflectionForAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/elevator/@deflection_angle_degree");
		
		if(elevatorDeflectionForAnalysisString != null)
			elevatorDeflectionForAnalysis = Amount.valueOf(
					Double.valueOf(elevatorDeflectionForAnalysisString), 
					NonSI.DEGREE_ANGLE);
		
		//---------------------------------------------------------------
		// ELEVATOR EFFECTS
		String hTailElevatorEffectsPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/elevator/high_lift_devices_effects/@perform");
		
		if(hTailElevatorEffectsPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailElevatorEffectsMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/elevator/high_lift_devices_effects/@method");
			
			if(hTailElevatorEffectsMethodString != null) {
				
				if(hTailElevatorEffectsMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailElevatorEffectsMethod = MethodEnum.SEMIEMPIRICAL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, hTailElevatorEffectsMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ELEVATOR LIFT CURVE
		String hTailElevatorLiftCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/elevator/cL_vs_alpha/@perform");
		
		if(hTailElevatorLiftCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String hTailElevatorLiftCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/elevator/cL_vs_alpha/@method");
			
			if(hTailElevatorLiftCurveMethodString != null) {
				
				if(hTailElevatorLiftCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailElevatorLiftCurveMethod = MethodEnum.SEMIEMPIRICAL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, hTailElevatorLiftCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ELEVATOR LIFT CURVE
		String hTailElevatorPolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/elevator/cD_vs_cL/@perform");
		
		if(hTailElevatorPolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String hTailElevatorPolarCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/elevator/cD_vs_cL/@method");
			
			if(hTailElevatorPolarCurveMethodString != null) {
				
				if(hTailElevatorPolarCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailElevatorPolarCurveMethod = MethodEnum.SEMIEMPIRICAL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D, hTailElevatorPolarCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ELEVATOR MOMENT CURVE
		String hTailElevatorMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/elevator/cM_vs_alpha/@perform");
		
		if(hTailElevatorMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String hTailElevatorMomentCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/elevator/cM_vs_alpha/@method");
			
			if(hTailElevatorMomentCurveMethodString != null) {
				
				if(hTailElevatorMomentCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailElevatorMomentCurveMethod = MethodEnum.SEMIEMPIRICAL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D, hTailElevatorMomentCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL AT ALPHA ELEVATOR
		String hTailCLAtAlphaElevatorPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/elevator/cL_at_alpha_current/@perform");
		
		if(hTailCLAtAlphaElevatorPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCLAtAlphaElevatorMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/elevator/cL_at_alpha_current/@method");
			
			if(hTailCLAtAlphaElevatorMethodString != null) {
				
				if(hTailCLAtAlphaElevatorMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailCLAtAlphaElevatorMethod = MethodEnum.SEMIEMPIRICAL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT, hTailCLAtAlphaElevatorMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD AT ALPHA ELEVATOR
		String hTailCDAtAlphaElevatorPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/elevator/cD_at_alpha_current/@perform");
		
		if(hTailCDAtAlphaElevatorPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCDAtAlphaElevatorMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/elevator/cD_at_alpha_current/@method");
			
			if(hTailCDAtAlphaElevatorMethodString != null) {
				
				if(hTailCDAtAlphaElevatorMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailCDAtAlphaElevatorMethod = MethodEnum.SEMIEMPIRICAL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT, hTailCDAtAlphaElevatorMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM AT ALPHA ELEVATOR
		String hTailCMAtAlphaElevatorPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/elevator/cM_at_alpha_current/@perform");
		
		if(hTailCMAtAlphaElevatorPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCMAtAlphaElevatorMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/elevator/cM_at_alpha_current/@method");
			
			if(hTailCMAtAlphaElevatorMethodString != null) {
				
				if(hTailCMAtAlphaElevatorMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailCMAtAlphaElevatorMethod = MethodEnum.SEMIEMPIRICAL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT, hTailCMAtAlphaElevatorMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0
		String hTailCD0PerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/drag/cD_Zero/@perform");
		
		if(hTailCD0PerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCD0MethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/drag/cD_Zero/@method");
			
			if(hTailCD0MethodString != null) {
				
				if(hTailCD0MethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailCD0Method = MethodEnum.SEMIEMPIRICAL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CD0, hTailCD0Method);
				
			}
		}
		
		//---------------------------------------------------------------
		// OSWALD FACTOR
		String hTailOswaldFactorPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/drag/oswald_factor/@perform");
		
		if(hTailOswaldFactorPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailOswaldFactorMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/drag/oswald_factor/@method");
			
			if(hTailOswaldFactorMethodString != null) {
				
				if(hTailOswaldFactorMethodString.equalsIgnoreCase("GROSU")) 
					hTailOswaldFactorMethod = MethodEnum.GROSU;
				
				if(hTailOswaldFactorMethodString.equalsIgnoreCase("HOWE")) 
					hTailOswaldFactorMethod = MethodEnum.HOWE;
				
				if(hTailOswaldFactorMethodString.equalsIgnoreCase("RAYMER")) 
					hTailOswaldFactorMethod = MethodEnum.RAYMER;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.OSWALD_FACTOR, hTailOswaldFactorMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CDi
		String hTailCDInducedPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/drag/cD_induced/@perform");
		
		if(hTailCDInducedPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCDInducedMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/drag/cD_induced/@method");
			
			if(hTailCDInducedMethodString != null) {
				
				if(hTailCDInducedMethodString.equalsIgnoreCase("GROSU")) 
					hTailCDInducedMethod = MethodEnum.GROSU;
				
				if(hTailCDInducedMethodString.equalsIgnoreCase("HOWE")) 
					hTailCDInducedMethod = MethodEnum.HOWE;
				
				if(hTailCDInducedMethodString.equalsIgnoreCase("RAYMER")) 
					hTailCDInducedMethod = MethodEnum.RAYMER;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE, hTailCDInducedMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD WAVE
		String hTailCDWavePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/drag/cD_wave/@perform");
		
		if(hTailCDWavePerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCDWaveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/drag/cD_wave/@method");
			
			if(hTailCDWaveMethodString != null) {
				
				if(hTailCDWaveMethodString.equalsIgnoreCase("LOCK_KORN_WITH_KORN_MASON")) 
					hTailCDWaveMethod = MethodEnum.LOCK_KORN_WITH_KORN_MASON;
				
				if(hTailCDWaveMethodString.equalsIgnoreCase("LOCK_KORN_WITH_KROO")) 
					hTailCDWaveMethod = MethodEnum.LOCK_KORN_WITH_KROO;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CD_WAVE, hTailCDWaveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// POLAR CURVE
		String hTailPolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/drag/cD_vs_cL/@perform");
		
		if(hTailPolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String hTailPolarCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/drag/cD_vs_cL/@user_defined");
			
			if(hTailPolarCurveUserDefinedString != null) {
				
				if(hTailPolarCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					hTailPolarCurveFunction = new MyInterpolatingFunction();
					List<Double> hTailPolarCurveCL = reader.readArrayDoubleFromXML("//horizontal_tail_analyses/drag/cD_vs_cL/cL");
					List<Double> hTailPolarCurveCD = reader.readArrayDoubleFromXML("//horizontal_tail_analyses/drag/cD_vs_cL/cD");
					hTailPolarCurveFunction.interpolate(
							MyArrayUtils.convertToDoublePrimitive(hTailPolarCurveCL), 
							MyArrayUtils.convertToDoublePrimitive(hTailPolarCurveCD)
							);
					
					hTailTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE, MethodEnum.INPUT);
					
				}
				else if(hTailPolarCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String hTailPolarCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//horizontal_tail_analyses/drag/cD_vs_cL/@method");
					
					if(hTailPolarCurveMethodString != null) {
						
						if(hTailPolarCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							hTailPolarCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						if(hTailPolarCurveMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
							hTailPolarCurveMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
						
						hTailTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE, hTailPolarCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// DRAG DISTRIBUTION
		String hTailDragDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/drag/drag_distributions/@perform");
		
		if(hTailDragDistributionPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailDragDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/drag/drag_distributions/@method");
			
			if(hTailDragDistributionMethodString != null) {
				
				if(hTailDragDistributionMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					hTailDragDistributionMethod = MethodEnum.NASA_BLACKWELL;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION, hTailDragDistributionMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD AT ALPHA
		String hTailCDAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/drag/cD_at_alpha_current/@perform");
		
		if(hTailCDAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCDAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/drag/cD_at_alpha_current/@method");
			
			if(hTailCDAtAlphaMethodString != null) {
				
				if(hTailCDAtAlphaMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					hTailCDAtAlphaMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(hTailCDAtAlphaMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					hTailCDAtAlphaMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(hTailCDAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					hTailCDAtAlphaMethod = MethodEnum.INPUT;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE, hTailCDAtAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM_ac
		String hTailCMacPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/pitching_moment/cM_ac/@perform");
		
		if(hTailCMacPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCMacMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/pitching_moment/cM_ac/@method");
			
			if(hTailCMacMethodString != null) {
				
				if(hTailCMacMethodString.equalsIgnoreCase("BASIC_AND_ADDITIONAL")) 
					hTailCMacMethod = MethodEnum.BASIC_AND_ADDITIONAL;
				
				if(hTailCMacMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					hTailCMacMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(hTailCMacMethodString.equalsIgnoreCase("INTEGRAL_MEAN")) 
					hTailCMacMethod = MethodEnum.INTEGRAL_MEAN;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE, hTailCMacMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM ALPHA
		String hTailCMAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/pitching_moment/cM_alpha/@perform");
		
		if(hTailCMAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCMAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/pitching_moment/cM_alpha/@method");
			
			if(hTailCMAlphaMethodString != null) {
				
				if(hTailCMAlphaMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					hTailCMAlphaMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(hTailCMAlphaMethodString.equalsIgnoreCase("POLHAMUS")) 
					hTailCMAlphaMethod = MethodEnum.POLHAMUS;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE, hTailCMAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// MOMENT CURVE
		String hTailMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/pitching_moment/cM_vs_alpha/@perform");
		
		if(hTailMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String hTailMomentCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/pitching_moment/cM_vs_alpha/@user_defined");
			
			if(hTailMomentCurveUserDefinedString != null) {
				
				if(hTailMomentCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					hTailMomentCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> hTailMomentCurveAlpha = reader.readArrayofAmountFromXML("//horizontal_tail_analyses/pitching_moment/cM_vs_alpha/alpha");
					List<Double> hTailMomentCurveCM = reader.readArrayDoubleFromXML("//horizontal_tail_analyses/pitching_moment/cM_vs_alpha/cM");
					hTailMomentCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									hTailMomentCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(hTailMomentCurveCM)
							);
					
					hTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, MethodEnum.INPUT);
					
				}
				else if(hTailMomentCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String hTailMomentCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//horizontal_tail_analyses/pitching_moment/cM_vs_alpha/@method");
					
					if(hTailMomentCurveMethodString != null) {
						
						if(hTailMomentCurveMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
							hTailMomentCurveMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
						
						hTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, hTailMomentCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// CM AT ALPHA
		String hTailCMAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/pitching_moment/cM_at_alpha_current/@perform");
		
		if(hTailCMAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailCMAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/pitching_moment/cM_at_alpha_current/@method");
			
			if(hTailCMAtAlphaMethodString != null) {
				
				if(hTailCMAtAlphaMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					hTailCMAtAlphaMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(hTailCMAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					hTailCMAtAlphaMethod = MethodEnum.INPUT;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE, hTailCMAtAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// MOMENT DISTRIBUTION
		String hTailMomentDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//horizontal_tail_analyses/pitching_moment/moment_distributions/@perform");
		
		if(hTailMomentDistributionPerformString.equalsIgnoreCase("TRUE")){
			
			String hTailMomentDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//horizontal_tail_analyses/pitching_moment/moment_distributions/@method");
			
			if(hTailMomentDistributionMethodString != null) {
				
				if(hTailMomentDistributionMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					hTailMomentDistributionMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				hTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE, hTailMomentDistributionMethod);
				
			}
		}
		
		//...............................................................
		// VERTICAL TAIL:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> vTailTaskList = new HashMap<>();
		MyInterpolatingFunction vTailLiftCurveFunction = new MyInterpolatingFunction();;
		MyInterpolatingFunction vTailPolarCurveFunction = new MyInterpolatingFunction();;
		MyInterpolatingFunction vTailMomentCurveFunction = new MyInterpolatingFunction();;
		Amount<Angle> rudderDeflectionForAnalysis = null;
		MethodEnum vTailCriticalMachMethod = null;
		MethodEnum vTailAerodynamicCenterMethod = null;
		MethodEnum vTailCLAtAlphaMethod = null;
		MethodEnum vTailCLZeroMethod = null;
		MethodEnum vTailAlphaZeroLiftMethod = null;
		MethodEnum vTailCLStarMethod = null;
		MethodEnum vTailAlphaStarMethod = null;
		MethodEnum vTailCLAlphaMethod = null;
		MethodEnum vTailCLMaxMethod = null;
		MethodEnum vTailAlphaStallMethod = null;
		MethodEnum vTailLiftCurveMethod = null;
		MethodEnum vTailLiftDistributionMethod = null;
		MethodEnum vTailCD0Method = null;
		MethodEnum vTailCDInducedMethod = null;
		MethodEnum vTailCDWaveMethod = null;
		MethodEnum vTailOswaldFactorMethod = null;
		MethodEnum vTailPolarCurveMethod = null;
		MethodEnum vTailDragDistributionMethod = null;
		MethodEnum vTailCDAtAlphaMethod = null;
		MethodEnum vTailRudderEffectsMethod = null;
		MethodEnum vTailRudderLiftCurveMethod = null;
		MethodEnum vTailRudderPolarCurveMethod = null;
		MethodEnum vTailRudderMomentCurveMethod = null;
		MethodEnum vTailCLAtAlphaRudderMethod = null;
		MethodEnum vTailCDAtAlphaRudderMethod = null;
		MethodEnum vTailCMAtAlphaRudderMethod = null;
		MethodEnum vTailCMacMethod = null;
		MethodEnum vTailCMAlphaMethod = null;
		MethodEnum vTailCMAtAlphaMethod = null;
		MethodEnum vTailMomentCurveMethod = null;
		MethodEnum vTailMomentDistributionMethod = null;
		
		//---------------------------------------------------------------
		// CRITICAL MACH
		String vTailCriticalMachPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/critical_mach/@perform");
		
		if(vTailCriticalMachPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCriticalMachMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/critical_mach/@method");
			
			if(vTailCriticalMachMethodString != null) {
				
				if(vTailCriticalMachMethodString.equalsIgnoreCase("KORN_MASON")) 
					vTailCriticalMachMethod = MethodEnum.KORN_MASON;
				
				if(vTailCriticalMachMethodString.equalsIgnoreCase("KROO"))  
					vTailCriticalMachMethod = MethodEnum.KROO;
					
				vTailTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, vTailCriticalMachMethod);
			}
		}
		
		
		//---------------------------------------------------------------
		// AERODYNAMIC CENTER
		String vTailAerodynamicCenterPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/aerodynamic_center/@perform");
		
		if(vTailAerodynamicCenterPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailAerodynamicCenterMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/aerodynamic_center/@method");
			
			if(vTailAerodynamicCenterMethodString != null) {
				
				if(vTailAerodynamicCenterMethodString.equalsIgnoreCase("QUARTER")) 
					vTailAerodynamicCenterMethod = MethodEnum.QUARTER;
				
				if(vTailAerodynamicCenterMethodString.equalsIgnoreCase("DEYOUNG_HARPER"))  
					vTailAerodynamicCenterMethod = MethodEnum.DEYOUNG_HARPER;
				
				if(vTailAerodynamicCenterMethodString.equalsIgnoreCase("NAPOLITANO_DATCOM"))  
					vTailAerodynamicCenterMethod = MethodEnum.NAPOLITANO_DATCOM;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, vTailAerodynamicCenterMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL ALPHA
		String vTailCLAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/cL_alpha/@perform");
		
		if(vTailCLAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCLAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/cL_alpha/@method");
			
			if(vTailCLAlphaMethodString != null) {
				
				if(vTailCLAlphaMethodString.equalsIgnoreCase("HELMBOLD_DIEDERICH")) 
					vTailCLAlphaMethod = MethodEnum.HELMBOLD_DIEDERICH;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CL_ALPHA, vTailCLAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL ZERO
		String vTailCLZeroPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/cL_zero/@perform");
		
		if(vTailCLZeroPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCLZeroMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/cL_zero/@method");
			
			if(vTailCLZeroMethodString != null) {
				
				if(vTailCLZeroMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					vTailCLZeroMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(vTailCLZeroMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					vTailCLZeroMethod = MethodEnum.NASA_BLACKWELL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CL_ZERO, vTailCLZeroMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL STAR
		String vTailCLStarPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/cL_star/@perform");
		
		if(vTailCLStarPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCLStarMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/cL_star/@method");
			
			if(vTailCLStarMethodString != null) {
				
				if(vTailCLStarMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					vTailCLStarMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(vTailCLStarMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					vTailCLStarMethod = MethodEnum.NASA_BLACKWELL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CL_STAR, vTailCLStarMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL MAX
		String vTailCLMaxPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/cL_max/@perform");
		
		if(vTailCLMaxPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCLMaxMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/cL_max/@method");
			
			if(vTailCLMaxMethodString != null) {
				
				if(vTailCLMaxMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
					vTailCLMaxMethod = MethodEnum.PHILLIPS_ALLEY;
				
				if(vTailCLMaxMethodString.equalsIgnoreCase("NASA_BLACKWELL"))  
					vTailCLMaxMethod = MethodEnum.NASA_BLACKWELL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CL_MAX, vTailCLMaxMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA ZERO LIFT
		String vTailAlphaZeroLiftPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/alpha_zero_lift/@perform");
		
		if(vTailAlphaZeroLiftPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailAlphaZeroLiftMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/alpha_zero_lift/@method");
			
			if(vTailAlphaZeroLiftMethodString != null) {
				
				if(vTailAlphaZeroLiftMethodString.equalsIgnoreCase("INTEGRAL_MEAN_NO_TWIST")) 
					vTailAlphaZeroLiftMethod = MethodEnum.INTEGRAL_MEAN_NO_TWIST;
				
				if(vTailAlphaZeroLiftMethodString.equalsIgnoreCase("INTEGRAL_MEAN_TWIST"))  
					vTailAlphaZeroLiftMethod = MethodEnum.INTEGRAL_MEAN_TWIST;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, vTailAlphaZeroLiftMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA STAR
		String vTailAlphaStarPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/alpha_star/@perform");
		
		if(vTailAlphaStarPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailAlphaStarMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/alpha_star/@method");
			
			if(vTailAlphaStarMethodString != null) {
				
				if(vTailAlphaStarMethodString.equalsIgnoreCase("MEAN_AIRFOIL_INFLUENCE_AREAS")) 
					vTailAlphaStarMethod = MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STAR, vTailAlphaStarMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// ALPHA STALL
		String vTailAlphaStallPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/alpha_stall/@perform");
		
		if(vTailAlphaStallPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailAlphaStallMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/alpha_stall/@method");
			
			if(vTailAlphaStallMethodString != null) {
				
				if(vTailAlphaStallMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
					vTailAlphaStallMethod = MethodEnum.PHILLIPS_ALLEY;
				
				if(vTailAlphaStallMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					vTailAlphaStallMethod = MethodEnum.NASA_BLACKWELL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STALL, vTailAlphaStallMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// LIFT CURVE
		String vTailLiftCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/cL_vs_alpha/@perform");
		
		if(vTailLiftCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String vTailLiftCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/cL_vs_alpha/@user_defined");
			
			if(vTailLiftCurveUserDefinedString != null) {
				
				if(vTailLiftCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					vTailLiftCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> vTailLiftCurveAlpha = reader.readArrayofAmountFromXML("//vertical_tail_analyses/lift/cL_vs_alpha/alpha");
					List<Double> vTailLiftCurveCL = reader.readArrayDoubleFromXML("//vertical_tail_analyses/lift/cL_vs_alpha/cL");
					vTailLiftCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									vTailLiftCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(vTailLiftCurveCL)
							);
				
					vTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.INPUT);
					
				}
				else if(vTailLiftCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String vTailLiftCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//vertical_tail_analyses/lift/cL_vs_alpha/@method");
					
					if(vTailLiftCurveMethodString != null) {
						
						if(vTailLiftCurveMethodString.equalsIgnoreCase("PHILLIPS_ALLEY")) 
							vTailLiftCurveMethod = MethodEnum.PHILLIPS_ALLEY;
						
						if(vTailLiftCurveMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
							vTailLiftCurveMethod = MethodEnum.NASA_BLACKWELL;
						
						vTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, vTailLiftCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// LIFT DISTRIBUTION
		String vTailLiftDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/lift_distributions/@perform");

		if(vTailLiftDistributionPerformString.equalsIgnoreCase("TRUE")){

			String vTailLiftDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/lift_distributions/@method");

			if(vTailLiftDistributionMethodString != null) {

				if(vTailLiftDistributionMethodString.equalsIgnoreCase("SCHRENK")) 
					vTailLiftDistributionMethod = MethodEnum.SCHRENK;

				if(vTailLiftDistributionMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					vTailLiftDistributionMethod = MethodEnum.NASA_BLACKWELL;

				vTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION, vTailLiftDistributionMethod);

			}
		}

		//---------------------------------------------------------------
		// CL AT ALPHA
		String vTailCLAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/lift/cL_at_alpha_current/@perform");

		if(vTailCLAtAlphaPerformString.equalsIgnoreCase("TRUE")){

			String vTailCLAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/lift/cL_at_alpha_current/@method");

			if(vTailCLAtAlphaMethodString != null) {

				if(vTailCLAtAlphaMethodString.equalsIgnoreCase("LINEAR_DLR")) 
					vTailCLAtAlphaMethod = MethodEnum.LINEAR_DLR;

				if(vTailCLAtAlphaMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					vTailCLAtAlphaMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(vTailCLAtAlphaMethodString.equalsIgnoreCase("LINEAR_NASA_BLACKWELL")) 
					vTailCLAtAlphaMethod = MethodEnum.LINEAR_NASA_BLACKWELL;
				
				if(vTailCLAtAlphaMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					vTailCLAtAlphaMethod = MethodEnum.NASA_BLACKWELL;
				
				if(vTailCLAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					vTailCLAtAlphaMethod = MethodEnum.INPUT;

				vTailTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, vTailCLAtAlphaMethod);

			}
		}
		
		//---------------------------------------------------------------
		// RUDDER DEFLECTION FOR ANALYSIS
		String rudderDeflectionForAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/rudder/@deflection_angle_degree");
		
		if(rudderDeflectionForAnalysisString != null)
			rudderDeflectionForAnalysis = Amount.valueOf(
					Double.valueOf(rudderDeflectionForAnalysisString), 
					NonSI.DEGREE_ANGLE);
		
		//---------------------------------------------------------------
		// RUDDER EFFECTS
		String vTailRudderEffectsPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/rudder/high_lift_devices_effects/@perform");
		
		if(vTailRudderEffectsPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailRudderEffectsMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/rudder/high_lift_devices_effects/@method");
			
			if(vTailRudderEffectsMethodString != null) {
				
				if(vTailRudderEffectsMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailRudderEffectsMethod = MethodEnum.SEMIEMPIRICAL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, vTailRudderEffectsMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// RUDDER LIFT CURVE
		String vTailRudderLiftCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/rudder/cL_vs_alpha/@perform");
		
		if(vTailRudderLiftCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String vTailRudderLiftCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/rudder/cL_vs_alpha/@method");
			
			if(vTailRudderLiftCurveMethodString != null) {
				
				if(vTailRudderLiftCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailRudderLiftCurveMethod = MethodEnum.SEMIEMPIRICAL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, vTailRudderLiftCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// RUDDER POLAR CURVE
		String vTailRudderPolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/rudder/cD_vs_cL/@perform");
		
		if(vTailRudderPolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String vTailRudderPolarCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/rudder/cD_vs_cL/@method");
			
			if(vTailRudderPolarCurveMethodString != null) {
				
				if(vTailRudderPolarCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailRudderPolarCurveMethod = MethodEnum.SEMIEMPIRICAL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D, vTailRudderPolarCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// RUDDER MOMENT CURVE
		String vTailRudderMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/rudder/cM_vs_alpha/@perform");
		
		if(vTailRudderMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String vTailRudderMomentCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/rudder/cM_vs_alpha/@method");
			
			if(vTailRudderMomentCurveMethodString != null) {
				
				if(vTailRudderMomentCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailRudderMomentCurveMethod = MethodEnum.SEMIEMPIRICAL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D, vTailRudderMomentCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CL AT ALPHA RUDDER
		String vTailCLAtAlphaRudderPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/rudder/cL_at_alpha_current/@perform");
		
		if(vTailCLAtAlphaRudderPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCLAtAlphaRudderMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/rudder/cL_at_alpha_current/@method");
			
			if(vTailCLAtAlphaRudderMethodString != null) {
				
				if(vTailCLAtAlphaRudderMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailCLAtAlphaRudderMethod = MethodEnum.SEMIEMPIRICAL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT, vTailCLAtAlphaRudderMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD AT ALPHA RUDDER
		String vTailCDAtAlphaRudderPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/rudder/cD_at_alpha_current/@perform");
		
		if(vTailCDAtAlphaRudderPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCDAtAlphaRudderMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/rudder/cD_at_alpha_current/@method");
			
			if(vTailCDAtAlphaRudderMethodString != null) {
				
				if(vTailCDAtAlphaRudderMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailCDAtAlphaRudderMethod = MethodEnum.SEMIEMPIRICAL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT, vTailCDAtAlphaRudderMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM AT ALPHA RUDDER
		String vTailCMAtAlphaRudderPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/rudder/cM_at_alpha_current/@perform");
		
		if(vTailCMAtAlphaRudderPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCMAtAlphaRudderMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/rudder/cM_at_alpha_current/@method");
			
			if(vTailCMAtAlphaRudderMethodString != null) {
				
				if(vTailCMAtAlphaRudderMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailCMAtAlphaRudderMethod = MethodEnum.SEMIEMPIRICAL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT, vTailCMAtAlphaRudderMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0
		String vTailCD0PerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/drag/cD_Zero/@perform");
		
		if(vTailCD0PerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCD0MethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/drag/cD_Zero/@method");
			
			if(vTailCD0MethodString != null) {
				
				if(vTailCD0MethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailCD0Method = MethodEnum.SEMIEMPIRICAL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CD0, vTailCD0Method);
				
			}
		}
		
		//---------------------------------------------------------------
		// OSWALD FACTOR
		String vTailOswaldFactorPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/drag/oswald_factor/@perform");
		
		if(vTailOswaldFactorPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailOswaldFactorMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/drag/oswald_factor/@method");
			
			if(vTailOswaldFactorMethodString != null) {
				
				if(vTailOswaldFactorMethodString.equalsIgnoreCase("GROSU")) 
					vTailOswaldFactorMethod = MethodEnum.GROSU;
				
				if(vTailOswaldFactorMethodString.equalsIgnoreCase("HOWE")) 
					vTailOswaldFactorMethod = MethodEnum.HOWE;
				
				if(vTailOswaldFactorMethodString.equalsIgnoreCase("RAYMER")) 
					vTailOswaldFactorMethod = MethodEnum.RAYMER;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.OSWALD_FACTOR, vTailOswaldFactorMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CDi
		String vTailCDInducedPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/drag/cD_induced/@perform");
		
		if(vTailCDInducedPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCDInducedMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/drag/cD_induced/@method");
			
			if(vTailCDInducedMethodString != null) {
				
				if(vTailCDInducedMethodString.equalsIgnoreCase("GROSU")) 
					vTailCDInducedMethod = MethodEnum.GROSU;
				
				if(vTailCDInducedMethodString.equalsIgnoreCase("HOWE")) 
					vTailCDInducedMethod = MethodEnum.HOWE;
				
				if(vTailCDInducedMethodString.equalsIgnoreCase("RAYMER")) 
					vTailCDInducedMethod = MethodEnum.RAYMER;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE, vTailCDInducedMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD WAVE
		String vTailCDWavePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/drag/cD_wave/@perform");
		
		if(vTailCDWavePerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCDWaveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/drag/cD_wave/@method");
			
			if(vTailCDWaveMethodString != null) {
				
				if(vTailCDWaveMethodString.equalsIgnoreCase("LOCK_KORN_WITH_KORN_MASON")) 
					vTailCDWaveMethod = MethodEnum.LOCK_KORN_WITH_KORN_MASON;
				
				if(vTailCDWaveMethodString.equalsIgnoreCase("LOCK_KORN_WITH_KROO")) 
					vTailCDWaveMethod = MethodEnum.LOCK_KORN_WITH_KROO;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CD_WAVE, vTailCDWaveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// POLAR CURVE
		String vTailPolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/drag/cD_vs_cL/@perform");
		
		if(vTailPolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String vTailPolarCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/drag/cD_vs_cL/@user_defined");
			
			if(vTailPolarCurveUserDefinedString != null) {
				
				if(vTailPolarCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					vTailPolarCurveFunction = new MyInterpolatingFunction();
					List<Double> vTailPolarCurveCL = reader.readArrayDoubleFromXML("//vertical_tail_analyses/drag/cD_vs_cL/cL");
					List<Double> vTailPolarCurveCD = reader.readArrayDoubleFromXML("//vertical_tail_analyses/drag/cD_vs_cL/cD");
					vTailPolarCurveFunction.interpolate(
							MyArrayUtils.convertToDoublePrimitive(vTailPolarCurveCL), 
							MyArrayUtils.convertToDoublePrimitive(vTailPolarCurveCD)
							);
					
					vTailTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE, MethodEnum.INPUT);
					
				}
				else if(vTailPolarCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String vTailPolarCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//vertical_tail_analyses/drag/cD_vs_cL/@method");
					
					if(vTailPolarCurveMethodString != null) {
						
						if(vTailPolarCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							vTailPolarCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						if(vTailPolarCurveMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
							vTailPolarCurveMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
						
						vTailTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE, vTailPolarCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// DRAG DISTRIBUTION
		String vTailDragDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/drag/drag_distributions/@perform");
		
		if(vTailDragDistributionPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailDragDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/drag/drag_distributions/@method");
			
			if(vTailDragDistributionMethodString != null) {
				
				if(vTailDragDistributionMethodString.equalsIgnoreCase("NASA_BLACKWELL")) 
					vTailDragDistributionMethod = MethodEnum.NASA_BLACKWELL;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION, vTailDragDistributionMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD AT ALPHA
		String vTailCDAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/drag/cD_at_alpha_current/@perform");
		
		if(vTailCDAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCDAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/drag/cD_at_alpha_current/@method");
			
			if(vTailCDAtAlphaMethodString != null) {
				
				if(vTailCDAtAlphaMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					vTailCDAtAlphaMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(vTailCDAtAlphaMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					vTailCDAtAlphaMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(vTailCDAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					vTailCDAtAlphaMethod = MethodEnum.INPUT;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE, vTailCDAtAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM_ac
		String vTailCMacPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/pitching_moment/cM_ac/@perform");
		
		if(vTailCMacPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCMacMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/pitching_moment/cM_ac/@method");
			
			if(vTailCMacMethodString != null) {
				
				if(vTailCMacMethodString.equalsIgnoreCase("BASIC_AND_ADDITIONAL")) 
					vTailCMacMethod = MethodEnum.BASIC_AND_ADDITIONAL;
				
				if(vTailCMacMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					vTailCMacMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(vTailCMacMethodString.equalsIgnoreCase("INTEGRAL_MEAN")) 
					vTailCMacMethod = MethodEnum.INTEGRAL_MEAN;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE, vTailCMacMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM ALPHA
		String vTailCMAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/pitching_moment/cM_alpha/@perform");
		
		if(vTailCMAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCMAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/pitching_moment/cM_alpha/@method");
			
			if(vTailCMAlphaMethodString != null) {
				
				if(vTailCMAlphaMethodString.equalsIgnoreCase("ANDERSON_COMPRESSIBLE_SUBSONIC")) 
					vTailCMAlphaMethod = MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC;
				
				if(vTailCMAlphaMethodString.equalsIgnoreCase("POLHAMUS")) 
					vTailCMAlphaMethod = MethodEnum.POLHAMUS;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE, vTailCMAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// MOMENT CURVE
		String vTailMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/pitching_moment/cM_vs_alpha/@perform");
		
		if(vTailMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String vTailMomentCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/pitching_moment/cM_vs_alpha/@user_defined");
			
			if(vTailMomentCurveUserDefinedString != null) {
				
				if(vTailMomentCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					vTailMomentCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> vTailMomentCurveAlpha = reader.readArrayofAmountFromXML("//vertical_tail_analyses/pitching_moment/cM_vs_alpha/alpha");
					List<Double> vTailMomentCurveCM = reader.readArrayDoubleFromXML("//vertical_tail_analyses/pitching_moment/cM_vs_alpha/cM");
					vTailMomentCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									vTailMomentCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(vTailMomentCurveCM)
							);
					
					vTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, MethodEnum.INPUT);
					
				}
				else if(vTailMomentCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String vTailMomentCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//vertical_tail_analyses/pitching_moment/cM_vs_alpha/@method");
					
					if(vTailMomentCurveMethodString != null) {
						
						if(vTailMomentCurveMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
							vTailMomentCurveMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
						
						vTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, vTailMomentCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// CM AT ALPHA
		String vTailCMAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/pitching_moment/cM_at_alpha_current/@perform");
		
		if(vTailCMAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailCMAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/pitching_moment/cM_at_alpha_current/@method");
			
			if(vTailCMAtAlphaMethodString != null) {
				
				if(vTailCMAtAlphaMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					vTailCMAtAlphaMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				if(vTailCMAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					vTailCMAtAlphaMethod = MethodEnum.INPUT;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE, vTailCMAtAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// MOMENT DISTRIBUTION
		String vTailMomentDistributionPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//vertical_tail_analyses/pitching_moment/moment_distributions/@perform");
		
		if(vTailMomentDistributionPerformString.equalsIgnoreCase("TRUE")){
			
			String vTailMomentDistributionMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//vertical_tail_analyses/pitching_moment/moment_distributions/@method");
			
			if(vTailMomentDistributionMethodString != null) {
				
				if(vTailMomentDistributionMethodString.equalsIgnoreCase("AIRFOIL_DISTRIBUTION")) 
					vTailMomentDistributionMethod = MethodEnum.AIRFOIL_DISTRIBUTION;
				
				vTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE, vTailMomentDistributionMethod);
				
			}
		}
		
		//...............................................................
		// FUSELAGE:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> fuselageTaskList = new HashMap<>();
		MyInterpolatingFunction fuselagePolarCurveFunction = new MyInterpolatingFunction();;
		MyInterpolatingFunction fuselageMomentCurveFunction = new MyInterpolatingFunction();;
		MethodEnum fuselageCD0ParasiteMethod = null;
		MethodEnum fuselageCD0BaseMethod = null;
		MethodEnum fuselageCD0UpsweepMethod = null;
		MethodEnum fuselageCD0WindshieldMethod = null;
		MethodEnum fuselageCD0TotalMethod = null;
		MethodEnum fuselageCDInducedMethod = null;
		MethodEnum fuselagePolarCurveMethod = null;
		MethodEnum fuselageCDAtAlphaMethod = null;
		MethodEnum fuselageCM0Method = null;
		MethodEnum fuselageCMAlphaMethod = null;
		MethodEnum fuselageCMAtAlphaMethod = null;
		MethodEnum fuselageMomentCurveMethod = null;
		
		//---------------------------------------------------------------
		// CD0 PARASITE
		String fuselageCD0ParasitePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/drag/cD0_parasite/@perform");
		
		if(fuselageCD0ParasitePerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCD0ParasiteMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/drag/cD0_parasite/@method");
			
			if(fuselageCD0ParasiteMethodString != null) {
				
				if(fuselageCD0ParasiteMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					fuselageCD0ParasiteMethod = MethodEnum.SEMIEMPIRICAL;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_PARASITE_FUSELAGE, fuselageCD0ParasiteMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0 BASE
		String fuselageCD0BasePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/drag/cD0_base/@perform");
		
		if(fuselageCD0BasePerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCD0BaseMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/drag/cD0_base/@method");
			
			if(fuselageCD0BaseMethodString != null) {
				
				if(fuselageCD0BaseMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					fuselageCD0BaseMethod = MethodEnum.SEMIEMPIRICAL;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_BASE_FUSELAGE, fuselageCD0BaseMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0 UPSWEEP
		String fuselageCD0UpsweepPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/drag/cD0_upsweep/@perform");
		
		if(fuselageCD0UpsweepPerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCD0UpsweepMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/drag/cD0_upsweep/@method");
			
			if(fuselageCD0UpsweepMethodString != null) {
				
				if(fuselageCD0UpsweepMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					fuselageCD0UpsweepMethod = MethodEnum.SEMIEMPIRICAL;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_UPSWEEP_FUSELAGE, fuselageCD0UpsweepMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0 WINDSHIELD
		String fuselageCD0WindshieldPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/drag/cD0_windshield/@perform");
		
		if(fuselageCD0WindshieldPerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCD0WindshieldMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/drag/cD0_windshield/@method");
			
			if(fuselageCD0WindshieldMethodString != null) {
				
				if(fuselageCD0WindshieldMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					fuselageCD0WindshieldMethod = MethodEnum.SEMIEMPIRICAL;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_WINDSHIELD_FUSELAGE, fuselageCD0WindshieldMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0 TOTAL
		String fuselageCD0TotalPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/drag/cD0_total/@perform");
		
		if(fuselageCD0TotalPerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCD0TotalMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/drag/cD0_total/@method");
			
			if(fuselageCD0TotalMethodString != null) {
				
				if(fuselageCD0TotalMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					fuselageCD0TotalMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(fuselageCD0TotalMethodString.equalsIgnoreCase("FUSDES")) 
					fuselageCD0TotalMethod = MethodEnum.FUSDES;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_TOTAL_FUSELAGE, fuselageCD0TotalMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CDi
		String fuselageCDInducedPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/drag/cD_induced/@perform");
		
		if(fuselageCDInducedPerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCDInducedMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/drag/cD_induced/@method");
			
			if(fuselageCDInducedMethodString != null) {
				
				if(fuselageCDInducedMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					fuselageCDInducedMethod = MethodEnum.SEMIEMPIRICAL;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE, fuselageCDInducedMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// POLAR CURVE
		String fuselagePolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/drag/cD_vs_alpha/@perform");
		
		if(fuselagePolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String fuselagePolarCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/drag/cD_vs_alpha/@user_defined");
			
			if(fuselagePolarCurveUserDefinedString != null) {
				
				if(fuselagePolarCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					fuselagePolarCurveFunction = new MyInterpolatingFunction();
					List<Double> fuselagePolarCurveAlpha = reader.readArrayDoubleFromXML("//fuselage_analyses/drag/cD_vs_alpha/alpha");
					List<Double> fuselagePolarCurveCD = reader.readArrayDoubleFromXML("//fuselage_analyses/drag/cD_vs_alpha/cD");
					fuselagePolarCurveFunction.interpolate(
							MyArrayUtils.convertToDoublePrimitive(fuselagePolarCurveAlpha), 
							MyArrayUtils.convertToDoublePrimitive(fuselagePolarCurveCD)
							);
					
					fuselageTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE, MethodEnum.INPUT);
					
				}
				else if(fuselagePolarCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String fuselagePolarCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//fuselage_analyses/drag/cD_vs_alpha/@method");
					
					if(fuselagePolarCurveMethodString != null) {
						
						if(fuselagePolarCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							fuselagePolarCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						if(fuselagePolarCurveMethodString.equalsIgnoreCase("FUSDES")) 
							fuselagePolarCurveMethod = MethodEnum.FUSDES;
						
						fuselageTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE, fuselagePolarCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// CD AT ALPHA
		String fuselageCDAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/drag/cD_at_alpha_current/@perform");
		
		if(fuselageCDAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCDAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/drag/cD_at_alpha_current/@method");
			
			if(fuselageCDAtAlphaMethodString != null) {
				
				if(fuselageCDAtAlphaMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					fuselageCDAtAlphaMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(fuselageCDAtAlphaMethodString.equalsIgnoreCase("FUSDES")) 
					fuselageCDAtAlphaMethod = MethodEnum.FUSDES;
				
				if(fuselageCDAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					fuselageCDAtAlphaMethod = MethodEnum.INPUT;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE, fuselageCDAtAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM0
		String fuselageCM0PerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/pitching_moment/cM0/@perform");
		
		if(fuselageCM0PerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCM0MethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/pitching_moment/cM0/@method");
			
			if(fuselageCM0MethodString != null) {
				
				if(fuselageCM0MethodString.equalsIgnoreCase("MULTHOPP")) 
					fuselageCM0Method = MethodEnum.MULTHOPP;
				
				if(fuselageCM0MethodString.equalsIgnoreCase("FUSDES")) 
					fuselageCM0Method = MethodEnum.FUSDES;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CM0_FUSELAGE, fuselageCM0Method);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM ALPHA
		String fuselageCMAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/pitching_moment/cM_alpha/@perform");
		
		if(fuselageCMAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCMAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/pitching_moment/cM_alpha/@method");
			
			if(fuselageCMAlphaMethodString != null) {
				
				if(fuselageCMAlphaMethodString.equalsIgnoreCase("GILRUTH")) 
					fuselageCMAlphaMethod = MethodEnum.GILRUTH;
				
				if(fuselageCMAlphaMethodString.equalsIgnoreCase("FUSDES")) 
					fuselageCMAlphaMethod = MethodEnum.FUSDES;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_FUSELAGE, fuselageCMAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// MOMENT CURVE
		String fuselageMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/pitching_moment/cM_vs_alpha/@perform");
		
		if(fuselageMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageMomentCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/pitching_moment/cM_vs_alpha/@user_defined");
			
			if(fuselageMomentCurveUserDefinedString != null) {
				
				if(fuselageMomentCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					fuselageMomentCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> fuselageMomentCurveAlpha = reader.readArrayofAmountFromXML("//fuselage_analyses/pitching_moment/cM_vs_alpha/alpha");
					List<Double> fuselageMomentCurveCM = reader.readArrayDoubleFromXML("//fuselage_analyses/pitching_moment/cM_vs_alpha/cM");
					fuselageMomentCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									fuselageMomentCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(fuselageMomentCurveCM)
							);
					
					fuselageTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE, MethodEnum.INPUT);
					
				}
				else if(fuselageMomentCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String fuselageMomentCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//fuselage_analyses/pitching_moment/cM_vs_alpha/@method");
					
					if(fuselageMomentCurveMethodString != null) {
						
						if(fuselageMomentCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							fuselageMomentCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						if(fuselageMomentCurveMethodString.equalsIgnoreCase("FUSDES")) 
							fuselageMomentCurveMethod = MethodEnum.FUSDES;
						
						fuselageTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE, fuselageMomentCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// CM AT ALPHA
		String fuselageCMAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselage_analyses/pitching_moment/cM_at_alpha_current/@perform");
		
		if(fuselageCMAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String fuselageCMAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//fuselage_analyses/pitching_moment/cM_at_alpha_current/@method");
			
			if(fuselageCMAtAlphaMethodString != null) {
				
				if(fuselageCMAtAlphaMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					fuselageCMAtAlphaMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(fuselageCMAtAlphaMethodString.equalsIgnoreCase("FUSDES")) 
					fuselageCMAtAlphaMethod = MethodEnum.FUSDES;
				
				if(fuselageCMAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					fuselageCMAtAlphaMethod = MethodEnum.INPUT;
				
				fuselageTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE, fuselageCMAtAlphaMethod);
				
			}
		}
		
		//...............................................................
		// NACELLE:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> nacelleTaskList = new HashMap<>();
		MyInterpolatingFunction nacellePolarCurveFunction = new MyInterpolatingFunction();;
		MyInterpolatingFunction nacelleMomentCurveFunction = new MyInterpolatingFunction();;
		MethodEnum nacelleCD0ParasiteMethod = null;
		MethodEnum nacelleCD0BaseMethod = null;
		MethodEnum nacelleCD0TotalMethod = null;
		MethodEnum nacelleCDInducedMethod = null;
		MethodEnum nacellePolarCurveMethod = null;
		MethodEnum nacelleCDAtAlphaMethod = null;
		MethodEnum nacelleCM0Method = null;
		MethodEnum nacelleCMAlphaMethod = null;
		MethodEnum nacelleCMAtAlphaMethod = null;
		MethodEnum nacelleMomentCurveMethod = null;
		
		//---------------------------------------------------------------
		// CD0 PARASITE
		String nacelleCD0ParasitePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/drag/cD0_parasite/@perform");
		
		if(nacelleCD0ParasitePerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleCD0ParasiteMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/drag/cD0_parasite/@method");
			
			if(nacelleCD0ParasiteMethodString != null) {
				
				if(nacelleCD0ParasiteMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					nacelleCD0ParasiteMethod = MethodEnum.SEMIEMPIRICAL;
				
				nacelleTaskList.put(AerodynamicAndStabilityEnum.CD0_PARASITE_NACELLE, nacelleCD0ParasiteMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0 BASE
		String nacelleCD0BasePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/drag/cD0_base/@perform");
		
		if(nacelleCD0BasePerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleCD0BaseMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/drag/cD0_base/@method");
			
			if(nacelleCD0BaseMethodString != null) {
				
				if(nacelleCD0BaseMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					nacelleCD0BaseMethod = MethodEnum.SEMIEMPIRICAL;
				
				nacelleTaskList.put(AerodynamicAndStabilityEnum.CD0_BASE_NACELLE, nacelleCD0BaseMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CD0 TOTAL
		String nacelleCD0TotalPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/drag/cD0_total/@perform");
		
		if(nacelleCD0TotalPerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleCD0TotalMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/drag/cD0_total/@method");
			
			if(nacelleCD0TotalMethodString != null) {
				
				if(nacelleCD0TotalMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					nacelleCD0TotalMethod = MethodEnum.SEMIEMPIRICAL;
				
				nacelleTaskList.put(AerodynamicAndStabilityEnum.CD0_TOTAL_NACELLE, nacelleCD0TotalMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CDi
		String nacelleCDInducedPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/drag/cD_induced/@perform");
		
		if(nacelleCDInducedPerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleCDInducedMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/drag/cD_induced/@method");
			
			if(nacelleCDInducedMethodString != null) {
				
				if(nacelleCDInducedMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					nacelleCDInducedMethod = MethodEnum.SEMIEMPIRICAL;
				
				nacelleTaskList.put(AerodynamicAndStabilityEnum.CD_INDUCED_NACELLE, nacelleCDInducedMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// POLAR CURVE
		String nacellePolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/drag/cD_vs_alpha/@perform");
		
		if(nacellePolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String nacellePolarCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/drag/cD_vs_alpha/@user_defined");
			
			if(nacellePolarCurveUserDefinedString != null) {
				
				if(nacellePolarCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					nacellePolarCurveFunction = new MyInterpolatingFunction();
					List<Double> nacellePolarCurveAlpha = reader.readArrayDoubleFromXML("//nacelle_analyses/drag/cD_vs_alpha/alpha");
					List<Double> nacellePolarCurveCD = reader.readArrayDoubleFromXML("//nacelle_analyses/drag/cD_vs_cL/cD");
					nacellePolarCurveFunction.interpolate(
							MyArrayUtils.convertToDoublePrimitive(nacellePolarCurveAlpha), 
							MyArrayUtils.convertToDoublePrimitive(nacellePolarCurveCD)
							);
					
					nacelleTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE, MethodEnum.INPUT);
					
				}
				else if(nacellePolarCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String nacellePolarCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//nacelle_analyses/drag/cD_vs_alpha/@method");
					
					if(nacellePolarCurveMethodString != null) {
						
						if(nacellePolarCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							nacellePolarCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						nacelleTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE, nacellePolarCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// CD AT ALPHA
		String nacelleCDAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/drag/cD_at_alpha_current/@perform");
		
		if(nacelleCDAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleCDAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/drag/cD_at_alpha_current/@method");
			
			if(nacelleCDAtAlphaMethodString != null) {
				
				if(nacelleCDAtAlphaMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					nacelleCDAtAlphaMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(nacelleCDAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					nacelleCDAtAlphaMethod = MethodEnum.INPUT;
				
				nacelleTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE, nacelleCDAtAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM0
		String nacelleCM0PerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/pitching_moment/cM0/@perform");
		
		if(nacelleCM0PerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleCM0MethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/pitching_moment/cM0/@method");
			
			if(nacelleCM0MethodString != null) {
				
				if(nacelleCM0MethodString.equalsIgnoreCase("MULTHOPP")) 
					nacelleCM0Method = MethodEnum.MULTHOPP;
				
				nacelleTaskList.put(AerodynamicAndStabilityEnum.CM0_NACELLE, nacelleCM0Method);
				
			}
		}
		
		//---------------------------------------------------------------
		// CM ALPHA
		String nacelleCMAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/pitching_moment/cM_alpha/@perform");
		
		if(nacelleCMAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleCMAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/pitching_moment/cM_alpha/@method");
			
			if(nacelleCMAlphaMethodString != null) {
				
				if(nacelleCMAlphaMethodString.equalsIgnoreCase("GILRUTH")) 
					nacelleCMAlphaMethod = MethodEnum.GILRUTH;
				
				nacelleTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_NACELLE, nacelleCMAlphaMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// MOMENT CURVE
		String nacelleMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/pitching_moment/cM_vs_alpha/@perform");
		
		if(nacelleMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleMomentCurveUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/pitching_moment/cM_vs_alpha/@user_defined");
			
			if(nacelleMomentCurveUserDefinedString != null) {
				
				if(nacelleMomentCurveUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					nacelleMomentCurveFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> nacelleMomentCurveAlpha = reader.readArrayofAmountFromXML("//nacelle_analyses/pitching_moment/cM_vs_alpha/alpha");
					List<Double> nacelleMomentCurveCM = reader.readArrayDoubleFromXML("//nacelle_analyses/pitching_moment/cM_vs_alpha/cM");
					nacelleMomentCurveFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									nacelleMomentCurveAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(nacelleMomentCurveCM)
							);
					
					nacelleTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE, MethodEnum.INPUT);
					
				}
				else if(nacelleMomentCurveUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String nacelleMomentCurveMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//nacelle_analyses/pitching_moment/cM_vs_alpha/@method");
					
					if(nacelleMomentCurveMethodString != null) {
						
						if(nacelleMomentCurveMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
							nacelleMomentCurveMethod = MethodEnum.SEMIEMPIRICAL;
						
						nacelleTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE, nacelleMomentCurveMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// CM AT ALPHA
		String nacelleCMAtAlphaPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//nacelle_analyses/pitching_moment/cM_at_alpha_current/@perform");
		
		if(nacelleCMAtAlphaPerformString.equalsIgnoreCase("TRUE")){
			
			String nacelleCMAtAlphaMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//nacelle_analyses/pitching_moment/cM_at_alpha_current/@method");
			
			if(nacelleCMAtAlphaMethodString != null) {
				
				if(nacelleCMAtAlphaMethodString.equalsIgnoreCase("SEMIEMPIRICAL")) 
					nacelleCMAtAlphaMethod = MethodEnum.SEMIEMPIRICAL;
				
				if(nacelleCMAtAlphaMethodString.equalsIgnoreCase("INPUT")) 
					nacelleCMAtAlphaMethod = MethodEnum.INPUT;
				
				nacelleTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE, nacelleCMAtAlphaMethod);
				
			}
		}
		
		//...............................................................
		// AIRCRAFT:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> aircraftTaskList = new HashMap<>();
		MyInterpolatingFunction aircraftDownwashGradientFunction = new MyInterpolatingFunction();;
		Boolean aircraftDownwashConstantGradient = Boolean.FALSE;
		Boolean aircraftFuselageEffectOnWingLiftCurve = Boolean.FALSE;
		Boolean aircraftWingPendularStability = Boolean.FALSE;
		Double aircraftDeltaCD0Miscellaneous = 0.0;
		Double aircraftDeltaCD0LandingGears = 0.0;
		MethodEnum aircraftDownwashMethod = null;
		MethodEnum aircraftBuffetBarrierMethod = null;
		MethodEnum aircraftTotalLiftCurveMethod = null;
		MethodEnum aircraftTotalPolarCurveMethod = null;
		MethodEnum aircraftTotalMomentCurveMethod = null;
		MethodEnum aircraftLongitudinalStaticStabilityMethod = null;
		MethodEnum aircraftLateralStaticStabilityMethod = null;
		MethodEnum aircraftDirectionalStaticStabilityMethod = null;
		MethodEnum aircraftDynamicStabilityMethod = null;
		
		//---------------------------------------------------------------
		// DOWNWASH
		String aircraftDownwashPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft_analyses/downwash/@perform");
		
		if(aircraftDownwashPerformString.equalsIgnoreCase("TRUE")){
			
			String aircraftDownwashGradientUserDefinedString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/downwash/@user_defined_gradient");
			
			if(aircraftDownwashGradientUserDefinedString != null) {
				
				if(aircraftDownwashGradientUserDefinedString.equalsIgnoreCase("TRUE")) {
					
					aircraftDownwashConstantGradient = Boolean.FALSE;
					
					aircraftDownwashGradientFunction = new MyInterpolatingFunction();
					List<Amount<Angle>> aircraftDownwashGradientAlpha = reader.readArrayofAmountFromXML("//aircraft_analyses/downwash/downwash_gradient_vs_alpha/alpha");
					List<Double> aircraftDownwashGradient = reader.readArrayDoubleFromXML("//aircraft_analyses/downwash/downwash_gradient_vs_alpha/downwash_gradient");
					aircraftDownwashGradientFunction.interpolate(
							MyArrayUtils.convertListOfAmountTodoubleArray(
									aircraftDownwashGradientAlpha.stream()
									.map(a -> a.to(NonSI.DEGREE_ANGLE))
									.collect(Collectors.toList())
									), 
							MyArrayUtils.convertToDoublePrimitive(aircraftDownwashGradient)
							);

					aircraftTaskList.put(AerodynamicAndStabilityEnum.DOWNWASH, MethodEnum.INPUT);
					
				}
				else if(aircraftDownwashGradientUserDefinedString.equalsIgnoreCase("FALSE")) {
					
					String aircraftDownwashConstantGradientString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//aircraft_analyses/downwash/@constant_downwash_gradient");
					
					if(aircraftDownwashConstantGradientString != null) {
						
						if(aircraftDownwashConstantGradientString.equalsIgnoreCase("TRUE"))
							aircraftDownwashConstantGradient = Boolean.TRUE;
						
						if(aircraftDownwashConstantGradientString.equalsIgnoreCase("FALSE"))
							aircraftDownwashConstantGradient = Boolean.FALSE;
						
					}
					
					String aircraftDownwashMethodString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//aircraft_analyses/downwash/@method");
					
					if(aircraftDownwashMethodString != null) {
						
						if(aircraftDownwashMethodString.equalsIgnoreCase("ROSKAM")) 
							aircraftDownwashMethod = MethodEnum.ROSKAM;
						
						if(aircraftDownwashMethodString.equalsIgnoreCase("SLINGERLAND")) 
							aircraftDownwashMethod = MethodEnum.SLINGERLAND;
						
						aircraftTaskList.put(AerodynamicAndStabilityEnum.DOWNWASH, aircraftDownwashMethod);
						
					}
				}
			}
		}
		
		//---------------------------------------------------------------
		// BUFFET BARRIER
		String aircraftBuffetBarrierPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft_analyses/buffet_barrier/@perform");
		
		if(aircraftBuffetBarrierPerformString.equalsIgnoreCase("TRUE")){
			
			String aircraftBuffetBarrierMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/buffet_barrier/@method");
			
			if(aircraftBuffetBarrierMethodString != null) {
				
				if(aircraftBuffetBarrierMethodString.equalsIgnoreCase("KROO")) 
					aircraftBuffetBarrierMethod = MethodEnum.KROO;
				
				aircraftTaskList.put(AerodynamicAndStabilityEnum.BUFFET_BARRIER, aircraftBuffetBarrierMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// TOTAL LIFT CURVE
		String aircraftTotalLiftCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft_analyses/lift_curve/@perform");
		
		if(aircraftTotalLiftCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String aircraftFuselageEffectsOnWingLiftCurveString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/lift_curve/@fuselage_effects");
			
			if(aircraftFuselageEffectsOnWingLiftCurveString != null) {
				
				if(aircraftFuselageEffectsOnWingLiftCurveString.equalsIgnoreCase("TRUE")) 
					aircraftFuselageEffectOnWingLiftCurve = Boolean.TRUE;
				else if(aircraftFuselageEffectsOnWingLiftCurveString.equalsIgnoreCase("FALSE"))
					aircraftFuselageEffectOnWingLiftCurve = Boolean.FALSE;
				
			}
			
			String aircraftTotalLiftCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/lift_curve/@method");
			
			if(aircraftTotalLiftCurveMethodString != null) {
				
				if(aircraftTotalLiftCurveMethodString.equalsIgnoreCase("FROM_BALANCE_EQUATION")) 
					aircraftTotalLiftCurveMethod = MethodEnum.FROM_BALANCE_EQUATION;
				
				aircraftTaskList.put(AerodynamicAndStabilityEnum.CL_TOTAL, aircraftTotalLiftCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// TOTAL POLAR CURVE
		String aircraftTotalPolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft_analyses/drag_polar/@perform");
		
		if(aircraftTotalPolarCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String aircraftDeltaCD0MiscellaneousString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/drag_polar/@delta_CD0_miscellaneous");
			
			if(aircraftDeltaCD0MiscellaneousString != null) 
				aircraftDeltaCD0Miscellaneous = Double.valueOf(aircraftDeltaCD0MiscellaneousString);
			
			String aircraftDeltaCD0LandingGearsString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/drag_polar/@delta_CD0_landing_gears");
			
			if(aircraftDeltaCD0LandingGearsString != null) 
				aircraftDeltaCD0LandingGears = Double.valueOf(aircraftDeltaCD0MiscellaneousString);
			
			
			String aircraftTotalPolarCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/drag_polar/@method");
			
			if(aircraftTotalPolarCurveMethodString != null) {
				
				if(aircraftTotalPolarCurveMethodString.equalsIgnoreCase("FROM_BALANCE_EQUATION")) 
					aircraftTotalPolarCurveMethod = MethodEnum.FROM_BALANCE_EQUATION;
				
				if(aircraftTotalPolarCurveMethodString.equalsIgnoreCase("ROSKAM")) 
					aircraftTotalPolarCurveMethod = MethodEnum.ROSKAM;
				
				aircraftTaskList.put(AerodynamicAndStabilityEnum.CD_TOTAL, aircraftTotalPolarCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// TOTAL MOMENT CURVE
		String aircraftTotalMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft_analyses/moment_curve/@perform");
		
		if(aircraftTotalMomentCurvePerformString.equalsIgnoreCase("TRUE")){
			
			String aircraftTotalMomentCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/moment_curve/@method");
			
			if(aircraftTotalMomentCurveMethodString != null) {
				
				if(aircraftTotalMomentCurveMethodString.equalsIgnoreCase("FROM_BALANCE_EQUATION")) 
					aircraftTotalMomentCurveMethod = MethodEnum.FROM_BALANCE_EQUATION;
				
				aircraftTaskList.put(AerodynamicAndStabilityEnum.CM_TOTAL, aircraftTotalMomentCurveMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// LONGITUDINAL STATIC STABILITY
		String aircraftLongitudinalStaticStabilityPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft_analyses/longitudinal_static_stability/@perform");
		
		if(aircraftLongitudinalStaticStabilityPerformString.equalsIgnoreCase("TRUE")){
			
			String aircraftWingPendularStabilityString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/longitudinal_static_stability/@wing_pendular_stability_effect");
			
			if(aircraftWingPendularStabilityString != null) {
				
				if(aircraftWingPendularStabilityString.equalsIgnoreCase("TRUE")) 
					aircraftWingPendularStability = Boolean.TRUE;
				else if(aircraftWingPendularStabilityString.equalsIgnoreCase("FALSE"))
					aircraftWingPendularStability = Boolean.FALSE;
				
			}
			
			String aircraftLongitudinalStaticStabilityMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/longitudinal_static_stability/@method");
			
			if(aircraftLongitudinalStaticStabilityMethodString != null) {
				
				if(aircraftLongitudinalStaticStabilityMethodString.equalsIgnoreCase("FROM_BALANCE_EQUATION")) 
					aircraftLongitudinalStaticStabilityMethod = MethodEnum.FROM_BALANCE_EQUATION;
				
				aircraftTaskList.put(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY, aircraftLongitudinalStaticStabilityMethod);
				
			}
		}
		
		//---------------------------------------------------------------
		// DIRECTIONAL STATIC STABILITY
		String aircraftDirectionalStaticStabilityPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft_analyses/directional_static_stability/@perform");
		
		if(aircraftDirectionalStaticStabilityPerformString.equalsIgnoreCase("TRUE")){
			
			String aircraftDirectionalStaticStabilityMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/directional_static_stability/@method");
			
			if(aircraftDirectionalStaticStabilityMethodString != null) {
				
				if(aircraftDirectionalStaticStabilityMethodString.equalsIgnoreCase("VEDSC_SIMPLIFIED_WING")) 
					aircraftDirectionalStaticStabilityMethod = MethodEnum.VEDSC_SIMPLIFIED_WING;
				
				if(aircraftDirectionalStaticStabilityMethodString.equalsIgnoreCase("VEDSC_USAFDATCOM_WING")) 
					aircraftDirectionalStaticStabilityMethod = MethodEnum.VEDSC_USAFDATCOM_WING;
				
				aircraftTaskList.put(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY, aircraftDirectionalStaticStabilityMethod);
				
			}
		}
		
		/*
		 * TODO: COMPLETE WITH LATERAL STABILITY AND DYNAMIC STABILITY WHEN AVAILABLE!
		 */
		
		//===============================================================
		// READING PLOT DATA
		//===============================================================
		List<AerodynamicAndStabilityPlotEnum> plotList = new ArrayList<>();
		
		//...............................................................
		// WING:
		//...............................................................
		// LIFT CURVE
		String wingLiftCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/cL_vs_alpha/@perform");

		if(wingLiftCurvePlotPerformString != null) 
			if(wingLiftCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_LIFT_CURVE_CLEAN);

		//----------------------------------------------------------------
		// STALL PATH
		String wingStallPathPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/stall_path/@perform");

		if(wingStallPathPlotPerformString != null) 
			if(wingStallPathPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_STALL_PATH);

		//----------------------------------------------------------------
		// CL DISTRIBUTION
		String wingCLDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/cl_distribution/@perform");

		if(wingCLDistributionPlotPerformString != null) {
			if(wingCLDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_CL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// CL ADDITIONAL DISTRIBUTION
		String wingCLAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/cl_additional_distribution/@perform");

		if(wingCLAdditionalDistributionPlotPerformString != null) {
			if(wingCLAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_CL_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// CL BASIC DISTRIBUTION
		String wingCLBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/cl_basic_distribution/@perform");

		if(wingCLBasicDistributionPlotPerformString != null) {
			if(wingCLBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_CL_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL DISTRIBUTION
		String wingCCLDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/ccl_distribution/@perform");

		if(wingCCLDistributionPlotPerformString != null) {
			if(wingCCLDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_cCL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL ADDITIONAL DISTRIBUTION
		String wingCCLAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/ccl_additional_distribution/@perform");

		if(wingCCLAdditionalDistributionPlotPerformString != null) {
			if(wingCCLAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_cCL_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL BASIC DISTRIBUTION
		String wingCCLBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/ccl_basic_distribution/@perform");

		if(wingCCLBasicDistributionPlotPerformString != null) {
			if(wingCCLBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_cCL_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA DISTRIBUTION
		String wingGammaDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/gamma_distribution/@perform");

		if(wingGammaDistributionPlotPerformString != null) {
			if(wingGammaDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_GAMMA_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA ADDITIONAL DISTRIBUTION
		String wingGammaAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/gamma_additional_distribution/@perform");

		if(wingGammaAdditionalDistributionPlotPerformString != null) {
			if(wingGammaAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_GAMMA_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA BASIC DISTRIBUTION
		String wingGammaBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/gamma_basic_distribution/@perform");

		if(wingGammaBasicDistributionPlotPerformString != null) {
			if(wingGammaBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_GAMMA_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// TOTAL LOAD DISTRIBUTION
		String wingTotalLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/total_load_distribution/@perform");

		if(wingTotalLoadDistributionPlotPerformString != null) {
			if(wingTotalLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_TOTAL_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// ADDITIONAL LOAD DISTRIBUTION
		String wingAdditionalLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/additional_load_distribution/@perform");

		if(wingAdditionalLoadDistributionPlotPerformString != null) {
			if(wingAdditionalLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_ADDITIONAL_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// BASIC LOAD DISTRIBUTION
		String wingBasicLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/lift/basic_load_distribution/@perform");

		if(wingBasicLoadDistributionPlotPerformString != null) {
			if(wingBasicLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_BASIC_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// HIGH LIFT CURVE
		String wingHighLiftCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/high_lift/cL_vs_alpha_high_lift/@perform");

		if(wingHighLiftCurvePlotPerformString != null) {
			if(wingHighLiftCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_LIFT_CURVE_HIGH_LIFT);
		}
		
		//----------------------------------------------------------------
		// HIGH LIFT POLAR CURVE
		String wingHighLiftPolarCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/high_lift/cD_vs_cL_high_lift/@perform");

		if(wingHighLiftPolarCurvePlotPerformString != null) {
			if(wingHighLiftPolarCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_POLAR_CURVE_HIGH_LIFT);
		}
		
		//----------------------------------------------------------------
		// HIGH LIFT MOMENT CURVE
		String wingHighLiftMomentCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/high_lift/cM_vs_alpha_high_lift/@perform");

		if(wingHighLiftMomentCurvePlotPerformString != null) {
			if(wingHighLiftMomentCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_MOMENT_CURVE_HIGH_LIFT);
		}
		
		//----------------------------------------------------------------
		// POLAR CURVE BREAKDOWN
		String wingPolarCurveBreakdownPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/drag/cD_vs_cL_breakdown/@perform");

		if(wingPolarCurveBreakdownPlotPerformString != null) {
			if(wingPolarCurveBreakdownPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_POLAR_CURVE_CLEAN_BREAKDOWN);
		}
		
		//----------------------------------------------------------------
		// DRAG DISTRIBUTION
		String wingDragDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/drag/drag_distributions/@perform");

		if(wingDragDistributionPlotPerformString != null) {
			if(wingDragDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_DRAG_DISTRIBUTION);
		}
		
		//----------------------------------------------------------------
		// MOMENT CURVE
		String wingMomentCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/pitching_moment/cM_vs_alpha/@perform");

		if(wingMomentCurvePlotPerformString != null) {
			if(wingMomentCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_MOMENT_CURVE_CLEAN);
		}
		
		//----------------------------------------------------------------
		// MOMENT DISTRIBUTION
		String wingMomentDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/pitching_moment/moment_distributions/@perform");

		if(wingMomentDistributionPlotPerformString != null) {
			if(wingMomentDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.WING_MOMENT_DISTRIBUTION);
		}
		
		
		//...............................................................
		// HTAIL:
		//...............................................................
		// LIFT CURVE
		String hTailLiftCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/cL_vs_alpha/@perform");

		if(hTailLiftCurvePlotPerformString != null) 
			if(hTailLiftCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_LIFT_CURVE_CLEAN);

		//----------------------------------------------------------------
		// STALL PATH
		String hTailStallPathPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/stall_path/@perform");

		if(hTailStallPathPlotPerformString != null) 
			if(hTailStallPathPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_STALL_PATH);

		//----------------------------------------------------------------
		// CL DISTRIBUTION
		String hTailCLDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/cl_distribution/@perform");

		if(hTailCLDistributionPlotPerformString != null) {
			if(hTailCLDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_CL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// CL ADDITIONAL DISTRIBUTION
		String hTailCLAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/cl_additional_distribution/@perform");

		if(hTailCLAdditionalDistributionPlotPerformString != null) {
			if(hTailCLAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_CL_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// CL BASIC DISTRIBUTION
		String hTailCLBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/cl_basic_distribution/@perform");

		if(hTailCLBasicDistributionPlotPerformString != null) {
			if(hTailCLBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_CL_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL DISTRIBUTION
		String hTailCCLDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/ccl_distribution/@perform");

		if(hTailCCLDistributionPlotPerformString != null) {
			if(hTailCCLDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_cCL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL ADDITIONAL DISTRIBUTION
		String hTailCCLAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/ccl_additional_distribution/@perform");

		if(hTailCCLAdditionalDistributionPlotPerformString != null) {
			if(hTailCCLAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_cCL_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL BASIC DISTRIBUTION
		String hTailCCLBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/ccl_basic_distribution/@perform");

		if(hTailCCLBasicDistributionPlotPerformString != null) {
			if(hTailCCLBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_cCL_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA DISTRIBUTION
		String hTailGammaDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/gamma_distribution/@perform");

		if(hTailGammaDistributionPlotPerformString != null) {
			if(hTailGammaDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_GAMMA_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA ADDITIONAL DISTRIBUTION
		String hTailGammaAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/gamma_additional_distribution/@perform");

		if(hTailGammaAdditionalDistributionPlotPerformString != null) {
			if(hTailGammaAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_GAMMA_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA BASIC DISTRIBUTION
		String hTailGammaBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/gamma_basic_distribution/@perform");

		if(hTailGammaBasicDistributionPlotPerformString != null) {
			if(hTailGammaBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_GAMMA_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// TOTAL LOAD DISTRIBUTION
		String hTailTotalLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/total_load_distribution/@perform");

		if(hTailTotalLoadDistributionPlotPerformString != null) {
			if(hTailTotalLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_TOTAL_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// ADDITIONAL LOAD DISTRIBUTION
		String hTailAdditionalLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/additional_load_distribution/@perform");

		if(hTailAdditionalLoadDistributionPlotPerformString != null) {
			if(hTailAdditionalLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_ADDITIONAL_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// BASIC LOAD DISTRIBUTION
		String hTailBasicLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/lift/basic_load_distribution/@perform");

		if(hTailBasicLoadDistributionPlotPerformString != null) {
			if(hTailBasicLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_BASIC_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// POLAR CURVE BREAKDOWN
		String hTailPolarCurveBreakdownPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/drag/cD_vs_cL_breakdown/@perform");

		if(hTailPolarCurveBreakdownPlotPerformString != null) {
			if(hTailPolarCurveBreakdownPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_POLAR_CURVE_CLEAN_BREAKDOWN);
		}
		
		//----------------------------------------------------------------
		// DRAG DISTRIBUTION
		String hTailDragDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/drag/drag_distributions/@perform");

		if(hTailDragDistributionPlotPerformString != null) {
			if(hTailDragDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_DRAG_DISTRIBUTION);
		}
		
		//----------------------------------------------------------------
		// MOMENT CURVE
		String hTailMomentCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/pitching_moment/cM_vs_alpha/@perform");

		if(hTailMomentCurvePlotPerformString != null) {
			if(hTailMomentCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_MOMENT_CURVE_CLEAN);
		}
		
		//----------------------------------------------------------------
		// MOMENT DISTRIBUTION
		String hTailMomentDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/pitching_moment/moment_distributions/@perform");

		if(hTailMomentDistributionPlotPerformString != null) {
			if(hTailMomentDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_MOMENT_DISTRIBUTION);
		}
		
		
		//...............................................................
		// VTAIL:
		//...............................................................
		// LIFT CURVE
		String vTailLiftCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/cL_vs_alpha/@perform");

		if(vTailLiftCurvePlotPerformString != null) 
			if(vTailLiftCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_LIFT_CURVE_CLEAN);

		//----------------------------------------------------------------
		// STALL PATH
		String vTailStallPathPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/stall_path/@perform");

		if(vTailStallPathPlotPerformString != null) 
			if(vTailStallPathPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_STALL_PATH);

		//----------------------------------------------------------------
		// CL DISTRIBUTION
		String vTailCLDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/cl_distribution/@perform");

		if(vTailCLDistributionPlotPerformString != null) {
			if(vTailCLDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_CL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// CL ADDITIONAL DISTRIBUTION
		String vTailCLAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/cl_additional_distribution/@perform");

		if(vTailCLAdditionalDistributionPlotPerformString != null) {
			if(vTailCLAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_CL_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// CL BASIC DISTRIBUTION
		String vTailCLBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/cl_basic_distribution/@perform");

		if(vTailCLBasicDistributionPlotPerformString != null) {
			if(vTailCLBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_CL_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL DISTRIBUTION
		String vTailCCLDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/ccl_distribution/@perform");

		if(vTailCCLDistributionPlotPerformString != null) {
			if(vTailCCLDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_cCL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL ADDITIONAL DISTRIBUTION
		String vTailCCLAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/ccl_additional_distribution/@perform");

		if(vTailCCLAdditionalDistributionPlotPerformString != null) {
			if(vTailCCLAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_cCL_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// cCL BASIC DISTRIBUTION
		String vTailCCLBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/ccl_basic_distribution/@perform");

		if(vTailCCLBasicDistributionPlotPerformString != null) {
			if(vTailCCLBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_cCL_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA DISTRIBUTION
		String vTailGammaDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/gamma_distribution/@perform");

		if(vTailGammaDistributionPlotPerformString != null) {
			if(vTailGammaDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_GAMMA_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA ADDITIONAL DISTRIBUTION
		String vTailGammaAdditionalDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/gamma_additional_distribution/@perform");

		if(vTailGammaAdditionalDistributionPlotPerformString != null) {
			if(vTailGammaAdditionalDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_GAMMA_ADDITIONAL_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// GAMMA BASIC DISTRIBUTION
		String vTailGammaBasicDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/gamma_basic_distribution/@perform");

		if(vTailGammaBasicDistributionPlotPerformString != null) {
			if(vTailGammaBasicDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_GAMMA_BASIC_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// TOTAL LOAD DISTRIBUTION
		String vTailTotalLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/total_load_distribution/@perform");

		if(vTailTotalLoadDistributionPlotPerformString != null) {
			if(vTailTotalLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_TOTAL_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// ADDITIONAL LOAD DISTRIBUTION
		String vTailAdditionalLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/additional_load_distribution/@perform");

		if(vTailAdditionalLoadDistributionPlotPerformString != null) {
			if(vTailAdditionalLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_ADDITIONAL_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// BASIC LOAD DISTRIBUTION
		String vTailBasicLoadDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/lift/basic_load_distribution/@perform");

		if(vTailBasicLoadDistributionPlotPerformString != null) {
			if(vTailBasicLoadDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_BASIC_LOAD_DISTRIBUTION);
		}

		//----------------------------------------------------------------
		// POLAR CURVE BREAKDOWN
		String vTailPolarCurveBreakdownPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/drag/cD_vs_cL_breakdown/@perform");

		if(vTailPolarCurveBreakdownPlotPerformString != null) {
			if(vTailPolarCurveBreakdownPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_POLAR_CURVE_CLEAN_BREAKDOWN);
		}
		
		//----------------------------------------------------------------
		// DRAG DISTRIBUTION
		String vTailDragDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/drag/drag_distributions/@perform");

		if(vTailDragDistributionPlotPerformString != null) {
			if(vTailDragDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_DRAG_DISTRIBUTION);
		}
		
		//----------------------------------------------------------------
		// MOMENT CURVE
		String vTailMomentCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/pitching_moment/cM_vs_alpha/@perform");

		if(vTailMomentCurvePlotPerformString != null) {
			if(vTailMomentCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_MOMENT_CURVE_CLEAN);
		}
		
		//----------------------------------------------------------------
		// MOMENT DISTRIBUTION
		String vTailMomentDistributionPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/pitching_moment/moment_distributions/@perform");

		if(vTailMomentDistributionPlotPerformString != null) {
			if(vTailMomentDistributionPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_MOMENT_DISTRIBUTION);
		}
		
		//...............................................................
		// FUSELAGE:
		//...............................................................
		// POLAR CURVE
		String fuselagePolarCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/fuselage/drag/cD_vs_cL/@perform");

		if(fuselagePolarCurvePlotPerformString != null) 
			if(fuselagePolarCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.FUSELAGE_POLAR_CURVE);

		//----------------------------------------------------------------
		// MOMENT CURVE
		String fuselageMomentCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/fuselage/pitching_moment/cM_vs_alpha/@perform");

		if(fuselageMomentCurvePlotPerformString != null) 
			if(fuselageMomentCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.FUSELAGE_MOMENT_CURVE);


		//...............................................................
		// NACELLE:
		//...............................................................
		// POLAR CURVE
		String nacellePolarCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/nacelles/drag/cD_vs_cL/@perform");

		if(nacellePolarCurvePlotPerformString != null) 
			if(nacellePolarCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.NACELLE_POLAR_CURVE);

		//----------------------------------------------------------------
		// MOMENT CURVE
		String nacelleMomentCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/nacelles/pitching_moment/cM_vs_alpha/@perform");

		if(nacelleMomentCurvePlotPerformString != null) 
			if(nacelleMomentCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.NACELLE_MOMENT_CURVE);
		
		
		//...............................................................
		// AIRCRAFT:
		//...............................................................
		// DOWNWASH GRADIENT
		String downwashGradientPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/downwash_gradient/@perform");

		if(downwashGradientPlotPerformString != null) 
			if(downwashGradientPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.DOWNWASH_GRADIENT);
		
		//----------------------------------------------------------------
		// DOWNWASH ANGLE
		String downwashAnglePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/downwash_angle/@perform");

		if(downwashAnglePlotPerformString != null) 
			if(downwashAnglePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.DOWNWASH_ANGLE);
		
		//----------------------------------------------------------------
		// TOTAL LIFT CURVE
		String totalLiftCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cL_total_vs_alpha/@perform");

		if(totalLiftCurvePlotPerformString != null) 
			if(totalLiftCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_LIFT_CURVE);
		
		//----------------------------------------------------------------
		// TOTAL POLAR CURVE
		String totalPolarCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cD_total_vs_cL_total/@perform");

		if(totalPolarCurvePlotPerformString != null) 
			if(totalPolarCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_POLAR_CURVE);
		
		//----------------------------------------------------------------
		// TOTAL MOMENT CURVE VS ALPHA
		String totalMomentCurveAlphaPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cM_total_vs_alpha/@perform");

		if(totalMomentCurveAlphaPlotPerformString != null) 
			if(totalMomentCurveAlphaPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_MOMENT_CURVE_VS_ALPHA);
		
		//----------------------------------------------------------------
		// TOTAL MOMENT CURVE VS CL TOTAL
		String totalMomentCurveCLPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cM_total_vs_cL_total/@perform");

		if(totalMomentCurveCLPlotPerformString != null) 
			if(totalMomentCurveCLPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_MOMENT_CURVE_VS_CL);
		
		//----------------------------------------------------------------
		// TRIMMED LIFT CURVE
		String trimmedLiftCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cL_total_equilibrium_vs_alpha/@perform");

		if(trimmedLiftCurvePlotPerformString != null) 
			if(trimmedLiftCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_LIFT_CURVE);
		
		//----------------------------------------------------------------
		// TRIMMED LIFT CURVE HTAIL
		String trimmedLiftCurveHTailPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cL_horizontal_tail_equilibrium_vs_alpha/@perform");

		if(trimmedLiftCurveHTailPlotPerformString != null) 
			if(trimmedLiftCurveHTailPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_LIFT_CURVE_HTAIL);
		
		//----------------------------------------------------------------
		// TRIMMED POLAR CURVE HTAIL
		String trimmedPolarCurvePlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cD_total_equilibrium_vs_cL_total_equilibrium/@perform");

		if(trimmedPolarCurvePlotPerformString != null) 
			if(trimmedPolarCurvePlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_POLAR_CURVE);
		
		//----------------------------------------------------------------
		// DELTA ELEVATOR EQUILIBRIUM
		String deltaElevatorEquilibriumPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/delta_elevator_equilibrium_vs_alpha/@perform");

		if(deltaElevatorEquilibriumPlotPerformString != null) 
			if(deltaElevatorEquilibriumPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.DELTA_ELEVATOR_EQUILIBRIUM);
		
		//----------------------------------------------------------------
		// TOTAL CM BREAKDOWN
		String totalCMBreakdownPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cM_vs_alpha_breakdown/@perform");

		if(totalCMBreakdownPlotPerformString != null) 
			if(totalCMBreakdownPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_CM_BREAKDOWN);
		
		//----------------------------------------------------------------
		// TOTAL CN BREAKDOWN
		String totalCNBreakdownPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cN_vs_beta_breakdown/@perform");

		if(totalCNBreakdownPlotPerformString != null) 
			if(totalCNBreakdownPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_CN_BREAKDOWN);
		
		//----------------------------------------------------------------
		// TOTAL CN VS BETA VS DELTA RUDDER
		String totalCNBetaDeltaRudderPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/cN_vs_beta_delta_rudder/@perform");

		if(totalCNBetaDeltaRudderPlotPerformString != null) 
			if(totalCNBetaDeltaRudderPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_CN_VS_BETA_VS_DELTA_RUDDER);
		
		//----------------------------------------------------------------
		// DELTA RUDDER EQUILIBRIUM
		String deltaRudderEquilibriumPlotPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/delta_rudder_equilibrium_vs_beta/@perform");

		if(deltaRudderEquilibriumPlotPerformString != null) 
			if(deltaRudderEquilibriumPlotPerformString.equalsIgnoreCase("TRUE")) 
				plotList.add(AerodynamicAndStabilityPlotEnum.DELTA_RUDDER_EQUILIBRIUM);
		
		
		// TODO: COMPLETE ME WITH ALL THE PLOT OF THE ANALYSES UNDER DEVELOPMENT!!
		
		/********************************************************************************************
		 * Once the data are ready, it's possible to create the manager object. This can be created
		 * using the builder pattern.
		 */
		IACAerodynamicAndStabilityManager theAerodynamicAndStabilityBuilderInterface = new IACAerodynamicAndStabilityManager.Builder()
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
				.setWingHighLiftCurveFunction(wingHighLiftCurveFunction)
				.setWingHighLiftPolarCurveFunction(wingHighLiftPolarCurveFunction)
				.setWingHighLiftMomentCurveFunction(wingHighLiftMomentCurveFunction)
				.setWingPolarCurveFunction(wingPolarCurveFunction)
				.setWingMomentCurveFunction(wingMomentCurveFunction)
				.putComponentTaskList(ComponentEnum.HORIZONTAL_TAIL, hTailTaskList)
				.setHTailLiftCurveFunction(hTailLiftCurveFunction)
				.setHTailPolarCurveFunction(hTailPolarCurveFunction)
				.setHTailMomentCurveFunction(hTailMomentCurveFunction)
				.setElevatorDeflectionForAnalysis(elevatorDeflectionForAnalysis)
				.putComponentTaskList(ComponentEnum.VERTICAL_TAIL, vTailTaskList)
				.setVTailLiftCurveFunction(vTailLiftCurveFunction)
				.setVTailPolarCurveFunction(vTailPolarCurveFunction)
				.setVTailMomentCurveFunction(vTailMomentCurveFunction)
				.setRudderDeflectionForAnalysis(rudderDeflectionForAnalysis)
				.putComponentTaskList(ComponentEnum.FUSELAGE, fuselageTaskList)
				.setFuselagePolarCurveFunction(fuselagePolarCurveFunction)
				.setFuselageMomentCurveFunction(fuselageMomentCurveFunction)
				.putComponentTaskList(ComponentEnum.NACELLE, nacelleTaskList)
				.setNacellePolarCurveFunction(nacellePolarCurveFunction)
				.setNacelleMomentCurveFunction(nacelleMomentCurveFunction)
				.putComponentTaskList(ComponentEnum.AIRCRAFT, aircraftTaskList)
				.setDownwashConstant(aircraftDownwashConstantGradient)
				.setAircraftDownwashGradientFunction(aircraftDownwashGradientFunction)
				.setFuselageEffectOnWingLiftCurve(aircraftFuselageEffectOnWingLiftCurve)
				.setLandingGearDragCoefficient(aircraftDeltaCD0LandingGears)
				.setDeltaCD0Miscellaneous(aircraftDeltaCD0Miscellaneous)
				.setWingPendularStability(aircraftWingPendularStability)
				.addAllPlotList(plotList)
				.build();

		ACAerodynamicAndStabilityManager theAerodynamicAndStabilityManager = new ACAerodynamicAndStabilityManager();
		theAerodynamicAndStabilityManager.setTheAerodynamicBuilderInterface(theAerodynamicAndStabilityBuilderInterface);

		return theAerodynamicAndStabilityManager;

	}

	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException {

		Workbook wb;
		File outputFile = new File(filenameWithPathAndExt + ".xlsx");
		if (outputFile.exists()) { 
			outputFile.delete();		
			System.out.println("Deleting the old .xls file ...");
		} 

		if (outputFile.getName().endsWith(".xls")) {
			wb = new HSSFWorkbook();
		}
		else if (outputFile.getName().endsWith(".xlsx")) {
			wb = new XSSFWorkbook();
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}

		CellStyle styleHead = wb.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		styleHead.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font font = wb.createFont();
		font.setFontHeightInPoints((short) 20);
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		styleHead.setFont(font);

		CellStyle styleHeader = wb.createCellStyle();
		Font fontBold = wb.createFont();
		fontBold.setFontHeightInPoints((short) 15);
		fontBold.setColor(IndexedColors.BLACK.getIndex());
		fontBold.setBoldweight(Font.BOLDWEIGHT_BOLD);
		styleHeader.setFont(fontBold);

		//--------------------------------------------------------------------------------
		// WING ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_writeWing) {

			Sheet wingSheet = wb.createSheet("WING");
			List<Object[]> dataListWing = new ArrayList<>();

			List<Integer> boldRowIndex = new ArrayList<>();
			int currentBoldIndex = 1;

			dataListWing.add(new Object[] {"Description","Unit","Value"});

			dataListWing.add(new Object[] {"GLOBAL DATA"});
			currentBoldIndex = 2;
			boldRowIndex.add(currentBoldIndex);


			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
				dataListWing.add(new Object[] {
						"Critical Mach Number",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCriticalMachNumber().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {
				dataListWing.add(new Object[] {
						"Aerodynamic Center (LRF)",
						"m",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacLRF().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
								).doubleValue(SI.METER)
				});
				dataListWing.add(new Object[] {
						"Aerodynamic Center (MAC)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacMRF().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
								)
				});
				currentBoldIndex = currentBoldIndex+2;
			}

			dataListWing.add(new Object[] {""});
			dataListWing.add(new Object[] {""});
			dataListWing.add(new Object[] {"LIFT"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {
				dataListWing.add(new Object[] {
						"CL_alpha",
						"1/deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ALPHA)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {
				dataListWing.add(new Object[] {
						"CL_zero",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ZERO)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_STAR)) {
				dataListWing.add(new Object[] {
						"CL_star",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStar().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_STAR)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_MAX)) {
				dataListWing.add(new Object[] {
						"CL_max",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLMax().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_MAX)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
				dataListWing.add(new Object[] {
						"Alpha_zero_lift",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_STAR)) {
				dataListWing.add(new Object[] {
						"Alpha_star",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStar().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.ALPHA_STALL)) {
				dataListWing.add(new Object[] {
						"Alpha_stall",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStall().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
				dataListWing.add(new Object[] {
						"CL at Alpha = " + _alphaWingCurrent,
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingLiftCurve3D
				 */
				dataListWing.add(new Object[] {""});
				dataListWing.add(new Object[] {"LIFT CURVE"});

				Object[] liftCurveAlpha = new Object[_alphaWingList.size()+2];
				liftCurveAlpha[0] = "Alpha";
				liftCurveAlpha[1] = "deg";
				for(int i=0; i<_alphaWingList.size(); i++) 
					liftCurveAlpha[i+2] = _alphaWingList.get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] liftCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						).length+2];
				liftCurveCL[0] = "CL";
				liftCurveCL[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
								).length; 
						i++) 
					liftCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
							)[i];

				dataListWing.add(liftCurveAlpha);
				dataListWing.add(liftCurveCL);
				dataListWing.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

				dataListWing.add(new Object[] {""});
				dataListWing.add(new Object[] {"LIFT DISTRIBUTIONS"});

				Object[] yStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().size()+2];
				yStationsLift[0] = "y stations";
				yStationsLift[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().size(); i++) 
					yStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListWing.add(yStationsLift);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaWingForDistribution().size(); i++) {

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListWing.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cLDistribution[0] = "Cl";
					cLDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cLDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(cLDistribution);
					//..................................................................................................................................................
					Object[] cLAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cLAdditionalDistribution[0] = "Cl_additional";
					cLAdditionalDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cLAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(cLAdditionalDistribution);
					//..................................................................................................................................................
					Object[] cLBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cLBasicDistribution[0] = "Cl_basic";
					cLBasicDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cLBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(cLBasicDistribution);
					//..................................................................................................................................................
					Object[] cCLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cCLDistribution[0] = "cCl";
					cCLDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cCLDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListWing.add(cCLDistribution);
					//..................................................................................................................................................
					Object[] cCLAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cCLAdditionalDistribution[0] = "cCl_additional";
					cCLAdditionalDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cCLAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListWing.add(cCLAdditionalDistribution);
					//..................................................................................................................................................
					Object[] cCLBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cCLBasicDistribution[0] = "cCl_basic";
					cCLBasicDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cCLBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListWing.add(cCLBasicDistribution);
					//..................................................................................................................................................
					Object[] gammaDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					gammaDistribution[0] = "Gamma";
					gammaDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						gammaDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(gammaDistribution);
					//..................................................................................................................................................
					Object[] gammaAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					gammaAdditionalDistribution[0] = "Gamma_additional";
					gammaAdditionalDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						gammaAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(gammaAdditionalDistribution);
					//..................................................................................................................................................
					Object[] gammaBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					gammaBasicDistribution[0] = "Gamma_basic";
					gammaBasicDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						gammaBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(gammaBasicDistribution);
					//..................................................................................................................................................
					Object[] wingLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					wingLoadDistribution[0] = "Wing load";
					wingLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						wingLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListWing.add(wingLoadDistribution);
					//..................................................................................................................................................
					Object[] wingAdditionalLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAdditionalLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					wingAdditionalLoadDistribution[0] = "Additional load";
					wingAdditionalLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAdditionalLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						wingAdditionalLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAdditionalLoadDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListWing.add(wingAdditionalLoadDistribution);
					//..................................................................................................................................................	
					Object[] wingBasicLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getBasicLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					wingBasicLoadDistribution[0] = "Basic load";
					wingBasicLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getBasicLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						wingBasicLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getBasicLoadDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListWing.add(wingBasicLoadDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+15;

				}
			}

			dataListWing.add(new Object[] {""});
			dataListWing.add(new Object[] {""});
			dataListWing.add(new Object[] {"DRAG"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD0)) {
				dataListWing.add(new Object[] {
						"CD_zero",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCD0().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD0)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {
				dataListWing.add(new Object[] {
						"Oswald factor",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getOswaldFactor().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.OSWALD_FACTOR)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {
				dataListWing.add(new Object[] {
						"CD_induced",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDInduced().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_WAVE)) {
				dataListWing.add(new Object[] {
						"CD_wave",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDWave().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_WAVE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
				dataListWing.add(new Object[] {
						"CD at alpha = " + _alphaWingCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingPolarCurve3D
				 */
				dataListWing.add(new Object[] {""});
				dataListWing.add(new Object[] {"DRAG POLAR CURVE"});

				Object[] polarCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						).length+2];
				polarCurveCL[0] = "CL";
				polarCurveCL[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
								).length; 
						i++) 
					polarCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
							)[i];

				Object[] polarCurveCD = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
						).length+2];
				polarCurveCD[0] = "CD";
				polarCurveCD[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								).length; 
						i++) 
					polarCurveCD[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
							)[i];

				dataListWing.add(polarCurveCL);
				dataListWing.add(polarCurveCD);
				dataListWing.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

				dataListWing.add(new Object[] {""});
				dataListWing.add(new Object[] {"DRAG DISTRIBUTIONS"});

				Object[] yStationsDrag = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().size()+2];
				yStationsDrag[0] = "y stations";
				yStationsDrag[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().size(); i++) 
					yStationsDrag[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListWing.add(yStationsDrag);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaWingForDistribution().size(); i++) {

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListWing.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cDParasiteDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getParasiteDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cDParasiteDistribution[0] = "Cd_parasite";
					cDParasiteDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getParasiteDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cDParasiteDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getParasiteDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(cDParasiteDistribution);
					//..................................................................................................................................................
					Object[] cDInducedDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getInducedDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cDInducedDistribution[0] = "Cd_induced";
					cDInducedDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getInducedDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cDInducedDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getInducedDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(cDInducedDistribution);
					//..................................................................................................................................................
					Object[] cDDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cDDistribution[0] = "Cd";
					cDDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cDDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(cDDistribution);
					//..................................................................................................................................................
					Object[] dragDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					dragDistribution[0] = "Drag";
					dragDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						dragDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListWing.add(dragDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+7;

				}
			}

			dataListWing.add(new Object[] {""});
			dataListWing.add(new Object[] {""});
			dataListWing.add(new Object[] {"PITCHING MOMENT"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {
				dataListWing.add(new Object[] {
						"CM_ac",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMac().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {
				dataListWing.add(new Object[] {
						"CM_alpha",
						"1/deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
				dataListWing.add(new Object[] {
						"CM at alpha = " + _alphaWingCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingMomentCurve3D
				 */
				dataListWing.add(new Object[] {""});
				dataListWing.add(new Object[] {"PITCHING MOMENT CURVE"});

				Object[] momentCurveAlpha = new Object[_alphaWingList.size()+2];
				momentCurveAlpha[0] = "Alpha";
				momentCurveAlpha[1] = "deg";
				for(int i=0; i<_alphaWingList.size(); i++) 
					momentCurveAlpha[i+2] = _alphaWingList.get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] momentCurveCM = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
						).length+2];
				momentCurveCM[0] = "CM";
				momentCurveCM[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
								).length; 
						i++) 
					momentCurveCM[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
							)[i];

				dataListWing.add(momentCurveAlpha);
				dataListWing.add(momentCurveCM);
				dataListWing.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

				dataListWing.add(new Object[] {""});
				dataListWing.add(new Object[] {"PITCHING MOMENT DISTRIBUTIONS"});

				Object[] yStationsMoment = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().size()+2];
				yStationsMoment[0] = "y stations";
				yStationsMoment[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().size(); i++) 
					yStationsMoment[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListWing.add(yStationsMoment);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaWingForDistribution().size(); i++) {

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListWing.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cMDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					cMDistribution[0] = "Cm";
					cMDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						cMDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j);
					dataListWing.add(cMDistribution);
					//..................................................................................................................................................
					Object[] momentDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
					momentDistribution[0] = "Moment";
					momentDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
							j++) 
						momentDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListWing.add(momentDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+5;

				}
			}

			if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF) 
					|| _theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING)) {

				dataListWing.add(new Object[] {""});
				dataListWing.add(new Object[] {""});
				dataListWing.add(new Object[] {"HIGH LIFT"});
				currentBoldIndex = currentBoldIndex+3;
				boldRowIndex.add(currentBoldIndex);

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"HIGH LIFT DEVICES EFFECTS (2D)"});

					//.........................................................................................................................................
					Object[] deltaCl0List = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCl0FlapList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).size()+2];
					deltaCl0List[0] = "Delta Cl0 (each flap)";
					deltaCl0List[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCl0FlapList().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).size(); 
							i++) 
						deltaCl0List[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCl0FlapList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).get(i);

					dataListWing.add(deltaCl0List);
					//.........................................................................................................................................
					dataListWing.add(new Object[] {
							"Delta Cl0 (total)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCl0Flap().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									)});
					//.........................................................................................................................................
					Object[] deltaClmaxFlapList = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxFlapList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).size()+2];
					deltaClmaxFlapList[0] = "Delta Clmax (each flap)";
					deltaClmaxFlapList[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxFlapList().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).size(); 
							i++) 
						deltaClmaxFlapList[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxFlapList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).get(i);
					dataListWing.add(deltaClmaxFlapList);
					//.........................................................................................................................................
					dataListWing.add(new Object[] {
							"Delta Clmax (all flaps)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxFlap().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									)});
					//.........................................................................................................................................
					if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxSlatList().get(MethodEnum.SEMIEMPIRICAL) != null) {

						Object[] deltaClmaxSlatList = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxSlatList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).size()+2];
						deltaClmaxSlatList[0] = "Delta Clmax (each slat)";
						deltaClmaxSlatList[1] = "";
						for(int i=0; 
								i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxSlatList().get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
										).size(); 
								i++) 
							deltaClmaxSlatList[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxSlatList().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).get(i);

						dataListWing.add(deltaClmaxSlatList);
						//.........................................................................................................................................
						dataListWing.add(new Object[] {
								"Delta Clmax (all slats)",
								"",
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxSlat().get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
										)});
						//.........................................................................................................................................
					}

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"HIGH LIFT DEVICES EFFECTS (3D)"});

					//.........................................................................................................................................
					Object[] deltaCL0List = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCL0FlapList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).size()+2];
					deltaCL0List[0] = "Delta CL0 (each flap)";
					deltaCL0List[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCL0FlapList().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).size(); 
							i++) 
						deltaCL0List[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCL0FlapList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).get(i);

					dataListWing.add(deltaCL0List);
					//.........................................................................................................................................
					dataListWing.add(new Object[] {
							"Delta CL0 (total)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCL0Flap().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									)});
					//.........................................................................................................................................
					Object[] deltaCLmaxFlapList = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxFlapList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).size()+2];
					deltaCLmaxFlapList[0] = "Delta CLmax (each flap)";
					deltaCLmaxFlapList[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxFlapList().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).size(); 
							i++) 
						deltaCLmaxFlapList[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxFlapList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).get(i);

					dataListWing.add(deltaCLmaxFlapList);
					//.........................................................................................................................................
					dataListWing.add(new Object[] {
							"Delta CLmax (all flaps)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxFlap().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									)});
					//.........................................................................................................................................
					if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlatList().get(MethodEnum.SEMIEMPIRICAL) != null) {

						Object[] deltaCLmaxSlatList = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlatList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).size()+2];
						deltaCLmaxSlatList[0] = "Delta CLmax (each slat)";
						deltaCLmaxSlatList[1] = "";
						for(int i=0; 
								i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlatList().get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
										).size(); 
								i++) 
							deltaCLmaxSlatList[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlatList().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).get(i);

						dataListWing.add(deltaCLmaxSlatList);
						//.........................................................................................................................................
						dataListWing.add(new Object[] {
								"Delta CLmax (all slats)",
								"",
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlat().get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
										)});
						//.........................................................................................................................................
					}

					//.........................................................................................................................................
					Object[] deltaCD0List = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD0List().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).size()+2];
					deltaCD0List[0] = "Delta CD0 (each flap)";
					deltaCD0List[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD0List().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).size(); 
							i++) 
						deltaCD0List[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD0List().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).get(i);

					dataListWing.add(deltaCD0List);
					//.........................................................................................................................................
					dataListWing.add(new Object[] {
							"Delta CD0 (total)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD0().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									)});
					//.........................................................................................................................................
					Object[] deltaCMc4List = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCMc4List().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).size()+2];
					deltaCMc4List[0] = "Delta CM_c/4 (each flap)";
					deltaCMc4List[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCMc4List().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).size(); 
							i++) 
						deltaCMc4List[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCMc4List().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).get(i);

					dataListWing.add(deltaCMc4List);
					//.........................................................................................................................................
					dataListWing.add(new Object[] {
							"Delta CM_c/4 (total)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCMc4().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									)});
					//.........................................................................................................................................

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"GLOBAL HIGH LIFT EFFECTS"});
					dataListWing.add(new Object[] {
							"Alpha stall (High Lift)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStallHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).doubleValue(NonSI.DEGREE_ANGLE)});
					dataListWing.add(new Object[] {
							"Alpha star (High Lift)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStarHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).doubleValue(NonSI.DEGREE_ANGLE)});
					dataListWing.add(new Object[] {
							"CL_max (High Lift)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLMaxHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									)});
					dataListWing.add(new Object[] {
							"CL_star (High Lift)",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStarHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									)});
					dataListWing.add(new Object[] {
							"CL_alpha (High Lift)",
							"1/deg",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlphaHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});

					if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlatList().get(MethodEnum.SEMIEMPIRICAL) != null) 
						currentBoldIndex = currentBoldIndex+27;
					else
						currentBoldIndex = currentBoldIndex+23;

				}

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"HIGH LIFT CURVE"});

					Object[] highLiftCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayPlotHighLift().length+2];
					highLiftCurveAlpha[0] = "Alpha";
					highLiftCurveAlpha[1] = "deg";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayPlotHighLift().length; i++) 
						highLiftCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayPlotHighLift()[i];

					Object[] highLiftCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
							).length+2];
					highLiftCurveCL[0] = "CL";
					highLiftCurveCL[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
									).length; 
							i++) 
						highLiftCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
								)[i];

					dataListWing.add(highLiftCurveAlpha);
					dataListWing.add(highLiftCurveCL);
					dataListWing.add(new Object[] {""});

					currentBoldIndex = currentBoldIndex+5;
				}

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"HIGH LIFT DRAG POLAR CURVE"});

					Object[] highLiftPolarCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
							).length+2];
					highLiftPolarCurveCL[0] = "CL";
					highLiftPolarCurveCL[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
							).length; i++) 
						highLiftPolarCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
								)[i];

					Object[] highLiftPolarCurveCD = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
							).length+2];
					highLiftPolarCurveCD[0] = "CD";
					highLiftPolarCurveCD[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurveHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
									).length; 
							i++) 
						highLiftPolarCurveCD[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getPolar3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
								)[i];

					dataListWing.add(highLiftPolarCurveCL);
					dataListWing.add(highLiftPolarCurveCD);
					dataListWing.add(new Object[] {""});

					currentBoldIndex = currentBoldIndex+5;
				}

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

					dataListWing.add(new Object[] {""});
					dataListWing.add(new Object[] {"HIGH LIFT PITCHING MOMENT CURVE"});

					Object[] highLiftMomentCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayPlotHighLift().length+2];
					highLiftMomentCurveAlpha[0] = "Alpha";
					highLiftMomentCurveAlpha[1] = "deg";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayPlotHighLift().length; i++) 
						highLiftMomentCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayPlotHighLift()[i];

					Object[] highLiftMomentCurveCM = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
							).length+2];
					highLiftMomentCurveCM[0] = "CM";
					highLiftMomentCurveCM[1] = "";
					for(int i=0; 
							i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficient3DCurveHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
									).length; 
							i++) 
						highLiftMomentCurveCM[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
								)[i];

					dataListWing.add(highLiftMomentCurveAlpha);
					dataListWing.add(highLiftMomentCurveCM);
					dataListWing.add(new Object[] {""});

					currentBoldIndex = currentBoldIndex+5;
				}

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {
					dataListWing.add(new Object[] {
							"CL (High Lift) at Alpha = " + _alphaWingCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlphaHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)
									)});
					currentBoldIndex = currentBoldIndex+1;
				}
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {
					dataListWing.add(new Object[] {
							"CD (High Lift) at Alpha = " + _alphaWingCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDAtAlphaHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)
									)});
					currentBoldIndex = currentBoldIndex+1;
				}
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {
					dataListWing.add(new Object[] {
							"CM (High Lift) at Alpha = " + _alphaWingCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
							"",
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMAtAlphaHighLift().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)
									)});
					currentBoldIndex = currentBoldIndex+1;
				}

			}

			//------------------------------------------------------------------------------------------------------------------------
			// CREATING CELLS ...
			//--------------------------------------------------------------------------------
			Row rowWing = wingSheet.createRow(0);
			Object[] objArrWing = dataListWing.get(0);
			int cellnumWing = 0;
			for (Object obj : objArrWing) {
				Cell cell = rowWing.createCell(cellnumWing++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}

			int rownumWing = 1;
			for (int i = 1; i < dataListWing.size(); i++) {
				objArrWing = dataListWing.get(i);
				rowWing = wingSheet.createRow(rownumWing++);
				cellnumWing = 0;
				Boolean isBold = Boolean.FALSE;
				for(int bri=0; bri<boldRowIndex.size(); bri++) 
					if(rownumWing == boldRowIndex.get(bri))
						isBold = Boolean.TRUE;
				for (Object obj : objArrWing) {
					Cell cell = rowWing.createCell(cellnumWing++);
					if(isBold == Boolean.TRUE)
						cell.setCellStyle(styleHeader);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}

				wingSheet.setDefaultColumnWidth(35);
				wingSheet.setColumnWidth(1, 2048);
				for(int k=2; k<100; k++)
					wingSheet.setColumnWidth(k, 3840);

			}
		}

		//--------------------------------------------------------------------------------
		// HTAIL ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_writeHTail) {

			Sheet hTailSheet = wb.createSheet("HORIZONTAL_TAIL");
			List<Object[]> dataListHTail = new ArrayList<>();

			List<Integer> boldRowIndex = new ArrayList<>();
			int currentBoldIndex = 1;

			dataListHTail.add(new Object[] {"Description","Unit","Value"});

			dataListHTail.add(new Object[] {"GLOBAL DATA"});
			currentBoldIndex = 2;
			boldRowIndex.add(currentBoldIndex);


			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
				dataListHTail.add(new Object[] {
						"Critical Mach Number",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCriticalMachNumber().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {
				dataListHTail.add(new Object[] {
						"Aerodynamic Center (LRF)",
						"m",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacLRF().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
								).doubleValue(SI.METER)
				});
				dataListHTail.add(new Object[] {
						"Aerodynamic Center (MAC)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacMRF().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
								)
				});
				currentBoldIndex = currentBoldIndex+2;
			}

			dataListHTail.add(new Object[] {""});
			dataListHTail.add(new Object[] {""});
			dataListHTail.add(new Object[] {"LIFT"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {
				dataListHTail.add(new Object[] {
						"CL_alpha",
						"1/deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ALPHA)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {
				dataListHTail.add(new Object[] {
						"CL_zero",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLZero().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ZERO)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_STAR)) {
				dataListHTail.add(new Object[] {
						"CL_star",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLStar().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_STAR)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_MAX)) {
				dataListHTail.add(new Object[] {
						"CL_max",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLMax().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
				dataListHTail.add(new Object[] {
						"Alpha_zero_lift",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaZeroLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_STAR)) {
				dataListHTail.add(new Object[] {
						"Alpha_star",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStar().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_STALL)) {
				dataListHTail.add(new Object[] {
						"Alpha_stall",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStall().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
				dataListHTail.add(new Object[] {
						"CL at Alpha = " + _alphaHTailCurrent,
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingLiftCurve3D
				 */
				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"LIFT CURVE"});

				Object[] liftCurveAlpha = new Object[_alphaHTailList.size()+2];
				liftCurveAlpha[0] = "Alpha";
				liftCurveAlpha[1] = "deg";
				for(int i=0; i<_alphaHTailList.size(); i++) 
					liftCurveAlpha[i+2] = _alphaHTailList.get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] liftCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						).length+2];
				liftCurveCL[0] = "CL";
				liftCurveCL[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
								).length; 
						i++) 
					liftCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
							)[i];

				dataListHTail.add(liftCurveAlpha);
				dataListHTail.add(liftCurveCL);
				dataListHTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"LIFT DISTRIBUTIONS"});

				Object[] yStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().size()+2];
				yStationsLift[0] = "y stations";
				yStationsLift[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().size(); i++) 
					yStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListHTail.add(yStationsLift);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().size(); i++) {

					dataListHTail.add(new Object[] {""});
					dataListHTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListHTail.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cLDistribution[0] = "Cl";
					cLDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cLDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(cLDistribution);
					//..................................................................................................................................................
					Object[] cLAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cLAdditionalDistribution[0] = "Cl_additional";
					cLAdditionalDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cLAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(cLAdditionalDistribution);
					//..................................................................................................................................................
					Object[] cLBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cLBasicDistribution[0] = "Cl_basic";
					cLBasicDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cLBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(cLBasicDistribution);
					//..................................................................................................................................................
					Object[] cCLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cCLDistribution[0] = "cCl";
					cCLDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cCLDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListHTail.add(cCLDistribution);
					//..................................................................................................................................................
					Object[] cCLAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cCLAdditionalDistribution[0] = "cCl_additional";
					cCLAdditionalDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cCLAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListHTail.add(cCLAdditionalDistribution);
					//..................................................................................................................................................
					Object[] cCLBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cCLBasicDistribution[0] = "cCl_basic";
					cCLBasicDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cCLBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListHTail.add(cCLBasicDistribution);
					//..................................................................................................................................................
					Object[] gammaDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					gammaDistribution[0] = "Gamma";
					gammaDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						gammaDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(gammaDistribution);
					//..................................................................................................................................................
					Object[] gammaAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					gammaAdditionalDistribution[0] = "Gamma_additional";
					gammaAdditionalDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						gammaAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(gammaAdditionalDistribution);
					//..................................................................................................................................................
					Object[] gammaBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					gammaBasicDistribution[0] = "Gamma_basic";
					gammaBasicDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						gammaBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(gammaBasicDistribution);
					//..................................................................................................................................................
					Object[] wingLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					wingLoadDistribution[0] = "Wing load";
					wingLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						wingLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListHTail.add(wingLoadDistribution);
					//..................................................................................................................................................
					Object[] wingAdditionalLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAdditionalLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					wingAdditionalLoadDistribution[0] = "Additional load";
					wingAdditionalLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAdditionalLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						wingAdditionalLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAdditionalLoadDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListHTail.add(wingAdditionalLoadDistribution);
					//..................................................................................................................................................	
					Object[] wingBasicLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getBasicLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					wingBasicLoadDistribution[0] = "Basic load";
					wingBasicLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getBasicLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						wingBasicLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getBasicLoadDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListHTail.add(wingBasicLoadDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+15;

				}
			}

			dataListHTail.add(new Object[] {""});
			dataListHTail.add(new Object[] {""});
			dataListHTail.add(new Object[] {"DRAG"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD0)) {
				dataListHTail.add(new Object[] {
						"CD_zero",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD0)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {
				dataListHTail.add(new Object[] {
						"Oswald factor",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getOswaldFactor().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.OSWALD_FACTOR)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {
				dataListHTail.add(new Object[] {
						"CD_induced",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCDInduced().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_WAVE)) {
				dataListHTail.add(new Object[] {
						"CD_wave",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCDWave().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_WAVE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
				dataListHTail.add(new Object[] {
						"CD at alpha = " + _alphaHTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCDAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingPolarCurve3D
				 */
				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"DRAG POLAR CURVE"});

				Object[] polarCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						).length+2];
				polarCurveCL[0] = "CL";
				polarCurveCL[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
								).length; 
						i++) 
					polarCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
							)[i];

				Object[] polarCurveCD = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
						).length+2];
				polarCurveCD[0] = "CD";
				polarCurveCD[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								).length; 
						i++) 
					polarCurveCD[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
							)[i];

				dataListHTail.add(polarCurveCL);
				dataListHTail.add(polarCurveCD);
				dataListHTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"DRAG DISTRIBUTIONS"});

				Object[] yStationsDrag = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().size()+2];
				yStationsDrag[0] = "y stations";
				yStationsDrag[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().size(); i++) 
					yStationsDrag[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListHTail.add(yStationsDrag);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().size(); i++) {

					dataListHTail.add(new Object[] {""});
					dataListHTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListHTail.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cDParasiteDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getParasiteDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cDParasiteDistribution[0] = "Cd_parasite";
					cDParasiteDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getParasiteDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cDParasiteDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getParasiteDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(cDParasiteDistribution);
					//..................................................................................................................................................
					Object[] cDInducedDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getInducedDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cDInducedDistribution[0] = "Cd_induced";
					cDInducedDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getInducedDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cDInducedDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getInducedDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(cDInducedDistribution);
					//..................................................................................................................................................
					Object[] cDDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cDDistribution[0] = "Cd";
					cDDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cDDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(cDDistribution);
					//..................................................................................................................................................
					Object[] dragDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					dragDistribution[0] = "Drag";
					dragDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						dragDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListHTail.add(dragDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+7;

				}
			}

			dataListHTail.add(new Object[] {""});
			dataListHTail.add(new Object[] {""});
			dataListHTail.add(new Object[] {"PITCHING MOMENT"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {
				dataListHTail.add(new Object[] {
						"CM_ac",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCMac().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {
				dataListHTail.add(new Object[] {
						"CM_alpha",
						"1/deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCMAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
				dataListHTail.add(new Object[] {
						"CM at alpha = " + _alphaHTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCMAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingMomentCurve3D
				 */
				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"PITCHING MOMENT CURVE"});

				Object[] momentCurveAlpha = new Object[_alphaHTailList.size()+2];
				momentCurveAlpha[0] = "Alpha";
				momentCurveAlpha[1] = "deg";
				for(int i=0; i<_alphaHTailList.size(); i++) 
					momentCurveAlpha[i+2] = _alphaHTailList.get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] momentCurveCM = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
						).length+2];
				momentCurveCM[0] = "CM";
				momentCurveCM[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
								).length; 
						i++) 
					momentCurveCM[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
							)[i];

				dataListHTail.add(momentCurveAlpha);
				dataListHTail.add(momentCurveCM);
				dataListHTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"PITCHING MOMENT DISTRIBUTIONS"});

				Object[] yStationsMoment = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().size()+2];
				yStationsMoment[0] = "y stations";
				yStationsMoment[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().size(); i++) 
					yStationsMoment[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListHTail.add(yStationsMoment);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().size(); i++) {

					dataListHTail.add(new Object[] {""});
					dataListHTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListHTail.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cMDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					cMDistribution[0] = "Cm";
					cMDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						cMDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j);
					dataListHTail.add(cMDistribution);
					//..................................................................................................................................................
					Object[] momentDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
					momentDistribution[0] = "Moment";
					momentDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
									).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size(); 
							j++) 
						momentDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListHTail.add(momentDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+5;

				}
			}

			dataListHTail.add(new Object[] {""});
			dataListHTail.add(new Object[] {""});
			dataListHTail.add(new Object[] {"ELEVATOR EFFECTS AT de = " + _theAerodynamicBuilderInterface.getElevatorDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE)});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"ELEVATOR EFFECTS (2D)"});

				//.........................................................................................................................................
				dataListHTail.add(new Object[] {
						"Delta Cl0",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDeltaCl0Flap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................
				dataListHTail.add(new Object[] {
						"Delta Clmax",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDeltaClmaxFlap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"ELEVATOR EFFECTS (3D)"});

				//.........................................................................................................................................
				dataListHTail.add(new Object[] {
						"Delta CL0",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDeltaCL0Flap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................
				dataListHTail.add(new Object[] {
						"Delta CLmax",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDeltaCLmaxFlap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................
				dataListHTail.add(new Object[] {
						"Delta CD0",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDeltaCD0().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................
				dataListHTail.add(new Object[] {
						"Delta CM_c/4",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDeltaCMc4().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"GLOBAL ELEVATOR EFFECTS"});
				dataListHTail.add(new Object[] {
						"Alpha stall (with Elevator)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStallHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				dataListHTail.add(new Object[] {
						"Alpha star (with Elevator)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStarHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				dataListHTail.add(new Object[] {
						"CL_max (with Elevator)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLMaxHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				dataListHTail.add(new Object[] {
						"CL_star (with Elevator)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLStarHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				dataListHTail.add(new Object[] {
						"CL_alpha (with Elevator)",
						"1/deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});

				currentBoldIndex = currentBoldIndex+15;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"LIFT CURVE WITH ELEVATOR"});

				Object[] highLiftCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayPlotHighLift().length+2];
				highLiftCurveAlpha[0] = "Alpha";
				highLiftCurveAlpha[1] = "deg";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayPlotHighLift().length; i++) 
					highLiftCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayPlotHighLift()[i];

				Object[] highLiftCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
						).length+2];
				highLiftCurveCL[0] = "CL";
				highLiftCurveCL[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
								).length; 
						i++) 
					highLiftCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
							)[i];

				dataListHTail.add(highLiftCurveAlpha);
				dataListHTail.add(highLiftCurveCL);
				dataListHTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"DRAG POLAR CURVE WITH ELEVATOR"});

				Object[] highLiftPolarCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
						).length+2];
				highLiftPolarCurveCL[0] = "CL";
				highLiftPolarCurveCL[1] = "";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
						).length; i++) 
					highLiftPolarCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
							)[i];

				Object[] highLiftPolarCurveCD = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
						).length+2];
				highLiftPolarCurveCD[0] = "CD";
				highLiftPolarCurveCD[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
								).length; 
						i++) 
					highLiftPolarCurveCD[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getPolar3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
							)[i];

				dataListHTail.add(highLiftPolarCurveCL);
				dataListHTail.add(highLiftPolarCurveCD);
				dataListHTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

				dataListHTail.add(new Object[] {""});
				dataListHTail.add(new Object[] {"PITCHING MOMENT CURVE WITH ELEVATOR"});

				Object[] highLiftMomentCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayPlotHighLift().length+2];
				highLiftMomentCurveAlpha[0] = "Alpha";
				highLiftMomentCurveAlpha[1] = "deg";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayPlotHighLift().length; i++) 
					highLiftMomentCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayPlotHighLift()[i];

				Object[] highLiftMomentCurveCM = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
						).length+2];
				highLiftMomentCurveCM[0] = "CM";
				highLiftMomentCurveCM[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
								).length; 
						i++) 
					highLiftMomentCurveCM[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
							)[i];

				dataListHTail.add(highLiftMomentCurveAlpha);
				dataListHTail.add(highLiftMomentCurveCM);
				dataListHTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {
				dataListHTail.add(new Object[] {
						"CL (with Elevator) at Alpha = " + _alphaHTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAtAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {
				dataListHTail.add(new Object[] {
						"CD (with Elevator) at Alpha = " + _alphaHTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCDAtAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {
				dataListHTail.add(new Object[] {
						"CM (with Elevator) at Alpha = " + _alphaHTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCMAtAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			//------------------------------------------------------------------------------------------------------------------------
			// CREATING CELLS ...
			//--------------------------------------------------------------------------------
			Row rowHTail = hTailSheet.createRow(0);
			Object[] objArrHTail = dataListHTail.get(0);
			int cellnumHTail = 0;
			for (Object obj : objArrHTail) {
				Cell cell = rowHTail.createCell(cellnumHTail++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}

			int rownumHTail = 1;
			for (int i = 1; i < dataListHTail.size(); i++) {
				objArrHTail = dataListHTail.get(i);
				rowHTail = hTailSheet.createRow(rownumHTail++);
				cellnumHTail = 0;
				Boolean isBold = Boolean.FALSE;
				for(int bri=0; bri<boldRowIndex.size(); bri++) 
					if(rownumHTail == boldRowIndex.get(bri))
						isBold = Boolean.TRUE;
				for (Object obj : objArrHTail) {
					Cell cell = rowHTail.createCell(cellnumHTail++);
					if(isBold == Boolean.TRUE)
						cell.setCellStyle(styleHeader);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}

				hTailSheet.setDefaultColumnWidth(35);
				hTailSheet.setColumnWidth(1, 2048);
				for(int k=2; k<100; k++)
					hTailSheet.setColumnWidth(k, 3840);

			}
		}

		//--------------------------------------------------------------------------------
		// VTAIL ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_writeVTail) {

			Sheet vTailSheet = wb.createSheet("VERTICAL_TAIL");
			List<Object[]> dataListVTail = new ArrayList<>();

			List<Integer> boldRowIndex = new ArrayList<>();
			int currentBoldIndex = 1;

			dataListVTail.add(new Object[] {"Description","Unit","Value"});

			dataListVTail.add(new Object[] {"GLOBAL DATA"});
			currentBoldIndex = 2;
			boldRowIndex.add(currentBoldIndex);


			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
				dataListVTail.add(new Object[] {
						"Critical Mach Number",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCriticalMachNumber().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {
				dataListVTail.add(new Object[] {
						"Aerodynamic Center (LRF)",
						"m",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getXacLRF().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
								).doubleValue(SI.METER)
				});
				dataListVTail.add(new Object[] {
						"Aerodynamic Center (MAC)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getXacMRF().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
								)
				});
				currentBoldIndex = currentBoldIndex+2;
			}

			dataListVTail.add(new Object[] {""});
			dataListVTail.add(new Object[] {""});
			dataListVTail.add(new Object[] {"LIFT"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) {
				dataListVTail.add(new Object[] {
						"CL_alpha",
						"1/deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ALPHA)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_ZERO)) {
				dataListVTail.add(new Object[] {
						"CL_zero",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLZero().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ZERO)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_STAR)) {
				dataListVTail.add(new Object[] {
						"CL_star",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLStar().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_STAR)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_MAX)) {
				dataListVTail.add(new Object[] {
						"CL_max",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLMax().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)) {
				dataListVTail.add(new Object[] {
						"Alpha_zero_lift",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaZeroLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_STAR)) {
				dataListVTail.add(new Object[] {
						"Alpha_star",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStar().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.ALPHA_STALL)) {
				dataListVTail.add(new Object[] {
						"Alpha_stall",
						"deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStall().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
				dataListVTail.add(new Object[] {
						"CL at Alpha = " + _betaVTailCurrent,
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingLiftCurve3D
				 */
				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"LIFT CURVE"});

				Object[] liftCurveAlpha = new Object[_alphaHTailList.size()+2];
				liftCurveAlpha[0] = "Alpha";
				liftCurveAlpha[1] = "deg";
				for(int i=0; i<_alphaHTailList.size(); i++) 
					liftCurveAlpha[i+2] = _alphaHTailList.get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] liftCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						).length+2];
				liftCurveCL[0] = "CL";
				liftCurveCL[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
								).length; 
						i++) 
					liftCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
							)[i];

				dataListVTail.add(liftCurveAlpha);
				dataListVTail.add(liftCurveCL);
				dataListVTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"LIFT DISTRIBUTIONS"});

				Object[] yStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().size()+2];
				yStationsLift[0] = "y stations";
				yStationsLift[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().size(); i++) 
					yStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListVTail.add(yStationsLift);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().size(); i++) {

					dataListVTail.add(new Object[] {""});
					dataListVTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListVTail.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cLDistribution[0] = "Cl";
					cLDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cLDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(cLDistribution);
					//..................................................................................................................................................
					Object[] cLAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cLAdditionalDistribution[0] = "Cl_additional";
					cLAdditionalDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cLAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(cLAdditionalDistribution);
					//..................................................................................................................................................
					Object[] cLBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cLBasicDistribution[0] = "Cl_basic";
					cLBasicDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cLBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(cLBasicDistribution);
					//..................................................................................................................................................
					Object[] cCLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cCLDistribution[0] = "cCl";
					cCLDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cCLDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListVTail.add(cCLDistribution);
					//..................................................................................................................................................
					Object[] cCLAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cCLAdditionalDistribution[0] = "cCl_additional";
					cCLAdditionalDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cCLAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListVTail.add(cCLAdditionalDistribution);
					//..................................................................................................................................................
					Object[] cCLBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cCLBasicDistribution[0] = "cCl_basic";
					cCLBasicDistribution[1] = "";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cCLBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j).doubleValue(SI.METER);
					dataListVTail.add(cCLBasicDistribution);
					//..................................................................................................................................................
					Object[] gammaDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					gammaDistribution[0] = "Gamma";
					gammaDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						gammaDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(gammaDistribution);
					//..................................................................................................................................................
					Object[] gammaAdditionalDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					gammaAdditionalDistribution[0] = "Gamma_additional";
					gammaAdditionalDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistributionAdditionalLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						gammaAdditionalDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(gammaAdditionalDistribution);
					//..................................................................................................................................................
					Object[] gammaBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					gammaBasicDistribution[0] = "Gamma_basic";
					gammaBasicDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						gammaBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(gammaBasicDistribution);
					//..................................................................................................................................................
					Object[] wingLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					wingLoadDistribution[0] = "Wing load";
					wingLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						wingLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListVTail.add(wingLoadDistribution);
					//..................................................................................................................................................
					Object[] wingAdditionalLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAdditionalLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					wingAdditionalLoadDistribution[0] = "Additional load";
					wingAdditionalLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAdditionalLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						wingAdditionalLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAdditionalLoadDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListVTail.add(wingAdditionalLoadDistribution);
					//..................................................................................................................................................	
					Object[] wingBasicLoadDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getBasicLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					wingBasicLoadDistribution[0] = "Basic load";
					wingBasicLoadDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getBasicLoadDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						wingBasicLoadDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getBasicLoadDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListVTail.add(wingBasicLoadDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+15;

				}
			}

			dataListVTail.add(new Object[] {""});
			dataListVTail.add(new Object[] {""});
			dataListVTail.add(new Object[] {"DRAG"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD0)) {
				dataListVTail.add(new Object[] {
						"CD_zero",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCD0().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD0)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.OSWALD_FACTOR)) {
				dataListVTail.add(new Object[] {
						"Oswald factor",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getOswaldFactor().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.OSWALD_FACTOR)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)) {
				dataListVTail.add(new Object[] {
						"CD_induced",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCDInduced().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_INDUCED_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_WAVE)) {
				dataListVTail.add(new Object[] {
						"CD_wave",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCDWave().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_WAVE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)) {
				dataListVTail.add(new Object[] {
						"CD at alpha = " + _betaVTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCDAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingPolarCurve3D
				 */
				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"DRAG POLAR CURVE"});

				Object[] polarCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						).length+2];
				polarCurveCL[0] = "CL";
				polarCurveCL[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
								).length; 
						i++) 
					polarCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
							)[i];

				Object[] polarCurveCD = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getPolar3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
						).length+2];
				polarCurveCD[0] = "CD";
				polarCurveCD[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
								).length; 
						i++) 
					polarCurveCD[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)
							)[i];

				dataListVTail.add(polarCurveCL);
				dataListVTail.add(polarCurveCD);
				dataListVTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"DRAG DISTRIBUTIONS"});

				Object[] yStationsDrag = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().size()+2];
				yStationsDrag[0] = "y stations";
				yStationsDrag[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().size(); i++) 
					yStationsDrag[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListVTail.add(yStationsDrag);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().size(); i++) {

					dataListVTail.add(new Object[] {""});
					dataListVTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListVTail.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cDParasiteDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getParasiteDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cDParasiteDistribution[0] = "Cd_parasite";
					cDParasiteDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getParasiteDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cDParasiteDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getParasiteDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(cDParasiteDistribution);
					//..................................................................................................................................................
					Object[] cDInducedDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getInducedDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cDInducedDistribution[0] = "Cd_induced";
					cDInducedDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getInducedDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cDInducedDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getInducedDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(cDInducedDistribution);
					//..................................................................................................................................................
					Object[] cDDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cDDistribution[0] = "Cd";
					cDDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDragCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cDDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(cDDistribution);
					//..................................................................................................................................................
					Object[] dragDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDragDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					dragDistribution[0] = "Drag";
					dragDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDragDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						dragDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDragDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListVTail.add(dragDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+7;

				}
			}

			dataListVTail.add(new Object[] {""});
			dataListVTail.add(new Object[] {""});
			dataListVTail.add(new Object[] {"PITCHING MOMENT"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)) {
				dataListVTail.add(new Object[] {
						"CM_ac",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCMac().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)) {
				dataListVTail.add(new Object[] {
						"CM_alpha",
						"1/deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCMAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)) {
				dataListVTail.add(new Object[] {
						"CM at alpha = " + _betaVTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCMAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)) {

				/*
				 * THIS IS ONLY THE CLEAN CURVE SO YOU DON'T HAVE TO CONSIDER THE _currentWingMomentCurve3D
				 */
				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"PITCHING MOMENT CURVE"});

				Object[] momentCurveAlpha = new Object[_alphaHTailList.size()+2];
				momentCurveAlpha[0] = "Alpha";
				momentCurveAlpha[1] = "deg";
				for(int i=0; i<_alphaHTailList.size(); i++) 
					momentCurveAlpha[i+2] = _alphaHTailList.get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] momentCurveCM = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMoment3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
						).length+2];
				momentCurveCM[0] = "CM";
				momentCurveCM[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
								).length; 
						i++) 
					momentCurveCM[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
							)[i];

				dataListVTail.add(momentCurveAlpha);
				dataListVTail.add(momentCurveCM);
				dataListVTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)) {

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"PITCHING MOMENT DISTRIBUTIONS"});

				Object[] yStationsMoment = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().size()+2];
				yStationsMoment[0] = "y stations";
				yStationsMoment[1] = "m";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().size(); i++) 
					yStationsMoment[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution().get(i).doubleValue(SI.METER);

				dataListVTail.add(yStationsMoment);

				currentBoldIndex = currentBoldIndex+3;

				for(int i=0; i<_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().size(); i++) {

					dataListVTail.add(new Object[] {""});
					dataListVTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					dataListVTail.add(new Object[] {""});

					//..................................................................................................................................................
					Object[] cMDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					cMDistribution[0] = "Cm";
					cMDistribution[1] = "1/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficientDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						cMDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j);
					dataListVTail.add(cMDistribution);
					//..................................................................................................................................................
					Object[] momentDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
					momentDistribution[0] = "Moment";
					momentDistribution[1] = "N/m";
					for(int j=0; 
							j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentDistribution().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
									).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size(); 
							j++) 
						momentDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).get(j).doubleValue(SI.NEWTON);
					dataListVTail.add(momentDistribution);
					//..................................................................................................................................................

					currentBoldIndex = currentBoldIndex+5;

				}
			}

			dataListVTail.add(new Object[] {""});
			dataListVTail.add(new Object[] {""});
			dataListVTail.add(new Object[] {"RUDDER EFFECTS AT dr = " + _theAerodynamicBuilderInterface.getRudderDeflectionForAnalysis().doubleValue(NonSI.DEGREE_ANGLE) + "�"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)) {

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"RUDDER EFFECTS (2D)"});

				//.........................................................................................................................................
				dataListVTail.add(new Object[] {
						"Delta Cl0",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDeltaCl0Flap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................
				dataListVTail.add(new Object[] {
						"Delta Clmax",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDeltaClmaxFlap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"RUDDER EFFECTS (3D)"});

				//.........................................................................................................................................
				dataListVTail.add(new Object[] {
						"Delta CL0",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDeltaCL0Flap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................
				dataListVTail.add(new Object[] {
						"Delta CLmax",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDeltaCLmaxFlap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................
				dataListVTail.add(new Object[] {
						"Delta CD0",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDeltaCD0().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................
				dataListVTail.add(new Object[] {
						"Delta CM_c/4",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getDeltaCMc4().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				//.........................................................................................................................................

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"GLOBAL RUDDER EFFECTS"});
				dataListVTail.add(new Object[] {
						"Alpha stall (with Rudder)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStallHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				dataListVTail.add(new Object[] {
						"Alpha star (with Rudder)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStarHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).doubleValue(NonSI.DEGREE_ANGLE)});
				dataListVTail.add(new Object[] {
						"CL_max (with Rudder)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLMaxHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				dataListVTail.add(new Object[] {
						"CL_star (with Rudder)",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLStarHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								)});
				dataListVTail.add(new Object[] {
						"CL_alpha (with Rudder)",
						"1/deg",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});

				currentBoldIndex = currentBoldIndex+15;

			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)) {

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"LIFT CURVE WITH RUDDER"});

				Object[] highLiftCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayPlotHighLift().length+2];
				highLiftCurveAlpha[0] = "Alpha";
				highLiftCurveAlpha[1] = "deg";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayPlotHighLift().length; i++) 
					highLiftCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayPlotHighLift()[i];

				Object[] highLiftCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
						).length+2];
				highLiftCurveCL[0] = "CL";
				highLiftCurveCL[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
								).length; 
						i++) 
					highLiftCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
							)[i];

				dataListVTail.add(highLiftCurveAlpha);
				dataListVTail.add(highLiftCurveCL);
				dataListVTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)) {

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"DRAG POLAR CURVE WITH RUDDER"});

				Object[] highLiftPolarCurveCL = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
						).length+2];
				highLiftPolarCurveCL[0] = "CL";
				highLiftPolarCurveCL[1] = "";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
						).length; i++) 
					highLiftPolarCurveCL[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
							)[i];

				Object[] highLiftPolarCurveCD = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getPolar3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
						).length+2];
				highLiftPolarCurveCD[0] = "CD";
				highLiftPolarCurveCD[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getPolar3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
								).length; 
						i++) 
					highLiftPolarCurveCD[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getPolar3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_POLAR_CURVE_3D)
							)[i];

				dataListVTail.add(highLiftPolarCurveCL);
				dataListVTail.add(highLiftPolarCurveCD);
				dataListVTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)) {

				dataListVTail.add(new Object[] {""});
				dataListVTail.add(new Object[] {"PITCHING MOMENT CURVE WITH RUDDER"});

				Object[] highLiftMomentCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayPlotHighLift().length+2];
				highLiftMomentCurveAlpha[0] = "Alpha";
				highLiftMomentCurveAlpha[1] = "deg";
				for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayPlotHighLift().length; i++) 
					highLiftMomentCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayPlotHighLift()[i];

				Object[] highLiftMomentCurveCM = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
						).length+2];
				highLiftMomentCurveCM[0] = "CM";
				highLiftMomentCurveCM[1] = "";
				for(int i=0; 
						i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficient3DCurveHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
								).length; 
						i++) 
					highLiftMomentCurveCM[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
							)[i];

				dataListVTail.add(highLiftMomentCurveAlpha);
				dataListVTail.add(highLiftMomentCurveCM);
				dataListVTail.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)) {
				dataListVTail.add(new Object[] {
						"CL (with Rudder) at Alpha = " + _betaVTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAtAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)) {
				dataListVTail.add(new Object[] {
						"CD (with Rudder) at Alpha = " + _betaVTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCDAtAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_HIGH_LIFT)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)) {
				dataListVTail.add(new Object[] {
						"CM (with Rudder) at Alpha = " + _betaVTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCMAtAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			//------------------------------------------------------------------------------------------------------------------------
			// CREATING CELLS ...
			//--------------------------------------------------------------------------------
			Row rowVTail = vTailSheet.createRow(0);
			Object[] objArrVTail = dataListVTail.get(0);
			int cellnumVTail = 0;
			for (Object obj : objArrVTail) {
				Cell cell = rowVTail.createCell(cellnumVTail++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}

			int rownumVTail = 1;
			for (int i = 1; i < dataListVTail.size(); i++) {
				objArrVTail = dataListVTail.get(i);
				rowVTail = vTailSheet.createRow(rownumVTail++);
				cellnumVTail = 0;
				Boolean isBold = Boolean.FALSE;
				for(int bri=0; bri<boldRowIndex.size(); bri++) 
					if(rownumVTail == boldRowIndex.get(bri))
						isBold = Boolean.TRUE;
				for (Object obj : objArrVTail) {
					Cell cell = rowVTail.createCell(cellnumVTail++);
					if(isBold == Boolean.TRUE)
						cell.setCellStyle(styleHeader);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}

				vTailSheet.setDefaultColumnWidth(35);
				vTailSheet.setColumnWidth(1, 2048);
				for(int k=2; k<100; k++)
					vTailSheet.setColumnWidth(k, 3840);

			}
		}

		//--------------------------------------------------------------------------------
		// FUSELAGE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_writeFuselage) {

			Sheet fuselageSheet = wb.createSheet("FUSELAGE");
			List<Object[]> dataListFuselage = new ArrayList<>();

			List<Integer> boldRowIndex = new ArrayList<>();
			int currentBoldIndex = 1;

			dataListFuselage.add(new Object[] {"Description","Unit","Value"});

			dataListFuselage.add(new Object[] {"DRAG"});
			currentBoldIndex = 2;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_PARASITE_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CD0_parasite",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Parasite().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_PARASITE_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_BASE_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CD0_base",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Base().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_BASE_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_UPSWEEP_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CD0_upsweep",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Upsweep().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_UPSWEEP_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_WINDSHIELD_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CD0_windshield",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Windshield().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_WINDSHIELD_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_TOTAL_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CD0_total",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Total().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_TOTAL_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CD_induced",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCDInduced().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CD at alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCDAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)) {

				dataListFuselage.add(new Object[] {""});
				dataListFuselage.add(new Object[] {"DRAG POLAR CURVE"});

				Object[] polarCurveAlpha = new Object[_alphaBodyList.size()+2];
				polarCurveAlpha[0] = "Alpha";
				polarCurveAlpha[1] = "deg";
				for(int i=0; i<_alphaBodyList.size(); i++) 
					polarCurveAlpha[i+2] = _alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] polarCurveCD = new Object[_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getPolar3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)
						).length+2];
				polarCurveCD[0] = "CD";
				polarCurveCD[1] = "";
				for(int i=0; 
						i<_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)
								).length; 
						i++) 
					polarCurveCD[i+2] = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)
							)[i];

				dataListFuselage.add(polarCurveAlpha);
				dataListFuselage.add(polarCurveCD);
				dataListFuselage.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			dataListFuselage.add(new Object[] {""});
			dataListFuselage.add(new Object[] {""});
			dataListFuselage.add(new Object[] {"PITCHING MOMENT"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM0_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CM0",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCM0().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM0_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CM_alpha",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCMAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM_ALPHA_FUSELAGE)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE)) {
				dataListFuselage.add(new Object[] {
						"CM at alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCMAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)) {

				dataListFuselage.add(new Object[] {""});
				dataListFuselage.add(new Object[] {"PITCHING MOMENT CURVE"});

				Object[] momentCurveAlpha = new Object[_alphaBodyList.size()+2];
				momentCurveAlpha[0] = "Alpha";
				momentCurveAlpha[1] = "deg";
				for(int i=0; i<_alphaBodyList.size(); i++) 
					momentCurveAlpha[i+2] = _alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] momentCurveCM = new Object[_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getMoment3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)
						).length+2];
				momentCurveCM[0] = "CM";
				momentCurveCM[1] = "";
				for(int i=0; 
						i<_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)
								).length; 
						i++) 
					momentCurveCM[i+2] = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)
							)[i];

				dataListFuselage.add(momentCurveAlpha);
				dataListFuselage.add(momentCurveCM);
				dataListFuselage.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			//------------------------------------------------------------------------------------------------------------------------
			// CREATING CELLS ...
			//--------------------------------------------------------------------------------
			Row rowFuselage = fuselageSheet.createRow(0);
			Object[] objArrFuselage = dataListFuselage.get(0);
			int cellnumFuselage = 0;
			for (Object obj : objArrFuselage) {
				Cell cell = rowFuselage.createCell(cellnumFuselage++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}

			int rownumFuselage = 1;
			for (int i = 1; i < dataListFuselage.size(); i++) {
				objArrFuselage = dataListFuselage.get(i);
				rowFuselage = fuselageSheet.createRow(rownumFuselage++);
				cellnumFuselage = 0;
				Boolean isBold = Boolean.FALSE;
				for(int bri=0; bri<boldRowIndex.size(); bri++) 
					if(rownumFuselage == boldRowIndex.get(bri))
						isBold = Boolean.TRUE;
				for (Object obj : objArrFuselage) {
					Cell cell = rowFuselage.createCell(cellnumFuselage++);
					if(isBold == Boolean.TRUE)
						cell.setCellStyle(styleHeader);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}

				fuselageSheet.setDefaultColumnWidth(35);
				fuselageSheet.setColumnWidth(1, 2048);
				for(int k=2; k<100; k++)
					fuselageSheet.setColumnWidth(k, 3840);

			}
		}

		//--------------------------------------------------------------------------------
		// NACELLE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_writeNacelle) {

			Sheet nacelleSheet = wb.createSheet("NACELLE");
			List<Object[]> dataListNacelle = new ArrayList<>();

			List<Integer> boldRowIndex = new ArrayList<>();
			int currentBoldIndex = 1;

			dataListNacelle.add(new Object[] {"Description","Unit","Value"});

			dataListNacelle.add(new Object[] {"DRAG"});
			currentBoldIndex = 2;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_PARASITE_NACELLE)) {
				dataListNacelle.add(new Object[] {
						"CD0_parasite",
						"",
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCD0Parasite().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_PARASITE_NACELLE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_BASE_NACELLE)) {
				dataListNacelle.add(new Object[] {
						"CD0_base",
						"",
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCD0Base().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_BASE_NACELLE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_TOTAL_NACELLE)) {
				dataListNacelle.add(new Object[] {
						"CD0_total",
						"",
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCD0Total().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_TOTAL_NACELLE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_NACELLE)) {
				dataListNacelle.add(new Object[] {
						"CD_induced",
						"",
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCDInduced().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD_INDUCED_NACELLE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE)) {
				dataListNacelle.add(new Object[] {
						"CD at alpha = " + _alphaNacelleCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCDAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)) {

				dataListNacelle.add(new Object[] {""});
				dataListNacelle.add(new Object[] {"DRAG POLAR CURVE"});

				Object[] polarCurveAlpha = new Object[getAlphaNacelleList().size()+2];
				polarCurveAlpha[0] = "Alpha";
				polarCurveAlpha[1] = "deg";
				for(int i=0; i< getAlphaNacelleList().size(); i++) 
					polarCurveAlpha[i+2] = getAlphaNacelleList().get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] polarCurveCD = new Object[_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getPolar3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)
						).length+2];
				polarCurveCD[0] = "CD";
				polarCurveCD[1] = "";
				for(int i=0; 
						i<_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getPolar3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)
								).length; 
						i++) 
					polarCurveCD[i+2] = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getPolar3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE)
							)[i];

				dataListNacelle.add(polarCurveAlpha);
				dataListNacelle.add(polarCurveCD);
				dataListNacelle.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			dataListNacelle.add(new Object[] {""});
			dataListNacelle.add(new Object[] {""});
			dataListNacelle.add(new Object[] {"PITCHING MOMENT"});
			currentBoldIndex = currentBoldIndex+3;
			boldRowIndex.add(currentBoldIndex);

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM0_NACELLE)) {
				dataListNacelle.add(new Object[] {
						"CM0",
						"",
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCM0().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM0_NACELLE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_NACELLE)) {
				dataListNacelle.add(new Object[] {
						"CM_alpha",
						"",
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCMAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM_ALPHA_NACELLE)
								).to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE)) {
				dataListNacelle.add(new Object[] {
						"CM at alpha = " + _alphaNacelleCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
						"",
						_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCMAtAlpha().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE)
								)});
				currentBoldIndex = currentBoldIndex+1;
			}

			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)) {

				dataListNacelle.add(new Object[] {""});
				dataListNacelle.add(new Object[] {"PITCHING MOMENT CURVE"});

				Object[] momentCurveAlpha = new Object[getAlphaNacelleList().size()+2];
				momentCurveAlpha[0] = "Alpha";
				momentCurveAlpha[1] = "deg";
				for(int i=0; i<getAlphaNacelleList().size(); i++) 
					momentCurveAlpha[i+2] = getAlphaNacelleList().get(i).doubleValue(NonSI.DEGREE_ANGLE);

				Object[] momentCurveCM = new Object[_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getMoment3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)
						).length+2];
				momentCurveCM[0] = "CM";
				momentCurveCM[1] = "";
				for(int i=0; 
						i<_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getMoment3DCurve().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)
								).length; 
						i++) 
					momentCurveCM[i+2] = _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)
							)[i];

				dataListNacelle.add(momentCurveAlpha);
				dataListNacelle.add(momentCurveCM);
				dataListNacelle.add(new Object[] {""});

				currentBoldIndex = currentBoldIndex+5;

			}

			//------------------------------------------------------------------------------------------------------------------------
			// CREATING CELLS ...
			//--------------------------------------------------------------------------------
			Row rowNacelle = nacelleSheet.createRow(0);
			Object[] objArrNacelle = dataListNacelle.get(0);
			int cellnumNacelle = 0;
			for (Object obj : objArrNacelle) {
				Cell cell = rowNacelle.createCell(cellnumNacelle++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}

			int rownumNacelle = 1;
			for (int i = 1; i < dataListNacelle.size(); i++) {
				objArrNacelle = dataListNacelle.get(i);
				rowNacelle = nacelleSheet.createRow(rownumNacelle++);
				cellnumNacelle = 0;
				Boolean isBold = Boolean.FALSE;
				for(int bri=0; bri<boldRowIndex.size(); bri++) 
					if(rownumNacelle == boldRowIndex.get(bri))
						isBold = Boolean.TRUE;
				for (Object obj : objArrNacelle) {
					Cell cell = rowNacelle.createCell(cellnumNacelle++);
					if(isBold == Boolean.TRUE)
						cell.setCellStyle(styleHeader);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}

				nacelleSheet.setDefaultColumnWidth(35);
				nacelleSheet.setColumnWidth(1, 2048);
				for(int k=2; k<100; k++)
					nacelleSheet.setColumnWidth(k, 3840);

			}
		}
        
        //////////////////////////////////////////////////////////////////////
		// TODO : CONTINUE WITH ALL THE AIRCRAFT ANALYSES (EACH PER SHEET). //
        //////////////////////////////////////////////////////////////////////

        //--------------------------------------------------------------------------------
		// XLS FILE CREATION:
		//--------------------------------------------------------------------------------
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
        
	}

	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\n\n\t-------------------------------------\n")
				.append("\tAerodynamic and Stability Analysis\n")
				.append("\t-------------------------------------\n")
				;

		/*
		 * TODO : FILL ME !!
		 *        CHECK WHICH DATA HAVE TO BE REPORTED SINCE THEY CAN BE MASSIVE 
		 */

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
								_current3DHorizontalTailMomentCurve.get(de),
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
						_currentMachNumber 
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
											calcCLAtAlpha.nasaBlackwellCompleteCurve(_alphaBodyCurrent.to(NonSI.DEGREE_ANGLE)),
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
	public IACAerodynamicAndStabilityManager getTheAerodynamicBuilderInterface() {
		return _theAerodynamicBuilderInterface;
	}

	public void setTheAerodynamicBuilderInterface(IACAerodynamicAndStabilityManager _theAerodynamicBuilderInterface) {
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

	public List<Amount<Angle>> getAlphaNacelleList() {
		return _alphaNacelleList;
	}

	public void setAlphaNacelleList(List<Amount<Angle>> _alphaNacelleList) {
		this._alphaNacelleList = _alphaNacelleList;
	}

	public Boolean getWriteWing() {
		return _writeWing;
	}

	public void setWriteWing(Boolean _writeWing) {
		this._writeWing = _writeWing;
	}

	public Boolean getWriteHTail() {
		return _writeHTail;
	}

	public void setWriteHTail(Boolean _writeHTail) {
		this._writeHTail = _writeHTail;
	}

	public Boolean getWriteVTail() {
		return _writeVTail;
	}

	public void setWriteVTail(Boolean _writeVTail) {
		this._writeVTail = _writeVTail;
	}

	public Boolean getWriteFuselage() {
		return _writeFuselage;
	}

	public void setWriteFuselage(Boolean _writeFuselage) {
		this._writeFuselage = _writeFuselage;
	}

	public Boolean getWriteNacelle() {
		return _writeNacelle;
	}

	public void setWriteNacelle(Boolean _writeNacelle) {
		this._writeNacelle = _writeNacelle;
	}
}
