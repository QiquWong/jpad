package analyses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.nacelles.NacelleAerodynamicsManager;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import javaslang.Tuple2;

public class ACAerodynamicAndStabilityManager_v2 {

	/*
	 * @author Vittorio Trifari, Manuela Ruocco, Agostino De Marco
	 */

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface;
	
	//..............................................................................
	// DERIVED INPUT	
	private double _currentMachNumber;
	private Amount<Length> _currentAltitude;
	private Amount<Angle> _alphaBodyCurrent;
	private Amount<Angle> _alphaCanardCurrent;
	private Amount<Angle> _alphaWingCurrent;
	private Amount<Angle> _alphaHTailCurrent;
	private Amount<Angle> _alphaNacelleCurrent;
	private Amount<Angle> _betaVTailCurrent;
	private List<Amount<Angle>> _deltaEForEquilibrium;
	private List<Amount<Angle>> _deltaRForEquilibrium;
	
	private List<Amount<Angle>> _alphaBodyList;
	private List<Amount<Angle>> _alphaWingList;
	private List<Amount<Angle>> _alphaHTailList;
	private List<Amount<Angle>> _alphaHTailListWithGroundEfffect;
	private List<Amount<Angle>> _alphaCanardList;
	private List<Amount<Angle>> _alphaNacelleList;
	private List<Amount<Angle>> _betaList;
	
	// for downwash estimation
	// TODO: CANARD?
	private Amount<Length> _zACRootWing;
	private Amount<Length> _horizontalDistanceQuarterChordWingHTail;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailPARTIAL;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE;
	
	//..............................................................................
	// INNER CALCULATORS
	private Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> _liftingSurfaceAerodynamicManagers;
	private Map<ComponentEnum, FuselageAerodynamicsManager> _fuselageAerodynamicManagers;
	private Map<ComponentEnum, NacelleAerodynamicsManager>_nacelleAerodynamicManagers;
	
	//..............................................................................
	// OUTPUT
	
	// downwash
	private Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Double>>>> _downwashGradientMap;
	private Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Amount<Angle>>>>> _downwashAngleMap;
	private Map<ComponentEnum, Map<MethodEnum, Map<Boolean, Double>>> _downwashGradientCurrent;
	private Map<ComponentEnum, Map<MethodEnum, Map<Boolean, Amount<Angle>>>> _downwashAngleCurrent;
	
	// total aircraft curves
	private List<Double> _current3DWingLiftCurve;
	private List<Double> _current3DWingPolarCurve;
	private List<Double> _current3DWingMomentCurve;
	private List<Double> _current3DCanardLiftCurve;
	private List<Double> _current3DCanardPolarCurve;
	private List<Double> _current3DCanardMomentCurve;
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailLiftCurve; //delta_e, CL
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailPolarCurve; //delta_e, CD
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailMomentCurve; //delta_e, CM
	private Map<Amount<Angle>, List<Double>> _totalLiftCoefficient; //delta_e, CL
	private Map<Amount<Angle>, List<Double>> _totalDragCoefficient; //delta_e, CD
	private Map<Double, Map<Amount<Angle>, List<Double>>> _totalMomentCoefficient; //xcg, delta_e , CM
	private Map<Double, Map<ComponentEnum, List<Double>>> _momentCoefficientBreakDown; //xcg, component, CM
	
	// side force
	private Amount<?> _cYBetaWing;
	private Amount<?> _cYBetaFuselage;
	private Amount<?> _cYBetaHorizontal;
	private Amount<?> _cYBetaVertical;
	private Amount<?> _cYBetaTotal;
	private Amount<?> _cYDeltaR;
	private Map<Double, Amount<?>> _cYp; // xcg, cYp
	private Map<Double, Amount<?>> _cYr; // xcg, cYr
	
