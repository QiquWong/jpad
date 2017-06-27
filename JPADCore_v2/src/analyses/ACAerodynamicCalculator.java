package analyses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLAlpha;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLAtAlpha;
import analyses.liftingsurface.LSAerodynamicsManager.CalcLiftCurve;
import analyses.liftingsurface.LSAerodynamicsManager.CalcMachCr;
import analyses.liftingsurface.LSAerodynamicsManager.CalcMomentCurve;
import analyses.liftingsurface.LSAerodynamicsManager.CalcPolar;
import analyses.liftingsurface.LSAerodynamicsManager.CalcXAC;
import analyses.nacelles.NacelleAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.stability.StabilityCalculators;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

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
	private Double _wingMomentumPole;  // pole referred to M.A.C.
	private Double _hTailMomentumPole; // pole referred to M.A.C.
	private Double _currentMachNumber;
	private Amount<Length> _currentAltitude;
	private List<Double> _wingFinalLiftCurve = new ArrayList<>();
	
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
	// Methods are always the same used for the wing Lift curve. If the wing lift curve is not required, the use method is Nasa Blackwell
	private Double[] _liftCoefficient3DCurveWithFuselageEffect;
	private Amount<?> _clAlphaWingFuselage;
	private Double _clZeroWingFuselage;
	private Double _clMaxWingFuselage;
	private Double _clStarWingFuselage;
	private Amount<Angle> _alphaStarWingFuselage;
	private Amount<Angle> _alphaStallWingFuselage;
	private Amount<Angle> _alphaZeroLiftWingFuselage;
	
	
	Map<MethodEnum, List<Amount<Length>>> _verticalDistanceZeroLiftDirectionWingHTailVariable;
	private Map<Boolean, Map<MethodEnum, List<Double>>> _downwashGradientMap;
	private Map<Boolean, Map<MethodEnum, List<Amount<Angle>>>> _downwashAngleMap;
	private List<Tuple3<MethodEnum, Double, Double>> buffetBarrierCurve = new ArrayList<>();
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
	
	// COMPLETE ME !!
	
	
	
	
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
			break;
		case CLIMB:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachClimb();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeClimb();
			break;
		case CRUISE:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachCruise();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeCruise();
			break;
		case LANDING:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachLanding();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeLanding();
			break;
		default:
			break;
		}
		
		
		calculateComponentsData();
		// TODO --> Control the aircraft task list and set the components analyses which are necessary to perform the aircraft ones
		// (e.g. if stability analisys must be performed, it is necesary to set the lift, drag and moment analyses for all components) 
		
		initializeData();
		initializeArrays();
		
	}
	
	private void initializeData() {
		
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
		
		// TODO : FILL ME !!
		/*
		 * THIS WILL CREATE ALL THE COMPONENTS MANAGERS 
		 * AND RUN ALL THE COMPONENTS CALCULATIONS
		 */
		
		//========================================================================================================================
		// WING
		if(_theAerodynamicBuilderInterface.getTheAircraft().getWing() != null)
			_liftingSurfaceAerodynamicManagers.put(
					ComponentEnum.WING,
					new LSAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getWing(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(),
							_theAerodynamicBuilderInterface.getWingNumberOfPointSemiSpanWise(),
							_alphaWingList, 
							_theAerodynamicBuilderInterface.getAlphaWingForDistribution()
							)
					);
		
		//.........................................................................................................................
		//	CRITICAL_MACH
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
			
			CalcMachCr calcMachCr = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcMachCr();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
			case KORN_MASON:
				calcMachCr.kornMason(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCurrentLiftCoefficient());
				break;
			case KROO:
				calcMachCr.kroo(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCurrentLiftCoefficient());
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
				if(_wingMomentumPole == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacLRF().get(MethodEnum.QUARTER)
							);
				break;
			case DEYOUNG_HARPER:
				calcXAC.deYoungHarper();
				if(_wingMomentumPole == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacLRF().get(MethodEnum.DEYOUNG_HARPER)
							);
				break;
			case NAPOLITANO_DATCOM:
				calcXAC.datcomNapolitano();
				if(_wingMomentumPole == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacLRF().get(MethodEnum.NAPOLITANO_DATCOM)
							);
				break;
			default:
				if(_wingMomentumPole == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacLRF().get(MethodEnum.DEYOUNG_HARPER)
							);
				break;
			}
			
		}

		//.........................................................................................................................
		//	CL_AT_ALPHA
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {

			CalcCLAtAlpha calcCLAtAlpha = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcCLAtAlpha();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)) {
			case LINEAR_DLR:
				calcCLAtAlpha.linearDLR(_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCurrent());
				break;
			case ANDERSON_COMPRESSIBLE_SUBSONIC:
				calcCLAtAlpha.linearAndersonCompressibleSubsonic(_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCurrent());
				break;
			case LINEAR_NASA_BLACKWELL:
				calcCLAtAlpha.nasaBlackwellLinear(_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCurrent());
				break;
			case NASA_BLACKWELL:
				calcCLAtAlpha.nasaBlackwellCompleteCurve(_theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCurrent());
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
			case HELMBOLD_DIEDERICH:
				calcCLAlpha.helmboldDiederich(_currentMachNumber);
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
		//	CL_ALPHA WING BODY
		
		//.........................................................................................................................
		//	CL_ZERO
		
		//.........................................................................................................................
		//	CL_STAR
		
		//.........................................................................................................................
		//	CL_MAX
		
		//.........................................................................................................................
		//	ALPHA_ZERO_LIFT
		
		//.........................................................................................................................
		//	ALPHA_STAR
		
		//.........................................................................................................................
		//	ALPHA_STALL
		
		//.........................................................................................................................
		//	LIFT_CURVE_3D
		
		//.........................................................................................................................
		//	LIFT_CURVE_3D WITH FUSELAGE EFFECT
		
		//.........................................................................................................................
		//	LIFT_DISTRIBUTION
		
		//.........................................................................................................................
		//	CD0
		
		//.........................................................................................................................
		//	CD_INDUCED
		
		//.........................................................................................................................
		//	CD_WAVE
		
		//.........................................................................................................................
		//	OSWALD_FACTOR
		
		//.........................................................................................................................
		//	POLAR_CURVE_3D
		
		//.........................................................................................................................
		//	DRAG_DISTRIBUTION
		
		//.........................................................................................................................
		//	CD_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	HIGH_LIFT_DEVICES_EFFECTS
		
		//.........................................................................................................................
		//	HIGH_LIFT_CURVE_3D
		
		//.........................................................................................................................
		//	CL_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CD_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CM_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CM_AC
		
		//.........................................................................................................................
		//	CM_ALPHA
		
		//.........................................................................................................................
		//	CM_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	MOMENT_CURVE_3D
		
		//.........................................................................................................................
		//	MOMENT_DISTRIBUTION
		
		
		
		
		
		
		//============================================================================
		// HTAIL
		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null)
			_liftingSurfaceAerodynamicManagers.put(
					ComponentEnum.HORIZONTAL_TAIL,
					new LSAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getHTail(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(),
							_theAerodynamicBuilderInterface.getHTailNumberOfPointSemiSpanWise(),
							_alphaHTailList, 
							_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution()
							)
					);
		
		//.........................................................................................................................
		//	CRITICAL_MACH
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
			
			CalcMachCr calcMachCr = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMachCr();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
			case KORN_MASON:
				calcMachCr.kornMason(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCurrentLiftCoefficient());
				break;
			case KROO:
				calcMachCr.kroo(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCurrentLiftCoefficient());
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
				if(_hTailMomentumPole == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacLRF().get(MethodEnum.QUARTER)
							);
				break;
			case DEYOUNG_HARPER:
				calcXAC.deYoungHarper();
				if(_hTailMomentumPole == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacLRF().get(MethodEnum.DEYOUNG_HARPER)
							);
				break;
			case NAPOLITANO_DATCOM:
				calcXAC.datcomNapolitano();
				if(_hTailMomentumPole == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacLRF().get(MethodEnum.NAPOLITANO_DATCOM)
							);
				break;
			default:
				if(_hTailMomentumPole == null)
					_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).setMomentumPole(
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacLRF().get(MethodEnum.DEYOUNG_HARPER)
							);
				break;
			}
			
		}
		
		//.........................................................................................................................
		//	CL_AT_ALPHA (NECESSARY ??)
		
		//.........................................................................................................................
		//	CL_ALPHA
		
		//.........................................................................................................................
		//	CL_ZERO
		
		//.........................................................................................................................
		//	CL_STAR
		
		//.........................................................................................................................
		//	CL_MAX
		
		//.........................................................................................................................
		//	ALPHA_ZERO_LIFT
		
		//.........................................................................................................................
		//	ALPHA_STAR
		
		//.........................................................................................................................
		//	ALPHA_STALL
		
		//.........................................................................................................................
		//	LIFT_CURVE_3D
		
		//.........................................................................................................................
		//	LIFT_DISTRIBUTION
		
		//.........................................................................................................................
		//	CD0
		
		//.........................................................................................................................
		//	CD_INDUCED
		
		//.........................................................................................................................
		//	CD_WAVE
		
		//.........................................................................................................................
		//	OSWALD_FACTOR
		
		//.........................................................................................................................
		//	POLAR_CURVE_3D
		
		//.........................................................................................................................
		//	DRAG_DISTRIBUTION
		
		//.........................................................................................................................
		//	CD_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	HIGH_LIFT_DEVICES_EFFECTS
		
		//.........................................................................................................................
		//	HIGH_LIFT_CURVE_3D
		
		//.........................................................................................................................
		//	CL_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CD_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CM_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CM_AC
		
		//.........................................................................................................................
		//	CM_ALPHA
		
		//.........................................................................................................................
		//	CM_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	MOMENT_CURVE_3D
		
		//.........................................................................................................................
		//	MOMENT_DISTRIBUTION
		
		
		//============================================================================
		// VTAIL
		if(_theAerodynamicBuilderInterface.getTheAircraft().getVTail() != null)
			_liftingSurfaceAerodynamicManagers.put(
					ComponentEnum.VERTICAL_TAIL,
					new LSAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getVTail(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(),
							_theAerodynamicBuilderInterface.getVTailNumberOfPointSemiSpanWise(), 
							_betaList, // Alpha for VTail is Beta
							_theAerodynamicBuilderInterface.getAlphaVerticalTailForDistribution() 
							)
					);
		
		//.........................................................................................................................
		//	CRITICAL_MACH
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
			
			CalcMachCr calcMachCr = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).new CalcMachCr();
			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)) {
			case KORN_MASON:
				calcMachCr.kornMason(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCurrentLiftCoefficient());
				break;
			case KROO:
				calcMachCr.kroo(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCurrentLiftCoefficient());
				break;
			default:
				break;
			}
		}
			
		//.........................................................................................................................
		//	AERODYNAMIC_CENTER
		
		//.........................................................................................................................
		//	CL_AT_ALPHA (NECESSARY ??)
		
		//.........................................................................................................................
		//	CL_ALPHA
		
		//.........................................................................................................................
		//	CL_ZERO
		
		//.........................................................................................................................
		//	CL_STAR
		
		//.........................................................................................................................
		//	CL_MAX
		
		//.........................................................................................................................
		//	ALPHA_ZERO_LIFT
		
		//.........................................................................................................................
		//	ALPHA_STAR
		
		//.........................................................................................................................
		//	ALPHA_STALL
		
		//.........................................................................................................................
		//	LIFT_CURVE_3D
		
		//.........................................................................................................................
		//	LIFT_DISTRIBUTION
		
		//.........................................................................................................................
		//	CD0
		
		//.........................................................................................................................
		//	CD_INDUCED
		
		//.........................................................................................................................
		//	CD_WAVE
		
		//.........................................................................................................................
		//	OSWALD_FACTOR
		
		//.........................................................................................................................
		//	POLAR_CURVE_3D
		
		//.........................................................................................................................
		//	DRAG_DISTRIBUTION
		
		//.........................................................................................................................
		//	CD_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	HIGH_LIFT_DEVICES_EFFECTS
		
		//.........................................................................................................................
		//	HIGH_LIFT_CURVE_3D
		
		//.........................................................................................................................
		//	CL_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CD_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CM_AT_ALPHA_HIGH_LIFT  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CM_AC
		
		//.........................................................................................................................
		//	CM_ALPHA
		
		//.........................................................................................................................
		//	CM_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	MOMENT_CURVE_3D
		
		//.........................................................................................................................
		//	MOMENT_DISTRIBUTION
		
		
		//============================================================================
		// FUSELAGE
		if(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage() != null)
			_fuselageAerodynamicManagers.put(
					ComponentEnum.FUSELAGE,
					new FuselageAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getFuselage(), 
							_theAerodynamicBuilderInterface.getTheAircraft().getWing(), 
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_alphaBodyList, 
							_theAerodynamicBuilderInterface.getCurrentCondition(), 
							null  //fuselageXPercentPositionPole?? FIXME
							)
					);
		
		//.........................................................................................................................
		//	CD0_PARASITE
		
		//.........................................................................................................................
		//	CD0_BASE
		
		//.........................................................................................................................
		//	CD0_UPSWEEP
		
		//.........................................................................................................................
		//	CD0_WINDSHIELD
		
		//.........................................................................................................................
		//	CD0_TOTAL
		
		//.........................................................................................................................
		//	CD_INDUCED
		
		//.........................................................................................................................
		//	POLAR_CURVE_3D
		
		//.........................................................................................................................
		//	CD_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CM0
		
		//.........................................................................................................................
		//	CM_ALPHA
		
		//.........................................................................................................................
		//	CM_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	MOMENT_CURVE_3D
		
		
		//============================================================================
		// NACELLE
		if(_theAerodynamicBuilderInterface.getTheAircraft().getNacelles() != null)
			_nacelleAerodynamicManagers.put(
					ComponentEnum.NACELLE,
					new NacelleAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getNacelles(),
							_theAerodynamicBuilderInterface.getTheAircraft().getWing(), 
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING), 
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(), 
							_alphaBodyList // FIXME is it right ?? 
							)
					);
		
		//.........................................................................................................................
		//	CD0_PARASITE
		
		//.........................................................................................................................
		//	CD0_BASE
		
		//.........................................................................................................................
		//	CD0_TOTAL
		
		//.........................................................................................................................
		//	CD_INDUCED
		
		//.........................................................................................................................
		//	POLAR_CURVE_3D
		
		//.........................................................................................................................
		//	CD_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	CM0
		
		//.........................................................................................................................
		//	CM_ALPHA
		
		//.........................................................................................................................
		//	CM_AT_ALPHA  (NECESSARY ??)
		
		//.........................................................................................................................
		//	MOMENT_CURVE_3D
	
		
		//.........................................................................................................................
		//------USEFUL DATA FOR TOTAL MOMENT COEFFICIENT ------
		// if component task list doesn't contains the task, it will be set the default one
		
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {
			
			//.........................................................................................................................
			//	WING AERODYNAMIC_CENTER
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {
				
				CalcXAC calcXAC = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcXAC();
					calcXAC.deYoungHarper();
					if(_wingMomentumPole == null)
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).setMomentumPole(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacLRF().get(MethodEnum.DEYOUNG_HARPER)
								);
					
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER,MethodEnum.DEYOUNG_HARPER);
			}
			
			//.........................................................................................................................
			//	HORIZONTAL TAIL AERODYNAMIC_CENTER
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)) {
				
				CalcXAC calcXAC = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcXAC();
					calcXAC.deYoungHarper();
					if(_hTailMomentumPole == null)
						_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).setMomentumPole(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacLRF().get(MethodEnum.DEYOUNG_HARPER)
								);
					
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER,MethodEnum.DEYOUNG_HARPER);
			}
			
			//.........................................................................................................................
			//	WING LIFT_CURVE_3D
			
			
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

					CalcLiftCurve calcLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcLiftCurve();

					calcLiftCurve.nasaBlackwell(_currentMachNumber);
					
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D,MethodEnum.NASA_BLACKWELL);
				}
			
			//.........................................................................................................................
			//	WING LIFT_CURVE_3D WITH FUSELAGE EFFECT
			

			if(_theAerodynamicBuilderInterface.getFuselageEffectOnWingLiftCurve()){
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D) &&
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.PHILLIPS_ALLEY)) {
				
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
				
				if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D) || (
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D) &&
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D).equals(MethodEnum.NASA_BLACKWELL))
						) {
					
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
				this._liftCoefficient3DCurveWithFuselageEffect = LiftCalc.calculateCLvsAlphaArray(
						this._clZeroWingFuselage,
						this._clMaxWingFuselage,
						this._alphaStarWingFuselage,
						this._alphaStallWingFuselage,
						this._clAlphaWingFuselage,
						MyArrayUtils.convertListOfAmountToDoubleArray(_alphaWingList)
						);
			}
			
			
			//.........................................................................................................................
			//	WING POLAR_CURVE_3D
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				CalcPolar calcPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcPolar();

				calcPolarCurve.fromCdDistribution(_currentMachNumber, _currentAltitude);
				
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,MethodEnum.AIRFOIL_DISTRIBUTION);
			}
			
			//.........................................................................................................................
			//	WING MOMENT_CURVE_3D
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D)) {

				CalcMomentCurve calcWingMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcMomentCurve();

				calcWingMomentCurve.fromAirfoilDistribution();
				
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D,MethodEnum.AIRFOIL_DISTRIBUTION);
			}
			
			//.........................................................................................................................
			//	HORIZONTAL TAIL LIFT_CURVE_3D
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)) {

				CalcLiftCurve calcHTailLiftCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcLiftCurve();

				calcHTailLiftCurve.nasaBlackwell(_currentMachNumber);
				
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D,MethodEnum.NASA_BLACKWELL);
			}
			
			//.........................................................................................................................
			//	HORIZONTAL TAIL POLAR_CURVE_3D
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE)) {

				CalcPolar calcHTailPolarCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).new CalcPolar();

				calcHTailPolarCurve.fromCdDistribution(_currentMachNumber, _currentAltitude);
				
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE,MethodEnum.AIRFOIL_DISTRIBUTION);
			}
			
			//.........................................................................................................................
			//	HORIZONTAL TAIL MOMENT_CURVE_3D
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D)) {

				CalcMomentCurve calcHorizontalTailMomentCurve = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).new CalcMomentCurve();

				calcHorizontalTailMomentCurve.fromAirfoilDistribution();
				
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D,MethodEnum.AIRFOIL_DISTRIBUTION);
			}
			
			//.........................................................................................................................
			//	FUSELAGE POLAR_CURVE_3D
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE)) {
				
				analyses.fuselage.FuselageAerodynamicsManager.CalcPolar calcFuselagePolarCurve = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcPolar();
				
				calcFuselagePolarCurve.fusDes();
				
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE,MethodEnum.FUSDES);
			}
			
			//.........................................................................................................................
			//	FUSELAGE MOMENT_CURVE_3D
			
			if(!_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D)) {
				
				analyses.fuselage.FuselageAerodynamicsManager.CalcMomentCurve calcFuselageMomentCurve = _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).new CalcMomentCurve();
				
				calcFuselageMomentCurve.fusDes();
				
				_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D,MethodEnum.FUSDES);
			}
		}
		
	}
	

	
	public void calculate() {
		
		initializeAnalysis();
		
		// TODO : FILL ME !!
		/*
		 * CREATE INNER CLASSES FOR EACH "AIRCRAFT" ANALYSIS
		 * AND CALL THEM HERE IF REQUIRED BY THE TASK MAP 
		 */
		
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
				calcDirectionalStability.vedscSimplifiedWing(_theAerodynamicBuilderInterface.getTheOperatingConditions().getMachCruise()); // DEFINE MACH FOR EACH CONDITION !!
				break;
			case VEDSC_USAFDATCOM_WING:
				calcDirectionalStability.vedscUsafDatcomWing(_theAerodynamicBuilderInterface.getTheOperatingConditions().getMachCruise()); // DEFINE MACH FOR EACH CONDITION !!
				break;
			default:
				break;
			}
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		// TODO: CONTINUE
		//------------------------------------------------------------------------------------------------------------------------------------
		
		try {
			toXLSFile("???");
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static ACAerodynamicCalculator importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			ConditionEnum theCondition
			) throws IOException {
		
		// TODO : FILL ME !!
		
		// NB remember to set the current condition
		
//		ACAerodynamicCalculator theAerodynamicAndStabilityManager = new _theAerodynamicBuilderInterface()
//				.currentCondition(theCondition)
//				.build();
	  
		return null;
		
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
			// TODO
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
			// TODO
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
			
			//---------------------------------
			//Necessary values
			//---------------------------------
		
			
			if(_theAerodynamicBuilderInterface.getFuselageEffectOnWingLiftCurve()){
				_wingFinalLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(_liftCoefficient3DCurveWithFuselageEffect);
			}
			
			if(!_theAerodynamicBuilderInterface.getFuselageEffectOnWingLiftCurve()){
				_wingFinalLiftCurve = MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
						.get(ComponentEnum.WING)
						.getLiftCoefficient3DCurve()
						.get(_theAerodynamicBuilderInterface.getComponentTaskList()
								.get(ComponentEnum.WING)
								.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)));
			}
			
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
								_theAerodynamicBuilderInterface.getTheAircraft().getFuselage().getCG().getZBRF(), 
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
								_wingFinalLiftCurve, // isolated wing of wing fuselage if required
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.WING)
										.getPolar3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.WING)
												.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE))),
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.WING)
										.getMoment3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.WING)
												.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D))),
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
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.HORIZONTAL_TAIL)
										.getLiftCoefficient3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.HORIZONTAL_TAIL)
												.get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D))),
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.HORIZONTAL_TAIL)
										.getPolar3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.HORIZONTAL_TAIL)
												.get(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_LIFTING_SURFACE))),
								MyArrayUtils.convertDoubleArrayToListDouble(_liftingSurfaceAerodynamicManagers
										.get(ComponentEnum.HORIZONTAL_TAIL)
										.getMoment3DCurve()
										.get(_theAerodynamicBuilderInterface.getComponentTaskList()
												.get(ComponentEnum.HORIZONTAL_TAIL)
												.get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D))),
								MyArrayUtils.convertDoubleArrayToListDouble(
										_theAerodynamicBuilderInterface.getLandingGearDragCoefficient()), 
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
		// Calculating moment coefficient with delta e deflections... CM
		//=======================================================================================
			
		
		//=======================================================================================
		// Calculating total lift coefficient with delta e deflections... CL
		//=======================================================================================
		
		//=======================================================================================
		// Calculating total drag coefficient with delta e deflections... CD
		//=======================================================================================
		
		//=======================================================================================
		// Calculating horizontal tail equilibrium lift coefficient ... CLh_e
		//=======================================================================================
		
		//=======================================================================================
		// Calculating total equilibrium lift coefficient ... CLtot_e
		//=======================================================================================
		
		//=======================================================================================
		// Calculating delta e equilibrium ... deltae_e
		//=======================================================================================
		
		//=======================================================================================
		// Calculating total equilibrium lift coefficient ... CDtot_e
		//=======================================================================================
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
			
			
			
			_cNbWing.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_theAerodynamicBuilderInterface.getXCGAircraft().stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaWing(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCurrentLiftCoefficient(),
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

	public Double getWingMomentumPole() {
		return _wingMomentumPole;
	}

	public void setWingMomentumPole(Double _wingMomentumPole) {
		this._wingMomentumPole = _wingMomentumPole;
	}

	public Double getHTailMomentumPole() {
		return _hTailMomentumPole;
	}

	public void setHTailMomentumPole(Double _hTailMomentumPole) {
		this._hTailMomentumPole = _hTailMomentumPole;
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

	public Double[] getLiftCoefficient3DCurveWithFuselageEffect() {
		return _liftCoefficient3DCurveWithFuselageEffect;
	}

	public void setLiftCoefficient3DCurveWithFuselageEffect(Double[] _liftCoefficient3DCurveWithFuselageEffect) {
		this._liftCoefficient3DCurveWithFuselageEffect = _liftCoefficient3DCurveWithFuselageEffect;
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
		return buffetBarrierCurve;
	}

	public void setBuffetBarrierCurve(List<Tuple3<MethodEnum, Double, Double>> buffetBarrierCurve) {
		this.buffetBarrierCurve = buffetBarrierCurve;
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
}
