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
import aircraft.components.fuselage.Fuselage;
import aircraft.components.fuselage.creator.FuselageCreator;
import analyses.ACPerformanceManager.ACPerformanceCalculatorBuilder;
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.nacelles.NacelleAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.AirfoilCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import calculators.stability.StabilityCalculators;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
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
	// FROM INPUT (Passed from ACAnalysisManager)
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private List<ConditionEnum> _theConditions;
	private ConditionEnum _currentCondition;
	//..............................................................................
	// FROM INPUT (Passed from XML file)
	private Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> _componentTaskList;
	private List<Double> _xCGAircraft;
	private List<Double> _zCGAircraft;
	private Amount<Angle> _alphaBodyInitial;
	private Amount<Angle> _alphaBodyFinal;
	private int _numberOfAlphasBody;
	private Amount<Angle> _betaInitial;
	private Amount<Angle> _betaFinal;
	private int _numberOfBeta;
	private int _wingNumberOfPointSemiSpanWise;
	private int _hTailNumberOfPointSemiSpanWise;
	private List<Amount<Angle>> _alphaWingForDistribution;
	private List<Amount<Angle>> _alphaHorizontalTailForDistribution;
	private Boolean _downwashConstant; // if TRUE--> constant, if FALSE--> variable
	private Double _dynamicPressureRatio;
	private MyInterpolatingFunction _tauElevatorFunction;
	private MyInterpolatingFunction _tauRudderFunction;
	private List<Amount<Angle>> _deltaElevatorList;
	private List<Amount<Angle>> _deltaRudderList;
	//..............................................................................
	// DERIVED INPUT	
	private Double _wingMomentumPole;  // pole referred to M.A.C.
	private Double _hTailMomentumPole; // pole referred to M.A.C.
	
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
	
	private Map<List<Double>, Map<Amount<Angle>, List<Double>>> _totalMomentCoefficient = new HashMap<>(); //xcg, delta e , CM
	private Map<Amount<Angle>, List<Double>> _totalLiftCoefficient = new HashMap<>(); //delta e, CL
	private Map<Amount<Angle>, List<Double>> _totalDragCoefficient = new HashMap<>(); //delta e, CD
	private Map<List<Double>, List<Double>> _horizontalTailEquilibriumLiftCoefficient = new HashMap<>(); //xcg, CLh
	private Map<List<Double>, List<Double>> _totalEquilibriumLiftCoefficient = new HashMap<>(); //xcg, CL
	private Map<List<Double>, List<Double>> _totalEquilibriumDragCoefficient = new HashMap<>(); //xcg, CL
	
	// COMPLETE ME !!
	
	
	
	
	private void initializeAnalysis() {
		
		_componentTaskList = new HashMap<>();
		_downwashGradientMap = new HashMap<>();
		_downwashAngleMap = new HashMap<>();
		_verticalDistanceZeroLiftDirectionWingHTailVariable = new HashMap<>();
		_discretizedWingAirfoilsCl = new ArrayList<List<Double>>();
		_discretizedWingAirfoilsCd = new ArrayList<List<Double>>();
		_discretizedWingAirfoilsCm = new ArrayList<List<Double>>();
		_discretizedHTailAirfoilsCl = new ArrayList<List<Double>>();
		_discretizedHTailAirfoilsCd = new ArrayList<List<Double>>();
		
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
				_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				- (
						_theAircraft.getWing().getXAcAirfoilVsY().get(0)*
						_theAircraft.getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(0).doubleValue(SI.METER)*
						Math.tan(_theAircraft.getWing().getRiggingAngle().doubleValue(SI.RADIAN))
						),
				SI.METER
				);
		
		//Horizontal and vertical distance
		_horizontalDistanceQuarterChordWingHTail = Amount.valueOf(
				(_theAircraft.getHTail().getXApexConstructionAxes().doubleValue(SI.METER)
						+ _theAircraft.getHTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(0).doubleValue(SI.METER)/4) - 
				(_theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER)
						+ _theAircraft.getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(0).doubleValue(SI.METER)/4),
				SI.METER
				);

		if ( (_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) 
				|| (_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) ) {
			
			this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
					_theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
					- this._zACRootWing.doubleValue(SI.METER),
					SI.METER
					);
			
		}

		if ( (_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 
				&& _theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ) 
				|| (_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 
						&& _theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ) ) { // different sides
			
			if(_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) < 0 ){
				
				this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
						_theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
						+ Math.abs(this._zACRootWing.doubleValue(SI.METER)),
						SI.METER
						);
				
			}

			if(_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) > 0 ){
				this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = Amount.valueOf(
						-( Math.abs(_theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER)) 
								+ this._zACRootWing.doubleValue(SI.METER)
								),
						SI.METER
						);	
			}
		}

		// the horizontal distance is always the same, the vertical changes in function of the angle of attack.
		
		if (_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				< _theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER)
				){

			_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = 
					Amount.valueOf(
							_verticalDistanceZeroLiftDirectionWingHTailPARTIAL.doubleValue(SI.METER) + (
									(_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
											Math.tan(_theAircraft.getWing().getRiggingAngle().doubleValue(SI.RADIAN) -
													_theAircraft.getWing()
														.getTheAerodynamicsCalculatorMap().get(_currentCondition)
															.getAlphaZeroLift().get(
																	_componentTaskList
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

		if (_theAircraft.getWing().getZApexConstructionAxes().doubleValue(SI.METER) 
				> _theAircraft.getHTail().getZApexConstructionAxes().doubleValue(SI.METER) 
				) {

			this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = Amount.valueOf(
					this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL.doubleValue(SI.METER) - (
							(this._horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) *
									Math.tan(_theAircraft.getWing().getRiggingAngle().doubleValue(SI.RADIAN) -
											_theAircraft.getWing()
											.getTheAerodynamicsCalculatorMap().get(_currentCondition)
												.getAlphaZeroLift().get(
														_componentTaskList
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
				Math.cos(_theAircraft.getWing().getRiggingAngle().doubleValue(SI.RADIAN) -
						_theAircraft.getWing()
						.getTheAerodynamicsCalculatorMap().get(_currentCondition)
							.getAlphaZeroLift().get(
									_componentTaskList
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
						_alphaBodyInitial.doubleValue(NonSI.DEGREE_ANGLE),
						_alphaBodyFinal.doubleValue(NonSI.DEGREE_ANGLE),
						_numberOfAlphasBody),
				NonSI.DEGREE_ANGLE
				);
		
		/////////////////////////////////////////////////////////////////////////////////////
		// ALPHA WING ARRAY CLEAN
		_alphaWingList = _alphaBodyList.stream()
				.map(x -> x.to(NonSI.DEGREE_ANGLE).plus(
						_theAircraft.getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE))
						)
				.collect(Collectors.toList());  
				
		/////////////////////////////////////////////////////////////////////////////////////
		// DOWNWASH ARRAY 
		//...................................................................................
		// ROSKAM (constant gradient)
		//...................................................................................		
		// calculate cl alpha at M=0
		Amount<Length> altitude = Amount.valueOf(0.0, SI.METER);
		if(_currentCondition.equals(ConditionEnum.TAKE_OFF))
			altitude = _theOperatingConditions.getAltitudeTakeOff();
		else if(_currentCondition.equals(ConditionEnum.CLIMB))
			altitude = _theOperatingConditions.getAltitudeClimb();
		else if(_currentCondition.equals(ConditionEnum.CRUISE))
			altitude = _theOperatingConditions.getAltitudeCruise();
		else if(_currentCondition.equals(ConditionEnum.LANDING))
			altitude = _theOperatingConditions.getAltitudeLanding();
		
		double cLAlphaMachZero = LiftCalc.calculateCLAlphaAtMachNasaBlackwell(
				_theAircraft.getWing().getSemiSpan(),
				_theAircraft.getWing().getSurface(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(_currentCondition).getYStationDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(_currentCondition).getChordDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(_currentCondition).getXLEDistribution(), 
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(_currentCondition).getDihedralDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(_currentCondition).getTwistDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(_currentCondition).getAlphaZeroLiftDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(_currentCondition).getVortexSemiSpanToSemiSpanRatio(),
				0.0,
				altitude
				);

		// Roskam method
		List<Double> downwashGradientConstantList = new ArrayList<>();
		for(int i=0; i<_alphaBodyList.size(); i++)
			downwashGradientConstantList.add(
					AerodynamicCalc.calculateDownwashRoskamWithMachEffect(
							_theAircraft.getWing().getAspectRatio(), 
							_theAircraft.getWing().getTaperRatioEquivalent(), 
							_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER) / _theAircraft.getWing().getSpan().doubleValue(SI.METER), 
							_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE.doubleValue(SI.METER) / _theAircraft.getWing().getSpan().doubleValue(SI.METER), 
							_theAircraft.getWing().getSweepQuarterChordEquivalent(),
							cLAlphaMachZero, 
							_theAircraft.getWing()
							.getTheAerodynamicsCalculatorMap()
								.get(_currentCondition)
									.getCLAlpha().get(
											_componentTaskList
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
				* _theAircraft.getWing()
					.getTheAerodynamicsCalculatorMap()
						.get(_currentCondition)
							.getAlphaZeroLift()
								.get(
										_componentTaskList
										.get(ComponentEnum.WING)
										.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
										).doubleValue(NonSI.DEGREE_ANGLE);
		
		List<Amount<Angle>> downwashAngleConstantList = new ArrayList<>();
		for (int i=0; i<this._numberOfAlphasBody; i++)
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
		for (int i=0; i<this._numberOfAlphasBody; i++){
			double cl = 
					_theAircraft.getWing()
						.getTheAerodynamicsCalculatorMap()
							.get(_currentCondition)
								.getCLAlpha()
									.get(
											_componentTaskList
											.get(ComponentEnum.WING)
											.get(AerodynamicAndStabilityEnum.CL_ALPHA)
											)
									.to(NonSI.DEGREE_ANGLE.inverse())
										.getEstimatedValue() 
					* _alphaWingList.get(i).doubleValue(NonSI.DEGREE_ANGLE) 
					+ _theAircraft.getWing()
						.getTheAerodynamicsCalculatorMap()
							.get(_currentCondition)
								.getCLZero()
									.get(
											_componentTaskList
											.get(ComponentEnum.WING)
											.get(AerodynamicAndStabilityEnum.CL_ZERO)
											);
			
			downwashAngleConstantList.clear();
			downwashAngleConstantList.add(
					AerodynamicCalc.calculateDownwashAngleLinearSlingerland(
							_horizontalDistanceQuarterChordWingHTail.doubleValue(SI.METER), 
							_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE.doubleValue(SI.METER), 
							cl, 
							_theAircraft.getWing().getSweepQuarterChordEquivalent(),
							_theAircraft.getWing().getAspectRatio(), 
							_theAircraft.getWing().getSemiSpan()
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
						_theAircraft.getWing().getAspectRatio(),
						_theAircraft.getWing().getTaperRatioEquivalent(),
						_theAircraft.getWing().getZApexConstructionAxes(),
						_theAircraft.getHTail().getZApexConstructionAxes(),
						_theAircraft.getWing().getRiggingAngle(),
						_theAircraft.getWing()
						.getTheAerodynamicsCalculatorMap()
							.get(_currentCondition)
								.getAlphaZeroLift()
									.get(
											_componentTaskList
											.get(ComponentEnum.WING)
											.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
											),
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						_theAircraft.getWing().getSweepQuarterChordEquivalent(),
						cLAlphaMachZero,
						_theAircraft.getWing()
						.getTheAerodynamicsCalculatorMap()
							.get(_currentCondition)
								.getCLAlpha()
									.get(
											_componentTaskList
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
						_theAircraft.getWing().getRiggingAngle(),
						_theAircraft.getWing()
						.getTheAerodynamicsCalculatorMap()
							.get(_currentCondition)
								.getAlphaZeroLift()
									.get(
											_componentTaskList
											.get(ComponentEnum.WING)
											.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
											)
						)
				);
		
		_verticalDistanceZeroLiftDirectionWingHTailVariable.put(
				MethodEnum.ROSKAM,
				AerodynamicCalc.calculateVortexPlaneHorizontalTailVerticalDistance(
						_theAircraft.getWing().getRiggingAngle(),
						_theAircraft.getWing()
						.getTheAerodynamicsCalculatorMap()
							.get(_currentCondition)
								.getAlphaZeroLift()
									.get(
											_componentTaskList
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
						_theAircraft.getWing().getRiggingAngle(),
						_theAircraft.getWing().getZApexConstructionAxes(),
						_theAircraft.getHTail().getZApexConstructionAxes(),
						_theAircraft.getWing()
							.getTheAerodynamicsCalculatorMap()
								.get(_currentCondition)
									.getAlphaZeroLift()
										.get(
												_componentTaskList
												.get(ComponentEnum.WING)
												.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
												),
						_theAircraft.getWing().getSweepQuarterChordEquivalent(),
						_theAircraft.getWing().getAspectRatio(),
						_theAircraft.getWing().getSemiSpan(), 
						_horizontalDistanceQuarterChordWingHTail,
						_verticalDistanceZeroLiftDirectionWingHTailPARTIAL, 
						MyArrayUtils.convertToDoublePrimitive(
								_theAircraft.getWing()
								.getTheAerodynamicsCalculatorMap()
									.get(_currentCondition)
										.getLiftCoefficient3DCurve()
											.get(
													_componentTaskList
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
						_theAircraft.getWing().getRiggingAngle(),
						_theAircraft.getWing()
						.getTheAerodynamicsCalculatorMap()
							.get(_currentCondition)
								.getAlphaZeroLift()
									.get(
											_componentTaskList
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
		if (_downwashConstant == Boolean.TRUE){
			_alphaHTailList = new ArrayList<>();
			for (int i=0; i<_numberOfAlphasBody; i++){
				_alphaHTailList.add(
						Amount.valueOf(
								_alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
								- _downwashAngleMap
									.get(Boolean.TRUE)
										.get(
												_componentTaskList
												.get(ComponentEnum.AIRCRAFT)
												.get(AerodynamicAndStabilityEnum.DOWNWASH)
												)
											.get(i)
												.doubleValue(NonSI.DEGREE_ANGLE)
								+ _theAircraft.getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
								NonSI.DEGREE_ANGLE
								)
						);
			}
		}
		if (_downwashConstant == Boolean.FALSE){
			_alphaHTailList = new ArrayList<>();
			for (int i=0; i<_numberOfAlphasBody; i++){
				_alphaHTailList.add(
						Amount.valueOf(
								_alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
								- _downwashAngleMap
									.get(Boolean.FALSE)
										.get(
												_componentTaskList
												.get(ComponentEnum.AIRCRAFT)
												.get(AerodynamicAndStabilityEnum.DOWNWASH)
												)
											.get(i)
												.doubleValue(NonSI.DEGREE_ANGLE)
								+ _theAircraft.getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
								NonSI.DEGREE_ANGLE
								)
						);
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////
		// BETA ARRAY
		_betaList = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						_betaInitial.doubleValue(NonSI.DEGREE_ANGLE),
						_betaFinal.doubleValue(NonSI.DEGREE_ANGLE),
						_numberOfBeta),
				NonSI.DEGREE_ANGLE
				);
	}
	
	private void calculateComponentsData() {
		
		// TODO : FILL ME !!
		/*
		 * THIS WILL CREATE ALL THE COMPONENTS MANAGERS 
		 * AND RUN ALL THE COMPONENTS CALCULATIONS
		 */
		

		
	}
	

	
	public void calculate() {
		
		initializeAnalysis();
	
		
		// TODO : FILL ME !!
		/*
		 * CREATE INNER CLASSES FOR EACH "AIRCRAFT" ANALYSIS
		 * AND CALL THEM HERE IF REQUIRED BY THE TASK MAP 
		 */
		
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
	// Longitudinal Stability INNER CLASS
	//............................................................................
	public class CalcLongitudinalStability {
		
		public void fromForceBalanceEquation() {
			
		//=======================================================================================
		// Calculating moment coefficient with delta e deflections... CM
		//=======================================================================================
			
			//CONTINUE HERE ----------
			
//		_xCGAircraft.stream().forEach(xcg -> {
//			
//			Map<Amount<Angle>, List<Double>> momentMap = new HashMap<>();
//			_deltaElevatorList.stream().forEach( de -> 
//			momentMap.put(
//					de,
//					MomentCalc.METODO
//					)
//					);
//			_totalMomentCoefficient.put(
//					xcg,
//					momentMap
//					);
//			
//		});
			
		
		
		
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
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaFuselage(
											_theAircraft.getFuselage().getFusDesDatabaseReader(), 
											_theAircraft.getWing().getVEDSCDatabaseReader(),
											_theAircraft.getFuselage().getFuselageCreator().getLambdaF(), 
											_theAircraft.getFuselage().getFuselageCreator().getLambdaN(), 
											_theAircraft.getFuselage().getFuselageCreator().getLambdaT(), 
											((x*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
													+ _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
													+ _theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER))
											/_theAircraft.getFuselage().getLength().doubleValue(SI.METER),
											_theAircraft.getFuselage().getFuselageCreator().getEquivalentDiameterGM(),
											_theAircraft.getWing().getSurface(),
											_theAircraft.getWing().getSpan(),
											_theAircraft.getVTail().getSpan(),
											Amount.valueOf(
													_theAircraft.getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAircraft.getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER)
															),
													SI.METER
													), 
											_theAircraft.getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
											/(_theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2),
											_theAircraft.getHTail().getPositionRelativeToAttachment(),
											_theAircraft.getVTail().getAspectRatio(),
											_theAircraft.getWing().getPositionRelativeToAttachment()
											)
									)
							)
					.collect(Collectors.toList())
					);
			
			
			
			_cNbWing.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaWing(_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord())
									)
							)
					.collect(Collectors.toList())
					);
			
			 
			_cNbVertical.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNbetaVerticalTail(
											_theAircraft.getWing().getAspectRatio(), 
											_theAircraft.getVTail().getAspectRatio(), 
											Math.abs(
													(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAircraft.getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER))
													- ((x*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
															+ _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
															+ _theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER))
													),
											_theAircraft.getWing().getSpan().doubleValue(SI.METER),
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_theAircraft.getVTail().getSurface().doubleValue(SI.SQUARE_METRE),
											_theAircraft.getVTail().getLiftingSurfaceCreator().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMeanAirfoil().getAirfoilCreator().getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
											mach, 
											_theAircraft.getVTail().getVEDSCDatabaseReader().get_KFv_vs_bv_over_dfv(
													_theAircraft.getVTail().getSpan().doubleValue(SI.METER), 
													_theAircraft.getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAircraft.getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER)
															), 
													_theAircraft.getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2)
													),
											_theAircraft.getVTail().getVEDSCDatabaseReader().get_KWv_vs_zw_over_rf(
													_theAircraft.getWing().getPositionRelativeToAttachment(),
													_theAircraft.getWing().getAspectRatio(),
													_theAircraft.getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2)),
											_theAircraft.getVTail().getVEDSCDatabaseReader().get_KHv_vs_zh_over_bv1(
													_theAircraft.getHTail().getPositionRelativeToAttachment(),
													_theAircraft.getVTail().getAspectRatio(),
													_theAircraft.getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2), 
													_theAircraft.getWing().getPositionRelativeToAttachment())
		
											)
									)
							).collect(Collectors.toList())
					);
		
			
			_cNbTotal.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									_cNbVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
										.get(_xCGAircraft.indexOf(x))
											._2()
									+ _cNbWing.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
										.get(_xCGAircraft.indexOf(x))
											._2()
									+ _cNbFuselage.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
										.get(_xCGAircraft.indexOf(x))
											._2()
									)
							).collect(Collectors.toList())
					);
			
			//=======================================================================================
			// Calculating control derivatives ...
			//=======================================================================================
			Map<Amount<Angle>, List<Tuple2<Double, Double>>> cNdrMap = new HashMap<>();
			
			_deltaRudderList.stream().forEach(
					dr -> cNdrMap.put(
							dr,
							_xCGAircraft.stream().map(
									x -> Tuple.of(
											x,
											MomentCalc.calcCNdr(
													_cNbVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
													.get(_xCGAircraft.indexOf(x))
													._2(),
													dr, 
													_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
													_theAircraft.getHTail().getAspectRatio(),
													_theAircraft.getWing().getAerodynamicDatabaseReader(), 
													_theAircraft.getWing().getHighLiftDatabaseReader()
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
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcNonLinearCNFuselage(
											_cNbFuselage.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
												.get(_xCGAircraft.indexOf(x))
													._2(),
											_betaList
											)
							)
						).collect(Collectors.toList())
					);
			
			_cNWing.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNWing(
											_cNbWing.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
												.get(_xCGAircraft.indexOf(x))
													._2(),
											_betaList
											)
							)
						).collect(Collectors.toList())
					);
			
			_cNVertical.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcNonLinearCNVTail(
											_theAircraft.getVTail().getAerodynamicDatabaseReader(),
											_theAircraft.getVTail().getSweepLEEquivalent(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
												.getMeanAirfoil().getAirfoilCreator().getThicknessToChordRatio(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
												.getMeanAirfoil().getAirfoilCreator().getFamily(),
											_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
												.get(_xCGAircraft.indexOf(x))
												._2(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLMax().get(
													_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)
													)*Math.cos(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStall().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
																	).doubleValue(SI.RADIAN)
															),
											_theAircraft.getVTail().getSurface(),
											_theAircraft.getWing().getSurface(),
											Amount.valueOf(
													Math.abs(
													(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAircraft.getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER))
													- ((x*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
															+ _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
															+ _theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER))
													),
													SI.METER),
											_theAircraft.getWing().getSpan(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStar().get(
													_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
													),
											_betaList
											)
							)
						).collect(Collectors.toList())
					);
			
			_cNTotal.put(
					MethodEnum.VEDSC_SIMPLIFIED_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcTotalCN(
											_cNFuselage.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
												.get(_xCGAircraft.indexOf(x))
													._2(),
											_cNWing.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
												.get(_xCGAircraft.indexOf(x))
													._2(),
											_cNVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
												.get(_xCGAircraft.indexOf(x))
													._2()
											)
							)
						).collect(Collectors.toList())
					);
			
			Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> cNDueToDeltaRudderMap = new HashMap<>();
			
			List<Double> tauRudderList = new ArrayList<>();
			if(_tauRudderFunction == null)
				_deltaRudderList.stream()
					.forEach(dr -> tauRudderList.add(
							StabilityCalculators.calculateTauIndex(
									_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
									_theAircraft.getVTail().getAspectRatio(),
									_theAircraft.getVTail().getAerodynamicDatabaseReader(), 
									_theAircraft.getVTail().getHighLiftDatabaseReader(), 
									dr
									)
							));
			else
				_deltaRudderList.stream()
					.forEach(dr -> tauRudderList.add(
							_tauRudderFunction.value(dr.doubleValue(NonSI.DEGREE_ANGLE))
							));
				
			_deltaRudderList.stream().forEach(
					dr -> cNDueToDeltaRudderMap.put(
							dr,
							_xCGAircraft.stream().map(
									x -> Tuple.of(
												x,
												MomentCalc.calcCNDueToDeltaRudder(
														_betaList,
														_cNVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(_xCGAircraft.indexOf(x))._2,
														_cNbVertical.get(MethodEnum.VEDSC_SIMPLIFIED_WING).get(_xCGAircraft.indexOf(x))._2,
														dr, 
														tauRudderList.get(_deltaRudderList.indexOf(dr))
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
			
			_xCGAircraft.stream().forEach(
					x -> betaOfEquilibriumListAtCG.put(
							x, 
							_deltaRudderList.stream().map(
									dr -> Tuple.of( 
											Amount.valueOf(
													MyMathUtils.getIntersectionXAndY(
															MyArrayUtils.convertListOfAmountTodoubleArray(_betaList),
															MyArrayUtils.convertToDoublePrimitive(
																	_cNDueToDeltaRudder.get(MethodEnum.VEDSC_SIMPLIFIED_WING)
																	.get(dr)
																	.get(_xCGAircraft.indexOf(x))
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
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaFuselage(
											_theAircraft.getFuselage().getFusDesDatabaseReader(), 
											_theAircraft.getWing().getVEDSCDatabaseReader(),
											_theAircraft.getFuselage().getFuselageCreator().getLambdaF(), 
											_theAircraft.getFuselage().getFuselageCreator().getLambdaN(), 
											_theAircraft.getFuselage().getFuselageCreator().getLambdaT(), 
											((x*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
													+ _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
													+ _theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER))
											/_theAircraft.getFuselage().getLength().doubleValue(SI.METER),
											_theAircraft.getFuselage().getFuselageCreator().getEquivalentDiameterGM(),
											_theAircraft.getWing().getSurface(),
											_theAircraft.getWing().getSpan(),
											_theAircraft.getVTail().getSpan(),
											Amount.valueOf(
													_theAircraft.getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAircraft.getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER)
															),
													SI.METER
													), 
											_theAircraft.getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
											/(_theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2),
											_theAircraft.getHTail().getPositionRelativeToAttachment(),
											_theAircraft.getVTail().getAspectRatio(),
											_theAircraft.getWing().getPositionRelativeToAttachment()
											)
									)
							)
					.collect(Collectors.toList())
					);
			
			
			
			_cNbWing.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNBetaWing(
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCurrentLiftCoefficient(),
											_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
											_theAircraft.getWing().getAspectRatio(),
											_liftingSurfaceAerodynamicManagers
												.get(ComponentEnum.WING)
													.getXacMRF().get(
															_componentTaskList
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
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNbetaVerticalTail(
											_theAircraft.getWing().getAspectRatio(), 
											_theAircraft.getVTail().getAspectRatio(), 
											Math.abs(
													(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAircraft.getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER))
													- ((x*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
															+ _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
															+ _theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER))
													),
											_theAircraft.getWing().getSpan().doubleValue(SI.METER),
											_theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE),
											_theAircraft.getVTail().getSurface().doubleValue(SI.SQUARE_METRE),
											_theAircraft.getVTail().getLiftingSurfaceCreator().getPanels().get(0).getSweepHalfChord().doubleValue(SI.RADIAN),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMeanAirfoil().getAirfoilCreator().getClAlphaLinearTrait().to(SI.RADIAN.inverse()).getEstimatedValue(),
											mach, 
											_theAircraft.getVTail().getVEDSCDatabaseReader().get_KFv_vs_bv_over_dfv(
													_theAircraft.getVTail().getSpan().doubleValue(SI.METER), 
													_theAircraft.getFuselage().getFuselageCreator().getEquivalentDiameterAtX(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAircraft.getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER)
															), 
													_theAircraft.getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2)
													),
											_theAircraft.getVTail().getVEDSCDatabaseReader().get_KWv_vs_zw_over_rf(
													_theAircraft.getWing().getPositionRelativeToAttachment(),
													_theAircraft.getWing().getAspectRatio(),
													_theAircraft.getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2)),
											_theAircraft.getVTail().getVEDSCDatabaseReader().get_KHv_vs_zh_over_bv1(
													_theAircraft.getHTail().getPositionRelativeToAttachment(),
													_theAircraft.getVTail().getAspectRatio(),
													_theAircraft.getFuselage().getFuselageCreator().getHeightT().doubleValue(SI.METER)
													/(_theAircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight().doubleValue(SI.METER)/2), 
													_theAircraft.getWing().getPositionRelativeToAttachment())

											)
									)
							).collect(Collectors.toList())
					);

			
			_cNbTotal.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
										.get(_xCGAircraft.indexOf(x))
											._2()
									+ _cNbWing.get(MethodEnum.VEDSC_USAFDATCOM_WING)
										.get(_xCGAircraft.indexOf(x))
											._2()
									+ _cNbFuselage.get(MethodEnum.VEDSC_USAFDATCOM_WING)
										.get(_xCGAircraft.indexOf(x))
											._2()
									)
							).collect(Collectors.toList())
					);
			
			//=======================================================================================
			// Calculating control derivatives ...
			//=======================================================================================
			Map<Amount<Angle>, List<Tuple2<Double, Double>>> cNdrMap = new HashMap<>();
			
			_deltaRudderList.stream().forEach(
					dr -> cNdrMap.put(
							dr,
							_xCGAircraft.stream().map(
									x -> Tuple.of(
											x,
											MomentCalc.calcCNdr(
													_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
													.get(_xCGAircraft.indexOf(x))
													._2(),
													dr, 
													_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
													_theAircraft.getHTail().getAspectRatio(),
													_theAircraft.getWing().getAerodynamicDatabaseReader(), 
													_theAircraft.getWing().getHighLiftDatabaseReader()
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
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcNonLinearCNFuselage(
											_cNbFuselage.get(MethodEnum.VEDSC_USAFDATCOM_WING)
												.get(_xCGAircraft.indexOf(x))
													._2(),
											_betaList
											)
							)
						).collect(Collectors.toList())
					);
			
			_cNWing.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcCNWing(
											_cNbWing.get(MethodEnum.VEDSC_USAFDATCOM_WING)
												.get(_xCGAircraft.indexOf(x))
													._2(),
											_betaList
											)
							)
						).collect(Collectors.toList())
					);
			
			_cNVertical.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcNonLinearCNVTail(
											_theAircraft.getVTail().getAerodynamicDatabaseReader(),
											_theAircraft.getVTail().getSweepLEEquivalent(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
												.getMeanAirfoil().getAirfoilCreator().getThicknessToChordRatio(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
												.getMeanAirfoil().getAirfoilCreator().getFamily(),
											_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
												.get(_xCGAircraft.indexOf(x))
												._2(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLMax().get(
													_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_MAX)
													)*Math.cos(
															_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStall().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
																	).doubleValue(SI.RADIAN)
															),
											_theAircraft.getVTail().getSurface(),
											_theAircraft.getWing().getSurface(),
											Amount.valueOf(
													Math.abs(
													(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
															.getXacLRF().get(
																	_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
																	).plus(_theAircraft.getVTail().getXApexConstructionAxes())
															.doubleValue(SI.METER))
													- ((x*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
															+ _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
															+ _theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER))
													),
													SI.METER),
											_theAircraft.getWing().getSpan(),
											_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStar().get(
													_componentTaskList.get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
													),
											_betaList
											)
							)
						).collect(Collectors.toList())
					);
			
			_cNTotal.put(
					MethodEnum.VEDSC_USAFDATCOM_WING,
					_xCGAircraft.stream().map(
							x -> Tuple.of(
									x,
									MomentCalc.calcTotalCN(
											_cNFuselage.get(MethodEnum.VEDSC_USAFDATCOM_WING)
												.get(_xCGAircraft.indexOf(x))
													._2(),
											_cNWing.get(MethodEnum.VEDSC_USAFDATCOM_WING)
												.get(_xCGAircraft.indexOf(x))
													._2(),
											_cNVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING)
												.get(_xCGAircraft.indexOf(x))
													._2()
											)
							)
						).collect(Collectors.toList())
					);
			
			Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> cNDueToDeltaRudderMap = new HashMap<>();
			
			List<Double> tauRudderList = new ArrayList<>();
			if(_tauRudderFunction == null)
				_deltaRudderList.stream()
					.forEach(dr -> tauRudderList.add(
							StabilityCalculators.calculateTauIndex(
									_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
									_theAircraft.getVTail().getAspectRatio(),
									_theAircraft.getVTail().getAerodynamicDatabaseReader(), 
									_theAircraft.getVTail().getHighLiftDatabaseReader(), 
									dr
									)
							));
			else
				_deltaRudderList.stream()
					.forEach(dr -> tauRudderList.add(
							_tauRudderFunction.value(dr.doubleValue(NonSI.DEGREE_ANGLE))
							));
				
			_deltaRudderList.stream().forEach(
					dr -> cNDueToDeltaRudderMap.put(
							dr,
							_xCGAircraft.stream().map(
									x -> Tuple.of(
												x,
												MomentCalc.calcCNDueToDeltaRudder(
														_betaList,
														_cNVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING).get(_xCGAircraft.indexOf(x))._2,
														_cNbVertical.get(MethodEnum.VEDSC_USAFDATCOM_WING).get(_xCGAircraft.indexOf(x))._2,
														dr, 
														tauRudderList.get(_deltaRudderList.indexOf(dr))
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
			
			_xCGAircraft.stream().forEach(
					x -> betaOfEquilibriumListAtCG.put(
							x, 
							_deltaRudderList.stream().map(
									dr -> Tuple.of( 
											Amount.valueOf(
													MyMathUtils.getIntersectionXAndY(
															MyArrayUtils.convertListOfAmountTodoubleArray(_betaList),
															MyArrayUtils.convertToDoublePrimitive(
																	_cNDueToDeltaRudder.get(MethodEnum.VEDSC_USAFDATCOM_WING)
																	.get(dr)
																	.get(_xCGAircraft.indexOf(x))
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
	public List<Amount<Angle>> getBetaList() {
		return _betaList;
	}

	public void setBetaList(List<Amount<Angle>> _betaList) {
		this._betaList = _betaList;
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

	public List<Double> getXCGAircraft() {
		return _xCGAircraft;
	}

	public void setXCGAircraft(List<Double> _xCGAircraft) {
		this._xCGAircraft = _xCGAircraft;
	}

	public Map<ComponentEnum, LSAerodynamicsManager> getLiftingSurfaceAerodynamicManagers() {
		return _liftingSurfaceAerodynamicManagers;
	}

	public void setLiftingSurfaceAerodynamicManagers(
			Map<ComponentEnum, LSAerodynamicsManager> _liftingSurfaceAerodynamicManagers) {
		this._liftingSurfaceAerodynamicManagers = _liftingSurfaceAerodynamicManagers;
	}

	public List<Amount<Angle>> getDeltaRudderList() {
		return _deltaRudderList;
	}

	public void setDeltaRudderList(List<Amount<Angle>> _deltaRudderList) {
		this._deltaRudderList = _deltaRudderList;
	}

	public Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public void setTheAircraft(Aircraft _theAircraft) {
		this._theAircraft = _theAircraft;
	}

	public Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> getComponentTaskList() {
		return _componentTaskList;
	}

	public void setComponentTaskList(Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> _componentTaskList) {
		this._componentTaskList = _componentTaskList;
	}

	public MyInterpolatingFunction getTauRudderFunction() {
		return _tauRudderFunction;
	}

	public void setTauRudderFunction(MyInterpolatingFunction _tauRudderFunction) {
		this._tauRudderFunction = _tauRudderFunction;
	}

	public ConditionEnum get_currentCondition() {
		return _currentCondition;
	}

	public void set_currentCondition(ConditionEnum _currentCondition) {
		this._currentCondition = _currentCondition;
	}
	
}