	// longitudinal static stability
	private Map<Double, List<Double>> _canardEquilibriumLiftCoefficient; //xcg, CLc_e
	private Map<Double, List<Double>> _canardEquilibriumDragCoefficient; // xcg, CDc_e
	private Map<Double, List<Double>> _wingEquilibriumLiftCoefficient; //xcg, CLw_e
	private Map<Double, List<Double>> _wingEquilibriumDragCoefficient; // xcg, CDw_e
	private Map<Double, List<Double>> _horizontalTailEquilibriumLiftCoefficient; //xcg, CLh_e
	private Map<Double, List<Double>> _horizontalTailEquilibriumDragCoefficient; // xcg, CDh_e
	private Map<Double, List<Double>> _totalEquilibriumLiftCoefficient; //xcg, CL_e
	private Map<Double, List<Double>> _totalEquilibriumDragCoefficient; //xcg, CD_e
	private Map<Double, List<Double>> _totalEquilibriumEfficiencyMap; // xcg, efficiency curve
	private Map<Double, List<Amount<Angle>>> _deltaEEquilibrium; //xcg, de_e
	private Map<Double, Double> _totalEquilibriumMaximumEfficiencyMap; // xcg, max efficiency 
	private Map<Double, Double> _neutralPointPositionMap; // xcg, N0
	private Map<Double, Double> _staticStabilityMarginMap; // xcg, SSM
	private Map<Double, Double> _maximumTrimmedLiftingCoefficientMap; // xcg, CLmax_trim
	private Map<Double, Amount<?>> _cLAlphaEquilibriumMap; // xcg, CL_alpha_e
	private Map<Double, Amount<?>> _cMAlphaEquilibriumMap; // xcg, CM_alpha_e
	
	// lateral static stability
	private Amount<?> _cRollBetaWingBody;
	private Amount<?> _cRollBetaHorizontal;
	private Map<Double, Amount<?>> _cRollBetaVertical;
	private Map<Double, Amount<?>> _cRollBetaTotal;
	private Amount<?> _cRollDeltaA;
	private Map<Double, Amount<?>> _cRollDeltaR;
	private Amount<?> _cRollpWingBody;
	private Amount<?> _cRollpHorizontal;
	private Amount<?> _cRollpVertical;
	private Amount<?> _cRollpTotal;
	private Amount<?> _cRollrWing;
	private Map<Double, Amount<?>> _cRollrVertical;
	private Map<Double, Amount<?>> _cRollrTotal;
	
	// directional static stability
	private List<Tuple2<Double, Double>> _cNBetaFuselage; // xcg, CNbf
	private List<Tuple2<Double, Double>> _cNBetaNacelles; // xcg, CNbnac
	private List<Tuple2<Double, Double>> _cNBetaVertical; // xcg, CNbv
	private List<Tuple2<Double, Double>> _cNBetaWing; // xcg, CNbw
	private List<Tuple2<Double, Double>> _cNBetaHTail; // xcg, CNbh
	private List<Tuple2<Double, Double>> _cNBetaTotal; // xcg, CNbTot 
	private Map<Amount<Angle>, List<Tuple2<Double, Double>>> _cNDeltaR = new HashMap<>(); // delta_r, xcg, CNdr
	private double _cNDeltaA;
	private List<Tuple2<Double, Double>> _cNpWing; // xcg, CNpw 
	private List<Tuple2<Double, Double>> _cNpVertical; // xcg, CNpv
	private List<Tuple2<Double, Double>> _cNpTotal; // xcg, CNpTot
	private List<Tuple2<Double, Double>> _cNrWing; // xcg, CNrw
	private List<Tuple2<Double, Double>> _cNrVertical; // xcg, CNrv
	private List<Tuple2<Double, Double>> _cNrTotal; // xcg, CNrTot
	private List<Tuple2<Double, List<Double>>> _cNFuselage; // xcg, CN_fus_vs_Beta
	private List<Tuple2<Double, List<Double>>> _cNNacelles; // xcg, CN_nacelle_vs_Beta
	private List<Tuple2<Double, List<Double>>> _cNVertical; // xcg, CN_vertical_vs_Beta
	private List<Tuple2<Double, List<Double>>> _cNWing; // xcg, CN_wing_vs_Beta
	private List<Tuple2<Double, List<Double>>> _cNHTail; // xcg, CN_hTail_vs_Beta
	private List<Tuple2<Double, List<Double>>> _cNTotal; // xcg, CN_total_vs_Beta
	private Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> _cNDueToDeltaRudder; // dr, xcg, CN_total_vs_Beta
	private Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>> _betaOfEquilibrium; // xcg, beta_eq, dr_eq
	private Map<Double, Amount<Angle>> _betaMaxOfEquilibrium; // xcg, beta_max_eq
	
	
}
