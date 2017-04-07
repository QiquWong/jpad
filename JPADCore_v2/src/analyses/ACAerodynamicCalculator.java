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
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.nacelles.NacelleAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.AirfoilCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.aerodynamics.MomentCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
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
	//..............................................................................
	// FROM INPUT (Passed from XML file)
	private Map<ComponentEnum, Map<AerodynamicAndStabilityEnum, MethodEnum>> _componentTaskList;
	private List<Double> _xCGAircraft;
	private List<Double> _zCGAircraft;
	private Amount<Angle> _alphaBodyInitial;
	private Amount<Angle> _alphaBodyFinal;
	private int _numberOfAlphasBody;
	private int _wingNumberOfPointSemiSpanWise;
	private int _hTailNumberOfPointSemiSpanWise;
	private List<Amount<Angle>> _alphaWingForDistribution;
	private List<Amount<Angle>> _alphaHorizontalTailForDistribution;
	private Boolean _downwashConstant; // if TRUE--> constant, if FALSE--> variable
	private Double _dynamicPressureRatio;
	private MyInterpolatingFunction _tauElevatorFunction;
	private List<Amount<Angle>> _deltaElevatorList;
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
	private List<Tuple3<MethodEnum, Double, Double>> _cNbFuselage = new ArrayList<>();
	private List<Tuple3<MethodEnum, Double, Double>> _cNbVertical =new ArrayList<>();
	private List<Tuple3<MethodEnum, Double, Double>> _cNbWing = new ArrayList<>();
	private List<Tuple3<MethodEnum, Double, Double>> _cNbTotal =new ArrayList<>();
	private Map<Amount<Angle>, List<Tuple3<MethodEnum, Double, Double>>> _cNdr = new HashMap<>();
	
	
	// COMPLETE ME !!
	
	
	
	
	private void initializeAnalysis(ConditionEnum theCondition) {
		
		_componentTaskList = new HashMap<>();
		_downwashGradientMap = new HashMap<>();
		_downwashAngleMap = new HashMap<>();
		_verticalDistanceZeroLiftDirectionWingHTailVariable = new HashMap<>();
		_discretizedWingAirfoilsCl = new ArrayList<List<Double>>();
		_discretizedWingAirfoilsCd = new ArrayList<List<Double>>();
		_discretizedWingAirfoilsCm = new ArrayList<List<Double>>();
		_discretizedHTailAirfoilsCl = new ArrayList<List<Double>>();
		_discretizedHTailAirfoilsCd = new ArrayList<List<Double>>();
		
		calculateComponentsData(theCondition);
		
		initializeData(theCondition);
		initializeArrays(theCondition);
		
	}
	
	private void initializeData(ConditionEnum theCondition) {
		
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
														.getTheAerodynamicsCalculatorMap().get(theCondition)
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
											.getTheAerodynamicsCalculatorMap().get(theCondition)
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
						.getTheAerodynamicsCalculatorMap().get(theCondition)
							.getAlphaZeroLift().get(
									_componentTaskList
									.get(ComponentEnum.WING)
									.get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
									)
								.doubleValue(SI.RADIAN)
								),
				SI.METER);
	}
	
	private void initializeArrays(ConditionEnum theCondition) {
		
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
		if(theCondition.equals(ConditionEnum.TAKE_OFF))
			altitude = _theOperatingConditions.getAltitudeTakeOff();
		else if(theCondition.equals(ConditionEnum.CLIMB))
			altitude = _theOperatingConditions.getAltitudeClimb();
		else if(theCondition.equals(ConditionEnum.CRUISE))
			altitude = _theOperatingConditions.getAltitudeCruise();
		else if(theCondition.equals(ConditionEnum.LANDING))
			altitude = _theOperatingConditions.getAltitudeLanding();
		
		double cLAlphaMachZero = LiftCalc.calculateCLAlphaAtMachNasaBlackwell(
				_theAircraft.getWing().getSemiSpan(),
				_theAircraft.getWing().getSurface(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(theCondition).getYStationDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(theCondition).getChordDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(theCondition).getXLEDistribution(), 
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(theCondition).getDihedralDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(theCondition).getTwistDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(theCondition).getAlphaZeroLiftDistribution(),
				_theAircraft.getWing().getTheAerodynamicsCalculatorMap().get(theCondition).getVortexSemiSpanToSemiSpanRatio(),
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
								.get(theCondition)
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
						.get(theCondition)
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
							.get(theCondition)
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
							.get(theCondition)
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
						MyArrayUtils.convertFromDoublePrimitive(
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
							.get(theCondition)
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
							.get(theCondition)
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
							.get(theCondition)
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
							.get(theCondition)
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
								.get(theCondition)
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
									.get(theCondition)
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
						MyArrayUtils.convertFromDoublePrimitive(
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
							.get(theCondition)
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
	}
	
	private void calculateComponentsData(ConditionEnum theCondition) {
		
		// TODO : FILL ME !!
		/*
		 * THIS WILL CREATE ALL THE COMPONENTS MANAGERS 
		 * AND RUN ALL THE COMPONENTS CALCULATIONS
		 */
		

		
	}
	

	
	public void calculate(ConditionEnum theCondition) {
		
		initializeAnalysis(theCondition);
	
		
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
	// Directional Stability INNER CLASS
	//............................................................................
	public class CalcDirectionalStability {
		
		public void vedscSimplifiedWing(Double mach) {
			
			//=======================================================================================
			// Calculating stability derivatives for each component ...
			//=======================================================================================
			_xCGAircraft.stream().forEach(x -> 
				_cNbFuselage.add(
					Tuple.of(
							MethodEnum.VEDSC_SIMPLIFIED_WING,
							x,
							MomentCalc.calcCNBetaFuselage(
									_theAircraft.getFuselage().getFusDesDatabaseReader(), 
									_theAircraft.getFuselage().getFinesseRatio(), 
									_theAircraft.getFuselage().getFuselageCreator().getLambdaN(), 
									_theAircraft.getFuselage().getFuselageCreator().getLambdaT(), 
									((x*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
										+ _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
										+ _theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER))
									/_theAircraft.getFuselage().getLength().doubleValue(SI.METER)
									)
							)
						)
					);
			
			
			_xCGAircraft.stream().forEach(x -> 
			_cNbWing.add(
				Tuple.of(
						MethodEnum.VEDSC_SIMPLIFIED_WING,
						x,
						MomentCalc.calcCNBetaWing(_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord())))
				);
			
			_xCGAircraft.stream().forEach(x -> 
			_cNbVertical.add(
				Tuple.of(
						MethodEnum.VEDSC_SIMPLIFIED_WING,
						x,
						MomentCalc.calcCNbetaVerticalTail(
								_theAircraft.getWing().getAspectRatio(), 
								_theAircraft.getVTail().getAspectRatio(), 
								Math.abs(
										(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
										.get_xacLRF().get(
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
														.get_xacLRF().get(
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
					)
			);

			//=======================================================================================
			// Calculating control derivatives ...
			//=======================================================================================
			List<Tuple3<MethodEnum, Double, Double>> cNdrListAtCG = new ArrayList<>();
			
			_deltaElevatorList.stream().forEach(
					dr -> _cNdr.put(
							dr,
							_xCGAircraft.stream().map(
									x -> cNdrListAtCG.set(
											_xCGAircraft.indexOf(x),
											Tuple.of(
													MethodEnum.VEDSC_SIMPLIFIED_WING,
													x,
													MomentCalc.calcCNdr(
															_cNbVertical.get(_xCGAircraft.indexOf(x))._3(),
															dr, 
															_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
															_theAircraft.getHTail().getAspectRatio(),
															_theAircraft.getWing().getAerodynamicDatabaseReader(), 
															_theAircraft.getWing().getHighLiftDatabaseReader()
															)
													)
											)
									)
							.collect(Collectors.toList())
							)
					);			
			//=======================================================================================
			// Calculating yawing coefficient breakdown ...
			//=======================================================================================
			
			
			//=======================================================================================
			// Calculating dr_equilibrium for each beta ...
			//=======================================================================================
			
			
			
		}
		
		public void vedscUsafDatcomWing(Double mach) {
			
			//=======================================================================================
			// Calculating stability derivatives for each component ...
			//=======================================================================================
			_xCGAircraft.stream().forEach(x -> 
				_cNbFuselage.add(
					Tuple.of(
							MethodEnum.VEDSC_USAFDATCOM_WING,
							x,
							MomentCalc.calcCNBetaFuselage(
									_theAircraft.getFuselage().getFusDesDatabaseReader(), 
									_theAircraft.getFuselage().getFinesseRatio(), 
									_theAircraft.getFuselage().getFuselageCreator().getLambdaN(), 
									_theAircraft.getFuselage().getFuselageCreator().getLambdaT(), 
									((x*_theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER))
										+ _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER)
										+ _theAircraft.getWing().getXApexConstructionAxes().doubleValue(SI.METER))
									/_theAircraft.getFuselage().getLength().doubleValue(SI.METER)
									)
							)
						)
					);
			
			
			_xCGAircraft.stream().forEach(x -> 
			_cNbWing.add(
				Tuple.of(
						MethodEnum.VEDSC_USAFDATCOM_WING,
						x,
						MomentCalc.calcCNBetaWing(
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCurrentLiftCoefficient(),
								_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepQuarterChord(),
								_theAircraft.getWing().getEquivalentWing().getPanels().get(0).getAspectRatio(), 
								_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacMRF().get(
										_componentTaskList.get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
										), 
								x)
					)
				)
			);
			
			_xCGAircraft.stream().forEach(x -> 
			_cNbVertical.add(
				Tuple.of(
						MethodEnum.VEDSC_USAFDATCOM_WING,
						x,
						MomentCalc.calcCNbetaVerticalTail(
								_theAircraft.getWing().getAspectRatio(), 
								_theAircraft.getVTail().getAspectRatio(), 
								Math.abs(
										(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL)
										.get_xacLRF().get(
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
														.get_xacLRF().get(
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
					)
			);

			//=======================================================================================
			// Calculating control derivatives ...
			//=======================================================================================
			List<Tuple3<MethodEnum, Double, Double>> cNdrListAtCG = new ArrayList<>();
			
			_deltaElevatorList.stream().forEach(
					dr -> _cNdr.put(
							dr,
							_xCGAircraft.stream().map(
									x -> cNdrListAtCG.set(
											_xCGAircraft.indexOf(x),
											Tuple.of(
													MethodEnum.VEDSC_SIMPLIFIED_WING,
													x,
													MomentCalc.calcCNdr(
															_cNbVertical.get(_xCGAircraft.indexOf(x))._3(),
															dr, 
															_theAircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(0).getMeanChordRatio(),
															_theAircraft.getHTail().getAspectRatio(),
															_theAircraft.getWing().getAerodynamicDatabaseReader(), 
															_theAircraft.getWing().getHighLiftDatabaseReader()
															)
													)
											)
									)
							.collect(Collectors.toList())
							)
					);			
			
			//=======================================================================================
			// Calculating yawing coefficient breakdown ...
			//=======================================================================================
			
			
			//=======================================================================================
			// Calculating dr_equilibrium for each beta ...
			//=======================================================================================
			
			
		}
		
	}
	//............................................................................
	// END Directional Stability INNER CLASS
	//............................................................................
	
}
