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

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.nacelles.NacelleAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.LiftCalc;
import configuration.enumerations.AerodynamicAndStabilityEnum;
import configuration.enumerations.AerodynamicAnlaysisApproachEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.MethodEnum;
import javaslang.Tuple2;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyXMLReaderUtils;

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
	private Map<Double, Double> _totalEquilibriumCurrentEfficiencyMap; // xcg, efficiency @ current CL
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
					
					AerodynamicAnlaysisApproachEnum wingAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(wingAnalysisTypeString);
					
					switch (wingAnalysisType) {
					case SEMIEMPIRICAL:
						wingTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, MethodEnum.KROO);
						wingTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, MethodEnum.NAPOLITANO_DATCOM); // FIXME DE-YOUNG HARPER??
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
					
					AerodynamicAnlaysisApproachEnum hTailAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(hTailAnalysisTypeString);
					
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
						hTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, MethodEnum.SEMIEMPIRICAL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, MethodEnum.SEMIEMPIRICAL);
						hTailTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT, MethodEnum.SEMIEMPIRICAL);
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
					
					AerodynamicAnlaysisApproachEnum vTailAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(vTailAnalysisTypeString);
					
					switch (vTailAnalysisType) {
					case SEMIEMPIRICAL:
						vTailTaskList.put(AerodynamicAndStabilityEnum.CRITICAL_MACH, MethodEnum.KROO);
						vTailTaskList.put(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER, MethodEnum.NAPOLITANO_DATCOM); // FIXME DE-YOUNG HARPER??
						vTailTaskList.put(AerodynamicAndStabilityEnum.CL_ALPHA, MethodEnum.NASA_BLACKWELL);
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
						vTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, MethodEnum.SEMIEMPIRICAL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, MethodEnum.SEMIEMPIRICAL);
						vTailTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT, MethodEnum.SEMIEMPIRICAL);
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
					
					AerodynamicAnlaysisApproachEnum canardAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(canardAnalysisTypeString);
					
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
						canardTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS, MethodEnum.SEMIEMPIRICAL);
						canardTaskList.put(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D, MethodEnum.SEMIEMPIRICAL);
						canardTaskList.put(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT, MethodEnum.SEMIEMPIRICAL);
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
					
					AerodynamicAnlaysisApproachEnum fuselageAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(fuselageAnalysisTypeString);
					
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
					
					AerodynamicAnlaysisApproachEnum nacellesAnalysisType = AerodynamicAnlaysisApproachEnum.valueOf(nacellesAnalysisTypeString);
					
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
		boolean calculateDeltaCD0Miscellaneous = true;
		boolean calculateDeltaCD0LandingGears = false;
		double deltaCD0Miscellaneous = 0.0;
		double deltaCD0LandingGears = 0.0;
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
			
			String aircraftCalculateDeltaCD0MiscellaneousString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/total_drag_polar/delta_CD0_miscellaneous/@calculate");
			
			if(aircraftCalculateDeltaCD0MiscellaneousString != null) {
				
				if(aircraftCalculateDeltaCD0MiscellaneousString.equalsIgnoreCase("TRUE")) { 
					calculateDeltaCD0Miscellaneous = true;

					// FIXME: NEED A METHOD TO ESTIMATE ... 
					deltaCD0Miscellaneous = 0.0;
				}
				else {
					calculateDeltaCD0Miscellaneous = false;
					String aircraftDeltaCD0MiscellaneousString = MyXMLReaderUtils
							.getXMLPropertyByPath(
									reader.getXmlDoc(), reader.getXpath(),
									"//aircraft/total_drag_polar/delta_CD0_miscellaneous/@value");
					
					if(aircraftDeltaCD0MiscellaneousString != null) 
						deltaCD0Miscellaneous = Double.valueOf(aircraftDeltaCD0MiscellaneousString);
					
				}
				
			}
			
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
					
					// FIXME: NEED A METHOD TO ESTIMATE ... CHECK DRAG_CALC
					deltaCD0LandingGears = 0.0;
				}
				
			}
			
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
		
		//------------------------------------------------------------------------------------------
		// TODO: READ PLOT DATA 
		//------------------------------------------------------------------------------------------
		
		/********************************************************************************************
		 * Once the data are ready, it's possible to create the manager object. This can be created
		 * using the builder pattern.
		 */
		IACAerodynamicAndStabilityManager_v2 theAerodynamicAndStabilityBuilderInterface = new IACAerodynamicAndStabilityManager_v2.Builder()
				// TODO: ADD SETTERS
				.build();

		ACAerodynamicAndStabilityManager_v2 theAerodynamicAndStabilityManager = new ACAerodynamicAndStabilityManager_v2();
		theAerodynamicAndStabilityManager.setTheAerodynamicBuilderInterface(theAerodynamicAndStabilityBuilderInterface);

		return theAerodynamicAndStabilityManager;
		
	}
	
	private void calculateDependentData() {
		
		setWingMomentumPole(Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalWingMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				));
		
		setHTailMomentumPole(Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalHTailMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				));
		
		setVTailMomentumPole(Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalVTailMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				));
		
		setCanardMomentumPole(Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalCanardMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getCanard().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				));
		
		// TODO: CONTINUE
		
		
	}
	
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
	public List<Amount<Angle>> getAlphaHTailListWithGroundEfffect() {
		return _alphaHTailListWithGroundEfffect;
	}
	public void setAlphaHTailListWithGroundEfffect(List<Amount<Angle>> _alphaHTailListWithGroundEfffect) {
		this._alphaHTailListWithGroundEfffect = _alphaHTailListWithGroundEfffect;
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
	public List<Double> getCurrent3DWingPolarCurve() {
		return _current3DWingPolarCurve;
	}
	public void setCurrent3DWingPolarCurve(List<Double> _current3DWingPolarCurve) {
		this._current3DWingPolarCurve = _current3DWingPolarCurve;
	}
	public List<Double> getCurrent3DWingMomentCurve() {
		return _current3DWingMomentCurve;
	}
	public void setCurrent3DWingMomentCurve(List<Double> _current3DWingMomentCurve) {
		this._current3DWingMomentCurve = _current3DWingMomentCurve;
	}
	public List<Double> getCurrent3DCanardLiftCurve() {
		return _current3DCanardLiftCurve;
	}
	public void setCurrent3DCanardLiftCurve(List<Double> _current3DCanardLiftCurve) {
		this._current3DCanardLiftCurve = _current3DCanardLiftCurve;
	}
	public List<Double> getCurrent3DCanardPolarCurve() {
		return _current3DCanardPolarCurve;
	}
	public void setCurrent3DCanardPolarCurve(List<Double> _current3DCanardPolarCurve) {
		this._current3DCanardPolarCurve = _current3DCanardPolarCurve;
	}
	public List<Double> getCurrent3DCanardMomentCurve() {
		return _current3DCanardMomentCurve;
	}
	public void setCurrent3DCanardMomentCurve(List<Double> _current3DCanardMomentCurve) {
		this._current3DCanardMomentCurve = _current3DCanardMomentCurve;
	}
	public Map<Amount<Angle>, List<Double>> getCurrent3DHorizontalTailLiftCurve() {
		return _current3DHorizontalTailLiftCurve;
	}
	public void setCurrent3DHorizontalTailLiftCurve(Map<Amount<Angle>, List<Double>> _current3DHorizontalTailLiftCurve) {
		this._current3DHorizontalTailLiftCurve = _current3DHorizontalTailLiftCurve;
	}
	public Map<Amount<Angle>, List<Double>> getCurrent3DHorizontalTailPolarCurve() {
		return _current3DHorizontalTailPolarCurve;
	}
	public void setCurrent3DHorizontalTailPolarCurve(Map<Amount<Angle>, List<Double>> _current3DHorizontalTailPolarCurve) {
		this._current3DHorizontalTailPolarCurve = _current3DHorizontalTailPolarCurve;
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
	public Map<Double, Map<ComponentEnum, List<Double>>> getMomentCoefficientBreakDown() {
		return _momentCoefficientBreakDown;
	}
	public void setMomentCoefficientBreakDown(Map<Double, Map<ComponentEnum, List<Double>>> _momentCoefficientBreakDown) {
		this._momentCoefficientBreakDown = _momentCoefficientBreakDown;
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
		
}
