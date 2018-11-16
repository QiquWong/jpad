package analyses;

import java.io.File;
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

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.nacelles.NacelleAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.LiftCalc;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.AerodynamicAndStabilityPlotEnum;
import configuration.enumerations.AerodynamicAnlaysisApproachEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import javaslang.Tuple2;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

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
	private Amount<Length> _wingMomentumPole;
	private Amount<Length> _hTailMomentumPole;
	private Amount<Length> _vTailMomentumPole;
	private Amount<Length> _canardMomentumPole;
	private List<Amount<Angle>> _alphaBodyList;
	private List<Amount<Angle>> _alphaWingList;
	private List<Amount<Angle>> _alphaHTailList;
	private List<Amount<Angle>> _alphaCanardList;
	private List<Amount<Angle>> _alphaNacelleList;
	private List<Amount<Angle>> _betaList;
	private Amount<Angle> currentDownwashAngle;

	// for downwash estimation
	//	// TODO: CANARD?

	private Amount<Length> _horizontalDistanceSlingerland;
	private Amount<Length> _verticalDistanceSlingerland;
	
	// for downwash estimation
	private Amount<Length> _zACRootWing;
	private Amount<Length> _horizontalDistanceQuarterChordWingHTail;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailPARTIAL = null;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = null;
	private Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE;
	private Map<MethodEnum, List<Amount<Length>>> _verticalDistanceZeroLiftDirectionWingHTailVariable;

	//..............................................................................
	// INNER CALCULATORS
	private Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> _liftingSurfaceAerodynamicManagers;
	private Map<ComponentEnum, FuselageAerodynamicsManager> _fuselageAerodynamicManagers;
	private Map<ComponentEnum, NacelleAerodynamicsManager>_nacelleAerodynamicManagers;

	//..............................................................................
	// OUTPUT
	
	// fuselage effects on wing lift
	private Amount<?> _clAlphaWingFuselage;
	private Double _clZeroWingFuselage;
	private Double _clMaxWingFuselage;
	private Double _clStarWingFuselage;
	private Amount<Angle> _alphaStarWingFuselage;
	private Amount<Angle> _alphaStallWingFuselage;
	private Amount<Angle> _alphaZeroLiftWingFuselage;
	
	// downwash
	private Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Double>>>> _downwashGradientMap;
	private Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Amount<Angle>>>>> _downwashAngleMap;
	private Map<ComponentEnum, Map<MethodEnum, Map<Boolean, Double>>> _downwashGradientCurrent;
	private Map<ComponentEnum, Map<MethodEnum, Map<Boolean, Amount<Angle>>>> _downwashAngleCurrent;

	// total aircraft curves
	private List<Double> _current3DWingLiftCurve;
	private List<Double> _current3DWingMomentCurve;
	private Map<Amount<Angle>, List<Double>> _current3DCanardLiftCurve;
	private Map<Amount<Angle>, List<Double>> _current3DCanardMomentCurve;
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailLiftCurve; //delta_e, CL
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailMomentCurve; //delta_e, CM
	private Map<Amount<Angle>, List<Double>> _current3DVerticalTailLiftCurve; //delta_r CL
	private Map<Amount<Angle>, List<Double>> _totalLiftCoefficient; //delta_e, CL
	private Map<Amount<Angle>, List<Double>> _totalDragCoefficient; //delta_e, CD
	private Map<Double, Map<Amount<Angle>, List<Double>>> _totalMomentCoefficient; //xcg, delta_e , CM
	private Map<Double, Map<ComponentEnum, List<Double>>> _totalMomentCoefficientBreakDown; //xcg, component, CM

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
	private Map<Double, Double> _totalEquilibriumCurrentEfficiencyMap; // xcg, efficiency @ current CL
	private Map<Double, Double> _neutralPointPositionMap; // xcg, N0
	private Map<Double, Double> _staticStabilityMarginMap; // xcg, SSM
	private Map<Double, Double> _maximumTrimmedLiftingCoefficientMap; // xcg, CLmax_trim
	private Map<Double, Amount<?>> _cLAlphaEquilibriumMap; // xcg, CL_alpha_e
	private Map<Double, Double> _cLZeroEquilibriumMap; // xcg, CL_zero_e
	private Map<Double, Amount<?>> _cMAlphaEquilibriumMap; // xcg, CM_alpha_e
	private Map<Double, Double> _cDZeroTotalEquilibriumMap; // xcg, CD_zero_e
	private Double _deltaCDZeroLandingGear = 0.0;
	private Double _deltaCDZeroExcrescences = 0.0;
	private Double _deltaCDZeroInterferences = 0.0;
	private Double _deltaCDZeroCooling = 0.0;
	private Double _deltaCDZeroFlap = 0.0;
	private Double _deltaCLZeroFlap = 0.0;

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

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	public static ACAerodynamicAndStabilityManager_v2 importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			ConditionEnum theCondition
			) throws IOException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading aerodynamic and stability analysis data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");

		boolean readBalanceFromPreviousAnalysisFlag;

		String readBalanceFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@balance_from_previous_analysis");

		if(readBalanceFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readBalanceFromPreviousAnalysisFlag = true;
		else {
			readBalanceFromPreviousAnalysisFlag = false;
			if(theAircraft.getTheAnalysisManager().getTheAnalysisManagerInterface().isIterativeLoop() == true) {
				System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): IF THE ITERATIVE LOOP FLAG IS 'TRUE', THE 'balance_from_previous_analysis' FLAG MUST BE TRUE. TERMINATING ...");
				System.exit(1);
			}
		}

		//===============================================================
		// READING BALANCE DATA
		//===============================================================
		List<Double> xCGAdimensionalPositions = new ArrayList<>();
		List<Double> zCGAdimensionalPositions = new ArrayList<>();
		Amount<Length> xCGFuselage = null;
		Amount<Length> zCGFuselage = null;
		Amount<Length> xCGLandingGears = null;
		Amount<Length> zCGLandingGears = null;
		Amount<Length> xCGNacelles = null;
		Amount<Length> zCGNacelles = null;

		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the ACBalanceManager and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the ACBalanceManger and reads the input data from the xml.
		 */
		if(readBalanceFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheBalance() != null) {

					//---------------------------------------------------------------
					// XCG POSITIONS
					xCGAdimensionalPositions.add(theAircraft.getTheAnalysisManager().getTheBalance().getMaxForwardOperativeCG());
					xCGAdimensionalPositions.add(theAircraft.getTheAnalysisManager().getTheBalance().getMaxAftCG());

					//---------------------------------------------------------------
					// ZCG POSITIONS
					zCGAdimensionalPositions.add(theAircraft.getTheAnalysisManager().getTheBalance().getCGMaximumTakeOffMass().getZMAC());
					zCGAdimensionalPositions.add(theAircraft.getTheAnalysisManager().getTheBalance().getCGMaximumTakeOffMass().getZMAC());

					//---------------------------------------------------------------
					// XCG AND ZCG POSITIONS FUSELAGE
					if(theAircraft.getFuselage() != null) {
						xCGFuselage = theAircraft.getTheAnalysisManager().getTheBalance().getXCGMap().get(ComponentEnum.FUSELAGE).to(SI.METER);
						zCGFuselage = theAircraft.getTheAnalysisManager().getTheBalance().getZCGMap().get(ComponentEnum.FUSELAGE).to(SI.METER);
					}

					//---------------------------------------------------------------
					// XCG AND ZCG POSITIONS LANDING GEAR
					if(theAircraft.getLandingGears() != null) {
						xCGLandingGears = theAircraft.getTheAnalysisManager().getTheBalance().getXCGMap().get(ComponentEnum.LANDING_GEAR).to(SI.METER);
						zCGLandingGears = theAircraft.getTheAnalysisManager().getTheBalance().getZCGMap().get(ComponentEnum.LANDING_GEAR).to(SI.METER);
					}

					//---------------------------------------------------------------
					// XCG AND ZCG POSITIONS NACELLE
					if(theAircraft.getNacelles() != null) {
						xCGNacelles = theAircraft.getTheAnalysisManager().getTheBalance().getXCGMap().get(ComponentEnum.NACELLE).to(SI.METER);
						zCGNacelles = theAircraft.getTheAnalysisManager().getTheBalance().getXCGMap().get(ComponentEnum.NACELLE).to(SI.METER);
					}
				}
				else {
					System.err.println("WARNING!! THE BALANCE ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
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
			// XCG POSITIONS FUSELAGE
			String xCGFuselageProperty = reader.getXMLPropertyByPath("//balance/fuselage_dimensional_center_of_gravity_x_position");
			if(xCGFuselageProperty != null)
				xCGFuselage= reader.getXMLAmountLengthByPath("//balance/fuselage_dimensional_center_of_gravity_x_position");

			//---------------------------------------------------------------
			// ZCG POSITIONS FUSELAGE
			String zCGFuselageProperty = reader.getXMLPropertyByPath("//balance/fuselage_dimensional_center_of_gravity_z_position");
			if(zCGFuselageProperty != null)
				zCGFuselage= reader.getXMLAmountLengthByPath("//balance/fuselage_dimensional_center_of_gravity_z_position");

			//---------------------------------------------------------------
			// XCG POSITIONS LANDING GEAR
			String xCGLandingGearsProperty = reader.getXMLPropertyByPath("//balance/landing_gear_dimensional_center_of_gravity_x_position");
			if(xCGLandingGearsProperty != null)
				xCGLandingGears = reader.getXMLAmountLengthByPath("//balance/landing_gear_dimensional_center_of_gravity_x_position");

			//---------------------------------------------------------------
			// ZCG POSITIONS LANDING GEAR
			String zCGLandingGearsProperty = reader.getXMLPropertyByPath("//balance/landing_gear_dimensional_center_of_gravity_z_position");
			if(zCGLandingGearsProperty != null)
				zCGLandingGears = reader.getXMLAmountLengthByPath("//balance/landing_gear_dimensional_center_of_gravity_z_position");

			//---------------------------------------------------------------
			// XCG POSITIONS NACELLES
			String xCGNacellesProperty = reader.getXMLPropertyByPath("//balance/nacelle_dimensional_center_of_gravity_x_position");
			if(xCGNacellesProperty != null)
				xCGNacelles = reader.getXMLAmountLengthByPath("//balance/nacelle_dimensional_center_of_gravity_x_position");

			//---------------------------------------------------------------
			// ZCG POSITIONS NACELLES
			String zCGNacellesProperty = reader.getXMLPropertyByPath("//balance/nacelle_dimensional_center_of_gravity_z_position");
			if(zCGNacellesProperty != null)
				zCGNacelles = reader.getXMLAmountLengthByPath("//balance/nacelle_dimensional_center_of_gravity_z_position");
		}

		//===============================================================
		// READING GLOBAL DATA
		//===============================================================

		// initialization and default values ...
		Amount<Angle> alphaBodyInitial = null;
		Amount<Angle> alphaBodyFinal = null;
		int numberOfAlphaBody = 20;
		Amount<Angle> betaInitial = null;
		Amount<Angle> betaFinal = null;
		int numberOfBeta = 20;
		List<Amount<Angle>> alphaWingArrayForDistributions = new ArrayList<>();
		List<Amount<Angle>> alphaHorizontalTailArrayForDistributions = new ArrayList<>();
		List<Amount<Angle>> betaVerticalTailArrayForDistributions = new ArrayList<>();
		List<Amount<Angle>> alphaCanardArrayForDistributions = new ArrayList<>();
		int numberOfPointsSemispanwiseWing = 30;
		int numberOfPointsSemispanwiseHTail = 30;
		int numberOfPointsSemispanwiseVTail = 30;
		int numberOfPointsSemispanwiseCanard = 30;
		List<Amount<Angle>> deltaElevatorList = new ArrayList<>();
		List<Amount<Angle>> deltaRudderList = new ArrayList<>();
		List<Amount<Angle>> deltaCanardControlSurfaceList = new ArrayList<>();
		double adimensionalWingMomentumPole = 0.25;
		double adimensionalHTailMomentumPole = 0.25;
		double adimensionalVTailMomentumPole = 0.25;
		double adimensionalCanardMomentumPole = 0.25;
		double adimensionalFuselageMomentumPole = 0.5;
		double wingDynamicPressureRatio = 1.0;
		double horizontalTailDynamicPressureRatio = 1.0;
		double verticalTailDynamicPressureRatio = 1.0;

		MyInterpolatingFunction tauElevator = new MyInterpolatingFunction();
		List<Double> tauElevatorFunction = new ArrayList<>();
		List<Amount<Angle>> tauElevatorFunctionDeltaElevator = new ArrayList<>();

		MyInterpolatingFunction tauRudder = new MyInterpolatingFunction();
		List<Double> tauRudderFunction = new ArrayList<>();
		List<Amount<Angle>> tauRudderFunctionDeltaRudder = new ArrayList<>();

		MyInterpolatingFunction tauCanardControlSurface = new MyInterpolatingFunction();
		List<Double> tauCanardControlSurfaceFunction = new ArrayList<>();
		List<Amount<Angle>> tauCanardControlSurfaceFunctionDeltaElevator = new ArrayList<>();

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
		if(theAircraft.getWing()!=null) {
			String alphaWingArrayForDistributionsProperty = reader.getXMLPropertyByPath("//global_data/alpha_wing_array_for_distributions");
			if(alphaWingArrayForDistributionsProperty != null)
				alphaWingArrayForDistributions = reader.readArrayofAmountFromXML("//global_data/alpha_wing_array_for_distributions");
		}

		//---------------------------------------------------------------
		// ALPHA HORIZONTAL TAIL FOR DISTRIBUTION
		if(theAircraft.getHTail()!=null) {
			String alphaHorizontalTailArrayForDistributionsProperty = reader.getXMLPropertyByPath("//global_data/alpha_horizontal_tail_array_for_distributions");
			if(alphaHorizontalTailArrayForDistributionsProperty != null)
				alphaHorizontalTailArrayForDistributions = reader.readArrayofAmountFromXML("//global_data/alpha_horizontal_tail_array_for_distributions");
		}

		//---------------------------------------------------------------
		// BETA VERTICAL TAIL FOR DISTRIBUTION
		if(theAircraft.getVTail()!=null) {
			String betaVerticalTailArrayForDistributionsProperty = reader.getXMLPropertyByPath("//global_data/beta_vertical_tail_array_for_distributions");
			if(betaVerticalTailArrayForDistributionsProperty != null)
				betaVerticalTailArrayForDistributions = reader.readArrayofAmountFromXML("//global_data/beta_vertical_tail_array_for_distributions");
		}

		//---------------------------------------------------------------
		// ALPHA CANARD FOR DISTRIBUTION
		if(theAircraft.getCanard()!=null) {
			String alphaCanardArrayForDistributionsProperty = reader.getXMLPropertyByPath("//global_data/alpha_canard_array_for_distributions");
			if(alphaCanardArrayForDistributionsProperty != null)
				alphaCanardArrayForDistributions = reader.readArrayofAmountFromXML("//global_data/alpha_canard_array_for_distributions");
		}

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
		// NUMBER OF POINTS SEMISPANWISE CANARD
		String numberOfPointsSemispanwiseCanardProperty = reader.getXMLPropertyByPath("//global_data/number_of_points_semispawise_canard");
		if(numberOfPointsSemispanwiseCanardProperty != null)
			numberOfPointsSemispanwiseCanard = Integer.valueOf(numberOfPointsSemispanwiseCanardProperty);

		//---------------------------------------------------------------
		// DELTA ELEVATOR LIST
		String deltaElevatorListProperty = reader.getXMLPropertyByPath("//global_data/delta_elevator_array");
		if(deltaElevatorListProperty != null)
			deltaElevatorList = reader.readArrayofAmountFromXML("//global_data/delta_elevator_array");
		else
			deltaElevatorList.addAll(
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(-25.0, 5.0, 7),
							NonSI.DEGREE_ANGLE
							)
					);

		if(!deltaElevatorList.contains(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE)))
			deltaElevatorList.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));

		//---------------------------------------------------------------
		// DELTA RUDDER LIST
		String deltaRudderListProperty = reader.getXMLPropertyByPath("//global_data/delta_rudder_array");
		if(deltaRudderListProperty != null)
			deltaRudderList = reader.readArrayofAmountFromXML("//global_data/delta_rudder_array");
		else
			deltaRudderList.addAll(
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(5.0, 25.0, 5),
							NonSI.DEGREE_ANGLE
							)
					);

		if(!deltaRudderList.contains(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE)))
			deltaRudderList.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));

		//---------------------------------------------------------------
		// DELTA CANARD CONTROL SURFACE LIST
		String deltaCanardControlSurfaceListProperty = reader.getXMLPropertyByPath("//global_data/delta_canard_control_surface_array");
		if(deltaCanardControlSurfaceListProperty != null)
			deltaCanardControlSurfaceList = reader.readArrayofAmountFromXML("//global_data/delta_canard_control_surface_array");
		else
			deltaCanardControlSurfaceList.addAll(
					MyArrayUtils.convertDoubleArrayToListOfAmount(
							MyArrayUtils.linspace(5.0, 25.0, 5),
							NonSI.DEGREE_ANGLE
							)
					);

		if(!deltaCanardControlSurfaceList.contains(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE)))
			deltaCanardControlSurfaceList.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));	

		//---------------------------------------------------------------
		// WING MOMENTUM POLE
		if(theAircraft.getWing() != null) {
			String adimensionalWingMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_wing_momentum_pole");
			if(adimensionalWingMomentumPoleProperty != null)
				adimensionalWingMomentumPole = Double.valueOf(adimensionalWingMomentumPoleProperty);
		}

		//---------------------------------------------------------------
		// HTAIL MOMENTUM POLE
		if(theAircraft.getHTail() != null) {
			String adimensionalHTailMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_horizontal_tail_momentum_pole");
			if(adimensionalHTailMomentumPoleProperty != null)
				adimensionalHTailMomentumPole = Double.valueOf(adimensionalHTailMomentumPoleProperty);

		}
		//---------------------------------------------------------------
		// VTAIL MOMENTUM POLE
		if(theAircraft.getVTail() != null) {
			String adimensionalVTailMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_vertical_tail_momentum_pole");
			if(adimensionalVTailMomentumPoleProperty != null)
				adimensionalVTailMomentumPole = Double.valueOf(adimensionalVTailMomentumPoleProperty);

		}
		//---------------------------------------------------------------
		// CANARD MOMENTUM POLE
		if(theAircraft.getCanard() != null) {
			String adimensionalCanardMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_canard_momentum_pole");
			if(adimensionalCanardMomentumPoleProperty != null)
				adimensionalCanardMomentumPole = Double.valueOf(adimensionalCanardMomentumPoleProperty);

		}
		//---------------------------------------------------------------
		// FUSELAGE MOMENTUM POLE
		if(theAircraft.getFuselage() != null) {
			String adimensionalFuselageMomentumPoleProperty = reader.getXMLPropertyByPath("//global_data/adimensional_fuselage_momentum_pole");
			if(adimensionalFuselageMomentumPoleProperty != null)
				adimensionalFuselageMomentumPole = Double.valueOf(adimensionalFuselageMomentumPoleProperty);
		}

		//---------------------------------------------------------------
		// DYNAMIC PRESSURE RATIO WING
		String wingDynamicPressureRatioProperty = reader.getXMLPropertyByPath("//global_data/horizontal_tail_dynamic_pressure_ratio");
		if(wingDynamicPressureRatioProperty != null)
			wingDynamicPressureRatio = Double.valueOf(wingDynamicPressureRatioProperty);
		else
			horizontalTailDynamicPressureRatio = 1.0; //FIXME: ADD THE METHOD WHEN AVAILABLE ...

		//---------------------------------------------------------------
		// DYNAMIC PRESSURE RATIO HTAIL
		String horizontalTailDynamicPressureRatioProperty = reader.getXMLPropertyByPath("//global_data/horizontal_tail_dynamic_pressure_ratio");
		if(horizontalTailDynamicPressureRatioProperty != null)
			horizontalTailDynamicPressureRatio = Double.valueOf(horizontalTailDynamicPressureRatioProperty);
		else
			horizontalTailDynamicPressureRatio = 
			AerodynamicCalc.calculateHTailDynamicPressureRatio(
					theAircraft.getHTail().getPositionRelativeToAttachment()
					);

		//---------------------------------------------------------------
		// DYNAMIC PRESSURE RATIO VTAIL
		String verticalTailDynamicPressureRatioProperty = reader.getXMLPropertyByPath("//global_data/horizontal_tail_dynamic_pressure_ratio");
		if(verticalTailDynamicPressureRatioProperty != null)
			verticalTailDynamicPressureRatio = Double.valueOf(verticalTailDynamicPressureRatioProperty);
		else
			verticalTailDynamicPressureRatio = 
			AerodynamicCalc.calculateVTailDynamicPressureRatio(
					theAircraft.getVTail().getPositionRelativeToAttachment()
					);

		//---------------------------------------------------------------
		// TAU ELEVATOR
		if(theAircraft.getHTail()!= null) {
			String tauElevatorAssignedFromFileString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//tau_elevator_function/@assigned");

			if(tauElevatorAssignedFromFileString.equalsIgnoreCase("FALSE")){
				double[] deltaElevatorArray = MyArrayUtils.linspace(-25, 5, 31);
				for(int i=0; i<deltaElevatorArray.length; i++)
					tauElevatorFunction.add(
							LiftCalc.calculateTauIndexElevator(
									theAircraft.getHTail().getSymmetricFlaps().get(0).getMeanChordRatio(), 
									theAircraft.getHTail().getAspectRatio(), 
									theAircraft.getHTail().getHighLiftDatabaseReader(), 
									theAircraft.getHTail().getAeroDatabaseReader(), 
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
		}

		//---------------------------------------------------------------
		// TAU RUDDER
		if(theAircraft.getVTail()!= null) {
			String tauRudderAssignedFromFileString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//tau_rudder_function/@assigned");

			if(tauRudderAssignedFromFileString.equalsIgnoreCase("FALSE")){
				double[] deltaRudderArray = MyArrayUtils.linspace(-25, 25, 51);
				for(int i=0; i<deltaRudderArray.length; i++)
					tauRudderFunction.add(
							LiftCalc.calculateTauIndexElevator(
									theAircraft.getVTail().getSymmetricFlaps().get(0).getMeanChordRatio(), 
									theAircraft.getVTail().getAspectRatio(), 
									theAircraft.getVTail().getHighLiftDatabaseReader(), 
									theAircraft.getVTail().getAeroDatabaseReader(), 
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
		}

		//---------------------------------------------------------------
		// TAU CANARD CONTROL SURFACE
		if(theAircraft.getCanard()!= null) {
			String tauCanardControlSurfaceAssignedFromFileString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//tau_canard_control_surface_function/@assigned");

			if(tauCanardControlSurfaceAssignedFromFileString.equalsIgnoreCase("FALSE")){
				double[] deltaCanardControlSurfaceArray = MyArrayUtils.linspace(-25, 5, 31);
				for(int i=0; i<deltaCanardControlSurfaceArray.length; i++)
					tauCanardControlSurfaceFunction.add(
							LiftCalc.calculateTauIndexElevator(
									theAircraft.getCanard().getSymmetricFlaps().get(0).getMeanChordRatio(), 
									theAircraft.getCanard().getAspectRatio(), 
									theAircraft.getCanard().getHighLiftDatabaseReader(), 
									theAircraft.getCanard().getAeroDatabaseReader(), 
									Amount.valueOf(deltaCanardControlSurfaceArray[i], NonSI.DEGREE_ANGLE)
									)
							);

				tauCanardControlSurface.interpolateLinear(
						deltaCanardControlSurfaceArray,
						MyArrayUtils.convertToDoublePrimitive(tauCanardControlSurfaceFunction)
						);
			}
			else {
				String tauCanardControlSurfaceFunctionProperty = reader.getXMLPropertyByPath("//global_data/tau_canard_control_surface_function/tau");
				if(tauCanardControlSurfaceFunctionProperty != null)
					tauCanardControlSurfaceFunction = reader.readArrayDoubleFromXML("//global_data/tau_canard_control_surface_function/tau"); 
				String tauCanardControlSurfaceFunctionDeltaElevatorProperty = reader.getXMLPropertyByPath("//global_data/tau_canard_control_surface_function/delta_canard_control_surface");
				if(tauCanardControlSurfaceFunctionDeltaElevatorProperty != null)
					tauCanardControlSurfaceFunctionDeltaElevator = reader.readArrayofAmountFromXML("//global_data/tau_canard_control_surface_function/delta_canard_control_surface");

				if(tauCanardControlSurfaceFunction.size() > 1)
					if(tauCanardControlSurfaceFunction.size() != tauCanardControlSurfaceFunctionDeltaElevator.size())
					{
						System.err.println("TAU CANARD CONTROL SURFACE ARRAY AND THE RELATED DELTA ELEVATOR ARRAY MUST HAVE THE SAME LENGTH !");
						System.exit(1);
					}
				if(tauCanardControlSurfaceFunction.size() == 1) {
					tauCanardControlSurfaceFunction.add(tauCanardControlSurfaceFunction.get(0));
					tauCanardControlSurfaceFunctionDeltaElevator.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
					tauCanardControlSurfaceFunctionDeltaElevator.add(Amount.valueOf(360.0, NonSI.DEGREE_ANGLE));
				}

				tauCanardControlSurface.interpolateLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								tauCanardControlSurfaceFunctionDeltaElevator.stream()
								.map(f -> f.to(NonSI.DEGREE_ANGLE))
								.collect(Collectors.toList())
								),
						MyArrayUtils.convertToDoublePrimitive(tauCanardControlSurfaceFunction)
						);
			}
		}

		//===============================================================
		// READING COMPONENTS TASK LIST DATA
		//===============================================================		
		// WING:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> wingTaskList = new HashMap<>();
		boolean performWingAnalysis = false;
		AerodynamicAnlaysisApproachEnum wingAnalysisType = null;
		String wingAnalysisPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//components/wing/@perform");

		if(wingAnalysisPerformString != null) {
			if(wingAnalysisPerformString.equalsIgnoreCase("TRUE")) {

				performWingAnalysis = true;

				String wingAnalysisTypeString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//components/wing/@type");

				if(wingAnalysisTypeString != null) {

					wingAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(wingAnalysisTypeString);

					switch (wingAnalysisType) {
					case SEMIEMPIRICAL:
						wingTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, MethodEnum.KROO);
						wingTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, MethodEnum.NAPOLITANO_DATCOM);
						wingTaskList.put(AerodynamicAndStabilityEnum.CL_ALPHA, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.CL_ZERO, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, MethodEnum.INTEGRAL_MEAN_TWIST);
						wingTaskList.put(AerodynamicAndStabilityEnum.CL_STAR, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STAR, MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS);
						wingTaskList.put(AerodynamicAndStabilityEnum.CL_MAX, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STALL, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.CD0, MethodEnum.SEMIEMPIRICAL);
						wingTaskList.put(AerodynamicAndStabilityEnum.CD_WAVE, MethodEnum.LOCK_KORN_WITH_KROO);
						wingTaskList.put(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						wingTaskList.put(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						wingTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE, MethodEnum.NASA_BLACKWELL); // FIXME NEW METHOD ADDED...
						wingTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						wingTaskList.put(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, MethodEnum.SEMIEMPIRICAL);
						wingTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, MethodEnum.SEMIEMPIRICAL);
						wingTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT, MethodEnum.SEMIEMPIRICAL);
						break;
					case AVL:
						// TODO
						break;
					case KK32:
						// TODO
						break;
					default:
						System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): WING ANALYSIS TYPE NOT RECOGNIZED!");
						break;
					}

				}
			}
		}

		//...............................................................
		// HORIZONTAL TAIL:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> hTailTaskList = new HashMap<>();
		boolean performHTailAnalysis = false;
		AerodynamicAnlaysisApproachEnum hTailAnalysisType = null;
		String hTailAnalysisPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//components/horizontal_tail/@perform");

		if(hTailAnalysisPerformString != null) {
			if(hTailAnalysisPerformString.equalsIgnoreCase("TRUE")) {

				performHTailAnalysis = true;

				String hTailAnalysisTypeString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//components/horizontal_tail/@type");

				if(hTailAnalysisTypeString != null) {

					hTailAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(hTailAnalysisTypeString);

					switch (hTailAnalysisType) {
					case SEMIEMPIRICAL:
						hTailTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, MethodEnum.KROO);
						hTailTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, MethodEnum.NAPOLITANO_DATCOM); // FIXME DE-YOUNG HARPER??
						hTailTaskList.put(AerodynamicAndStabilityEnum.CL_ALPHA, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CL_ZERO, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, MethodEnum.INTEGRAL_MEAN_TWIST);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CL_STAR, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STAR, MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CL_MAX, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STALL, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CD0, MethodEnum.SEMIEMPIRICAL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CD_WAVE, MethodEnum.LOCK_KORN_WITH_KROO);
						hTailTaskList.put(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE, MethodEnum.NASA_BLACKWELL); // FIXME NEW METHOD ADDED...
						hTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						hTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						hTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, MethodEnum.SEMIEMPIRICAL);
						break;
					case AVL:
						// TODO
						break;
					case KK32:
						// TODO
						break;
					default:
						System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): HORIZONTAL TAIL ANALYSIS TYPE NOT RECOGNIZED!");
						break;
					}

				}
			}
		}

		//...............................................................
		// VERTICAL TAIL:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> vTailTaskList = new HashMap<>();
		boolean performVTailAnalysis = false;
		AerodynamicAnlaysisApproachEnum vTailAnalysisType = null;
		String vTailAnalysisPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//components/vertical_tail/@perform");

		if(vTailAnalysisPerformString != null) {
			if(vTailAnalysisPerformString.equalsIgnoreCase("TRUE")) {

				performVTailAnalysis = true;

				String vTailAnalysisTypeString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//components/vertical_tail/@type");

				if(vTailAnalysisTypeString != null) {

					vTailAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(vTailAnalysisTypeString);

					switch (vTailAnalysisType) {
					case SEMIEMPIRICAL:
						vTailTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, MethodEnum.KROO);
						vTailTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, MethodEnum.NAPOLITANO_DATCOM); // FIXME DE-YOUNG HARPER??
						vTailTaskList.put(AerodynamicAndStabilityEnum.CL_ALPHA, MethodEnum.HELMBOLD_DIEDERICH);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CL_ZERO, MethodEnum.NASA_BLACKWELL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, MethodEnum.INTEGRAL_MEAN_TWIST);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CL_STAR, MethodEnum.NASA_BLACKWELL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STAR, MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CL_MAX, MethodEnum.NASA_BLACKWELL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STALL, MethodEnum.NASA_BLACKWELL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.NASA_BLACKWELL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, MethodEnum.NASA_BLACKWELL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CD0, MethodEnum.SEMIEMPIRICAL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CD_WAVE, MethodEnum.LOCK_KORN_WITH_KROO);
						vTailTaskList.put(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE, MethodEnum.NASA_BLACKWELL); // FIXME NEW METHOD ADDED...
						vTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						vTailTaskList.put(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						break;
					case AVL:
						// TODO
						break;
					case KK32:
						// TODO
						break;
					default:
						System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): VERTICAL TAIL ANALYSIS TYPE NOT RECOGNIZED!");
						break;
					}

				}
			}
		}

		//...............................................................
		// CANARD:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> canardTaskList = new HashMap<>();
		boolean performCanardAnalysis = false;
		AerodynamicAnlaysisApproachEnum canardAnalysisType = null;
		String canardAnalysisPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//components/canard/@perform");

		if(canardAnalysisPerformString != null) {
			if(canardAnalysisPerformString.equalsIgnoreCase("TRUE")) {

				performCanardAnalysis = true;

				String canardAnalysisTypeString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//components/canard/@type");

				if(canardAnalysisTypeString != null) {

					canardAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(canardAnalysisTypeString);

					switch (canardAnalysisType) {
					case SEMIEMPIRICAL:
						canardTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, MethodEnum.KROO);
						canardTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, MethodEnum.NAPOLITANO_DATCOM); // FIXME DE-YOUNG HARPER??
						canardTaskList.put(AerodynamicAndStabilityEnum.CL_ALPHA, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.CL_ZERO, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT, MethodEnum.INTEGRAL_MEAN_TWIST);
						canardTaskList.put(AerodynamicAndStabilityEnum.CL_STAR, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STAR, MethodEnum.MEAN_AIRFOIL_INFLUENCE_AREAS);
						canardTaskList.put(AerodynamicAndStabilityEnum.CL_MAX, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.ALPHA_STALL, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.LIFT_CURVE_3D, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.CD0, MethodEnum.SEMIEMPIRICAL);
						canardTaskList.put(AerodynamicAndStabilityEnum.CD_WAVE, MethodEnum.LOCK_KORN_WITH_KROO);
						canardTaskList.put(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION, MethodEnum.NASA_BLACKWELL);
						canardTaskList.put(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						canardTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE, MethodEnum.NASA_BLACKWELL); // FIXME NEW METHOD ADDED...
						canardTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						canardTaskList.put(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE, MethodEnum.AIRFOIL_DISTRIBUTION);
						canardTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, MethodEnum.SEMIEMPIRICAL);
						break;
					case AVL:
						// TODO
						break;
					case KK32:
						// TODO
						break;
					default:
						System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): CANARD ANALYSIS TYPE NOT RECOGNIZED!");
						break;
					}

				}
			}
		}

		//...............................................................
		// FUSELAGE:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> fuselageTaskList = new HashMap<>();
		boolean performFuselageAnalysis = false;
		AerodynamicAnlaysisApproachEnum fuselageAnalysisType = null;
		String fuselageAnalysisPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//components/fuselage/@perform");

		if(fuselageAnalysisPerformString != null) {
			if(fuselageAnalysisPerformString.equalsIgnoreCase("TRUE")) {

				performFuselageAnalysis = true;

				String fuselageAnalysisTypeString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//components/fuselage/@type");

				if(fuselageAnalysisTypeString != null) {

					fuselageAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(fuselageAnalysisTypeString);

					switch (fuselageAnalysisType) {
					case SEMIEMPIRICAL:
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_PARASITE_FUSELAGE, MethodEnum.SEMIEMPIRICAL);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_BASE_FUSELAGE, MethodEnum.SEMIEMPIRICAL);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_UPSWEEP_FUSELAGE, MethodEnum.SEMIEMPIRICAL);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_WINDSHIELD_FUSELAGE, MethodEnum.SEMIEMPIRICAL);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CD0_TOTAL_FUSELAGE, MethodEnum.SEMIEMPIRICAL);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE, MethodEnum.SEMIEMPIRICAL);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE, MethodEnum.SEMIEMPIRICAL);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_FUSELAGE, MethodEnum.SEMIEMPIRICAL);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CM0_FUSELAGE, MethodEnum.FUSDES);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_FUSELAGE, MethodEnum.FUSDES);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE, MethodEnum.FUSDES);
						fuselageTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE, MethodEnum.FUSDES);
						break;
					case AVL:
						// TODO
						break;
					case KK32:
						// TODO
						break;
					default:
						System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): CANARD ANALYSIS TYPE NOT RECOGNIZED!");
						break;
					}

				}
			}
		}

		//...............................................................
		// NACELLES:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> nacellesTaskList = new HashMap<>();
		boolean performNacellesAnalysis = false;
		AerodynamicAnlaysisApproachEnum nacellesAnalysisType = null;
		String nacellesAnalysisPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//components/nacelles/@perform");

		if(nacellesAnalysisPerformString != null) {
			if(nacellesAnalysisPerformString.equalsIgnoreCase("TRUE")) {

				performNacellesAnalysis = true;

				String nacellesAnalysisTypeString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//components/nacelles/@type");

				if(nacellesAnalysisTypeString != null) {

					nacellesAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(nacellesAnalysisTypeString);

					switch (nacellesAnalysisType) {
					case SEMIEMPIRICAL:
						nacellesTaskList.put(AerodynamicAndStabilityEnum.CD0_PARASITE_NACELLE, MethodEnum.SEMIEMPIRICAL);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.CD0_BASE_NACELLE, MethodEnum.SEMIEMPIRICAL);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.CD0_TOTAL_NACELLE, MethodEnum.SEMIEMPIRICAL);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.CD_INDUCED_NACELLE, MethodEnum.SEMIEMPIRICAL);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE, MethodEnum.SEMIEMPIRICAL);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.POLAR_CURVE_3D_NACELLE, MethodEnum.SEMIEMPIRICAL);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.CM0_NACELLE, MethodEnum.MULTHOPP);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.CM_ALPHA_NACELLE, MethodEnum.MULTHOPP);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE, MethodEnum.MULTHOPP);
						nacellesTaskList.put(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE, MethodEnum.MULTHOPP);
						break;
					case AVL:
						// TODO
						break;
					case KK32:
						// TODO
						break;
					default:
						System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): CANARD ANALYSIS TYPE NOT RECOGNIZED!");
						break;
					}

				}
			}
		}

		//...............................................................
		// AIRCRAFT:
		//...............................................................
		Map<AerodynamicAndStabilityEnum, MethodEnum> aircraftTaskList = new HashMap<>();

		// downwash
		boolean canardWingDownwashConstantGradient = false;
		MethodEnum canardWingDownwashMethod = null;
		boolean wingHTailDownwashConstantGradient = true;
		MethodEnum wingHTailDownwashMethod = null;

		// total lift
		MethodEnum totalLiftCurveMethod = null;
		boolean fuselageEffectOnWingLiftCurve = true;
		double totalLiftCalibrationAlphaScaleFactor = 1.0;
		double totalLiftCalibrationCLScaleFactor = 1.0;

		// total drag
		MethodEnum aircraftTotalPolarCurveMethod = null;
		boolean calculateDeltaCD0LandingGears = false;
		boolean calculatedeltaCD0Excrescences = false;
		boolean calculateDeltaCD0Interferences = false;
		boolean calculateDeltaCD0Cooling = false;
		double deltaCD0Cooling = 0.0;
		double deltaCD0LandingGears = 0.0;
		double deltaCD0Excrescences = 0.0;
		double deltaCD0Interferences = 0.0;
		double kCD0Cooling = 1.0;
		double kCD0LandingGears = 1.0;
		double kCD0Excrescences = 1.0;
		double kCD0Interferences = 1.0;
		double totalDragCalibrationCLScaleFactor = 1.0;
		double totalDragCalibrationCDScaleFactor = 1.0;

		// total pitching moment
		MethodEnum aircraftTotalMomentCurveMethod = null;
		double totalMomentCalibrationAlphaScaleFactor = 1.0;
		double totalMomentCalibrationCMScaleFactor = 1.0;

		// longitudinal static stability
		MethodEnum aircraftLongitudinalStaticStabilityMethod = null;
		boolean aircraftWingPendularStability = true;

		// lateral static stability
		MethodEnum aircraftLateralStaticStabilityMethod = null;

		// directional static stability
		MethodEnum aircraftDirectionalStaticStabilityMethod = null;

		// directional static stability
		MethodEnum aircraftSideForceMethod = null;

		// directional static stability
		MethodEnum minimumUnstickSpeedMethod = null;

		//---------------------------------------------------------------
		// CANARD DOWNWASH
		boolean canardWingConstantGradientFlag = true;
		String canardWingDownwashConstantGradientString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/canard_wing_downwash/@constant_gradient");

		if(canardWingDownwashConstantGradientString != null) 
			if(canardWingDownwashConstantGradientString.equalsIgnoreCase("FALSE"))
				canardWingConstantGradientFlag = false;

		String canardWingDownwashMethodString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/canard_wing_downwash/@method");

		if(canardWingDownwashMethodString != null) {

			// FIXME : CANARD METHODS !!
			if(canardWingDownwashMethodString.equalsIgnoreCase("ROSKAM")) 
				canardWingDownwashMethod = MethodEnum.ROSKAM;

			// FIXME : CANARD METHODS !!
			if(canardWingDownwashMethodString.equalsIgnoreCase("SLINGERLAND")) 
				canardWingDownwashMethod = MethodEnum.SLINGERLAND;

			aircraftTaskList.put(AerodynamicAndStabilityEnum.CANARD_DOWNWASH, canardWingDownwashMethod);

		}

		//---------------------------------------------------------------
		// WING DOWNWASH
		boolean wingHTailConstantGradientFlag = true;
		String wingHTailDownwashConstantGradientString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/wing_hTail_downwash/@constant_gradient");

		if(wingHTailDownwashConstantGradientString != null) 
			if(wingHTailDownwashConstantGradientString.equalsIgnoreCase("FALSE"))
				wingHTailConstantGradientFlag = false;

		String wingHTailDownwashMethodString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/wing_hTail_downwash/@method");

		if(wingHTailDownwashMethodString != null) {

			if(wingHTailDownwashMethodString.equalsIgnoreCase("ROSKAM")) 
				wingHTailDownwashMethod = MethodEnum.ROSKAM;

			if(wingHTailDownwashMethodString.equalsIgnoreCase("SLINGERLAND")) 
				wingHTailDownwashMethod = MethodEnum.SLINGERLAND;

			aircraftTaskList.put(AerodynamicAndStabilityEnum.WING_DOWNWASH, wingHTailDownwashMethod);

		}

		//---------------------------------------------------------------
		// TOTAL LIFT CURVE
		String aircraftTotalLiftCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/total_lift_curve/@perform");

		if(aircraftTotalLiftCurvePerformString.equalsIgnoreCase("TRUE")){

			String aircraftFuselageEffectsOnWingLiftCurveString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_lift_curve/fuselage_effects/@include");

			if(aircraftFuselageEffectsOnWingLiftCurveString != null) {

				if(aircraftFuselageEffectsOnWingLiftCurveString.equalsIgnoreCase("FALSE"))
					fuselageEffectOnWingLiftCurve = false;

			}

			String aircraftTotalLiftCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_lift_curve/@method");

			if(aircraftTotalLiftCurveMethodString != null) {

				if(aircraftTotalLiftCurveMethodString.equalsIgnoreCase("FROM_BALANCE_EQUATION")) 
					totalLiftCurveMethod = MethodEnum.FROM_BALANCE_EQUATION;

				aircraftTaskList.put(AerodynamicAndStabilityEnum.CL_TOTAL, totalLiftCurveMethod);

			}

			String aircraftTotalLiftCurveCalibrationAlphaScaleFactorProperty = reader.getXMLPropertyByPath("//aircraft/total_lift_curve/calibration/scale_factor_alpha");
			if(aircraftTotalLiftCurveCalibrationAlphaScaleFactorProperty != null)
				totalLiftCalibrationAlphaScaleFactor = Double.valueOf(aircraftTotalLiftCurveCalibrationAlphaScaleFactorProperty);

			String aircraftTotalLiftCurveCalibrationCLScaleFactorProperty = reader.getXMLPropertyByPath("//aircraft/total_lift_curve/calibration/scale_factor_CL");
			if(aircraftTotalLiftCurveCalibrationCLScaleFactorProperty != null)
				totalLiftCalibrationCLScaleFactor = Double.valueOf(aircraftTotalLiftCurveCalibrationCLScaleFactorProperty);

		}

		//---------------------------------------------------------------
		// TOTAL POLAR CURVE
		String aircraftTotalPolarCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/total_drag_polar/@perform");

		if(aircraftTotalPolarCurvePerformString.equalsIgnoreCase("TRUE")){

			//DELTA CD miscellaneous
			
			//LANDING GEAR
			String aircraftCalculateDeltaCD0LandingGearsString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_drag_polar/delta_CD0_landing_gears/@calculate");

			if(aircraftCalculateDeltaCD0LandingGearsString != null) {

				if(aircraftCalculateDeltaCD0LandingGearsString.equalsIgnoreCase("FALSE")) {
					calculateDeltaCD0LandingGears = false;
					String aircraftDeltaCD0LandingGearsString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//aircraft/total_drag_polar/delta_CD0_landing_gears/@value");

					if(aircraftDeltaCD0LandingGearsString != null) 
						deltaCD0LandingGears = Double.valueOf(aircraftDeltaCD0LandingGearsString);
				}
				else { 
					calculateDeltaCD0LandingGears = true;

					// Delta landing gear drag will be calculated using the method in ACAerodynamicAndStabilityManager
					deltaCD0LandingGears = 0.0;
				}


				String aircraftKCD0LandingGearString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//aircraft/total_drag_polar/delta_CD0_landing_gears/@kFactor");
				
				kCD0Excrescences = Double.valueOf(aircraftKCD0LandingGearString);
			}
			
			//Excrescences
			String aircraftCalculateDeltaCD0ExcrescencesString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_drag_polar/delta_CD0_excrescences/@calculate");

			if(aircraftCalculateDeltaCD0ExcrescencesString != null) {

				if(aircraftCalculateDeltaCD0ExcrescencesString.equalsIgnoreCase("FALSE")) {
					calculatedeltaCD0Excrescences = false;
					String aircraftDeltaCD0ExcrescencesString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//aircraft/total_drag_polar/delta_CD0_excrescences/@value");

					if(aircraftDeltaCD0ExcrescencesString != null) 
						deltaCD0Excrescences = Double.valueOf(aircraftDeltaCD0ExcrescencesString);
				}
				else { 
					calculatedeltaCD0Excrescences = true;

					// Delta landing gear drag will be calculated using the method in ACAerodynamicAndStabilityManager
					deltaCD0Excrescences = 0.0;
				}

				String aircraftKCD0ExcrescencesString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//aircraft/total_drag_polar/delta_CD0_excrescences/@kFactor");
				
				kCD0Excrescences = Double.valueOf(aircraftKCD0ExcrescencesString);
			}
			
			//Interferences 
			String aircraftCalculateDeltaCD0InterferencesString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_drag_polar/delta_CD0_Interferences/@calculate");

			if(aircraftCalculateDeltaCD0InterferencesString != null) {

				if(aircraftCalculateDeltaCD0InterferencesString.equalsIgnoreCase("FALSE")) {
					calculateDeltaCD0Interferences = false;
					String aircraftDeltaCD0InterferencesString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//aircraft/total_drag_polar/delta_CD0_Interferences/@value");

					if(aircraftDeltaCD0InterferencesString != null) 
						deltaCD0Interferences = Double.valueOf(aircraftDeltaCD0InterferencesString);
				}
				else { 
					calculateDeltaCD0Interferences = true;

					// Delta landing gear drag will be calculated using the method in ACAerodynamicAndStabilityManager
					deltaCD0Interferences = 0.0;
				}

				String aircraftKCD0InterferencesString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//aircraft/total_drag_polar/delta_CD0_Interferences/@kFactor");
				
				kCD0Excrescences = Double.valueOf(aircraftKCD0InterferencesString);
				
			}
			
			//COOLING
			String aircraftCalculateDeltaCD0CoolingString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_drag_polar/delta_CD0_Cooling/@calculate");

			if(aircraftCalculateDeltaCD0CoolingString != null) {

				if(aircraftCalculateDeltaCD0CoolingString.equalsIgnoreCase("FALSE")) {
					calculateDeltaCD0Cooling = false;
					String aircraftDeltaCD0CoolingString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//aircraft/total_drag_polar/delta_CD0_Cooling/@value");

					if(aircraftDeltaCD0CoolingString != null) 
						deltaCD0Cooling = Double.valueOf(aircraftDeltaCD0CoolingString);
				}
				else { 
					calculateDeltaCD0Cooling = true;

					// Delta landing gear drag will be calculated using the method in ACAerodynamicAndStabilityManager
					deltaCD0Cooling = 0.0;
				}

				String aircraftKCD0CoolingString = MyXMLReaderUtils
						.getXMLPropertyByPath(
								reader.getXmlDoc(), reader.getXpath(),
								"//aircraft/total_drag_polar/delta_CD0_Cooling/@kFactor");
				
				kCD0Excrescences = Double.valueOf(aircraftKCD0CoolingString);
				
			}
			

			//--------------------------------
			String aircraftTotalPolarCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_drag_polar/@method");

			if(aircraftTotalPolarCurveMethodString != null) {

				// TODO: CHECK METHODS. SEE METHOD FOR TOTAL DRAG POLAR (--> MANUELA)
				if(aircraftTotalPolarCurveMethodString.equalsIgnoreCase("FROM_BALANCE_EQUATION")) 
					aircraftTotalPolarCurveMethod = MethodEnum.FROM_BALANCE_EQUATION;

				// TODO: CHECK METHODS. SEE METHOD FOR TOTAL DRAG POLAR (--> MANUELA)
				if(aircraftTotalPolarCurveMethodString.equalsIgnoreCase("ROSKAM")) 
					aircraftTotalPolarCurveMethod = MethodEnum.ROSKAM;

				aircraftTaskList.put(AerodynamicAndStabilityEnum.CD_TOTAL, aircraftTotalPolarCurveMethod);

			}

			String aircraftTotalDragCurveCalibrationCLScaleFactorProperty = reader.getXMLPropertyByPath("//aircraft/total_drag_curve/calibration/scale_factor_CL");
			if(aircraftTotalDragCurveCalibrationCLScaleFactorProperty != null)
				totalDragCalibrationCLScaleFactor = Double.valueOf(aircraftTotalDragCurveCalibrationCLScaleFactorProperty);

			String aircraftTotalDragCurveCalibrationCDScaleFactorProperty = reader.getXMLPropertyByPath("//aircraft/total_drag_curve/calibration/scale_factor_CD");
			if(aircraftTotalDragCurveCalibrationCDScaleFactorProperty != null)
				totalDragCalibrationCDScaleFactor = Double.valueOf(aircraftTotalDragCurveCalibrationCDScaleFactorProperty);

		}

		//---------------------------------------------------------------
		// SIDE FORCE
		String aircraftSideForcePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/side_force/@perform");

		if(aircraftSideForcePerformString.equalsIgnoreCase("TRUE")){

			String aircraftSideForceMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/side_force/@method");

			if(aircraftSideForceMethodString != null) {

				if(aircraftSideForceMethodString.equalsIgnoreCase("NAPOLITANO_DATCOM")) 
					aircraftSideForceMethod = MethodEnum.NAPOLITANO_DATCOM;

				aircraftTaskList.put(AerodynamicAndStabilityEnum.SIDE_FORCE, aircraftSideForceMethod);

			}
		}

		//---------------------------------------------------------------
		// TOTAL MOMENT CURVE
		String aircraftTotalMomentCurvePerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/total_moment_curve/@perform");

		if(aircraftTotalMomentCurvePerformString.equalsIgnoreCase("TRUE")){

			String aircraftTotalMomentCurveMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_moment_curve/@method");

			if(aircraftTotalMomentCurveMethodString != null) {

				if(aircraftTotalMomentCurveMethodString.equalsIgnoreCase("FROM_BALANCE_EQUATION")) 
					aircraftTotalMomentCurveMethod = MethodEnum.FROM_BALANCE_EQUATION;

				aircraftTaskList.put(AerodynamicAndStabilityEnum.CM_TOTAL, aircraftTotalMomentCurveMethod);

			}

			String aircraftTotalMomentCurveCalibrationAlphaScaleFactorProperty = reader.getXMLPropertyByPath("//aircraft/total_moment_curve/calibration/scale_factor_Alpha");
			if(aircraftTotalMomentCurveCalibrationAlphaScaleFactorProperty != null)
				totalMomentCalibrationAlphaScaleFactor = Double.valueOf(aircraftTotalMomentCurveCalibrationAlphaScaleFactorProperty);

			String aircraftTotalMomentCurveCalibrationCMScaleFactorProperty = reader.getXMLPropertyByPath("//aircraft/total_moment_curve/calibration/scale_factor_CM");
			if(aircraftTotalMomentCurveCalibrationCMScaleFactorProperty != null)
				totalMomentCalibrationCMScaleFactor = Double.valueOf(aircraftTotalMomentCurveCalibrationCMScaleFactorProperty);

		}

		//---------------------------------------------------------------
		// LONGITUDINAL STATIC STABILITY
		String aircraftLongitudinalStaticStabilityPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/longitudinal_static_stability/@perform");

		if(aircraftLongitudinalStaticStabilityPerformString.equalsIgnoreCase("TRUE")){

			String aircraftWingPendularStabilityString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/longitudinal_static_stability/@wing_pendular_stability_effect");

			if(aircraftWingPendularStabilityString != null) 
				if(aircraftWingPendularStabilityString.equalsIgnoreCase("FALSE"))
					aircraftWingPendularStability = false;

			String aircraftLongitudinalStaticStabilityMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/longitudinal_static_stability/@method");

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
						"//aircraft/directional_static_stability/@perform");

		if(aircraftDirectionalStaticStabilityPerformString.equalsIgnoreCase("TRUE")){

			String aircraftDirectionalStaticStabilityMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/directional_static_stability/@method");

			if(aircraftDirectionalStaticStabilityMethodString != null) {

				if(aircraftDirectionalStaticStabilityMethodString.equalsIgnoreCase("NAPOLITANO_DATCOM")) 
					aircraftDirectionalStaticStabilityMethod = MethodEnum.NAPOLITANO_DATCOM;

				if(aircraftDirectionalStaticStabilityMethodString.equalsIgnoreCase("VEDSC_SIMPLIFIED_WING")) 
					aircraftDirectionalStaticStabilityMethod = MethodEnum.VEDSC_SIMPLIFIED_WING;

				if(aircraftDirectionalStaticStabilityMethodString.equalsIgnoreCase("VEDSC_USAFDATCOM_WING")) 
					aircraftDirectionalStaticStabilityMethod = MethodEnum.VEDSC_USAFDATCOM_WING;

				aircraftTaskList.put(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY, aircraftDirectionalStaticStabilityMethod);

			}
		}

		//---------------------------------------------------------------
		// LATERAL STATIC STABILITY
		String aircraftLateralStaticStabilityPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft/lateral_static_stability/@perform");

		if(aircraftLateralStaticStabilityPerformString.equalsIgnoreCase("TRUE")){

			String aircraftLateralStaticStabilityMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/lateral_static_stability/@method");

			if(aircraftLateralStaticStabilityMethodString != null) {

				if(aircraftLateralStaticStabilityMethodString.equalsIgnoreCase("NAPOLITANO_DATCOM")) 
					aircraftLateralStaticStabilityMethod = MethodEnum.NAPOLITANO_DATCOM;

				aircraftTaskList.put(AerodynamicAndStabilityEnum.LATERAL_STABILITY, aircraftLateralStaticStabilityMethod);

			}
		}

		//----------------------------------------------------------------
		// MINIMUM UNSTICK SPEED
		String minimumUnstickSpeedPerformString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//aircraft_analyses/VMU/@perform");

		if(minimumUnstickSpeedPerformString.equalsIgnoreCase("TRUE") && theCondition == ConditionEnum.TAKE_OFF){

			String minimumUnstickSpeedMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft_analyses/VMU/@method");

			if(minimumUnstickSpeedMethodString != null) {

				if(minimumUnstickSpeedMethodString.equalsIgnoreCase("DATCOM_VMU_FIRST_METHOD")) 
					minimumUnstickSpeedMethod = MethodEnum.DATCOM_VMU_FIRST_METHOD;

				if(minimumUnstickSpeedMethodString.equalsIgnoreCase("DATCOM_VMU_SECOND_METHOD")) 
					minimumUnstickSpeedMethod = MethodEnum.DATCOM_VMU_SECOND_METHOD;

				aircraftTaskList.put(AerodynamicAndStabilityEnum.MINIMUM_UNSTICK_SPEED, minimumUnstickSpeedMethod);

				aircraftTaskList.put(AerodynamicAndStabilityEnum.MINIMUM_UNSTICK_SPEED, minimumUnstickSpeedMethod);

			}
		}

		//===============================================================
		// READING PLOT DATA
		//===============================================================
		Map<ComponentEnum, List<AerodynamicAndStabilityPlotEnum>> plotMap = new HashMap<>();
		List<AerodynamicAndStabilityPlotEnum> wingPlotList = new ArrayList<>();
		List<AerodynamicAndStabilityPlotEnum> hTailPlotList = new ArrayList<>();
		List<AerodynamicAndStabilityPlotEnum> vTailPlotList = new ArrayList<>();
		List<AerodynamicAndStabilityPlotEnum> canardPlotList = new ArrayList<>();
		List<AerodynamicAndStabilityPlotEnum> fuselagePlotList = new ArrayList<>();
		List<AerodynamicAndStabilityPlotEnum> nacellesPlotList = new ArrayList<>();
		List<AerodynamicAndStabilityPlotEnum> aircraftPlotList = new ArrayList<>();
		boolean createWingPlot = false;
		boolean createHTailPlot = false;
		boolean createVTailPlot = false;
		boolean createCanardPlot = false;
		boolean createFuselagePlot = false;
		boolean createNacellesPlot = false;
		boolean createAircraftPlot = false;

		//...............................................................
		// WING:
		String createWingPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/wing/@perform");
		if(createWingPlotProperty != null)
			createWingPlot = true;

		if(theAircraft.getWing() != null) {
			if(createWingPlot == true) {
				if(performWingAnalysis == true) {

					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_LIFT_CURVE_CLEAN);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_STALL_PATH);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_CL_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_CL_ADDITIONAL_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_CL_BASIC_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_cCL_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_cCL_ADDITIONAL_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_cCL_BASIC_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_GAMMA_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_GAMMA_ADDITIONAL_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_GAMMA_BASIC_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_TOTAL_LOAD_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_ADDITIONAL_LOAD_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_BASIC_LOAD_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_LIFT_CURVE_HIGH_LIFT);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_DRAG_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_CD_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_CD_PARASITE_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_CD_INDUCED_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_MOMENT_CURVE_CLEAN);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_MOMENT_DISTRIBUTION);
					wingPlotList.add(AerodynamicAndStabilityPlotEnum.WING_CM_DISTRIBUTION);

					plotMap.put(ComponentEnum.WING, wingPlotList);

				}
				else 
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'WING' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
			}
		}
		//...............................................................
		// HTAIL:
		String createHTailPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/@perform");
		if(createHTailPlotProperty != null)
			createHTailPlot = true;

		if(theAircraft.getHTail() != null) {
			if(createHTailPlot == true) {
				if(performHTailAnalysis == true) {

					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_LIFT_CURVE_CLEAN);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_STALL_PATH);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_CL_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_cCL_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_GAMMA_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_TOTAL_LOAD_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_LIFT_CURVE_ELEVATOR);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_CD_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_CD_PARASITE_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_CD_INDUCED_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_DRAG_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_MOMENT_CURVE_CLEAN);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_MOMENT_DISTRIBUTION);
					hTailPlotList.add(AerodynamicAndStabilityPlotEnum.HTAIL_CM_DISTRIBUTION);

					plotMap.put(ComponentEnum.HORIZONTAL_TAIL, hTailPlotList);

				}
				else 
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'HORIZONTAL TAIL' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
			}
		}

		//...............................................................
		// CANARD:
		String createCanardPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/canard/@perform");
		if(createCanardPlotProperty != null)
			createCanardPlot = true;

		if(theAircraft.getCanard() != null) {
			if(createCanardPlot == true) {
				if(performCanardAnalysis == true) {

					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_LIFT_CURVE_CLEAN);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_STALL_PATH);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_CL_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_cCL_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_GAMMA_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_TOTAL_LOAD_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_LIFT_CURVE_CONTROL_SURFACE);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_CD_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_CD_PARASITE_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_CD_INDUCED_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_DRAG_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_MOMENT_CURVE_CLEAN);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_MOMENT_DISTRIBUTION);
					canardPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_CM_DISTRIBUTION);

					plotMap.put(ComponentEnum.CANARD, canardPlotList);

				}
				else 
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'CANARD' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
			}
		}

		//...............................................................
		// VTAIL:
		String createVTailPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/@perform");
		if(createVTailPlotProperty != null)
			createVTailPlot = true;

		if(theAircraft.getVTail() != null) {
			if(createVTailPlot == true) {
				if(performVTailAnalysis == true) {

					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_LIFT_CURVE_CLEAN);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_CL_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_cCL_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_GAMMA_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_TOTAL_LOAD_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_LIFT_CURVE_RUDDER);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_CD_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_CD_PARASITE_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_CD_INDUCED_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_DRAG_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_MOMENT_CURVE_CLEAN);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_MOMENT_DISTRIBUTION);
					vTailPlotList.add(AerodynamicAndStabilityPlotEnum.VTAIL_CM_DISTRIBUTION);

					plotMap.put(ComponentEnum.VERTICAL_TAIL, vTailPlotList);

				}
				else 
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'VERTICAL TAIL' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
			}
		}

		//...............................................................
		// FUSELAGE:
		String createFuselagePlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/fuselage/@perform");
		if(createFuselagePlotProperty != null)
			createFuselagePlot = true;

		if(theAircraft.getFuselage() != null) {
			if(createFuselagePlot == true) {
				if(performFuselageAnalysis == true) {

					fuselagePlotList.add(AerodynamicAndStabilityPlotEnum.FUSELAGE_POLAR_CURVE);
					fuselagePlotList.add(AerodynamicAndStabilityPlotEnum.FUSELAGE_MOMENT_CURVE);

					plotMap.put(ComponentEnum.FUSELAGE, fuselagePlotList);

				}
				else 
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'FUSELAGE' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
			}
		}

		//...............................................................
		// NACELLE:
		String createNacellesPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/nacelles/@perform");
		if(createNacellesPlotProperty != null)
			createNacellesPlot = true;

		if(theAircraft.getNacelles() != null) {
			if(createNacellesPlot == true) {
				if(performNacellesAnalysis == true) {

					nacellesPlotList.add(AerodynamicAndStabilityPlotEnum.NACELLE_POLAR_CURVE);
					nacellesPlotList.add(AerodynamicAndStabilityPlotEnum.NACELLE_MOMENT_CURVE);

					plotMap.put(ComponentEnum.NACELLE, nacellesPlotList);

				}
				else 
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'NACELLES' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
			}
		}

		//...............................................................
		// AIRCRAFT:
		//...............................................................
		String createAircraftPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/@perform");
		if(createAircraftPlotProperty != null)
			createAircraftPlot = true;		

		if(theAircraft != null) {
			if(createAircraftPlot == true) {			

				//----------------------------------------------------------------
				// CANARD-WING DOWNWASH  --> TODO: IMPLEMENT THE PLOT METHOD INSIDE plotAllChart(); (MANUELA) 
				if(aircraftTaskList.containsKey(AerodynamicAndStabilityEnum.CANARD_DOWNWASH)) {
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_WING_DOWNWASH_GRADIENT);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.CANARD_WING_DOWNWASH_ANGLE);
				}

				//----------------------------------------------------------------
				// WING-HTAIL DOWNWASH 
				if(aircraftTaskList.containsKey(AerodynamicAndStabilityEnum.WING_DOWNWASH)) {
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.WING_HTAIL_DOWNWASH_GRADIENT);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.WING_HTAIL_DOWNWASH_ANGLE);
				}

				//----------------------------------------------------------------
				// TOTAL LIFT CURVE
				if(aircraftTaskList.containsKey(AerodynamicAndStabilityEnum.CL_TOTAL)) 
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_LIFT_CURVE);

				//----------------------------------------------------------------
				// TOTAL POLAR CURVE
				if(aircraftTaskList.containsKey(AerodynamicAndStabilityEnum.CD_TOTAL)) 
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_POLAR_CURVE);

				//----------------------------------------------------------------
				// TOTAL MOMENT CURVE VS ALPHA
				if(aircraftTaskList.containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_MOMENT_CURVE_VS_ALPHA);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_MOMENT_CURVE_VS_CL);
				}

				//----------------------------------------------------------------
				// LONGITUDINAL STATIC STABILITY
				if(aircraftTaskList.containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) { 
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_LIFT_CURVE);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_LIFT_CURVE_HTAIL);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_POLAR_CURVE);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_POLAR_CURVE_HTAIL);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.DELTA_ELEVATOR_EQUILIBRIUM);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_EFFICIENCY_CURVE_VS_ALPHA);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TRIMMED_EFFICIENCY_CURVE_VS_CL);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_CM_BREAKDOWN);
				}

				//----------------------------------------------------------------
				// DIRECTIONAL STATIC STABILITY
				if(aircraftTaskList.containsKey(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY)) {
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_CN_BREAKDOWN);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.TOTAL_CN_VS_BETA_VS_DELTA_RUDDER);
					aircraftPlotList.add(AerodynamicAndStabilityPlotEnum.DELTA_RUDDER_EQUILIBRIUM);
				}

				plotMap.put(ComponentEnum.AIRCRAFT, aircraftPlotList);

			}
		}

		/********************************************************************************************
		 * Once the data are ready, it's possible to create the manager object. This can be created
		 * using the builder pattern.
		 */
		IACAerodynamicAndStabilityManager_v2 theAerodynamicAndStabilityBuilderInterface = new IACAerodynamicAndStabilityManager_v2.Builder()
				.setId(id)
				.setTheAircraft(theAircraft)
				.setTheOperatingConditions(theOperatingConditions)
				.setCurrentCondition(theCondition)
				.setPerformWingAnalyses(performWingAnalysis)
				.setWingAnalysisType(wingAnalysisType)
				.setPerformHTailAnalyses(performHTailAnalysis)
				.setHTailAnalysisType(hTailAnalysisType)
				.setPerformVTailAnalyses(performVTailAnalysis)
				.setVTailAnalysisType(vTailAnalysisType)
				.setPerformCanardAnalyses(performCanardAnalysis)
				.setCanardAnalysisType(canardAnalysisType)
				.setPerformFuselageAnalyses(performFuselageAnalysis)
				.setFuselageAnalysisType(fuselageAnalysisType)
				.setPerformNacelleAnalyses(performNacellesAnalysis)
				.setNacellesAnalysisType(nacellesAnalysisType)
				.putComponentTaskList(ComponentEnum.WING, wingTaskList)
				.putComponentTaskList(ComponentEnum.HORIZONTAL_TAIL, hTailTaskList)
				.putComponentTaskList(ComponentEnum.VERTICAL_TAIL, vTailTaskList)
				.putComponentTaskList(ComponentEnum.CANARD, canardTaskList)
				.putComponentTaskList(ComponentEnum.FUSELAGE, fuselageTaskList)
				.putComponentTaskList(ComponentEnum.NACELLE, nacellesTaskList)
				.putComponentTaskList(ComponentEnum.AIRCRAFT, aircraftTaskList)
				.putAllPlotList(plotMap)
				.addAllXCGAircraft(xCGAdimensionalPositions)
				.addAllZCGAircraft(zCGAdimensionalPositions)
				.setXCGFuselage(xCGFuselage)
				.setZCGFuselage(zCGFuselage)
				.setXCGLandingGear(xCGLandingGears)
				.setZCGLandingGear(zCGLandingGears)
				.setXCGNacelles(xCGNacelles)
				.setZCGNacelles(zCGNacelles)
				.setAlphaBodyInitial(alphaBodyInitial)
				.setAlphaBodyFinal(alphaBodyFinal)
				.setNumberOfAlphasBody(numberOfAlphaBody)
				.setBetaInitial(betaInitial)
				.setBetaFinal(betaFinal)
				.setNumberOfBeta(numberOfBeta)
				.setAlphaWingForDistribution(alphaWingArrayForDistributions)
				.setAlphaHorizontalTailForDistribution(alphaHorizontalTailArrayForDistributions)
				.setAlphaCanardForDistribution(alphaCanardArrayForDistributions)
				.setBetaVerticalTailForDistribution(betaVerticalTailArrayForDistributions)
				.setWingNumberOfPointSemiSpanWise(numberOfPointsSemispanwiseWing)
				.setHTailNumberOfPointSemiSpanWise(numberOfPointsSemispanwiseHTail)
				.setVTailNumberOfPointSemiSpanWise(numberOfPointsSemispanwiseVTail)
				.setCanardNumberOfPointSemiSpanWise(numberOfPointsSemispanwiseCanard)
				.addAllDeltaElevatorList(deltaElevatorList)
				.addAllDeltaRudderList(deltaRudderList)
				.addAllDeltaCanardControlSurfaceList(deltaCanardControlSurfaceList)
				.setAdimensionalWingMomentumPole(adimensionalWingMomentumPole)
				.setAdimensionalHTailMomentumPole(adimensionalHTailMomentumPole)
				.setAdimensionalVTailMomentumPole(adimensionalVTailMomentumPole)
				.setAdimensionalCanardMomentumPole(adimensionalCanardMomentumPole)
				.setAdimensionalFuselageMomentumPole(adimensionalFuselageMomentumPole)
				.setWingDynamicPressureRatio(wingDynamicPressureRatio)
				.setHTailDynamicPressureRatio(horizontalTailDynamicPressureRatio)
				.setVTailDynamicPressureRatio(verticalTailDynamicPressureRatio)
				.setTauElevatorFunction(tauElevator)
				.setTauRudderFunction(tauRudder)
				.setTauCanardFunction(tauCanardControlSurface)
				.setCanardWingDownwashConstant(canardWingDownwashConstantGradient)
				.setWingHTailDownwashConstant(wingHTailDownwashConstantGradient)
				.setFuselageEffectOnWingLiftCurve(fuselageEffectOnWingLiftCurve)
				.setTotalLiftCalibrationAlphaScaleFactor(totalLiftCalibrationAlphaScaleFactor)
				.setTotalLiftCalibrationCLScaleFactor(totalLiftCalibrationCLScaleFactor)
				.setCalculateLandingGearDeltaDragCoefficient(calculateDeltaCD0LandingGears)
				.setLandingGearDeltaDragCoefficient(deltaCD0LandingGears)
				.setCalculateCoolingDeltaDragCoefficient(calculateDeltaCD0Cooling)
				.setCoolingDeltaDragCoefficient(deltaCD0Cooling)
				.setCalculateExcrescencesDeltaDragCoefficient(calculatedeltaCD0Excrescences)
				.setExcrescencesDeltaDragCoefficient(deltaCD0Excrescences)
				.setCalculateInterferencesDeltaDragCoefficient(calculateDeltaCD0Interferences)
				.setInterferencesDeltaDragCoefficient(deltaCD0Interferences)
				.setTotalDragCalibrationCLScaleFactor(totalDragCalibrationCLScaleFactor)
				.setTotalDragCalibrationCDScaleFactor(totalDragCalibrationCDScaleFactor)
				.setTotalMomentCalibrationAlphaScaleFactor(totalMomentCalibrationAlphaScaleFactor)
				.setTotalMomentCalibrationCMScaleFactor(totalMomentCalibrationCMScaleFactor)
				.setCalculateWingPendularStability(aircraftWingPendularStability)
				.build();

		ACAerodynamicAndStabilityManager_v2 theAerodynamicAndStabilityManager = new ACAerodynamicAndStabilityManager_v2();
		theAerodynamicAndStabilityManager.setTheAerodynamicBuilderInterface(theAerodynamicAndStabilityBuilderInterface);

		return theAerodynamicAndStabilityManager;

	}

	private void initializeAnalysis() {

		_liftingSurfaceAerodynamicManagers = new HashMap<>();
		_fuselageAerodynamicManagers = new HashMap<>();
		_nacelleAerodynamicManagers = new HashMap<>();
		_downwashGradientMap = new HashMap<>();
		_downwashAngleMap = new HashMap<>();

		switch (_theAerodynamicBuilderInterface.getCurrentCondition()) {
		case TAKE_OFF:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachTakeOff();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeTakeOff();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaTakeOff();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaTakeOff();
			break;
		case CLIMB:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachClimb();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeClimb();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaClimb();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaClimb();
			break;
		case CRUISE:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachCruise();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeCruise();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaCruise();
			break;
		case LANDING:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachLanding();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeLanding();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaLanding();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaLanding();
			break;
		default:
			break;
		}

		calculateDependentData();
		calculateComponentsData();

	}

	private void calculateDependentData() {

		_alphaBodyList = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						_theAerodynamicBuilderInterface.getAlphaBodyInitial().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getAlphaBodyFinal().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getNumberOfAlphasBody()
						), 
				NonSI.DEGREE_ANGLE
				);

		_betaList = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						_theAerodynamicBuilderInterface.getBetaInitial().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getBetaFinal().doubleValue(NonSI.DEGREE_ANGLE),
						_theAerodynamicBuilderInterface.getNumberOfBeta()
						), 
				NonSI.DEGREE_ANGLE
				);

		_deltaEForEquilibrium = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail()
						.getSymmetricFlaps().get(0).getMinimumDeflection().doubleValue(NonSI.DEGREE_ANGLE),
						5,
						10
						), 
				NonSI.DEGREE_ANGLE
				);

		_deltaRForEquilibrium = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						5,
						_theAerodynamicBuilderInterface.getTheAircraft().getVTail()
						.getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE),
						10
						),  
				NonSI.DEGREE_ANGLE
				);

		_wingMomentumPole = Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalWingMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				);

		_hTailMomentumPole = Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalHTailMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				);

		_vTailMomentumPole = Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalVTailMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				);

		_canardMomentumPole = Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalCanardMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getCanard().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				);


	}

	private void calculateComponentsData() {

		//------------------------------------------------------------------------------
		// CANARD
		if(_theAerodynamicBuilderInterface.getTheAircraft().getCanard() != null) {
			if(_theAerodynamicBuilderInterface.isPerformCanardAnalyses() == true) {

				_alphaCanardList = _alphaBodyList.stream()
						.map(x -> x.to(NonSI.DEGREE_ANGLE).plus(
								_theAerodynamicBuilderInterface.getTheAircraft().getCanard().getRiggingAngle().to(NonSI.DEGREE_ANGLE))
								)
						.collect(Collectors.toList()); 

				_alphaCanardCurrent = _alphaBodyCurrent.to(NonSI.DEGREE_ANGLE)
						.plus(_theAerodynamicBuilderInterface.getTheAircraft().getCanard().getRiggingAngle().to(NonSI.DEGREE_ANGLE));

				_liftingSurfaceAerodynamicManagers.put(
						ComponentEnum.CANARD,
						new LiftingSurfaceAerodynamicsManager(
								_theAerodynamicBuilderInterface.getTheAircraft().getCanard(),
								_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
								_theAerodynamicBuilderInterface.getCurrentCondition(),
								_theAerodynamicBuilderInterface.getCanardNumberOfPointSemiSpanWise(),
								_alphaCanardList, 
								_theAerodynamicBuilderInterface.getAlphaCanardForDistribution(),
								_canardMomentumPole
								)
						);

				calculateCanardData();
				ACAerodynamicAndStabilityManagerUtils.initializeDataForDownwashCanard(this);
				ACAerodynamicAndStabilityManagerUtils.calculateDownwashDueToCanard(this);
			}
		}

		//------------------------------------------------------------------------------
		// WING
		//------------------------------------------------------------------------------
		if(_theAerodynamicBuilderInterface.getTheAircraft().getWing() != null) {
			if(_theAerodynamicBuilderInterface.isPerformWingAnalyses() == true) {


				//------------
				_alphaBodyList.stream()
				.forEach(x -> {

					int i=_alphaBodyList.indexOf(x);
					_alphaWingList.add(x.to(NonSI.DEGREE_ANGLE).minus(
							_downwashAngleMap
							.get(ComponentEnum.CANARD)
							.get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.AIRCRAFT)
									.get(AerodynamicAndStabilityEnum.CANARD_DOWNWASH)
									)
							.get(_theAerodynamicBuilderInterface.getDownwashConstant())
							.get(i)
							)
							.plus(
									_theAerodynamicBuilderInterface.getTheAircraft().getCanard().getRiggingAngle().to(NonSI.DEGREE_ANGLE)));
				}); 

				_alphaWingCurrent = _alphaBodyCurrent.minus(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(_alphaBodyList),
										MyArrayUtils.convertListOfAmountTodoubleArray(_downwashAngleMap
												.get(ComponentEnum.CANARD)
												.get(_theAerodynamicBuilderInterface
														.getComponentTaskList()
														.get(ComponentEnum.AIRCRAFT)
														.get(AerodynamicAndStabilityEnum.CANARD_DOWNWASH)).get(_theAerodynamicBuilderInterface.getDownwashConstant())),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)),
								NonSI.DEGREE_ANGLE)
						).plus(
								_theAerodynamicBuilderInterface.getTheAircraft().getCanard().getRiggingAngle().to(NonSI.DEGREE_ANGLE));

				_liftingSurfaceAerodynamicManagers.put(
						ComponentEnum.WING,
						new LiftingSurfaceAerodynamicsManager(
								_theAerodynamicBuilderInterface.getTheAircraft().getWing(),
								_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
								_theAerodynamicBuilderInterface.getCurrentCondition(),
								_theAerodynamicBuilderInterface.getWingNumberOfPointSemiSpanWise(),
								_alphaWingList, 
								_theAerodynamicBuilderInterface.getAlphaWingForDistribution(),
								_wingMomentumPole
								)
						);

				calculateWingData();
				ACAerodynamicAndStabilityManagerUtils.initializeDataForDownwash(this);
				ACAerodynamicAndStabilityManagerUtils.calculateDownwashDueToWing(this);
			
				ACAerodynamicAndStabilityManagerUtils.calculateCurrentWingLiftCurve(this);
				
			
			
			}
		}


		//------------------------------------------------------------------------------
		// HORIZONTAL TAIL 
		//------------------------------------------------------------------------------			
		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {
			if(_theAerodynamicBuilderInterface.isPerformHTailAnalyses() == true) {

				//------------
				_alphaBodyList.stream()
				.forEach(x -> {

					int i=_alphaBodyList.indexOf(x);
					_alphaHTailList.add(x.to(NonSI.DEGREE_ANGLE).minus(
							_downwashAngleMap
							.get(ComponentEnum.WING)
							.get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.AIRCRAFT)
									.get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
									)
							.get(_theAerodynamicBuilderInterface.getDownwashConstant())
							.get(i)
							)
							.plus(
									_theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().to(NonSI.DEGREE_ANGLE)));
				}); 
				

				_alphaHTailCurrent = _alphaBodyCurrent.minus(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(_alphaBodyList),
										MyArrayUtils.convertListOfAmountTodoubleArray(_downwashAngleMap
												.get(ComponentEnum.CANARD)
												.get(_theAerodynamicBuilderInterface
														.getComponentTaskList()
														.get(ComponentEnum.AIRCRAFT)
														.get(AerodynamicAndStabilityEnum.CANARD_DOWNWASH)).get(_theAerodynamicBuilderInterface.getDownwashConstant())),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)),
								NonSI.DEGREE_ANGLE)
						).plus(
								_theAerodynamicBuilderInterface.getTheAircraft().getCanard().getRiggingAngle().to(NonSI.DEGREE_ANGLE));

				_liftingSurfaceAerodynamicManagers.put(
						ComponentEnum.HORIZONTAL_TAIL,
						new LiftingSurfaceAerodynamicsManager(
								_theAerodynamicBuilderInterface.getTheAircraft().getHTail(),
								_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
								_theAerodynamicBuilderInterface.getCurrentCondition(),
								_theAerodynamicBuilderInterface.getHTailNumberOfPointSemiSpanWise(),
								_alphaHTailList, 
								_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution(),
								_hTailMomentumPole
								)
						);

				calculateHorizontalTailData();
				
				ACAerodynamicAndStabilityManagerUtils.calculateHorizontalTailLiftCurveWithElevatorDeflection(this);

			}
		}

		//------------------------------------------------------------------------------
		// VERTICAL TAIL 
		//------------------------------------------------------------------------------			
		if(_theAerodynamicBuilderInterface.getTheAircraft().getVTail() != null) {
			if(_theAerodynamicBuilderInterface.isPerformVTailAnalyses() == Boolean.TRUE) {
			
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
					new LiftingSurfaceAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getVTail(),
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(),
							_theAerodynamicBuilderInterface.getVTailNumberOfPointSemiSpanWise(), 
							_betaList, // Alpha for VTail is Beta
							_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution(),
							_vTailMomentumPole
							)
					);


				calculateVTailData();
				
				ACAerodynamicAndStabilityManagerUtils.calculateVerticalTailLiftCurveWithRudderDeflection(this);

			}
		}
		
		//------------------------------------------------------------------------------
		// FUSELAGE
		//------------------------------------------------------------------------------			
		if(_theAerodynamicBuilderInterface.getTheAircraft().getFuselage() != null) {
			if(_theAerodynamicBuilderInterface.isPerformFuselageAnalyses() == true) {

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

				calculateFuselageData();
			}
		}
		
		//------------------------------------------------------------------------------
		// NACELLE
		//------------------------------------------------------------------------------			
		if(_theAerodynamicBuilderInterface.getTheAircraft().getNacelles() != null) {
			if(_theAerodynamicBuilderInterface.isPerformNacelleAnalyses() == true) {
			
			_nacelleAerodynamicManagers.put(
					ComponentEnum.NACELLE,
					new NacelleAerodynamicsManager(
							_theAerodynamicBuilderInterface.getTheAircraft().getNacelles(),
							_theAerodynamicBuilderInterface.getTheAircraft().getWing(),
							_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING),
							_theAerodynamicBuilderInterface.getTheOperatingConditions(), 
							_theAerodynamicBuilderInterface.getCurrentCondition(), 
							_alphaBodyList
							)
					);

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
			
			calculateNacelleData();
		}
		}
		
		//------------------------------------------------------------------------------
		//LANDING GEARS 
		//------------------------------------------------------------------------------
		
		if(_theAerodynamicBuilderInterface.getTheAircraft().getLandingGears() != null) {

				calculateLandingGearData();
		}

	}
	
	

	private void calculateCanardData() {

		switch (_theAerodynamicBuilderInterface.getCanardAnalysisType()) {
		case SEMIEMPIRICAL:
			ACAerodynamicAndStabilityManagerUtils.calculateLiftingSurfaceDataSemiempirical(
					this,
					ComponentEnum.CANARD
					);
			break;
		case AVL:
			// TODO
			break;
		case KK32:
			// TODO
			break;
		default:
			break;
		}

	}
	
	private void calculateWingData() {

		switch (_theAerodynamicBuilderInterface.getWingAnalysisType()) {
		case SEMIEMPIRICAL:
			ACAerodynamicAndStabilityManagerUtils.calculateLiftingSurfaceDataSemiempirical(
					this,
					ComponentEnum.WING
					);
			break;
		case AVL:
			// TODO
			break;
		case KK32:
			// TODO
			break;
		default:
			break;
		}

	}
	
	private void calculateHorizontalTailData() {

		switch (_theAerodynamicBuilderInterface.getWingAnalysisType()) {
		case SEMIEMPIRICAL:
			ACAerodynamicAndStabilityManagerUtils.calculateLiftingSurfaceDataSemiempirical(
					this,
					ComponentEnum.HORIZONTAL_TAIL
					);
			break;
		case AVL:
			// TODO
			break;
		case KK32:
			// TODO
			break;
		default:
			break;
		}

	}
	
	private void calculateVTailData() {

		switch (_theAerodynamicBuilderInterface.getWingAnalysisType()) {
		case SEMIEMPIRICAL:
			ACAerodynamicAndStabilityManagerUtils.calculateLiftingSurfaceDataSemiempirical(
					this,
					ComponentEnum.VERTICAL_TAIL
					);
			break;
		case AVL:
			// TODO
			break;
		case KK32:
			// TODO
			break;
		default:
			break;
		}

	}

	private void calculateFuselageData() {

		switch (_theAerodynamicBuilderInterface.getFuselageAnalysisType()) {
		case SEMIEMPIRICAL:
			ACAerodynamicAndStabilityManagerUtils.calculateFuselageDataSemiempirical(
					this
					);
			break;
		default:
			break;
		}

	}
	

	private void calculateNacelleData() {

		switch (_theAerodynamicBuilderInterface.getNacellesAnalysisType()) {
		case SEMIEMPIRICAL:
			ACAerodynamicAndStabilityManagerUtils.calculateNacellesDataSemiempirical(
					this
					);
			break;
		default:
			break;
		}

	}
	
	private void calculateLandingGearData() {

		switch (_theAerodynamicBuilderInterface.getNacellesAnalysisType()) {
		case SEMIEMPIRICAL:
			ACAerodynamicAndStabilityManagerUtils.calculateLandingGearDataSemiempirical(
					this
					);
			break;
		default:
			break;
		}

	}
	
	private void calculateAircraftData() {

		//TOTAL LIFT
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL)) {

			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.CL_TOTAL)) {
			case FROM_BALANCE_EQUATION:
				ACAerodynamicAndStabilityManagerUtils.calculateTotalLiftCoefficientFromAircraftComponents(this);
				break;
			default:
				break;
			}
		}
		
		//TOTAL DRAG
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL)) {

			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.CD_TOTAL)) {
			case FROM_BALANCE_EQUATION:
				ACAerodynamicAndStabilityManagerUtils.calculateTotalPolarSemiempirical(this);
				break;
			default:
				break;
			}
		}
		
		//TOTAL MOMENT
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {

			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.CM_TOTAL)) {
			case FROM_BALANCE_EQUATION:
				ACAerodynamicAndStabilityManagerUtils.calculateTotalMomentfromAircraftComponents(this);
				break;
			default:
				break;
			}
		}

		//SIDE FORCE
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.SIDE_FORCE)) {

			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.SIDE_FORCE)) {
			case NAPOLITANO_DATCOM:
				ACAerodynamicAndStabilityManagerUtils.calculateSideForceCoeffient(this);
				break;
			default:
				break;
			}
		}
		
		//LONGITUDINAL STABILITY
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {
			case FROM_BALANCE_EQUATION:
				ACAerodynamicAndStabilityManagerUtils.calculateLongitudinalStaticStability(this);
				break;
			default:
				break;
			}
		}
		
		//LATERAL STABILITY
		if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LATERAL_STABILITY)) {

			switch (_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.LATERAL_STABILITY)) {
			case FROM_BALANCE_EQUATION:
				ACAerodynamicAndStabilityManagerUtils.calculateLateralStability(this);
				break;
			default:
				break;
			}
		}
	}

	
	public void calculate(String resultsFolderPath) {
		
		String aerodynamicAndStabilityFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "AERODYNAMIC_AND_STABILITY_" + _theAerodynamicBuilderInterface.getCurrentCondition().toString()
				+ File.separator
				);

		// TODO write output
		
		initializeAnalysis();
		calculateAircraftData();
		// TODO
		/*
		 * CALLS FOR INITIALIZE ANALYSIS 
		 * WHICH CALLS FOR:
		 *  - 'CALCULATE DEPENDENT DATA'
		 *  - 'CALCULATE COMPONENTS DATA'
		 */

	}

	public void plotAllCharts() {

		// TODO --> TAKE FROM PREVIOUS MANAGER

	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		// TODO --> TAKE FROM PREVIOUS MANAGER

		return sb.toString();

	}

	public void toXLS(String filenameWithPathAndExt) {

		// TODO -->  TAKE FROM PREVIOUS MANAGER
	}

	//==============================================================================================================
	/*
	 * TODO : ADD ALL INNER CLASSES. CREATE UTILITIES CLASS TO MANAGE AIRCRAFT DATA (??) (SUGGESTION --> MANUELA)
	 */
	//==============================================================================================================

	//------------------------------------------------------------------------------
	// GETTERS & SETTERS:
	//------------------------------------------------------------------------------
	public IACAerodynamicAndStabilityManager_v2 getTheAerodynamicBuilderInterface() {
		return _theAerodynamicBuilderInterface;
	}
	public void setTheAerodynamicBuilderInterface(IACAerodynamicAndStabilityManager_v2 _theAerodynamicBuilderInterface) {
		this._theAerodynamicBuilderInterface = _theAerodynamicBuilderInterface;
	}
	public double getCurrentMachNumber() {
		return _currentMachNumber;
	}
	public void setCurrentMachNumber(double _currentMachNumber) {
		this._currentMachNumber = _currentMachNumber;
	}
	public Amount<Length> getCurrentAltitude() {
		return _currentAltitude;
	}
	public void setCurrentAltitude(Amount<Length> _currentAltitude) {
		this._currentAltitude = _currentAltitude;
	}
	public Amount<Angle> getAlphaBodyCurrent() {
		return _alphaBodyCurrent;
	}
	public void setAlphaBodyCurrent(Amount<Angle> _alphaBodyCurrent) {
		this._alphaBodyCurrent = _alphaBodyCurrent;
	}
	public Amount<Angle> getAlphaCanardCurrent() {
		return _alphaCanardCurrent;
	}
	public void setAlphaCanardCurrent(Amount<Angle> _alphaCanardCurrent) {
		this._alphaCanardCurrent = _alphaCanardCurrent;
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
	public List<Amount<Angle>> getDeltaEForEquilibrium() {
		return _deltaEForEquilibrium;
	}
	public void setDeltaEForEquilibrium(List<Amount<Angle>> _deltaEForEquilibrium) {
		this._deltaEForEquilibrium = _deltaEForEquilibrium;
	}
	public List<Amount<Angle>> getDeltaRForEquilibrium() {
		return _deltaRForEquilibrium;
	}
	public void setDeltaRForEquilibrium(List<Amount<Angle>> _deltaRForEquilibrium) {
		this._deltaRForEquilibrium = _deltaRForEquilibrium;
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
	public List<Amount<Angle>> getAlphaCanardList() {
		return _alphaCanardList;
	}
	public void setAlphaCanardList(List<Amount<Angle>> _alphaCanardList) {
		this._alphaCanardList = _alphaCanardList;
	}
	public List<Amount<Angle>> getAlphaNacelleList() {
		return _alphaNacelleList;
	}
	public void setAlphaNacelleList(List<Amount<Angle>> _alphaNacelleList) {
		this._alphaNacelleList = _alphaNacelleList;
	}
	public List<Amount<Angle>> getBetaList() {
		return _betaList;
	}
	public void setBetaList(List<Amount<Angle>> _betaList) {
		this._betaList = _betaList;
	}
	public Amount<Length> getZACRootWing() {
		return _zACRootWing;
	}
	public void setZACRootWing(Amount<Length> _zACRootWing) {
		this._zACRootWing = _zACRootWing;
	}
	public Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> getLiftingSurfaceAerodynamicManagers() {
		return _liftingSurfaceAerodynamicManagers;
	}
	public void setLiftingSurfaceAerodynamicManagers(
			Map<ComponentEnum, LiftingSurfaceAerodynamicsManager> _liftingSurfaceAerodynamicManagers) {
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
	public Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Double>>>> getDownwashGradientMap() {
		return _downwashGradientMap;
	}
	public void setDownwashGradientMap(
			Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Double>>>> _downwashGradientMap) {
		this._downwashGradientMap = _downwashGradientMap;
	}
	public Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Amount<Angle>>>>> getDownwashAngleMap() {
		return _downwashAngleMap;
	}
	public void setDownwashAngleMap(
			Map<ComponentEnum, Map<MethodEnum, Map<Boolean, List<Amount<Angle>>>>> _downwashAngleMap) {
		this._downwashAngleMap = _downwashAngleMap;
	}
	public Map<ComponentEnum, Map<MethodEnum, Map<Boolean, Double>>> getDownwashGradientCurrent() {
		return _downwashGradientCurrent;
	}
	public void setDownwashGradientCurrent(
			Map<ComponentEnum, Map<MethodEnum, Map<Boolean, Double>>> _downwashGradientCurrent) {
		this._downwashGradientCurrent = _downwashGradientCurrent;
	}
	public Map<ComponentEnum, Map<MethodEnum, Map<Boolean, Amount<Angle>>>> getDownwashAngleCurrent() {
		return _downwashAngleCurrent;
	}
	public void setDownwashAngleCurrent(
			Map<ComponentEnum, Map<MethodEnum, Map<Boolean, Amount<Angle>>>> _downwashAngleCurrent) {
		this._downwashAngleCurrent = _downwashAngleCurrent;
	}
	public List<Double> getCurrent3DWingLiftCurve() {
		return _current3DWingLiftCurve;
	}
	public void setCurrent3DWingLiftCurve(List<Double> _current3DWingLiftCurve) {
		this._current3DWingLiftCurve = _current3DWingLiftCurve;
	}
	public List<Double> getCurrent3DWingMomentCurve() {
		return _current3DWingMomentCurve;
	}
	public void setCurrent3DWingMomentCurve(List<Double> _current3DWingMomentCurve) {
		this._current3DWingMomentCurve = _current3DWingMomentCurve;
	}
	public Map<Amount<Angle>, List<Double>> getCurrent3DCanardLiftCurve() {
		return _current3DCanardLiftCurve;
	}
	public void setCurrent3DCanardLiftCurve(Map<Amount<Angle>, List<Double>> _current3DCanardLiftCurve) {
		this._current3DCanardLiftCurve = _current3DCanardLiftCurve;
	}
	public Map<Amount<Angle>, List<Double>> getCurrent3DCanardMomentCurve() {
		return _current3DCanardMomentCurve;
	}
	public void setCurrent3DCanardMomentCurve(Map<Amount<Angle>, List<Double>> _current3DCanardMomentCurve) {
		this._current3DCanardMomentCurve = _current3DCanardMomentCurve;
	}
	public Map<Amount<Angle>, List<Double>> getCurrent3DHorizontalTailLiftCurve() {
		return _current3DHorizontalTailLiftCurve;
	}
	public void setCurrent3DHorizontalTailLiftCurve(Map<Amount<Angle>, List<Double>> _current3DHorizontalTailLiftCurve) {
		this._current3DHorizontalTailLiftCurve = _current3DHorizontalTailLiftCurve;
	}
	public Map<Amount<Angle>, List<Double>> getCurrent3DHorizontalTailMomentCurve() {
		return _current3DHorizontalTailMomentCurve;
	}
	public void setCurrent3DHorizontalTailMomentCurve(
			Map<Amount<Angle>, List<Double>> _current3DHorizontalTailMomentCurve) {
		this._current3DHorizontalTailMomentCurve = _current3DHorizontalTailMomentCurve;
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
	public Map<Double, Map<Amount<Angle>, List<Double>>> getTotalMomentCoefficient() {
		return _totalMomentCoefficient;
	}
	public void setTotalMomentCoefficient(Map<Double, Map<Amount<Angle>, List<Double>>> _totalMomentCoefficient) {
		this._totalMomentCoefficient = _totalMomentCoefficient;
	}
	public Map<Double, Map<ComponentEnum, List<Double>>> getTotalMomentCoefficientBreakDown() {
		return _totalMomentCoefficientBreakDown;
	}
	public void setTotalMomentCoefficientBreakDown(Map<Double, Map<ComponentEnum, List<Double>>> _totalMomentCoefficientBreakDown) {
		this._totalMomentCoefficientBreakDown = _totalMomentCoefficientBreakDown;
	}
	public Amount<?> getCYBetaWing() {
		return _cYBetaWing;
	}
	public void setCYBetaWing(Amount<?> _cYBetaWing) {
		this._cYBetaWing = _cYBetaWing;
	}
	public Amount<?> getCYBetaFuselage() {
		return _cYBetaFuselage;
	}
	public void setCYBetaFuselage(Amount<?> _cYBetaFuselage) {
		this._cYBetaFuselage = _cYBetaFuselage;
	}
	public Amount<?> getCYBetaHorizontal() {
		return _cYBetaHorizontal;
	}
	public void setCYBetaHorizontal(Amount<?> _cYBetaHorizontal) {
		this._cYBetaHorizontal = _cYBetaHorizontal;
	}
	public Amount<?> getCYBetaVertical() {
		return _cYBetaVertical;
	}
	public void setCYBetaVertical(Amount<?> _cYBetaVertical) {
		this._cYBetaVertical = _cYBetaVertical;
	}
	public Amount<?> getCYBetaTotal() {
		return _cYBetaTotal;
	}
	public void setCYBetaTotal(Amount<?> _cYBetaTotal) {
		this._cYBetaTotal = _cYBetaTotal;
	}
	public Amount<?> getCYDeltaR() {
		return _cYDeltaR;
	}
	public void setCYDeltaR(Amount<?> _cYDeltaR) {
		this._cYDeltaR = _cYDeltaR;
	}
	public Map<Double, Amount<?>> getCYp() {
		return _cYp;
	}
	public void setCYp(Map<Double, Amount<?>> _cYp) {
		this._cYp = _cYp;
	}
	public Map<Double, Amount<?>> getCYr() {
		return _cYr;
	}
	public void setCYr(Map<Double, Amount<?>> _cYr) {
		this._cYr = _cYr;
	}
	public Map<Double, List<Double>> getCanardEquilibriumLiftCoefficient() {
		return _canardEquilibriumLiftCoefficient;
	}
	public void setCanardEquilibriumLiftCoefficient(Map<Double, List<Double>> _canardEquilibriumLiftCoefficient) {
		this._canardEquilibriumLiftCoefficient = _canardEquilibriumLiftCoefficient;
	}
	public Map<Double, List<Double>> getCanardEquilibriumDragCoefficient() {
		return _canardEquilibriumDragCoefficient;
	}
	public void setCanardEquilibriumDragCoefficient(Map<Double, List<Double>> _canardEquilibriumDragCoefficient) {
		this._canardEquilibriumDragCoefficient = _canardEquilibriumDragCoefficient;
	}
	public Map<Double, List<Double>> getWingEquilibriumLiftCoefficient() {
		return _wingEquilibriumLiftCoefficient;
	}
	public void setWingEquilibriumLiftCoefficient(Map<Double, List<Double>> _wingEquilibriumLiftCoefficient) {
		this._wingEquilibriumLiftCoefficient = _wingEquilibriumLiftCoefficient;
	}
	public Map<Double, List<Double>> getWingEquilibriumDragCoefficient() {
		return _wingEquilibriumDragCoefficient;
	}
	public void setWingEquilibriumDragCoefficient(Map<Double, List<Double>> _wingEquilibriumDragCoefficient) {
		this._wingEquilibriumDragCoefficient = _wingEquilibriumDragCoefficient;
	}
	public Map<Double, List<Double>> getHorizontalTailEquilibriumLiftCoefficient() {
		return _horizontalTailEquilibriumLiftCoefficient;
	}
	public void setHorizontalTailEquilibriumLiftCoefficient(
			Map<Double, List<Double>> _horizontalTailEquilibriumLiftCoefficient) {
		this._horizontalTailEquilibriumLiftCoefficient = _horizontalTailEquilibriumLiftCoefficient;
	}
	public Map<Double, List<Double>> getHorizontalTailEquilibriumDragCoefficient() {
		return _horizontalTailEquilibriumDragCoefficient;
	}
	public void setHorizontalTailEquilibriumDragCoefficient(
			Map<Double, List<Double>> _horizontalTailEquilibriumDragCoefficient) {
		this._horizontalTailEquilibriumDragCoefficient = _horizontalTailEquilibriumDragCoefficient;
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
	public Map<Double, List<Double>> getTotalEquilibriumEfficiencyMap() {
		return _totalEquilibriumEfficiencyMap;
	}
	public void setTotalEquilibriumEfficiencyMap(Map<Double, List<Double>> _totalEquilibriumEfficiencyMap) {
		this._totalEquilibriumEfficiencyMap = _totalEquilibriumEfficiencyMap;
	}
	public Map<Double, List<Amount<Angle>>> getDeltaEEquilibrium() {
		return _deltaEEquilibrium;
	}
	public void setDeltaEEquilibrium(Map<Double, List<Amount<Angle>>> _deltaEEquilibrium) {
		this._deltaEEquilibrium = _deltaEEquilibrium;
	}
	public Map<Double, Double> getTotalEquilibriumMaximumEfficiencyMap() {
		return _totalEquilibriumMaximumEfficiencyMap;
	}
	public void setTotalEquilibriumMaximumEfficiencyMap(Map<Double, Double> _totalEquilibriumMaximumEfficiencyMap) {
		this._totalEquilibriumMaximumEfficiencyMap = _totalEquilibriumMaximumEfficiencyMap;
	}
	public Map<Double, Double> getNeutralPointPositionMap() {
		return _neutralPointPositionMap;
	}
	public void setNeutralPointPositionMap(Map<Double, Double> _neutralPointPositionMap) {
		this._neutralPointPositionMap = _neutralPointPositionMap;
	}
	public Map<Double, Double> getStaticStabilityMarginMap() {
		return _staticStabilityMarginMap;
	}
	public void setStaticStabilityMarginMap(Map<Double, Double> _staticStabilityMarginMap) {
		this._staticStabilityMarginMap = _staticStabilityMarginMap;
	}
	public Map<Double, Double> getMaximumTrimmedLiftingCoefficientMap() {
		return _maximumTrimmedLiftingCoefficientMap;
	}
	public void setMaximumTrimmedLiftingCoefficientMap(Map<Double, Double> _maximumTrimmedLiftingCoefficientMap) {
		this._maximumTrimmedLiftingCoefficientMap = _maximumTrimmedLiftingCoefficientMap;
	}
	public Map<Double, Amount<?>> getCLAlphaEquilibriumMap() {
		return _cLAlphaEquilibriumMap;
	}
	public void setCLAlphaEquilibriumMap(Map<Double, Amount<?>> _cLAlphaEquilibriumMap) {
		this._cLAlphaEquilibriumMap = _cLAlphaEquilibriumMap;
	}
	public Map<Double, Amount<?>> getCMAlphaEquilibriumMap() {
		return _cMAlphaEquilibriumMap;
	}
	public void setCMAlphaEquilibriumMap(Map<Double, Amount<?>> _cMAlphaEquilibriumMap) {
		this._cMAlphaEquilibriumMap = _cMAlphaEquilibriumMap;
	}
	public Amount<?> getCRollBetaWingBody() {
		return _cRollBetaWingBody;
	}
	public void setCRollBetaWingBody(Amount<?> _cRollBetaWingBody) {
		this._cRollBetaWingBody = _cRollBetaWingBody;
	}
	public Amount<?> getCRollBetaHorizontal() {
		return _cRollBetaHorizontal;
	}
	public void setCRollBetaHorizontal(Amount<?> _cRollBetaHorizontal) {
		this._cRollBetaHorizontal = _cRollBetaHorizontal;
	}
	public Map<Double, Amount<?>> getCRollBetaVertical() {
		return _cRollBetaVertical;
	}
	public void setCRollBetaVertical(Map<Double, Amount<?>> _cRollBetaVertical) {
		this._cRollBetaVertical = _cRollBetaVertical;
	}
	public Map<Double, Amount<?>> getCRollBetaTotal() {
		return _cRollBetaTotal;
	}
	public void setCRollBetaTotal(Map<Double, Amount<?>> _cRollBetaTotal) {
		this._cRollBetaTotal = _cRollBetaTotal;
	}
	public Amount<?> getCRollDeltaA() {
		return _cRollDeltaA;
	}
	public void setCRollDeltaA(Amount<?> _cRollDeltaA) {
		this._cRollDeltaA = _cRollDeltaA;
	}
	public Map<Double, Amount<?>> getCRollDeltaR() {
		return _cRollDeltaR;
	}
	public void setCRollDeltaR(Map<Double, Amount<?>> _cRollDeltaR) {
		this._cRollDeltaR = _cRollDeltaR;
	}
	public Amount<?> getCRollpWingBody() {
		return _cRollpWingBody;
	}
	public void setCRollpWingBody(Amount<?> _cRollpWingBody) {
		this._cRollpWingBody = _cRollpWingBody;
	}
	public Amount<?> getCRollpHorizontal() {
		return _cRollpHorizontal;
	}
	public void setCRollpHorizontal(Amount<?> _cRollpHorizontal) {
		this._cRollpHorizontal = _cRollpHorizontal;
	}
	public Amount<?> getCRollpVertical() {
		return _cRollpVertical;
	}
	public void setCRollpVertical(Amount<?> _cRollpVertical) {
		this._cRollpVertical = _cRollpVertical;
	}
	public Amount<?> getCRollpTotal() {
		return _cRollpTotal;
	}
	public void setCRollpTotal(Amount<?> _cRollpTotal) {
		this._cRollpTotal = _cRollpTotal;
	}
	public Amount<?> getCRollrWing() {
		return _cRollrWing;
	}
	public void setCRollrWing(Amount<?> _cRollrWing) {
		this._cRollrWing = _cRollrWing;
	}
	public Map<Double, Amount<?>> getCRollrVertical() {
		return _cRollrVertical;
	}
	public void setCRollrVertical(Map<Double, Amount<?>> _cRollrVertical) {
		this._cRollrVertical = _cRollrVertical;
	}
	public Map<Double, Amount<?>> getCRollrTotal() {
		return _cRollrTotal;
	}
	public void setCRollrTotal(Map<Double, Amount<?>> _cRollrTotal) {
		this._cRollrTotal = _cRollrTotal;
	}
	public List<Tuple2<Double, Double>> getCNBetaFuselage() {
		return _cNBetaFuselage;
	}
	public void setCNBetaFuselage(List<Tuple2<Double, Double>> _cNBetaFuselage) {
		this._cNBetaFuselage = _cNBetaFuselage;
	}
	public List<Tuple2<Double, Double>> getCNBetaNacelles() {
		return _cNBetaNacelles;
	}
	public void setCNBetaNacelles(List<Tuple2<Double, Double>> _cNBetaNacelles) {
		this._cNBetaNacelles = _cNBetaNacelles;
	}
	public List<Tuple2<Double, Double>> getCNBetaVertical() {
		return _cNBetaVertical;
	}
	public void setCNBetaVertical(List<Tuple2<Double, Double>> _cNBetaVertical) {
		this._cNBetaVertical = _cNBetaVertical;
	}
	public List<Tuple2<Double, Double>> getCNBetaWing() {
		return _cNBetaWing;
	}
	public void setCNBetaWing(List<Tuple2<Double, Double>> _cNBetaWing) {
		this._cNBetaWing = _cNBetaWing;
	}
	public List<Tuple2<Double, Double>> getCNBetaHTail() {
		return _cNBetaHTail;
	}
	public void setCNBetaHTail(List<Tuple2<Double, Double>> _cNBetaHTail) {
		this._cNBetaHTail = _cNBetaHTail;
	}
	public List<Tuple2<Double, Double>> getCNBetaTotal() {
		return _cNBetaTotal;
	}
	public void setCNBetaTotal(List<Tuple2<Double, Double>> _cNBetaTotal) {
		this._cNBetaTotal = _cNBetaTotal;
	}
	public Map<Amount<Angle>, List<Tuple2<Double, Double>>> getCNDeltaR() {
		return _cNDeltaR;
	}
	public void setCNDeltaR(Map<Amount<Angle>, List<Tuple2<Double, Double>>> _cNDeltaR) {
		this._cNDeltaR = _cNDeltaR;
	}
	public double getCNDeltaA() {
		return _cNDeltaA;
	}
	public void setCNDeltaA(double _cNDeltaA) {
		this._cNDeltaA = _cNDeltaA;
	}
	public List<Tuple2<Double, Double>> getCNpWing() {
		return _cNpWing;
	}
	public void setCNpWing(List<Tuple2<Double, Double>> _cNpWing) {
		this._cNpWing = _cNpWing;
	}
	public List<Tuple2<Double, Double>> getCNpVertical() {
		return _cNpVertical;
	}
	public void setCNpVertical(List<Tuple2<Double, Double>> _cNpVertical) {
		this._cNpVertical = _cNpVertical;
	}
	public List<Tuple2<Double, Double>> getCNpTotal() {
		return _cNpTotal;
	}
	public void setCNpTotal(List<Tuple2<Double, Double>> _cNpTotal) {
		this._cNpTotal = _cNpTotal;
	}
	public List<Tuple2<Double, Double>> getCNrWing() {
		return _cNrWing;
	}
	public void setCNrWing(List<Tuple2<Double, Double>> _cNrWing) {
		this._cNrWing = _cNrWing;
	}
	public List<Tuple2<Double, Double>> getCNrVertical() {
		return _cNrVertical;
	}
	public void setCNrVertical(List<Tuple2<Double, Double>> _cNrVertical) {
		this._cNrVertical = _cNrVertical;
	}
	public List<Tuple2<Double, Double>> getCNrTotal() {
		return _cNrTotal;
	}
	public void setCNrTotal(List<Tuple2<Double, Double>> _cNrTotal) {
		this._cNrTotal = _cNrTotal;
	}
	public List<Tuple2<Double, List<Double>>> getCNFuselage() {
		return _cNFuselage;
	}
	public void setCNFuselage(List<Tuple2<Double, List<Double>>> _cNFuselage) {
		this._cNFuselage = _cNFuselage;
	}
	public List<Tuple2<Double, List<Double>>> getCNNacelles() {
		return _cNNacelles;
	}
	public void setCNNacelles(List<Tuple2<Double, List<Double>>> _cNNacelles) {
		this._cNNacelles = _cNNacelles;
	}
	public List<Tuple2<Double, List<Double>>> getCNVertical() {
		return _cNVertical;
	}
	public void setCNVertical(List<Tuple2<Double, List<Double>>> _cNVertical) {
		this._cNVertical = _cNVertical;
	}
	public List<Tuple2<Double, List<Double>>> getCNWing() {
		return _cNWing;
	}
	public void setCNWing(List<Tuple2<Double, List<Double>>> _cNWing) {
		this._cNWing = _cNWing;
	}
	public List<Tuple2<Double, List<Double>>> getCNHTail() {
		return _cNHTail;
	}
	public void setCNHTail(List<Tuple2<Double, List<Double>>> _cNHTail) {
		this._cNHTail = _cNHTail;
	}
	public List<Tuple2<Double, List<Double>>> getCNTotal() {
		return _cNTotal;
	}
	public void setCNTotal(List<Tuple2<Double, List<Double>>> _cNTotal) {
		this._cNTotal = _cNTotal;
	}
	public Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> getCNDueToDeltaRudder() {
		return _cNDueToDeltaRudder;
	}
	public void setCNDueToDeltaRudder(Map<Amount<Angle>, List<Tuple2<Double, List<Double>>>> _cNDueToDeltaRudder) {
		this._cNDueToDeltaRudder = _cNDueToDeltaRudder;
	}
	public Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>> getBetaOfEquilibrium() {
		return _betaOfEquilibrium;
	}
	public void setBetaOfEquilibrium(Map<Double, List<Tuple2<Amount<Angle>, Amount<Angle>>>> _betaOfEquilibrium) {
		this._betaOfEquilibrium = _betaOfEquilibrium;
	}
	public Map<Double, Amount<Angle>> getBetaMaxOfEquilibrium() {
		return _betaMaxOfEquilibrium;
	}
	public void setBetaMaxOfEquilibrium(Map<Double, Amount<Angle>> _betaMaxOfEquilibrium) {
		this._betaMaxOfEquilibrium = _betaMaxOfEquilibrium;
	}

	public Map<Double, Double> getTotalEquilibriumCurrentEfficiencyMap() {
		return _totalEquilibriumCurrentEfficiencyMap;
	}

	public void setTotalEquilibriumCurrentEfficiencyMap(Map<Double, Double> _totalEquilibriumCurrentEfficiencyMap) {
		this._totalEquilibriumCurrentEfficiencyMap = _totalEquilibriumCurrentEfficiencyMap;
	}

	public Amount<Length> getWingMomentumPole() {
		return _wingMomentumPole;
	}

	public void setWingMomentumPole(Amount<Length> _wingMomentumPole) {
		this._wingMomentumPole = _wingMomentumPole;
	}

	public Amount<Length> getHTailMomentumPole() {
		return _hTailMomentumPole;
	}

	public void setHTailMomentumPole(Amount<Length> _hTailMomentumPole) {
		this._hTailMomentumPole = _hTailMomentumPole;
	}

	public Amount<Length> getVTailMomentumPole() {
		return _vTailMomentumPole;
	}

	public void setVTailMomentumPole(Amount<Length> _vTailMomentumPole) {
		this._vTailMomentumPole = _vTailMomentumPole;
	}

	public Amount<Length> getCanardMomentumPole() {
		return _canardMomentumPole;
	}

	public void setCanardMomentumPole(Amount<Length> _canardMomentumPole) {
		this._canardMomentumPole = _canardMomentumPole;
	}

	public Amount<Length> get_horizontalDistanceSlingerland() {
		return _horizontalDistanceSlingerland;
	}

	public void set_horizontalDistanceSlingerland(Amount<Length> _horizontalDistanceSlingerland) {
		this._horizontalDistanceSlingerland = _horizontalDistanceSlingerland;
	}

	public Amount<Length> get_verticalDistanceSlingerland() {
		return _verticalDistanceSlingerland;
	}

	public void set_verticalDistanceSlingerland(Amount<Length> _verticalDistanceSlingerland) {
		this._verticalDistanceSlingerland = _verticalDistanceSlingerland;
	}

	public Amount<Length> get_zACRootWing() {
		return _zACRootWing;
	}

	public void set_zACRootWing(Amount<Length> _zACRootWing) {
		this._zACRootWing = _zACRootWing;
	}

	public Amount<Length> get_horizontalDistanceQuarterChordWingHTail() {
		return _horizontalDistanceQuarterChordWingHTail;
	}

	public void set_horizontalDistanceQuarterChordWingHTail(Amount<Length> _horizontalDistanceQuarterChordWingHTail) {
		this._horizontalDistanceQuarterChordWingHTail = _horizontalDistanceQuarterChordWingHTail;
	}

	public Amount<Length> get_verticalDistanceZeroLiftDirectionWingHTailPARTIAL() {
		return _verticalDistanceZeroLiftDirectionWingHTailPARTIAL;
	}

	public void set_verticalDistanceZeroLiftDirectionWingHTailPARTIAL(
			Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailPARTIAL) {
		this._verticalDistanceZeroLiftDirectionWingHTailPARTIAL = _verticalDistanceZeroLiftDirectionWingHTailPARTIAL;
	}

	public Amount<Length> get_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE() {
		return _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	}

	public void set_verticalDistanceZeroLiftDirectionWingHTailCOMPLETE(
			Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE) {
		this._verticalDistanceZeroLiftDirectionWingHTailCOMPLETE = _verticalDistanceZeroLiftDirectionWingHTailCOMPLETE;
	}

	public Amount<Length> get_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE() {
		return _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE;
	}

	public void set_verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE(
			Amount<Length> _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE) {
		this._verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE = _verticalDistanceZeroLiftDirectionWingHTailEFFECTIVE;
	}

	public Map<MethodEnum, List<Amount<Length>>> get_verticalDistanceZeroLiftDirectionWingHTailVariable() {
		return _verticalDistanceZeroLiftDirectionWingHTailVariable;
	}

	public void set_verticalDistanceZeroLiftDirectionWingHTailVariable(
			Map<MethodEnum, List<Amount<Length>>> _verticalDistanceZeroLiftDirectionWingHTailVariable) {
		this._verticalDistanceZeroLiftDirectionWingHTailVariable = _verticalDistanceZeroLiftDirectionWingHTailVariable;
	}

	public Amount<Angle> getCurrentDownwashAngle() {
		return currentDownwashAngle;
	}

	public void setCurrentDownwashAngle(Amount<Angle> currentDownwashAngle) {
		this.currentDownwashAngle = currentDownwashAngle;
	}

	public Amount<?> get_clAlphaWingFuselage() {
		return _clAlphaWingFuselage;
	}

	public void set_clAlphaWingFuselage(Amount<?> _clAlphaWingFuselage) {
		this._clAlphaWingFuselage = _clAlphaWingFuselage;
	}

	public Double get_clZeroWingFuselage() {
		return _clZeroWingFuselage;
	}

	public void set_clZeroWingFuselage(Double _clZeroWingFuselage) {
		this._clZeroWingFuselage = _clZeroWingFuselage;
	}

	public Double get_clMaxWingFuselage() {
		return _clMaxWingFuselage;
	}

	public void set_clMaxWingFuselage(Double _clMaxWingFuselage) {
		this._clMaxWingFuselage = _clMaxWingFuselage;
	}

	public Double get_clStarWingFuselage() {
		return _clStarWingFuselage;
	}

	public void set_clStarWingFuselage(Double _clStarWingFuselage) {
		this._clStarWingFuselage = _clStarWingFuselage;
	}

	public Amount<Angle> get_alphaStarWingFuselage() {
		return _alphaStarWingFuselage;
	}

	public void set_alphaStarWingFuselage(Amount<Angle> _alphaStarWingFuselage) {
		this._alphaStarWingFuselage = _alphaStarWingFuselage;
	}

	public Amount<Angle> get_alphaStallWingFuselage() {
		return _alphaStallWingFuselage;
	}

	public void set_alphaStallWingFuselage(Amount<Angle> _alphaStallWingFuselage) {
		this._alphaStallWingFuselage = _alphaStallWingFuselage;
	}

	public Amount<Angle> get_alphaZeroLiftWingFuselage() {
		return _alphaZeroLiftWingFuselage;
	}

	public void set_alphaZeroLiftWingFuselage(Amount<Angle> _alphaZeroLiftWingFuselage) {
		this._alphaZeroLiftWingFuselage = _alphaZeroLiftWingFuselage;
	}

	public Map<Amount<Angle>, List<Double>> get_current3DVerticalTailLiftCurve() {
		return _current3DVerticalTailLiftCurve;
	}

	public void set_current3DVerticalTailLiftCurve(Map<Amount<Angle>, List<Double>> _current3DVerticalTailLiftCurve) {
		this._current3DVerticalTailLiftCurve = _current3DVerticalTailLiftCurve;
	}

	public Map<Double, Map<ComponentEnum, List<Double>>> get_totalMomentCoefficientBreakDown() {
		return _totalMomentCoefficientBreakDown;
	}

	public void set_totalMomentCoefficientBreakDown(
			Map<Double, Map<ComponentEnum, List<Double>>> _totalMomentCoefficientBreakDown) {
		this._totalMomentCoefficientBreakDown = _totalMomentCoefficientBreakDown;
	}

	public Double get_deltaCDZeroLandingGear() {
		return _deltaCDZeroLandingGear;
	}

	public void setDeltaCDZeroLandingGear(Double _deltaCDZeroLandingGear) {
		this._deltaCDZeroLandingGear = _deltaCDZeroLandingGear;
	}

	public Double get_deltaCDZeroFlap() {
		return _deltaCDZeroFlap;
	}

	public void setDeltaCDZeroFlap(Double _deltaCDZeroFlap) {
		this._deltaCDZeroFlap = _deltaCDZeroFlap;
	}

	public Double get_deltaCLZeroFlap() {
		return _deltaCLZeroFlap;
	}

	public void setDeltaCLZeroFlap(Double _deltaCLZeroFlap) {
		this._deltaCLZeroFlap = _deltaCLZeroFlap;
	}

	public Double get_deltaCDZeroExcrescences() {
		return _deltaCDZeroExcrescences;
	}

	public void set_deltaCDZeroExcrescences(Double _deltaCDZeroExcrescences) {
		this._deltaCDZeroExcrescences = _deltaCDZeroExcrescences;
	}

	public Double get_deltaCDZeroInterferences() {
		return _deltaCDZeroInterferences;
	}

	public void set_deltaCDZeroInterferences(Double _deltaCDZeroInterferences) {
		this._deltaCDZeroInterferences = _deltaCDZeroInterferences;
	}

	public Double get_deltaCDZeroCooling() {
		return _deltaCDZeroCooling;
	}

	public void set_deltaCDZeroCooling(Double _deltaCDZeroCooling) {
		this._deltaCDZeroCooling = _deltaCDZeroCooling;
	}

	public Amount<?> get_cRollBetaWingBody() {
		return _cRollBetaWingBody;
	}

	public void set_cRollBetaWingBody(Amount<?> _cRollBetaWingBody) {
		this._cRollBetaWingBody = _cRollBetaWingBody;
	}

	public Amount<?> get_cRollDeltaA() {
		return _cRollDeltaA;
	}

	public void set_cRollDeltaA(Amount<?> _cRollDeltaA) {
		this._cRollDeltaA = _cRollDeltaA;
	}

	public Map<Double, Amount<?>> get_cRollDeltaR() {
		return _cRollDeltaR;
	}

	public void set_cRollDeltaR(Map<Double, Amount<?>> _cRollDeltaR) {
		this._cRollDeltaR = _cRollDeltaR;
	}

	public Amount<?> get_cRollpWingBody() {
		return _cRollpWingBody;
	}

	public void set_cRollpWingBody(Amount<?> _cRollpWingBody) {
		this._cRollpWingBody = _cRollpWingBody;
	}

	public Amount<?> get_cRollpHorizontal() {
		return _cRollpHorizontal;
	}

	public void set_cRollpHorizontal(Amount<?> _cRollpHorizontal) {
		this._cRollpHorizontal = _cRollpHorizontal;
	}

	public Amount<?> get_cRollpVertical() {
		return _cRollpVertical;
	}

	public void set_cRollpVertical(Amount<?> _cRollpVertical) {
		this._cRollpVertical = _cRollpVertical;
	}

	public Amount<?> get_cRollpTotal() {
		return _cRollpTotal;
	}

	public void set_cRollpTotal(Amount<?> _cRollpTotal) {
		this._cRollpTotal = _cRollpTotal;
	}

	public Amount<?> get_cRollrWing() {
		return _cRollrWing;
	}

	public void set_cRollrWing(Amount<?> _cRollrWing) {
		this._cRollrWing = _cRollrWing;
	}

	public Map<Double, Amount<?>> get_cRollrVertical() {
		return _cRollrVertical;
	}

	public void set_cRollrVertical(Map<Double, Amount<?>> _cRollrVertical) {
		this._cRollrVertical = _cRollrVertical;
	}

	public Map<Double, Amount<?>> get_cRollrTotal() {
		return _cRollrTotal;
	}

	public void set_cRollrTotal(Map<Double, Amount<?>> _cRollrTotal) {
		this._cRollrTotal = _cRollrTotal;
	}

}
