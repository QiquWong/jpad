package analyses;

import java.io.File;
import java.io.FileNotFoundException;
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
import javax.measure.quantity.Temperature;
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

import aircraft.Aircraft;
import analyses.fuselage.FuselageAerodynamicsManager;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import analyses.nacelles.NacelleAerodynamicsManager;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.LiftCalc;
import calculators.plots.AerodynamicPlots;
import configuration.MyConfiguration;
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
	private Amount<Temperature> _currentDeltaTemperature;
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
	private List<Amount<Angle>> _alphaBodyList = new ArrayList<>();
	private List<Amount<Angle>> _alphaWingList = new ArrayList<>();
	private List<Amount<Angle>> _alphaHTailList = new ArrayList<>();
	private List<Amount<Angle>> _alphaCanardList = new ArrayList<>();
	private List<Amount<Angle>> _alphaNacelleList = new ArrayList<>();
	private List<Amount<Angle>> _betaList = new ArrayList<>();
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
	private Map<MethodEnum, List<Amount<Length>>> _verticalDistanceZeroLiftDirectionWingHTailVariable = new HashMap<>();

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
	private List<Double> _current3DCanardLiftCurve;
	private List<Double> _current3DCanardMomentCurve;
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailLiftCurve = new HashMap<>(); //delta_e, CL
	private Map<Amount<Angle>, List<Double>> _current3DHorizontalTailMomentCurve = new HashMap<>(); //delta_e, CM
	private Map<Amount<Angle>, List<Double>> _current3DVerticalTailLiftCurve = new HashMap<>(); //delta_r CL
	private Map<Amount<Angle>, List<Double>> _totalLiftCoefficient = new HashMap<>(); //delta_e, CL
	private Map<Amount<Angle>, List<Double>> _totalDragCoefficient = new HashMap<>(); //delta_e, CD
	private Map<Double, Map<Amount<Angle>, List<Double>>> _totalMomentCoefficient = new HashMap<>(); //xcg, delta_e , CM
	private Map<Double, Map<ComponentEnum, List<Double>>> _totalMomentCoefficientBreakDown = new HashMap<>(); //xcg, component, CM

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
	private Map<Double, List<Double>> _canardEquilibriumLiftCoefficient = new HashMap<>(); //xcg, CLc_e
	private Map<Double, List<Double>> _canardEquilibriumDragCoefficient = new HashMap<>(); // xcg, CDc_e
	private Map<Double, List<Double>> _wingEquilibriumLiftCoefficient = new HashMap<>(); //xcg, CLw_e
	private Map<Double, List<Double>> _wingEquilibriumDragCoefficient = new HashMap<>(); // xcg, CDw_e
	private Map<Double, List<Double>> _horizontalTailEquilibriumLiftCoefficient = new HashMap<>(); //xcg, CLh_e
	private Map<Double, List<Double>> _horizontalTailEquilibriumDragCoefficient = new HashMap<>(); // xcg, CDh_e
	private Map<Double, List<Double>> _totalEquilibriumLiftCoefficient = new HashMap<>(); //xcg, CL_e
	private Map<Double, List<Double>> _totalEquilibriumDragCoefficient = new HashMap<>(); //xcg, CD_e
	private Map<Double, List<Double>> _totalEquilibriumEfficiencyMap = new HashMap<>(); // xcg, efficiency curve
	private Map<Double, List<Amount<Angle>>> _deltaEEquilibrium = new HashMap<>(); //xcg, de_e
	private Map<Double, Double> _totalEquilibriumMaximumEfficiencyMap = new HashMap<>(); // xcg, max efficiency
	private Map<Double, Double> _totalEquilibriumCurrentEfficiencyMap = new HashMap<>(); // xcg, efficiency @ current CL
	private Map<Double, Double> _neutralPointPositionMap = new HashMap<>(); // xcg, N0
	private Map<Double, Double> _staticStabilityMarginMap = new HashMap<>(); // xcg, SSM
	private Map<Double, Double> _maximumTrimmedLiftingCoefficientMap = new HashMap<>(); // xcg, CLmax_trim
	private Map<Double, Amount<?>> _cLAlphaEquilibriumMap = new HashMap<>(); // xcg, CL_alpha_e
	private Map<Double, Double> _cLZeroEquilibriumMap = new HashMap<>(); // xcg, CL_zero_e
	private Map<Double, Amount<?>> _cMAlphaEquilibriumMap = new HashMap<>(); // xcg, CM_alpha_e
	private Map<Double, Double> _cDZeroTotalEquilibriumMap = new HashMap<>(); // xcg, CD_zero_e
	private Double _deltaCDZeroLandingGear = 0.0;
	private Double _deltaCDZeroExcrescences = 0.0;
	private Double _deltaCDZeroInterferences = 0.0;
	private Double _deltaCDZeroCooling = 0.0;
	private Double _deltaCDZeroFlap = 0.0;
	private Double _deltaCLZeroFlap = 0.0;
	private Double _deltaCMFlap = 0.0;

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
	
	//output path
	
	private String wingPlotFolderPath = new String();
	private String horizontalTailPlotFolderPath = new String();
	private String canardPlotFolderPath = new String();
	private String verticalTailPlotFolderPath = new String();
	private String fuselagePlotFolderPath = new String();
	private String nacellePlotFolderPath = new String();
	private String aircraftPlotFolderPath = new String();

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
						xCGFuselage = theAircraft.getTheAnalysisManager().getTheBalance().getCGMap().get(ComponentEnum.FUSELAGE).getXBRF().to(SI.METER);
						zCGFuselage = theAircraft.getTheAnalysisManager().getTheBalance().getCGMap().get(ComponentEnum.FUSELAGE).getXBRF().to(SI.METER);
					}

					//---------------------------------------------------------------
					// XCG AND ZCG POSITIONS LANDING GEAR
					if(theAircraft.getLandingGears() != null) {
						xCGLandingGears = theAircraft.getTheAnalysisManager().getTheBalance().getCGMap().get(ComponentEnum.LANDING_GEAR).getXBRF().to(SI.METER);
						zCGLandingGears = theAircraft.getTheAnalysisManager().getTheBalance().getCGMap().get(ComponentEnum.LANDING_GEAR).getXBRF().to(SI.METER);
					}

					//---------------------------------------------------------------
					// XCG AND ZCG POSITIONS NACELLE
					if(theAircraft.getNacelles() != null) {
						xCGNacelles = theAircraft.getTheAnalysisManager().getTheBalance().getCGMap().get(ComponentEnum.NACELLE).getXBRF().to(SI.METER);
						zCGNacelles = theAircraft.getTheAnalysisManager().getTheBalance().getCGMap().get(ComponentEnum.NACELLE).getXBRF().to(SI.METER);
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
						System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): NACELLES ANALYSIS TYPE NOT RECOGNIZED!");
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
				
				kCD0LandingGears = Double.valueOf(aircraftKCD0LandingGearString);
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
				
				kCD0Interferences = Double.valueOf(aircraftKCD0InterferencesString);
				
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
				
				kCD0Cooling = Double.valueOf(aircraftKCD0CoolingString);
				
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
						"//aircraft/VMU/@perform");

		if(minimumUnstickSpeedPerformString.equalsIgnoreCase("TRUE") && theCondition == ConditionEnum.TAKE_OFF){

			String minimumUnstickSpeedMethodString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//aircraft/VMU/@method");

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
			if(createWingPlotProperty.equalsIgnoreCase("true"))
				createWingPlot = true;
			else 
				createWingPlot = false;

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
			}
		}
				else 
					if(createWingPlot == true) {
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'WING' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
					}
		//...............................................................
		// HTAIL:
		String createHTailPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/horizontal_tail/@perform");
		
		if(createHTailPlotProperty != null)
			if(createHTailPlotProperty.equalsIgnoreCase("true"))
				createHTailPlot = true;
			else 
				createHTailPlot = false;

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
			}
		}
				else 
					if(performHTailAnalysis == true) {
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'HORIZONTAL TAIL' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
					}
		//...............................................................
		// CANARD:
		String createCanardPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/canard/@perform");
		if(createCanardPlotProperty != null)
			if(createCanardPlotProperty.equalsIgnoreCase("true"))
				performCanardAnalysis = true;
			else 
				performCanardAnalysis = false;

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
			}
		}
				else 
					if(createCanardPlot == true) {
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'CANARD' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
					}

		//...............................................................
		// VTAIL:
		String createVTailPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/vertical_tail/@perform");
		
			if(createVTailPlotProperty != null)
				if(createVTailPlotProperty.equalsIgnoreCase("true"))
					createVTailPlot = true;
				else 
					createVTailPlot = false;
			
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
			}
		}
				else 
					if(createVTailPlot == true) {
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'VERTICAL TAIL' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
					}
		

		//...............................................................
		// FUSELAGE:
		String createFuselagePlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/fuselage/@perform");
		
		if(createFuselagePlotProperty != null)
			if(createFuselagePlotProperty.equalsIgnoreCase("true"))
				createFuselagePlot = true;
			else 
				createFuselagePlot = false;

		if(theAircraft.getFuselage() != null) {
			if(createFuselagePlot == true) {
				if(performFuselageAnalysis == true) {

					fuselagePlotList.add(AerodynamicAndStabilityPlotEnum.FUSELAGE_POLAR_CURVE);
					fuselagePlotList.add(AerodynamicAndStabilityPlotEnum.FUSELAGE_MOMENT_CURVE);

					plotMap.put(ComponentEnum.FUSELAGE, fuselagePlotList);

				}
			}
		}
				else 
					if(performFuselageAnalysis == true) {
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'FUSELAGE' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
					}
		
		//...............................................................
		// NACELLE:
		String createNacellesPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/nacelles/@perform");

		if(createNacellesPlotProperty != null)
			if(createNacellesPlotProperty.equalsIgnoreCase("true"))
				createNacellesPlot = true;
			else 
				createNacellesPlot = false;

		if(theAircraft.getNacelles() != null) {
			if(createNacellesPlot == true) {
				if(performNacellesAnalysis == true) {

					nacellesPlotList.add(AerodynamicAndStabilityPlotEnum.NACELLE_POLAR_CURVE);
					nacellesPlotList.add(AerodynamicAndStabilityPlotEnum.NACELLE_MOMENT_CURVE);

					plotMap.put(ComponentEnum.NACELLE, nacellesPlotList);

				}
			}
		}
				else 
					if(performNacellesAnalysis == true) {
					System.err.println("WARNING (IMPORT AERODYNAMIC AND STABILITY DATA): THE COMPONENT 'NACELLES' HAS NOT BEEN DEFINED! IMPOSSIBLE TO READ THE PLOT LIST!");
					}
		
		//...............................................................
		// AIRCRAFT:
		//...............................................................
		String createAircraftPlotProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//plot/aircraft/@perform");
		
		if(createAircraftPlotProperty != null)
			if(createAircraftPlotProperty.equalsIgnoreCase("true"))
				createAircraftPlot = true;
			else 
				createAircraftPlot = false;	

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
				.setLandingGearDragKFactor(kCD0LandingGears)
				.setCalculateCoolingDeltaDragCoefficient(calculateDeltaCD0Cooling)
				.setCoolingDeltaDragCoefficient(deltaCD0Cooling)
				.setCoolingDragKFactor(kCD0Cooling)
				.setCalculateExcrescencesDeltaDragCoefficient(calculatedeltaCD0Excrescences)
				.setExcrescencesDeltaDragCoefficient(deltaCD0Excrescences)
				.setExcrescencesDragKFactor(kCD0Excrescences)
				.setCalculateInterferencesDeltaDragCoefficient(calculateDeltaCD0Interferences)
				.setInterferencesDeltaDragCoefficient(deltaCD0Interferences)
				.setInterferencesDragKFactor(kCD0Interferences)
				.setTotalDragCalibrationCLScaleFactor(totalDragCalibrationCLScaleFactor)
				.setTotalDragCalibrationCDScaleFactor(totalDragCalibrationCDScaleFactor)
				.setTotalMomentCalibrationAlphaScaleFactor(totalMomentCalibrationAlphaScaleFactor)
				.setTotalMomentCalibrationCMScaleFactor(totalMomentCalibrationCMScaleFactor)
				.setCalculateWingPendularStability(aircraftWingPendularStability)
				.setWingHTailDownwashConstant(wingHTailConstantGradientFlag)
				.setCanardWingDownwashConstant(canardWingConstantGradientFlag)
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
			this._currentDeltaTemperature = _theAerodynamicBuilderInterface.getTheOperatingConditions().getDeltaTemperatureTakeOff();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaTakeOff();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaTakeOff();
			break;
		case CLIMB:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachClimb();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeClimb();
			this._currentDeltaTemperature = _theAerodynamicBuilderInterface.getTheOperatingConditions().getDeltaTemperatureClimb();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaClimb();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaClimb();
			break;
		case CRUISE:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachCruise();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeCruise();
			this._currentDeltaTemperature = _theAerodynamicBuilderInterface.getTheOperatingConditions().getDeltaTemperatureCruise();
			this._alphaBodyCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAlphaCruise();
			this._betaVTailCurrent = _theAerodynamicBuilderInterface.getTheOperatingConditions().getBetaCruise();
			break;
		case LANDING:
			this._currentMachNumber = _theAerodynamicBuilderInterface.getTheOperatingConditions().getMachLanding();
			this._currentAltitude = _theAerodynamicBuilderInterface.getTheOperatingConditions().getAltitudeLanding();
			this._currentDeltaTemperature = _theAerodynamicBuilderInterface.getTheOperatingConditions().getDeltaTemperatureLanding();
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

		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {
		_deltaEForEquilibrium = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						_theAerodynamicBuilderInterface.getTheAircraft().getHTail()
						.getSymmetricFlaps().get(0).getMinimumDeflection().doubleValue(NonSI.DEGREE_ANGLE),
						5,
						10
						), 
				NonSI.DEGREE_ANGLE
				);
		}

		if(_theAerodynamicBuilderInterface.getTheAircraft().getVTail() != null) {
		_deltaRForEquilibrium = MyArrayUtils.convertDoubleArrayToListOfAmount(
				MyArrayUtils.linspace(
						5,
						_theAerodynamicBuilderInterface.getTheAircraft().getVTail()
						.getSymmetricFlaps().get(0).getMaximumDeflection().doubleValue(NonSI.DEGREE_ANGLE),
						10
						),  
				NonSI.DEGREE_ANGLE
				);
		}
		
		if(_theAerodynamicBuilderInterface.getTheAircraft().getWing() != null) {
		_wingMomentumPole = Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalWingMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				);
		}

		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {
		_hTailMomentumPole = Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalHTailMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				);
		}

		if(_theAerodynamicBuilderInterface.getTheAircraft().getVTail() != null) {
		_vTailMomentumPole = Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalVTailMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				);
		}

		if(_theAerodynamicBuilderInterface.getTheAircraft().getCanard() != null) {
		_canardMomentumPole = Amount.valueOf(
				_theAerodynamicBuilderInterface.getAdimensionalCanardMomentumPole()
				* _theAerodynamicBuilderInterface.getTheAircraft().getCanard().getMeanAerodynamicChord().doubleValue(SI.METER)
				+ _theAerodynamicBuilderInterface.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METER),
				SI.METER
				);
		}

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

				if(_theAerodynamicBuilderInterface.getTheAircraft().getCanard() != null) {

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
							.get(_theAerodynamicBuilderInterface.isCanardWingDownwashConstant())
							.get(i)
							)
							.plus(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE)));
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
														.get(AerodynamicAndStabilityEnum.CANARD_DOWNWASH)).get(_theAerodynamicBuilderInterface.isCanardWingDownwashConstant())),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)),
								NonSI.DEGREE_ANGLE)
						).plus(
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE));
				}
				
				if(_theAerodynamicBuilderInterface.getTheAircraft().getCanard() == null) {
				
					_alphaBodyList.stream()
					.forEach(x -> {
					_alphaWingList.add(x.to(NonSI.DEGREE_ANGLE)
							.plus(
									_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE)));
				}); 

				_alphaWingCurrent = _alphaBodyCurrent.plus(
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE));
				}
			
				
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
				ACAerodynamicAndStabilityManagerUtils.calculateCurrentWingMomentCurve(this);
				
			}
		}


		//------------------------------------------------------------------------------
		// HORIZONTAL TAIL 
		//------------------------------------------------------------------------------			
		if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {
			if(_theAerodynamicBuilderInterface.isPerformHTailAnalyses() == true) {

				//------------
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.WING_DOWNWASH)) {
				_alphaBodyList.stream()
				.forEach(x -> {
					int i=_alphaBodyList.indexOf(x);
					_alphaHTailList.add(				Amount.valueOf(
							_alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
							- _downwashAngleMap
							.get(ComponentEnum.WING)
							.get(
									_theAerodynamicBuilderInterface.getComponentTaskList()
									.get(ComponentEnum.AIRCRAFT)
									.get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
									)
							.get(_theAerodynamicBuilderInterface.isWingHTailDownwashConstant())
							.get(i)
							.doubleValue(NonSI.DEGREE_ANGLE)
							+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
							NonSI.DEGREE_ANGLE
							));
					});

				_alphaHTailCurrent = _alphaBodyCurrent.minus(
						Amount.valueOf(
								MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(_alphaBodyList),
										MyArrayUtils.convertListOfAmountTodoubleArray(_downwashAngleMap
												.get(ComponentEnum.WING)
												.get(
														_theAerodynamicBuilderInterface.getComponentTaskList()
														.get(ComponentEnum.AIRCRAFT)
														.get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
														)
												.get(_theAerodynamicBuilderInterface.isWingHTailDownwashConstant())),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)),
								NonSI.DEGREE_ANGLE)
						).plus(
								_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE));	
			
				}
					else{
						_alphaBodyList.stream()
						.forEach(x -> {
						int i=_alphaBodyList.indexOf(x);
						_alphaHTailList.add(
								Amount.valueOf(
										_alphaBodyList.get(i).doubleValue(NonSI.DEGREE_ANGLE)
										- _downwashAngleMap
										.get(ComponentEnum.WING)
										.get(MethodEnum.SLINGERLAND)
										.get(Boolean.FALSE)
										.get(i)
										.doubleValue(NonSI.DEGREE_ANGLE)
										+ _theAerodynamicBuilderInterface.getTheAircraft().getHTail().getRiggingAngle().doubleValue(NonSI.DEGREE_ANGLE),
										NonSI.DEGREE_ANGLE
										)
								);
						});
						
						_alphaHTailCurrent = _alphaBodyCurrent.minus(
								Amount.valueOf(
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertListOfAmountTodoubleArray(_alphaBodyList),
												MyArrayUtils.convertListOfAmountTodoubleArray( _downwashAngleMap
														.get(ComponentEnum.WING)
														.get(MethodEnum.SLINGERLAND)
														.get(Boolean.FALSE)),
												_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)),
										NonSI.DEGREE_ANGLE)
								).plus(
										_theAerodynamicBuilderInterface.getTheAircraft().getWing().getRiggingAngle().to(NonSI.DEGREE_ANGLE));
						
					}
				

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
				ACAerodynamicAndStabilityManagerUtils.calculateCurrentHTailMomentCurve(this);
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
		
		if(_theAerodynamicBuilderInterface.getTheAircraft().getTheAnalysisManager().getPlotAerodynamicAndStability()) {

			if(_theAerodynamicBuilderInterface.getPlotList().containsKey(ComponentEnum.WING)) {
				wingPlotFolderPath = JPADStaticWriteUtils.createNewFolder(
						aerodynamicAndStabilityFolderPath 
						+ "WING"
						+ File.separator
						);
			}


			if(_theAerodynamicBuilderInterface.getPlotList().containsKey(ComponentEnum.HORIZONTAL_TAIL)) {
				horizontalTailPlotFolderPath = JPADStaticWriteUtils.createNewFolder(
						aerodynamicAndStabilityFolderPath 
						+ "HORIZONTAL_TAIL"
						+ File.separator
						);
			}

			if(_theAerodynamicBuilderInterface.getPlotList().containsKey(ComponentEnum.VERTICAL_TAIL)) {
				verticalTailPlotFolderPath = JPADStaticWriteUtils.createNewFolder(
						aerodynamicAndStabilityFolderPath 
						+ "VERTICAL_TAIL"
						+ File.separator
						);
			}

			if(_theAerodynamicBuilderInterface.getPlotList().containsKey(ComponentEnum.CANARD)) {
				canardPlotFolderPath = JPADStaticWriteUtils.createNewFolder(
						aerodynamicAndStabilityFolderPath 
						+ "CANARD"
						+ File.separator
						);
			}
			
			if(_theAerodynamicBuilderInterface.getPlotList().containsKey(ComponentEnum.FUSELAGE)) {
				fuselagePlotFolderPath = JPADStaticWriteUtils.createNewFolder(
						aerodynamicAndStabilityFolderPath 
						+ "FUSELAGE"
						+ File.separator
						);
			}

			if(_theAerodynamicBuilderInterface.getPlotList().containsKey(ComponentEnum.NACELLE)) {
				nacellePlotFolderPath = JPADStaticWriteUtils.createNewFolder(
						aerodynamicAndStabilityFolderPath 
						+ "NACELLE"
						+ File.separator
						);
			}

			if(_theAerodynamicBuilderInterface.getPlotList().containsKey(ComponentEnum.AIRCRAFT)) {
				aircraftPlotFolderPath = JPADStaticWriteUtils.createNewFolder(
						aerodynamicAndStabilityFolderPath 
						+ "AIRCRAFT"
						+ File.separator
						);
			}

			plotAllCharts();

		}

	}

	public void plotAllCharts() {

//		// TODO --> TAKE FROM PREVIOUS MANAGER
		AerodynamicPlots theAerodynamicPlot = new AerodynamicPlots();
		theAerodynamicPlot.plotAllCharts(
				_theAerodynamicBuilderInterface,
				_liftingSurfaceAerodynamicManagers,
				wingPlotFolderPath,
				horizontalTailPlotFolderPath,
				canardPlotFolderPath,
				verticalTailPlotFolderPath,
				fuselagePlotFolderPath,
				nacellePlotFolderPath,
				aircraftPlotFolderPath
				, _current3DHorizontalTailLiftCurve,
				_current3DHorizontalTailMomentCurve,
				_current3DVerticalTailLiftCurve, 
				_alphaBodyList,
				_nacelleAerodynamicManagers,
				_fuselageAerodynamicManagers,
				_downwashAngleMap,
				_downwashGradientMap,
				_totalMomentCoefficient, 
				_totalLiftCoefficient,
				_totalDragCoefficient, 
				_horizontalTailEquilibriumLiftCoefficient,
				_horizontalTailEquilibriumDragCoefficient,
				_totalEquilibriumLiftCoefficient,
				_totalEquilibriumDragCoefficient,
				_deltaEEquilibrium,
				_totalMomentCoefficientBreakDown,
				_totalEquilibriumEfficiencyMap,
				_totalEquilibriumMaximumEfficiencyMap,
				_neutralPointPositionMap,
				_staticStabilityMarginMap, 
				_deltaEForEquilibrium,
				_betaList,
				_betaOfEquilibrium, 
				_cNDueToDeltaRudder, 
				_cNTotal, 
				_cNFuselage, 
				_cNVertical,
				_cNWing
				);

	}

	@Override
	public String toString() {
		//--------------

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\n\n\t-------------------------------------\n")
				.append("\tAerodynamic and Stability Analysis " + _theAerodynamicBuilderInterface.getCurrentCondition().toString() + "\n")
				.append("\t-------------------------------------\n")
				;

		if(_theAerodynamicBuilderInterface.isPerformWingAnalyses()) {

			sb.append("\n\t-------------------------------------\n")
			.append("\tWING\n")
			.append("\t-------------------------------------\n")
			.append("\n\tGLOBAL DATA\n");

			sb.append("\t\tCritical Mach Number = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCriticalMachNumber().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)
					) + "\n");
			sb.append("\t\tAerodynamic Center (LRF) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacLRF().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
					).to(SI.METER) + "\n");
			sb.append("\t\tAerodynamic Center (MAC) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getXacMRF().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
					) + "\n");

			sb.append("\n\tLIFT\n");
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) 
				sb.append("\t\tCL_alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ALPHA)
						).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");
			sb.append("\t\tCL_zero = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLZero().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_ZERO)
					) + "\n");
			sb.append("\t\tCL_star = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStar().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_STAR)
					) + "\n");
			sb.append("\t\tCL_max = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStar().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_STAR)
					) + "\n");
			sb.append("\t\tAlpha_zero_lift = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaZeroLift().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
					).to(NonSI.DEGREE_ANGLE) + "\n");
			sb.append("\t\tAlpha_star = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStar().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
					).to(NonSI.DEGREE_ANGLE) + "\n");
			sb.append("\t\tAlpha_stall = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStall().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
					).to(NonSI.DEGREE_ANGLE) + "\n");
			sb.append("\t\tCL at Alpha " + _alphaWingCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlpha().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
					) + "\n");

			sb.append("\n\t\tLIFT CURVE\n");
			sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().stream()
					.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList()) + "\n");
			sb.append("\t\tCL = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurve().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
					)) + "\n");


			sb.append("\n\t\tLIFT DISTRIBUTIONS\n");
			sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution() + "\n");
			sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()) + "\n");

			_theAerodynamicBuilderInterface.getAlphaWingForDistribution().stream().forEach(aw -> {
				sb.append("\n\t\tALPHA = " + aw.to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\t\tCl = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tCl_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionAdditionalLoad().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tCl_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistributionBasicLoad().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tcCl = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tcCl_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionAdditionalLoad().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tcCl_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionBasicLoad().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tGamma = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tGamma_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionAdditionalLoad().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tGamma_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistributionBasicLoad().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tWing load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tAdditional load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAdditionalLoadDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tBasic load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getBasicLoadDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
						).get(aw) + "\n");
			});


			sb.append("\n\tDRAG\n");

			sb.append("\t\tCD_zero = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCD0().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD0)
					) + "\n");
			sb.append("\t\tCD_wave = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCDWave().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CD_WAVE)
					) + "\n");



			sb.append("\n\t\tDRAG DISTRIBUTIONS\n");
			sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution() + "\n");
			sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()) + "\n");

			_theAerodynamicBuilderInterface.getAlphaWingForDistribution().stream().forEach(aw -> {
				sb.append("\n\t\tALPHA = " + aw.to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\t\tCd_parasite = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getParasiteDragCoefficientDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
						).get(aw) + "\n");
				sb.append("\t\t\tDrag = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDragDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
						).get(aw) + "\n");
			});

			sb.append("\n\tPITCHING MOMENT\n");

			sb.append("\t\tCM_ac = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMac().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)
					) + "\n");
			sb.append("\t\tCM_alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMAlpha().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)
					).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");
			sb.append("\t\tCM at alpha = " + _alphaWingCurrent + " = " +  _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMAtAlpha().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)
					) + "\n");


			sb.append("\n\t\tPITCHING MOMENT CURVE\n");
			sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().stream()
					.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
					.collect(Collectors.toList()) + "\n");
			sb.append("\t\tCM = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMoment3DCurve().get(
					_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
					)) + "\n");


			sb.append("\n\t\tPITCHING MOMENT DISTRIBUTIONS\n");
			sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getYStationDistribution() + "\n");
			sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()) + "\n");

			_theAerodynamicBuilderInterface.getAlphaWingForDistribution().stream().forEach(aw -> {
				sb.append("\n\t\tALPHA = " + aw.to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\t\tCm = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficientDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
						).get(aw) + "\n");
				sb.append("\t\t\tMoment = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentDistribution().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
						).get(aw) + "\n");
			});


			if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF) 
					|| _theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING)) {

				sb.append("\n\tHIGH LIFT\n");


				sb.append("\n\t\tHIGH LIFT DEVICES EFFECTS (2D)\n");
				sb.append("\t\tDelta Cl0 (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCl0FlapList().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
						) + "\n");
				sb.append("\t\tDelta Cl0 (total) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCl0Flap().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
						) + "\n");
				sb.append("\t\tDelta Clmax (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxFlapList().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
						) + "\n");
				sb.append("\t\tDelta Clmax (all flaps) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxFlap().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
						) + "\n");

				if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxSlatList().get(MethodEnum.SEMIEMPIRICAL) != null) {

					sb.append("\t\tDelta Clmax (each slat) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxSlatList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta Clmax (all slats) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaClmaxSlat().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");


					sb.append("\n\t\tHIGH LIFT DEVICES EFFECTS (3D)\n");

					sb.append("\t\tDelta CL0 (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCL0FlapList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta CL0 (total) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCL0Flap().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta CLmax (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxFlapList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta CLmax (all flaps) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxFlap().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");

					if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlatList().get(MethodEnum.SEMIEMPIRICAL) != null) {

						sb.append("\t\tDelta CLmax (each slat) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlatList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tDelta CLmax (all slats) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCLmaxSlat().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");

					}

					sb.append("\t\tDelta CD0 (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD0List().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta CD0 (total) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCD0().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta CM_c/4 (each flap)  =" + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCMc4List().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta CM_c/4 (total) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getDeltaCMc4().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");

					sb.append("\n\t\tGLOBAL HIGH LIFT EFFECTS\n");

					sb.append("\t\tAlpha stall (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStallHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\tAlpha star (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaStarHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\tCL_max (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLMaxHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tCL_star (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLStarHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tCL_alpha (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAlphaHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");

				}


				sb.append("\n\t\tHIGH LIFT CURVE\n");
				sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().stream()
						.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList()) + "\n");
				sb.append("\t\tCL = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
						)) + "\n");


				sb.append("\n\t\tHIGH LIFT PITCHING MOMENT CURVE\n");
				sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().stream()
						.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList()) + "\n");
				sb.append("\t\tCM = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficient3DCurveHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
						)) + "\n");

				sb.append("\n\t\tCL (High Lift) at Alpha = " + _alphaWingCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCLAtAlphaHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)
						) + "\n");
				sb.append("\t\tCM (High Lift) at Alpha = " + _alphaWingCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCMAtAlphaHighLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)
						) + "\n");

			}

			if(_theAerodynamicBuilderInterface.isPerformHTailAnalyses()) {

				sb.append("\n\t-------------------------------------\n")
				.append("\tHORIZONTAL TAIL\n")
				.append("\t-------------------------------------\n")
				.append("\n\tGLOBAL DATA\n");

				sb.append("\t\tCritical Mach Number = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCriticalMachNumber().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)
						) + "\n");
				sb.append("\t\tAerodynamic Center (LRF) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacLRF().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
						).to(SI.METER) + "\n");
				sb.append("\t\tAerodynamic Center (MAC) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getXacMRF().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
						) + "\n");

				sb.append("\n\tLIFT\n");

				sb.append("\t\tCL_alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ALPHA)
						).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");
				sb.append("\t\tCL_zero = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLZero().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ZERO)
						) + "\n");
				sb.append("\t\tCL_star = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_STAR)
						) + "\n");
				sb.append("\t\tCL_max = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_STAR)
						) + "\n");
				sb.append("\t\tAlpha_zero_lift = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaZeroLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tAlpha_star = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tAlpha_stall = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaStall().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tCL at Alpha " + _alphaHTailCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCLAtAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
						) + "\n");

				sb.append("\n\t\tLIFT CURVE\n");
				sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean().stream()
						.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList()) + "\n");
				sb.append("\t\tCL = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						)) + "\n");

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)) {

					sb.append("\n\t\tLIFT DISTRIBUTIONS\n");
					sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution() + "\n");
					sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()) + "\n");

					_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().stream().forEach(ah -> {
						sb.append("\n\t\tALPHA = " + ah.to(NonSI.DEGREE_ANGLE) + "\n");
						sb.append("\t\t\tCl = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tCl_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tCl_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tcCl = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tcCl_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tcCl_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCclDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tGamma = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tGamma_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistributionAdditionalLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tGamma_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getGammaDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tWing load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tAdditional load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAdditionalLoadDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
						sb.append("\t\t\tBasic load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getBasicLoadDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(ah) + "\n");
					});
				}

				sb.append("\n\tDRAG\n");

				sb.append("\t\tCD_zero = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCD0().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD0)
						) + "\n");
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_WAVE)) 
					sb.append("\t\tCD_wave = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCDWave().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CD_WAVE)
							) + "\n");


				sb.append("\n\t\tDRAG DISTRIBUTIONS\n");
				sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution() + "\n");
				sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()) + "\n");

				_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().stream().forEach(ah -> {
					sb.append("\n\t\tALPHA = " + ah.to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\t\tCd_parasite = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getParasiteDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(ah) + "\n");
					sb.append("\t\t\tDrag = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getDragDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(ah) + "\n");
				});

				sb.append("\n\tPITCHING MOMENT\n");

				sb.append("\t\tCM_ac = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCMac().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)
						) + "\n");
				sb.append("\t\tCM_alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCMAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)
						).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");
				sb.append("\t\tCM at alpha = " + _alphaHTailCurrent + " = " +  _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getCMAtAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)
						) + "\n");


				sb.append("\n\t\tPITCHING MOMENT CURVE\n");
				sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean().stream()
						.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList()) + "\n");
				sb.append("\t\tCM = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMoment3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
						)) + "\n");

				sb.append("\n\t\tPITCHING MOMENT DISTRIBUTIONS\n");
				sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getYStationDistribution() + "\n");
				sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()) + "\n");

				_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().stream().forEach(ah -> {
					sb.append("\n\t\tALPHA = " + ah.to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\t\tCm = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(ah) + "\n");
					sb.append("\t\t\tMoment = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(ah) + "\n");
				});


				sb.append("\n\t\tELEVATOR EFFECTS\n");	
				for(int i=0; i<_theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
					sb.append("\n\tAT de = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).to(NonSI.DEGREE_ANGLE) + "\n");

					sb.append("\n\t\tLIFT CURVE WITH ELEVATOR\n");
					sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArray().stream()
							.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList()) + "\n");
					sb.append("\t\tCL = " + Arrays.toString(MyArrayUtils.convertListOfDoubleToDoubleArray(
							getCurrent3DHorizontalTailLiftCurve().get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))
							)) + "\n");


				}
			}

			if(_theAerodynamicBuilderInterface.isPerformVTailAnalyses()) {

				sb.append("\n\t-------------------------------------\n")
				.append("\tVERTICAL TAIL\n")
				.append("\t-------------------------------------\n")
				.append("\n\tGLOBAL DATA\n");

				sb.append("\t\tCritical Mach Number = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCriticalMachNumber().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)
						) + "\n");
				sb.append("\t\tAerodynamic Center (LRF) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getXacLRF().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
						).to(SI.METER) + "\n");
				sb.append("\t\tAerodynamic Center (MAC) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getXacMRF().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
						) + "\n");

				sb.append("\n\tLIFT\n");

				sb.append("\t\tCL_alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ALPHA)
						).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");
				sb.append("\t\tCL_zero = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLZero().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_ZERO)
						) + "\n");
				sb.append("\t\tCL_star = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_STAR)
						) + "\n");
				sb.append("\t\tCL_max = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_STAR)
						) + "\n");
				sb.append("\t\tAlpha_zero_lift = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaZeroLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tAlpha_star = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tAlpha_stall = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaStall().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tCL at Alpha " + _betaVTailCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCLAtAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
						) + "\n");

				sb.append("\n\t\tLIFT CURVE\n");
				sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean().stream()
						.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList()) + "\n");
				sb.append("\t\tCL = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						)) + "\n");


				sb.append("\n\t\tLIFT DISTRIBUTIONS\n");
				sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution() + "\n");
				sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()) + "\n");

				_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().stream().forEach(bv -> {
					sb.append("\n\t\tALPHA = " + bv.to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\t\tCl = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tCl_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tCl_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tcCl = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tcCl_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tcCl_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCclDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tGamma = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tGamma_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tGamma_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getGammaDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tWing load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tAdditional load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAdditionalLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
					sb.append("\t\t\tBasic load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getBasicLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(bv) + "\n");
				});

				sb.append("\n\tDRAG\n");

				sb.append("\t\tCD_zero = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCD0().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD0)
						) + "\n");
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.CD_WAVE)) 
					sb.append("\t\tCD_wave = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCDWave().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CD_WAVE)
							) + "\n");

				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).containsKey(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)) {

					sb.append("\n\t\tDRAG DISTRIBUTIONS\n");
					sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution() + "\n");
					sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()) + "\n");

					_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().stream().forEach(bv -> {
						sb.append("\n\t\tALPHA = " + bv.to(NonSI.DEGREE_ANGLE) + "\n");
						sb.append("\t\t\tCd_parasite = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getParasiteDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(bv) + "\n");
					});
				}

				sb.append("\n\tPITCHING MOMENT\n");

				sb.append("\t\tCM_ac = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCMac().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)
						) + "\n");
				sb.append("\t\tCM_alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCMAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)
						).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");
				sb.append("\t\tCM at alpha = " + _betaVTailCurrent + " = " +  _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getCMAtAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)
						) + "\n");

				sb.append("\n\t\tPITCHING MOMENT CURVE\n");
				sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean().stream()
						.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList()) + "\n");
				sb.append("\t\tCM = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMoment3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
						)) + "\n");


				sb.append("\n\t\tPITCHING MOMENT DISTRIBUTIONS\n");
				sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getYStationDistribution() + "\n");
				sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()) + "\n");

				_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().stream().forEach(bv -> {
					sb.append("\n\t\tALPHA = " + bv.to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\t\tCm = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(bv) + "\n");
					sb.append("\t\t\tMoment = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(bv) + "\n");
				});



				sb.append("\n\t\tRUDDER EFFECTS\n");	
				for(int i=0; i<_theAerodynamicBuilderInterface.getDeltaRudderList().size(); i++) {
					sb.append("\n\tAT dR = " + _theAerodynamicBuilderInterface.getDeltaRudderList().get(i).to(NonSI.DEGREE_ANGLE) + "\n");

					sb.append("\n\t\tLIFT CURVE WITH RUDDER\n");
					sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArray().stream()
							.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList()) + "\n");
					sb.append("\t\tCL = " + Arrays.toString(MyArrayUtils.convertListOfDoubleToDoubleArray(
							get_current3DVerticalTailLiftCurve().get(_theAerodynamicBuilderInterface.getDeltaRudderList().get(i))
							)) + "\n");
				}
			}

			if(_theAerodynamicBuilderInterface.isPerformCanardAnalyses()) {

				sb.append("\n\t-------------------------------------\n")
				.append("\tCANARD\n")
				.append("\t-------------------------------------\n")
				.append("\n\tGLOBAL DATA\n");

				//QUI
				sb.append("\t\tCritical Mach Number = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCriticalMachNumber().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CRITICAL_MACH)
						) + "\n");
				sb.append("\t\tAerodynamic Center (LRF) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getXacLRF().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
						).to(SI.METER) + "\n");
				sb.append("\t\tAerodynamic Center (MAC) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getXacMRF().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.AERODYNAMIC_CENTER)
						) + "\n");

				sb.append("\n\tLIFT\n");
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).containsKey(AerodynamicAndStabilityEnum.CL_ALPHA)) 
					sb.append("\t\tCL_alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLAlpha().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CL_ALPHA)
							).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");
				sb.append("\t\tCL_zero = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLZero().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CL_ZERO)
						) + "\n");
				sb.append("\t\tCL_star = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CL_STAR)
						) + "\n");
				sb.append("\t\tCL_max = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CL_STAR)
						) + "\n");
				sb.append("\t\tAlpha_zero_lift = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaZeroLift().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.ALPHA_ZERO_LIFT)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tAlpha_star = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaStar().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.ALPHA_STAR)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tAlpha_stall = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaStall().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.ALPHA_STALL)
						).to(NonSI.DEGREE_ANGLE) + "\n");
				sb.append("\t\tCL at Alpha " + _alphaWingCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLAtAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA)
						) + "\n");

				sb.append("\n\t\tLIFT CURVE\n");
				sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean().stream()
						.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList()) + "\n");
				sb.append("\t\tCL = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_CURVE_3D)
						)) + "\n");


				sb.append("\n\t\tLIFT DISTRIBUTIONS\n");
				sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getYStationDistribution() + "\n");
				sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()) + "\n");

				_theAerodynamicBuilderInterface.getAlphaWingForDistribution().stream().forEach(aw -> {
					sb.append("\n\t\tALPHA = " + aw.to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\t\tCl = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tCl_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficientDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tCl_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficientDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tcCl = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCclDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tcCl_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCclDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tcCl_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCclDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tGamma = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getGammaDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tGamma_additional = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getGammaDistributionAdditionalLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tGamma_basic = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getGammaDistributionBasicLoad().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tWing load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tAdditional load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAdditionalLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tBasic load = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getBasicLoadDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
							).get(aw) + "\n");
				});


				sb.append("\n\tDRAG\n");

				sb.append("\t\tCD_zero = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCD0().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CD0)
						) + "\n");
				sb.append("\t\tCD_wave = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCDWave().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CD_WAVE)
						) + "\n");



				sb.append("\n\t\tDRAG DISTRIBUTIONS\n");
				sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getYStationDistribution() + "\n");
				sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()) + "\n");

				_theAerodynamicBuilderInterface.getAlphaWingForDistribution().stream().forEach(aw -> {
					sb.append("\n\t\tALPHA = " + aw.to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\t\tCd_parasite = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getParasiteDragCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(aw) + "\n");
					sb.append("\t\t\tDrag = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDragDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
							).get(aw) + "\n");
				});

				sb.append("\n\tPITCHING MOMENT\n");

				sb.append("\t\tCM_ac = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCMac().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CM_AC_LIFTING_SURFACE)
						) + "\n");
				sb.append("\t\tCM_alpha = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCMAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CM_ALPHA_LIFTING_SURFACE)
						).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");
				sb.append("\t\tCM at alpha = " + _alphaWingCurrent + " = " +  _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCMAtAlpha().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_LIFTING_SURFACE)
						) + "\n");


				sb.append("\n\t\tPITCHING MOMENT CURVE\n");
				sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArrayClean().stream()
						.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
						.collect(Collectors.toList()) + "\n");
				sb.append("\t\tCM = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMoment3DCurve().get(
						_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_LIFTING_SURFACE)
						)) + "\n");


				sb.append("\n\t\tPITCHING MOMENT DISTRIBUTIONS\n");
				sb.append("\t\ty stations = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getYStationDistribution() + "\n");
				sb.append("\t\teta stations = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getEtaStationDistribution()) + "\n");

				_theAerodynamicBuilderInterface.getAlphaWingForDistribution().stream().forEach(aw -> {
					sb.append("\n\t\tALPHA = " + aw.to(NonSI.DEGREE_ANGLE) + "\n");
					sb.append("\t\t\tCm = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMomentCoefficientDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(aw) + "\n");
					sb.append("\t\t\tMoment = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMomentDistribution().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
							).get(aw) + "\n");
				});


				if(_theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.TAKE_OFF) 
						|| _theAerodynamicBuilderInterface.getCurrentCondition().equals(ConditionEnum.LANDING)) {

					sb.append("\n\tHIGH LIFT\n");


					sb.append("\n\t\tHIGH LIFT DEVICES EFFECTS (2D)\n");
					sb.append("\t\tDelta Cl0 (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCl0FlapList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta Cl0 (total) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCl0Flap().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta Clmax (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaClmaxFlapList().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");
					sb.append("\t\tDelta Clmax (all flaps) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaClmaxFlap().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
							) + "\n");

					if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaClmaxSlatList().get(MethodEnum.SEMIEMPIRICAL) != null) {

						sb.append("\t\tDelta Clmax (each slat) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaClmaxSlatList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tDelta Clmax (all slats) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaClmaxSlat().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");


						sb.append("\n\t\tHIGH LIFT DEVICES EFFECTS (3D)\n");

						sb.append("\t\tDelta CL0 (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCL0FlapList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tDelta CL0 (total) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCL0Flap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tDelta CLmax (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCLmaxFlapList().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tDelta CLmax (all flaps) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCLmaxFlap().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");

						if(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCLmaxSlatList().get(MethodEnum.SEMIEMPIRICAL) != null) {

							sb.append("\t\tDelta CLmax (each slat) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCLmaxSlatList().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									) + "\n");
							sb.append("\t\tDelta CLmax (all slats) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCLmaxSlat().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
									) + "\n");

						}

						sb.append("\t\tDelta CD0 (each flap) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCD0List().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tDelta CD0 (total) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCD0().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tDelta CM_c/4 (each flap)  =" + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCMc4List().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tDelta CM_c/4 (total) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getDeltaCMc4().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");

						sb.append("\n\t\tGLOBAL HIGH LIFT EFFECTS\n");

						sb.append("\t\tAlpha stall (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaStallHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).to(NonSI.DEGREE_ANGLE) + "\n");
						sb.append("\t\tAlpha star (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaStarHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).to(NonSI.DEGREE_ANGLE) + "\n");
						sb.append("\t\tCL_max (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLMaxHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tCL_star (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLStarHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								) + "\n");
						sb.append("\t\tCL_alpha (High Lift) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLAlphaHighLift().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_DEVICES_EFFECTS)
								).to(NonSI.DEGREE_ANGLE.inverse()) + "\n");

					}


					sb.append("\n\t\tHIGH LIFT CURVE\n");
					sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArray().stream()
							.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList()) + "\n");
					sb.append("\t\tCL = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getLiftCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_CURVE_3D)
							)) + "\n");


					sb.append("\n\t\tHIGH LIFT PITCHING MOMENT CURVE\n");
					sb.append("\t\tAlpha (deg) = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getAlphaArray().stream()
							.map(aw -> aw.doubleValue(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList()) + "\n");
					sb.append("\t\tCM = " + Arrays.toString(_liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getMomentCoefficient3DCurveHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.HIGH_LIFT_MOMENT_CURVE_3D)
							)) + "\n");

					sb.append("\n\t\tCL (High Lift) at Alpha = " + _alphaWingCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCLAtAlphaHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CL_AT_ALPHA_HIGH_LIFT)
							) + "\n");
					sb.append("\t\tCM (High Lift) at Alpha = " + _alphaWingCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _liftingSurfaceAerodynamicManagers.get(ComponentEnum.CANARD).getCMAtAlphaHighLift().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.CANARD).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_HIGH_LIFT)
							) + "\n");

				}
			}
					
			if(_theAerodynamicBuilderInterface.isPerformFuselageAnalyses()) {
				
				sb.append("\n\t-------------------------------------\n")
				.append("\tFUSELAGE\n")
				.append("\t-------------------------------------\n")
				.append("\n\tDRAG\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_PARASITE_FUSELAGE)) 
					sb.append("\t\tCD0_parasite = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Parasite().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_PARASITE_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_BASE_FUSELAGE)) 
					sb.append("\t\tCD0_base = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Base().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_BASE_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_UPSWEEP_FUSELAGE)) 
					sb.append("\t\tCD0_upsweep = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Upsweep().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_UPSWEEP_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_WINDSHIELD_FUSELAGE)) 
					sb.append("\t\tCD0_windshield = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Windshield().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_WINDSHIELD_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD0_TOTAL_FUSELAGE)) 
					sb.append("\t\tCD0_total = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCD0Total().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD0_TOTAL_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)) 
					sb.append("\t\tCD_induced at Alpha " + _alphaBodyCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCDInduced().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD_INDUCED_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE)) 
					sb.append("\t\tCD at Alpha " + _alphaBodyCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCDAtAlpha().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_FUSELAGE)
							) + "\n");
				
				sb.append("\n\tPITCHING MOMENT\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM0_FUSELAGE)) 
					sb.append("\t\tCM0 = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCM0().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM0_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_FUSELAGE)) 
					sb.append("\t\tCM_alpha = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCMAlpha().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM_ALPHA_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE)) 
					sb.append("\t\tCM at alpha " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg = " + _fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getCMAtAlpha().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_FUSELAGE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)) {

					sb.append("\n\t\tPITCHING MOMENT CURVE\n");
					sb.append("\t\tAlpha (deg) = " + _alphaBodyList.stream().map(a -> a.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()) + "\n");
					sb.append("\t\tCM = " + Arrays.toString(_fuselageAerodynamicManagers.get(ComponentEnum.FUSELAGE).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.FUSELAGE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_FUSELAGE)
							)) + "\n");

				}
				
			}
			
			if(_theAerodynamicBuilderInterface.isPerformNacelleAnalyses()) {
				
				sb.append("\n\t-------------------------------------\n")
				.append("\tNACELLE\n")
				.append("\t-------------------------------------\n")
				.append("\n\tDRAG\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_PARASITE_NACELLE)) 
					sb.append("\t\tCD0_parasite = " + _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCD0Parasite().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_PARASITE_NACELLE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_BASE_NACELLE)) 
					sb.append("\t\tCD0_base = " + _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCD0Base().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_BASE_NACELLE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD0_TOTAL_NACELLE)) 
					sb.append("\t\tCD0_total = " + _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCD0Total().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD0_TOTAL_NACELLE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD_INDUCED_NACELLE)) 
					sb.append("\t\tCD_induced at Alpha " + _alphaNacelleCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCDInduced().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD_INDUCED_NACELLE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE)) 
					sb.append("\t\tCD at Alpha " + _alphaNacelleCurrent.to(NonSI.DEGREE_ANGLE) + " = " + _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCDAtAlpha().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CD_AT_ALPHA_NACELLE)
							) + "\n");

				sb.append("\n\tPITCHING MOMENT\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM0_NACELLE)) 
					sb.append("\t\tCM0 = " + _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCM0().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM0_NACELLE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM_ALPHA_NACELLE)) 
					sb.append("\t\tCM_alpha = " + _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCMAlpha().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM_ALPHA_NACELLE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE)) 
					sb.append("\t\tCM at alpha " + _alphaNacelleCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg = " + _nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getCMAtAlpha().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.CM_AT_ALPHA_NACELLE)
							) + "\n");
				
				if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).containsKey(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)) {

					sb.append("\n\t\tPITCHING MOMENT CURVE\n");
					sb.append("\t\tAlpha (deg) = " + _alphaNacelleList.stream().map(a -> a.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()) + "\n");
					sb.append("\t\tCM = " + Arrays.toString(_nacelleAerodynamicManagers.get(ComponentEnum.NACELLE).getMoment3DCurve().get(
							_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.NACELLE).get(AerodynamicAndStabilityEnum.MOMENT_CURVE_3D_NACELLE)
							)) + "\n");

				}
			}
			
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.WING_DOWNWASH)) {
				
				if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {
					sb.append("\n\t-------------------------------------\n")
					.append("\tDOWNWASH\n")
					.append("\t-------------------------------------\n");

					sb.append("\n\tAlpha body (deg) = " + _alphaBodyList.stream().map(a -> a.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()) + "\n");

					if(_theAerodynamicBuilderInterface.isWingHTailDownwashConstant()) {

						sb.append("\n\tLINEAR\n");

						sb.append("\t\tDownwash gradient " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " (1/deg) = "
								+ _downwashGradientMap.get(Boolean.TRUE).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										) + "\n");

						sb.append("\t\tDownwash angle " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " (deg) = "
								+ _downwashAngleMap.get(Boolean.TRUE).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										) + "\n");

						sb.append("\n\t\tDownwash gradient " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " at alpha " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " (deg) = "
								+ MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												_alphaBodyList.stream()
												.map(a -> a.to(NonSI.DEGREE_ANGLE))
												.collect(Collectors.toList())
												),
										_downwashGradientMap.get(ComponentEnum.WING).get(Boolean.TRUE).get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
												).stream()
										.mapToDouble(dg -> dg)
										.toArray(),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
										) + "\n");

						sb.append("\t\tDownwash angle " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " at alpha " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " (deg) = "
								+ MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												_alphaBodyList.stream()
												.map(a -> a.to(NonSI.DEGREE_ANGLE))
												.collect(Collectors.toList())
												),
										_downwashAngleMap.get(ComponentEnum.WING).get(Boolean.TRUE).get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
												).stream()
										.mapToDouble(dg -> dg.doubleValue(NonSI.DEGREE_ANGLE))
										.toArray(),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
										) + "\n");

					}
					else {

						sb.append("\n\tLINEAR\n");

						sb.append("\t\tDownwash gradient " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " (1/deg) = "
								+ _downwashGradientMap.get(ComponentEnum.WING).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).get(Boolean.TRUE) + "\n");

						sb.append("\t\tDownwash angle " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " (deg) = "
								+ _downwashAngleMap.get(ComponentEnum.WING).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).get(Boolean.TRUE) + "\n");

						sb.append("\n\t\tDownwash gradient " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " at alpha " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " (deg) = "
								+ MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												_alphaBodyList.stream()
												.map(a -> a.to(NonSI.DEGREE_ANGLE))
												.collect(Collectors.toList())
												),
										_downwashGradientMap.get(ComponentEnum.WING).get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
												).get(Boolean.TRUE).stream()
										.mapToDouble(dg -> dg)
										.toArray(),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
										) + "\n");

						sb.append("\t\tDownwash angle " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " at alpha " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " (deg) = "
								+ MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												_alphaBodyList.stream()
												.map(a -> a.to(NonSI.DEGREE_ANGLE))
												.collect(Collectors.toList())
												),
										_downwashAngleMap.get(ComponentEnum.WING).get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
												).get(Boolean.TRUE).stream()
										.mapToDouble(dg -> dg.doubleValue(NonSI.DEGREE_ANGLE))
										.toArray(),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
										) + "\n");

						sb.append("\n\tNON LINEAR\n");

						sb.append("\t\tDownwash gradient " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " (1/deg) = "
								+ _downwashGradientMap.get(ComponentEnum.WING).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).get(Boolean.FALSE) + "\n");

						sb.append("\t\tDownwash angle " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " (deg) = "
								+ _downwashAngleMap.get(ComponentEnum.WING).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).get(Boolean.FALSE) + "\n");

						sb.append("\n\t\tDownwash gradient " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " at alpha " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " (deg) = "
								+ MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												_alphaBodyList.stream()
												.map(a -> a.to(NonSI.DEGREE_ANGLE))
												.collect(Collectors.toList())
												),
										_downwashGradientMap.get(ComponentEnum.WING).get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
												).get(Boolean.FALSE).stream()
										.mapToDouble(dg -> dg)
										.toArray(),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
										) + "\n");

						sb.append("\t\tDownwash angle " +
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " at alpha " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " (deg) = "
								+ MyMathUtils.getInterpolatedValue1DLinear(
										MyArrayUtils.convertListOfAmountTodoubleArray(
												_alphaBodyList.stream()
												.map(a -> a.to(NonSI.DEGREE_ANGLE))
												.collect(Collectors.toList())
												),
										_downwashAngleMap.get(ComponentEnum.WING).get(
												_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
												).get(Boolean.FALSE).stream()
										.mapToDouble(dg -> dg.doubleValue(NonSI.DEGREE_ANGLE))
										.toArray(),
										_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
										) + "\n");

					}
				}
				else
					System.err.println("WARNING!! DOWNWASH ANGLE AND DOWNWASH GRADIENT CANNOT BE WRITTEN SINCE THERE IS NO HORIZONTAL TAIL ... ");
			}
			
			
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL)) {
				
				sb.append("\n\t-------------------------------------\n")
				.append("\tTOTAL LIFT COEFFICENT\n")
				.append("\t-------------------------------------\n");
				
				sb.append("\n\tAlpha body (deg) = " + _alphaBodyList.stream().map(a -> a.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()) + "\n");
				
				for (int i = 0; i < _theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
					
					sb.append("\n\tDELTA ELEVATOR = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg\n");
					sb.append("\t\tCL horizontal tail = " + _current3DHorizontalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)) + "\n");
					sb.append("\t\tCL total = " + _totalLiftCoefficient.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)) + "\n");
					
				}
			}
			
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL)) {
				
				sb.append("\n\t-------------------------------------\n")
				.append("\tTOTAL DRAG COEFFICENT\n")
				.append("\t-------------------------------------\n");
				
				sb.append("\n\tAlpha body (deg) = " + _alphaBodyList.stream().map(a -> a.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()) + "\n");
				
				for (int i = 0; i < _theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
					
					sb.append("\n\tDELTA ELEVATOR = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg\n");
					sb.append("\t\tCD total = " + _totalDragCoefficient.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)) + "\n");
					
				}
			}
			
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {
				
				sb.append("\n\t-------------------------------------\n")
				.append("\tTOTAL PITCHING MOMENT COEFFICENT\n")
				.append("\t-------------------------------------\n");
				
				sb.append("\n\tAlpha body (deg) = " + _alphaBodyList.stream().map(a -> a.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()) + "\n");
				
				for (int i = 0; i < _theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {
					
					sb.append("\n\tDELTA ELEVATOR = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg\n");
					sb.append("\t\tCM horizontal tail = " + _current3DHorizontalTailMomentCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)) + "\n");
					
					for (int j = 0; j < _theAerodynamicBuilderInterface.getXCGAircraft().size(); j++) {
						
						sb.append("\n\t\tXcg/C = " + _theAerodynamicBuilderInterface.getXCGAircraft().get(j) + "\n");
						sb.append("\t\t\tCM total = " + _totalMomentCoefficient
								.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(j))
								.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)) + "\n");
						
					}
				}
			}
			
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {
				
				sb.append("\n\t-------------------------------------\n")
				.append("\tLONGITUDINAL STABILITY AND CONTROL\n")
				.append("\t-------------------------------------\n");
			
				sb.append("\n\tAlpha body (deg) = " + _alphaBodyList.stream().map(a -> a.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()) + "\n");
				
				for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {
					
					int indexOfFirstMaximumDeltaElevatorOfEquilibrium = _alphaBodyList.size()-1;
					
					for(int j=0; j<_deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size(); j++)
						if(_deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j)
								.equals(_deltaEForEquilibrium.get(0))
								) {
							indexOfFirstMaximumDeltaElevatorOfEquilibrium = j;
							break;
						}
					
					sb.append("\n\tXcg/C = " + _theAerodynamicBuilderInterface.getXCGAircraft().get(i) + "\n");
					sb.append("\t\tCL_h equilibrium = " + _horizontalTailEquilibriumLiftCoefficient.get(
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
							).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium+1)+ "\n");
					sb.append("\t\tCL_total equilibrium = " + _totalEquilibriumLiftCoefficient.get(
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
							).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium+1) + "\n");
					sb.append("\t\tCD_h equilibrium = " + _horizontalTailEquilibriumDragCoefficient.get(
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
							).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium+1) + "\n");
					sb.append("\t\tCD_total equilibrium = " + _totalEquilibriumDragCoefficient.get(
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
							).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium+1) + "\n");
					sb.append("\t\tdelta_e equilibrium (deg) = " + _deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).stream().map(
							dee -> dee.doubleValue(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList()).subList(0, indexOfFirstMaximumDeltaElevatorOfEquilibrium+1) 
							+ "\n");
					sb.append("\t\tNeutral Point = " + _neutralPointPositionMap.get(
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
							));
					sb.append("\t\tStatic Stability Margin = " + _staticStabilityMarginMap.get(
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
							));
					sb.append("\t\tEfficiency = " + _totalEquilibriumEfficiencyMap.get(
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
							));
					sb.append("\t\tMaximum efficiency = " + _totalEquilibriumMaximumEfficiencyMap.get(
							_theAerodynamicBuilderInterface.getXCGAircraft().get(i)
							) + "\n");
					

				}
			}
			
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY)) {
				
				sb.append("\n\t-------------------------------------\n")
				.append("\tDIRECTIONAL STABILITY AND CONTROL\n")
				.append("\t-------------------------------------\n");
				
				sb.append("\n\tBeta (deg) = " + _betaList.stream().map(b -> b.doubleValue(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()) + "\n");
				
				for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {
					
					sb.append("\n\tXcg/C = " + _theAerodynamicBuilderInterface.getXCGAircraft().get(i) + "\n");
					
					sb.append("\t\tCN_beta_vertical_tail = " + _cNBetaVertical.get(i)._2() + "\n");
					
					sb.append("\t\tCN_beta_fuselage = " + _cNBetaFuselage.get(i)._2() + "\n");
					
					sb.append("\t\tCN_beta_wing = " + _cNBetaWing.get(i)._2() + "\n");
					
					sb.append("\t\tCN_beta_total = " + _cNBetaTotal.get(i)._2() + "\n");
					
					sb.append("\n\t\tDelta_rudder (deg) = " + _theAerodynamicBuilderInterface.getDeltaRudderList().stream().map(
							dr -> dr.doubleValue(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList()) 
							+ "\n");
					
					sb.append("\t\tCN_delta_rudder = "); 
					for(int j=0; j<_theAerodynamicBuilderInterface.getDeltaRudderList().size(); j++) 
						sb.append("[" + _cNDeltaR.get(_theAerodynamicBuilderInterface.getDeltaRudderList().get(j)).get(i)._2() + ", ");
					
					sb.append("]\n\n\t\tCN_vertical_tail = " + _cNVertical.get(i)._2()
							+ "\n");
					
					sb.append("\t\tCN_fuselage = " + _cNFuselage.get(i)._2()
							+ "\n");
					
					sb.append("\t\tCN_wing = " + _cNWing.get(i)._2()
							+ "\n");
					
					sb.append("\t\tCN_total = " + _cNTotal.get(i)._2()
							+ "\n\n");
					
					for (int j = 0; j < _theAerodynamicBuilderInterface.getDeltaRudderList().size(); j++) {
						
						sb.append("\t\tCN at delta rudder = " + 
								_theAerodynamicBuilderInterface.getDeltaRudderList().get(j).doubleValue(NonSI.DEGREE_ANGLE) + " deg = " +
								_cNDueToDeltaRudder.get(_theAerodynamicBuilderInterface.getDeltaRudderList().get(j)).get(i)._2()
								+ "\n");
						
					}
					
					sb.append("\n\t\tDelta_rudder equilibrium (deg) = " + _betaOfEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).stream()
							.map(dr -> dr._1().doubleValue(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList())
							+ "\n");
					sb.append("\t\tBeta equilibrium (deg) = " + _betaOfEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).stream()
							.map(dr -> dr._2().doubleValue(NonSI.DEGREE_ANGLE))
							.collect(Collectors.toList())
							+ "\n");
				}
			}
			
			/*
			 * TODO : CONTINUE WITH ALL THE MISSING ANALYSES !!
			 */
		
		//-----------------
			}
		return sb.toString();
	}

	public void toXLS(String filenameWithPathAndExt) throws IOException {

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
			if(_theAerodynamicBuilderInterface.isPerformWingAnalyses()) {

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

					Object[] liftCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().size()+2];
					liftCurveAlpha[0] = "Alpha";
					liftCurveAlpha[1] = "deg";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().size(); i++) 
						liftCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().get(i).doubleValue(NonSI.DEGREE_ANGLE);

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
					
					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()[i];

					dataListWing.add(etaStationsLift);

					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaWingForDistribution().size(); i++) {

						dataListWing.add(new Object[] {""});
						dataListWing.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListWing.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
						cLDistribution[0] = "Cl";
						cLDistribution[1] = "";
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
						cLAdditionalDistribution[1] = "";
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
						cLBasicDistribution[1] = "";
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
						cCLDistribution[1] = "m";
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
						cCLAdditionalDistribution[1] = "m";
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
						Object[] cCLBasicDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionBasicLoad().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
						cCLBasicDistribution[0] = "cCl_basic";
						cCLBasicDistribution[1] = "m";
						for(int j=0; 
								j<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionBasicLoad().get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
										).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size(); 
								j++) 
							cCLBasicDistribution[j+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getCclDistributionBasicLoad().get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
									).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).get(j).doubleValue(SI.METER);
						dataListWing.add(cCLBasicDistribution);
						//..................................................................................................................................................
						Object[] gammaDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getGammaDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
						gammaDistribution[0] = "Gamma";
						gammaDistribution[1] = "";
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
						gammaAdditionalDistribution[1] = "";
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
						gammaBasicDistribution[1] = "";
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
						wingLoadDistribution[1] = "N";
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
						wingAdditionalLoadDistribution[1] = "N";
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
						wingBasicLoadDistribution[1] = "N";
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
							"CD_induced at Alpha = " + _alphaWingCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
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

					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()[i];

					dataListWing.add(etaStationsLift);
					
					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaWingForDistribution().size(); i++) {

						dataListWing.add(new Object[] {""});
						dataListWing.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListWing.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cDParasiteDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getParasiteDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
						cDParasiteDistribution[0] = "Cd_parasite";
						cDParasiteDistribution[1] = "";
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
						cDInducedDistribution[1] = "";
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
						cDDistribution[1] = "";
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
						dragDistribution[1] = "N";
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

					Object[] momentCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().size()+2];
					momentCurveAlpha[0] = "Alpha";
					momentCurveAlpha[1] = "deg";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().size(); i++) 
						momentCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArrayClean().get(i).doubleValue(NonSI.DEGREE_ANGLE);

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

					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getEtaStationDistribution()[i];

					dataListWing.add(etaStationsLift);
					
					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaWingForDistribution().size(); i++) {

						dataListWing.add(new Object[] {""});
						dataListWing.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListWing.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cMDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.WING).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getAlphaWingForDistribution().get(i)).size()+2];
						cMDistribution[0] = "Cm";
						cMDistribution[1] = "";
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
						momentDistribution[1] = "N";
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

						Object[] highLiftCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().size()+2];
						highLiftCurveAlpha[0] = "Alpha";
						highLiftCurveAlpha[1] = "deg";
						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().size(); i++) 
							highLiftCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().get(i).doubleValue(NonSI.DEGREE_ANGLE);

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

						Object[] highLiftMomentCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().size()+2];
						highLiftMomentCurveAlpha[0] = "Alpha";
						highLiftMomentCurveAlpha[1] = "deg";
						for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().size(); i++) 
							highLiftMomentCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.WING).getAlphaArray().get(i).doubleValue(NonSI.DEGREE_ANGLE);

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
			if(_theAerodynamicBuilderInterface.isPerformHTailAnalyses()) {

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

					Object[] liftCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean().size()+2];
					liftCurveAlpha[0] = "Alpha";
					liftCurveAlpha[1] = "deg";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean().size(); i++) 
						liftCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean().get(i).doubleValue(NonSI.DEGREE_ANGLE);

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

					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()[i];

					dataListHTail.add(etaStationsLift);
					
					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().size(); i++) {

						dataListHTail.add(new Object[] {""});
						dataListHTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListHTail.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
						cLDistribution[0] = "Cl";
						cLDistribution[1] = "";
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
						cLAdditionalDistribution[1] = "";
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
						cLBasicDistribution[1] = "";
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
						cCLDistribution[1] = "m";
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
						cCLAdditionalDistribution[1] = "m";
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
						cCLBasicDistribution[1] = "m";
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
						gammaDistribution[1] = "";
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
						gammaAdditionalDistribution[1] = "";
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
						gammaBasicDistribution[1] = "";
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
						wingLoadDistribution[1] = "N";
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
						wingAdditionalLoadDistribution[1] = "N";
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
						wingBasicLoadDistribution[1] = "N";
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
							"CD_induced at Alpha = " + _alphaHTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
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

					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()[i];

					dataListHTail.add(etaStationsLift);
					
					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().size(); i++) {

						dataListHTail.add(new Object[] {""});
						dataListHTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListHTail.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cDParasiteDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getParasiteDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
						cDParasiteDistribution[0] = "Cd_parasite";
						cDParasiteDistribution[1] = "";
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
						cDInducedDistribution[1] = "";
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
						cDDistribution[1] = "";
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
						dragDistribution[1] = "N";
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

					Object[] momentCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean().size()+2];
					momentCurveAlpha[0] = "Alpha";
					momentCurveAlpha[1] = "deg";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean().size(); i++) 
						momentCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getAlphaArrayClean().get(i).doubleValue(NonSI.DEGREE_ANGLE);

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

					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getEtaStationDistribution()[i];

					dataListHTail.add(etaStationsLift);
					
					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().size(); i++) {

						dataListHTail.add(new Object[] {""});
						dataListHTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListHTail.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cMDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.HORIZONTAL_TAIL).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.HORIZONTAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getAlphaHorizontalTailForDistribution().get(i)).size()+2];
						cMDistribution[0] = "Cm";
						cMDistribution[1] = "";
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
						momentDistribution[1] = "N";
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
			if(_theAerodynamicBuilderInterface.isPerformVTailAnalyses()) {

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

					Object[] liftCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean().size()+2];
					liftCurveAlpha[0] = "Alpha";
					liftCurveAlpha[1] = "deg";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean().size(); i++) 
						liftCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean().get(i).doubleValue(NonSI.DEGREE_ANGLE);

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

					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()[i];

					dataListVTail.add(etaStationsLift);
					
					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().size(); i++) {

						dataListVTail.add(new Object[] {""});
						dataListVTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListVTail.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cLDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getLiftCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.LIFT_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
						cLDistribution[0] = "Cl";
						cLDistribution[1] = "";
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
						cLAdditionalDistribution[1] = "";
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
						cLBasicDistribution[1] = "";
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
						cCLDistribution[1] = "m";
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
						cCLAdditionalDistribution[1] = "m";
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
						cCLBasicDistribution[1] = "m";
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
						gammaDistribution[1] = "";
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
						gammaAdditionalDistribution[1] = "";
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
						gammaBasicDistribution[1] = "";
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
						wingLoadDistribution[1] = "N";
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
						wingAdditionalLoadDistribution[1] = "N";
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
						wingBasicLoadDistribution[1] = "N";
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
							"CD_induced at Alpha = " + _betaVTailCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
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
					
					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()[i];

					dataListVTail.add(etaStationsLift);
					
					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().size(); i++) {

						dataListVTail.add(new Object[] {""});
						dataListVTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListVTail.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cDParasiteDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getParasiteDragCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.DRAG_DISTRIBUTION)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
						cDParasiteDistribution[0] = "Cd_parasite";
						cDParasiteDistribution[1] = "";
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
						cDInducedDistribution[1] = "";
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
						cDDistribution[1] = "";
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
						dragDistribution[1] = "N";
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

					Object[] momentCurveAlpha = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean().size()+2];
					momentCurveAlpha[0] = "Alpha";
					momentCurveAlpha[1] = "deg";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean().size(); i++) 
						momentCurveAlpha[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getAlphaArrayClean().get(i).doubleValue(NonSI.DEGREE_ANGLE);

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

					Object[] etaStationsLift = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution().length+2];
					etaStationsLift[0] = "eta stations";
					etaStationsLift[1] = "";
					for(int i=0; i<_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution().length; i++) 
						etaStationsLift[i+2] = _liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getEtaStationDistribution()[i];

					dataListVTail.add(etaStationsLift);
					
					currentBoldIndex = currentBoldIndex+4;

					for(int i=0; i<_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().size(); i++) {

						dataListVTail.add(new Object[] {""});
						dataListVTail.add(new Object[] {"Alpha = " + _theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
						dataListVTail.add(new Object[] {""});

						//..................................................................................................................................................
						Object[] cMDistribution = new Object[_liftingSurfaceAerodynamicManagers.get(ComponentEnum.VERTICAL_TAIL).getMomentCoefficientDistribution().get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.VERTICAL_TAIL).get(AerodynamicAndStabilityEnum.MOMENT_DISTRIBUTION_LIFTING_SURFACE)
								).get(_theAerodynamicBuilderInterface.getBetaVerticalTailForDistribution().get(i)).size()+2];
						cMDistribution[0] = "Cm";
						cMDistribution[1] = "";
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
						momentDistribution[1] = "N";
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
			if(_theAerodynamicBuilderInterface.isPerformFuselageAnalyses()) {

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
							"CD_induced at Alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
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
			if(_theAerodynamicBuilderInterface.isPerformNacelleAnalyses()) {

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
							"CD_induced at Alpha = " + _alphaNacelleCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
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

			//--------------------------------------------------------------------------------
			// DOWNWASH ANALYSIS RESULTS:
			//--------------------------------------------------------------------------------
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.WING_DOWNWASH)) {

				if(_theAerodynamicBuilderInterface.getTheAircraft().getHTail() != null) {

					Sheet downwashSheet = wb.createSheet("DOWNWASH");
					List<Object[]> dataListDownwash = new ArrayList<>();

					List<Integer> boldRowIndex = new ArrayList<>();
					int currentBoldIndex = 1;

					dataListDownwash.add(new Object[] {"Description","Unit","Value"});

					Object[] alphaBodyArray = new Object[getAlphaBodyList().size()+2];
					alphaBodyArray[0] = "Alpha body";
					alphaBodyArray[1] = "deg";
					for(int i=0; i<getAlphaBodyList().size(); i++) 
						alphaBodyArray[i+2] = getAlphaBodyList().get(i).doubleValue(NonSI.DEGREE_ANGLE);			

					dataListDownwash.add(alphaBodyArray);
					dataListDownwash.add(new Object[] {""});
					currentBoldIndex = currentBoldIndex+2;

					//-----------------------------------------------------------------------------
					// CONSTANT GRADIENT
					if(_theAerodynamicBuilderInterface.isWingHTailDownwashConstant()) {

						dataListDownwash.add(new Object[] {"LINEAR"});
						currentBoldIndex = currentBoldIndex+1;
						boldRowIndex.add(currentBoldIndex);

						Object[] downwashGradientArray = new Object[_downwashGradientMap.get(Boolean.TRUE).get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
								).size()+2];
						downwashGradientArray[0] = "Downwash gradient (" 
								+ _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ ")";
						downwashGradientArray[1] = "";
						for(int i=0; 
								i<_downwashGradientMap.get(Boolean.TRUE).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).size();
								i++) 
							downwashGradientArray[i+2] = _downwashGradientMap.get(Boolean.TRUE).get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
									).get(i);

						dataListDownwash.add(downwashGradientArray);
						currentBoldIndex = currentBoldIndex+1;

						Object[] downwashAngleArray = new Object[_downwashAngleMap.get(Boolean.TRUE).get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
								).size()+2];
						downwashAngleArray[0] = "Downwash angle (" 
								+ _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ " )";
						downwashAngleArray[1] = "deg";
						for(int i=0; 
								i<_downwashAngleMap.get(Boolean.TRUE).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).size();
								i++) 
							downwashAngleArray[i+2] = _downwashAngleMap.get(ComponentEnum.WING).get(Boolean.TRUE).get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
									).get(i).doubleValue(NonSI.DEGREE_ANGLE);

						dataListDownwash.add(downwashAngleArray);
						currentBoldIndex = currentBoldIndex+1;

						dataListDownwash.add(new Object[] {""});
						currentBoldIndex = currentBoldIndex+1;

						dataListDownwash.add(new Object[] {
								"Downwash gradient (" + 
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
										+ ") at Alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
										"",
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertListOfAmountTodoubleArray(
														_alphaBodyList.stream()
														.map(a -> a.to(NonSI.DEGREE_ANGLE))
														.collect(Collectors.toList())
														),
												_downwashGradientMap.get(ComponentEnum.WING).get(Boolean.TRUE).get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
														).stream()
												.mapToDouble(dg -> dg)
												.toArray(),
												_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
												)
						});
						currentBoldIndex = currentBoldIndex+1;
						dataListDownwash.add(new Object[] {
								"Downwash angle (" +
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
										+ ") at Alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
										"",
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertListOfAmountTodoubleArray(
														_alphaBodyList.stream()
														.map(a -> a.to(NonSI.DEGREE_ANGLE))
														.collect(Collectors.toList())
														),
												_downwashAngleMap.get(ComponentEnum.WING).get(Boolean.TRUE).get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
														).stream()
												.mapToDouble(dg -> dg.doubleValue(NonSI.DEGREE_ANGLE))
												.toArray(),
												_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
												)
						});
						currentBoldIndex = currentBoldIndex+1;

					}
					//-----------------------------------------------------------------------------
					// NON LINEAR GRADIENT
					else {

						dataListDownwash.add(new Object[] {"LINEAR"});
						currentBoldIndex = currentBoldIndex+1;
						boldRowIndex.add(currentBoldIndex);

						Object[] downwashConstantGradientArray = new Object[_downwashGradientMap.get(ComponentEnum.WING).get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
								).get(Boolean.TRUE).size()+2];
						downwashConstantGradientArray[0] = "Downwash gradient (" 
								+ _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ ")";
						downwashConstantGradientArray[1] = "";
						for(int i=0; 
								i<_downwashGradientMap.get(ComponentEnum.WING).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).get(Boolean.TRUE).size();
								i++) 
							downwashConstantGradientArray[i+2] = _downwashGradientMap.get(ComponentEnum.WING).get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
									).get(Boolean.TRUE).get(i);

						dataListDownwash.add(downwashConstantGradientArray);
						currentBoldIndex = currentBoldIndex+1;

						Object[] downwashAngleConstantGradientArray = new Object[_downwashAngleMap.get(ComponentEnum.WING).get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
								).get(Boolean.TRUE).size()+2];
						downwashAngleConstantGradientArray[0] = "Downwash angle (" 
								+ _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
								+ ")";
						downwashAngleConstantGradientArray[1] = "deg";
						for(int i=0; 
								i<_downwashAngleMap.get(ComponentEnum.WING).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).get(Boolean.TRUE).size();
								i++) 
							downwashAngleConstantGradientArray[i+2] = _downwashAngleMap.get(ComponentEnum.WING).get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
									).get(Boolean.TRUE).get(i).doubleValue(NonSI.DEGREE_ANGLE);

						dataListDownwash.add(downwashAngleConstantGradientArray);
						currentBoldIndex = currentBoldIndex+1;

						dataListDownwash.add(new Object[] {""});
						currentBoldIndex = currentBoldIndex+1;

						dataListDownwash.add(new Object[] {
								"Downwash gradient ("+
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
										+ ") at Alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
										"",
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertListOfAmountTodoubleArray(
														_alphaBodyList.stream()
														.map(a -> a.to(NonSI.DEGREE_ANGLE))
														.collect(Collectors.toList())
														),
												_downwashGradientMap.get(ComponentEnum.WING).get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
														).get(Boolean.TRUE).stream()
												.mapToDouble(dg -> dg)
												.toArray(),
												_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
												)
						});
						currentBoldIndex = currentBoldIndex+1;

						dataListDownwash.add(new Object[] {
								"Downwash angle (" +
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
										+ ") at Alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
										"",
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertListOfAmountTodoubleArray(
														_alphaBodyList.stream()
														.map(a -> a.to(NonSI.DEGREE_ANGLE))
														.collect(Collectors.toList())
														),
												_downwashAngleMap.get(ComponentEnum.WING).get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
														).get(Boolean.TRUE).stream()
												.mapToDouble(dg -> dg.doubleValue(NonSI.DEGREE_ANGLE))
												.toArray(),
												_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
												)
						});
						currentBoldIndex = currentBoldIndex+1;

						dataListDownwash.add(new Object[] {""});
						currentBoldIndex = currentBoldIndex+1;

						//.............................................................................................................................................

						dataListDownwash.add(new Object[] {"NON LINEAR"});
						currentBoldIndex = currentBoldIndex+1;
						boldRowIndex.add(currentBoldIndex);

						Object[] downwashNonLinearGradientArray = new Object[_downwashGradientMap.get(ComponentEnum.WING).get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
								).get(Boolean.FALSE).size()+2];
						downwashNonLinearGradientArray[0] = "Downwash gradient (" 
								+ _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString() 
								+ ")";
						downwashNonLinearGradientArray[1] = "";
						for(int i=0; 
								i<_downwashGradientMap.get(ComponentEnum.WING).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).get(Boolean.FALSE).size();
								i++) 
							downwashNonLinearGradientArray[i+2] = _downwashGradientMap.get(ComponentEnum.WING).get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
									).get(Boolean.FALSE).get(i);

						dataListDownwash.add(downwashNonLinearGradientArray);
						currentBoldIndex = currentBoldIndex+1;

						Object[] downwashAngleNonLinearGradientArray = new Object[_downwashAngleMap.get(ComponentEnum.WING).get(
								_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
								).get(Boolean.FALSE).size()+2];
						downwashAngleNonLinearGradientArray[0] = "Downwash angle (" 
								+ _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString() 
								+ ")";
						downwashAngleNonLinearGradientArray[1] = "deg";
						for(int i=0; 
								i<_downwashAngleMap.get(ComponentEnum.WING).get(
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
										).get(Boolean.FALSE).size();
								i++) 
							downwashAngleNonLinearGradientArray[i+2] = _downwashAngleMap.get(ComponentEnum.WING).get(
									_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
									).get(Boolean.FALSE).get(i).doubleValue(NonSI.DEGREE_ANGLE);

						dataListDownwash.add(downwashAngleNonLinearGradientArray);
						currentBoldIndex = currentBoldIndex+1;

						dataListDownwash.add(new Object[] {""});
						currentBoldIndex = currentBoldIndex+1;

						dataListDownwash.add(new Object[] {
								"Downwash gradient (" + 
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
										+ ") at Alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
										"",
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertListOfAmountTodoubleArray(
														_alphaBodyList.stream()
														.map(a -> a.to(NonSI.DEGREE_ANGLE))
														.collect(Collectors.toList())
														),
												_downwashGradientMap.get(ComponentEnum.WING).get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
														).get(Boolean.FALSE).stream()
												.mapToDouble(dg -> dg)
												.toArray(),
												_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
												)
						});
						currentBoldIndex = currentBoldIndex+1;
						dataListDownwash.add(new Object[] {
								"Downwash angle (" +
										_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH).toString()
										+ ") at Alpha = " + _alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE) + " deg",
										"",
										MyMathUtils.getInterpolatedValue1DLinear(
												MyArrayUtils.convertListOfAmountTodoubleArray(
														_alphaBodyList.stream()
														.map(a -> a.to(NonSI.DEGREE_ANGLE))
														.collect(Collectors.toList())
														),
												_downwashAngleMap.get(ComponentEnum.WING).get(
														_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).get(AerodynamicAndStabilityEnum.WING_DOWNWASH)
														).get(Boolean.FALSE).stream()
												.mapToDouble(dg -> dg.doubleValue(NonSI.DEGREE_ANGLE))
												.toArray(),
												_alphaBodyCurrent.doubleValue(NonSI.DEGREE_ANGLE)
												)
						});
						currentBoldIndex = currentBoldIndex+1;

					}

					//------------------------------------------------------------------------------------------------------------------------
					// CREATING CELLS ...
					//--------------------------------------------------------------------------------
					Row rowDownwashCurves = downwashSheet.createRow(0);
					Object[] objArrDownwash = dataListDownwash.get(0);
					int cellnumDownwash = 0;
					for (Object obj : objArrDownwash) {
						Cell cell = rowDownwashCurves.createCell(cellnumDownwash++);
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

					int rownumDownwash = 1;
					for (int i = 1; i < dataListDownwash.size(); i++) {
						objArrDownwash = dataListDownwash.get(i);
						rowDownwashCurves = downwashSheet.createRow(rownumDownwash++);
						cellnumDownwash = 0;
						Boolean isBold = Boolean.FALSE;
						for(int bri=0; bri<boldRowIndex.size(); bri++) 
							if(rownumDownwash == boldRowIndex.get(bri))
								isBold = Boolean.TRUE;
						for (Object obj : objArrDownwash) {
							Cell cell = rowDownwashCurves.createCell(cellnumDownwash++);
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

						downwashSheet.setDefaultColumnWidth(55);
						downwashSheet.setColumnWidth(1, 2048);
						for(int k=2; k<100; k++)
							downwashSheet.setColumnWidth(k, 3840);

					}
				}
				else
					System.err.println("WARNING!! DOWNWASH ANGLE AND DOWNWASH GRADIENT CANNOT BE WRITTEN SINCE THERE IS NO HORIZONTAL TAIL ... ");
			}
			
			//--------------------------------------------------------------------------------
			// TOTAL AIRCRAFT LIFT, DRAG AND MOMENT CURVES RESULTS:
			//--------------------------------------------------------------------------------
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL) 
					|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL)
					|| _theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {

				Sheet totalAircraftCurvesSheet = wb.createSheet("TOTAL AIRCRAFT CURVES");
				List<Object[]> dataListTotalAircraftCurves = new ArrayList<>();

				List<Integer> boldRowIndex = new ArrayList<>();
				int currentBoldIndex = 1;

				dataListTotalAircraftCurves.add(new Object[] {"Description","Unit","Value"});

				Object[] alphaBodyArray = new Object[getAlphaBodyList().size()+2];
				alphaBodyArray[0] = "Alpha body";
				alphaBodyArray[1] = "deg";
				for(int i=0; i<getAlphaBodyList().size(); i++) 
					alphaBodyArray[i+2] = getAlphaBodyList().get(i).doubleValue(NonSI.DEGREE_ANGLE);			

				dataListTotalAircraftCurves.add(alphaBodyArray);
				currentBoldIndex = currentBoldIndex+1;

				for (int i = 0; i < _theAerodynamicBuilderInterface.getDeltaElevatorList().size(); i++) {

					dataListTotalAircraftCurves.add(new Object[] {""});
					dataListTotalAircraftCurves.add(new Object[] {"Delta elevator = " + _theAerodynamicBuilderInterface.getDeltaElevatorList().get(i).doubleValue(NonSI.DEGREE_ANGLE) + " deg"});
					currentBoldIndex = currentBoldIndex+2;
					boldRowIndex.add(currentBoldIndex);
					dataListTotalAircraftCurves.add(new Object[] {""});
					currentBoldIndex = currentBoldIndex+1;
					
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CL_TOTAL)) {
						
						Object[] cLHTailArray = new Object[_current3DHorizontalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).size()+2];
						cLHTailArray[0] = "CL horizontal tail";
						cLHTailArray[1] = "";
						for(int j=0; j<_current3DHorizontalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).size(); j++) 
							cLHTailArray[j+2] = _current3DHorizontalTailLiftCurve.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).get(j);
						
						dataListTotalAircraftCurves.add(cLHTailArray);
						currentBoldIndex = currentBoldIndex+1;
						
						Object[] cLTotalArray = new Object[_totalLiftCoefficient.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).size()+2];
						cLTotalArray[0] = "CL total";
						cLTotalArray[1] = "";
						for(int j=0; j<_totalLiftCoefficient.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).size(); j++) 
							cLTotalArray[j+2] = _totalLiftCoefficient.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).get(j);
						
						dataListTotalAircraftCurves.add(cLTotalArray);
						currentBoldIndex = currentBoldIndex+1;
						
					}

					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CD_TOTAL)) {
						
						
						Object[] cDTotalArray = new Object[_totalDragCoefficient.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).size()+2];
						cDTotalArray[0] = "CD total";
						cDTotalArray[1] = "";
						for(int j=0; j<_totalDragCoefficient.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).size(); j++) 
							cDTotalArray[j+2] = _totalDragCoefficient.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).get(j);
						
						dataListTotalAircraftCurves.add(cDTotalArray);
						currentBoldIndex = currentBoldIndex+1;
					}
					
					if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.CM_TOTAL)) {
						
						Object[] cMHTailArray = new Object[_current3DHorizontalTailMomentCurve
						                                   .get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))
						                                   .size()+2];
						cMHTailArray[0] = "CM horizontal Tail";
						cMHTailArray[1] = "";
						for(int j=0;
								j<_current3DHorizontalTailMomentCurve
								.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).size();
								j++
								) 
							cMHTailArray[j+2] = _current3DHorizontalTailMomentCurve
							.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))
							.get(j);

						dataListTotalAircraftCurves.add(cMHTailArray);
						currentBoldIndex = currentBoldIndex+1;
						
						for(int k=0; k<_theAerodynamicBuilderInterface.getXCGAircraft().size(); k++) {
							
							dataListTotalAircraftCurves.add(new Object[] {""});
							dataListTotalAircraftCurves.add(new Object[] {"Xcg = " + _theAerodynamicBuilderInterface.getXCGAircraft().get(k)*100 + " %"});
							currentBoldIndex = currentBoldIndex+2;

							Object[] cMTotalArray = new Object[_totalMomentCoefficient
							                                   .get(_theAerodynamicBuilderInterface.getXCGAircraft().get(k))
							                                   .get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))
							                                   .size()+2];
							cMTotalArray[0] = "CM total";
							cMTotalArray[1] = "";
							for(int j=0;
									j<_totalMomentCoefficient
									.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(k))
									.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i)).size();
									j++
									) 
								cMTotalArray[j+2] = _totalMomentCoefficient
								.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(k))
								.get(_theAerodynamicBuilderInterface.getDeltaElevatorList().get(i))
								.get(j);

							dataListTotalAircraftCurves.add(cMTotalArray);
							currentBoldIndex = currentBoldIndex+1;

						}
					}
				}

				//------------------------------------------------------------------------------------------------------------------------
				// CREATING CELLS ...
				//--------------------------------------------------------------------------------
				Row rowTotalAircraftCurves = totalAircraftCurvesSheet.createRow(0);
				Object[] objArrTotalAircraftCurves = dataListTotalAircraftCurves.get(0);
				int cellnumTotalAircraftCurves = 0;
				for (Object obj : objArrTotalAircraftCurves) {
					Cell cell = rowTotalAircraftCurves.createCell(cellnumTotalAircraftCurves++);
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

				int rownumTotalAircraftCurves = 1;
				for (int i = 1; i < dataListTotalAircraftCurves.size(); i++) {
					objArrTotalAircraftCurves = dataListTotalAircraftCurves.get(i);
					rowTotalAircraftCurves = totalAircraftCurvesSheet.createRow(rownumTotalAircraftCurves++);
					cellnumTotalAircraftCurves = 0;
					Boolean isBold = Boolean.FALSE;
					for(int bri=0; bri<boldRowIndex.size(); bri++) 
						if(rownumTotalAircraftCurves == boldRowIndex.get(bri))
							isBold = Boolean.TRUE;
					for (Object obj : objArrTotalAircraftCurves) {
						Cell cell = rowTotalAircraftCurves.createCell(cellnumTotalAircraftCurves++);
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

					totalAircraftCurvesSheet.setDefaultColumnWidth(35);
					totalAircraftCurvesSheet.setColumnWidth(1, 2048);
					for(int k=2; k<100; k++)
						totalAircraftCurvesSheet.setColumnWidth(k, 3840);

				}
			}
			
			//--------------------------------------------------------------------------------
			// LONGITUDINAL STATIC STABILITY AND CONTROL RESULTS:
			//--------------------------------------------------------------------------------
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.LONGITUDINAL_STABILITY)) {

				Sheet longitudinalStaticStabilityAndControlSheet = wb.createSheet("LONGITUDINAL STABILITY AND CONTROL");
				List<Object[]> dataListlongitudinalStaticStabilityAndControl = new ArrayList<>();

				List<Integer> boldRowIndex = new ArrayList<>();
				int currentBoldIndex = 1;

				dataListlongitudinalStaticStabilityAndControl.add(new Object[] {"Description","Unit","Value"});

				Object[] alphaBodyArray = new Object[getAlphaBodyList().size()+2];
				alphaBodyArray[0] = "Alpha body";
				alphaBodyArray[1] = "deg";
				for(int i=0; i<getAlphaBodyList().size(); i++) 
					alphaBodyArray[i+2] = getAlphaBodyList().get(i).doubleValue(NonSI.DEGREE_ANGLE);			

				dataListlongitudinalStaticStabilityAndControl.add(alphaBodyArray);
				currentBoldIndex = currentBoldIndex+1;

				for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

					int indexOfFirstMaximumDeltaElevatorOfEquilibrium = getAlphaBodyList().size()-1;
					
					for(int j=0; j<_deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size(); j++)
						if(_deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j)
								.equals(_deltaEForEquilibrium.get(0))
								) {
							indexOfFirstMaximumDeltaElevatorOfEquilibrium = j;
							break;
						}
					
					dataListlongitudinalStaticStabilityAndControl.add(new Object[] {""});
					dataListlongitudinalStaticStabilityAndControl.add(new Object[] {"Xcg = " + _theAerodynamicBuilderInterface.getXCGAircraft().get(i)*100 + " %"});
					currentBoldIndex = currentBoldIndex+2;
					boldRowIndex.add(currentBoldIndex);

					Object[] cLHTailEquilibriumArray = new Object[_horizontalTailEquilibriumLiftCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size()+2];
					cLHTailEquilibriumArray[0] = "CL_h equilibrium";
					cLHTailEquilibriumArray[1] = "";
					for(int j=0; j<=indexOfFirstMaximumDeltaElevatorOfEquilibrium; j++) 
						cLHTailEquilibriumArray[j+2] = _horizontalTailEquilibriumLiftCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j);

					dataListlongitudinalStaticStabilityAndControl.add(cLHTailEquilibriumArray);
					currentBoldIndex = currentBoldIndex+1;

					Object[] cLTotalEquilibriumArray = new Object[_totalEquilibriumLiftCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size()+2];
					cLTotalEquilibriumArray[0] = "CL_total equilibrium";
					cLTotalEquilibriumArray[1] = "";
					for(int j=0; j<=indexOfFirstMaximumDeltaElevatorOfEquilibrium; j++) 
						cLTotalEquilibriumArray[j+2] = _totalEquilibriumLiftCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j);

					dataListlongitudinalStaticStabilityAndControl.add(cLTotalEquilibriumArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] cDHTailEquilibriumArray = new Object[_horizontalTailEquilibriumDragCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size()+2];
					cDHTailEquilibriumArray[0] = "CD_h equilibrium";
					cDHTailEquilibriumArray[1] = "";
					for(int j=0; j<=indexOfFirstMaximumDeltaElevatorOfEquilibrium; j++) 
						cDHTailEquilibriumArray[j+2] = _horizontalTailEquilibriumDragCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j);
					
					dataListlongitudinalStaticStabilityAndControl.add(cDHTailEquilibriumArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] cDTotalEquilibriumArray = new Object[_totalEquilibriumDragCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size()+2];
					cDTotalEquilibriumArray[0] = "CD_total equilibrium";
					cDTotalEquilibriumArray[1] = "";
					for(int j=0; j<=indexOfFirstMaximumDeltaElevatorOfEquilibrium; j++) 
						cDTotalEquilibriumArray[j+2] = _totalEquilibriumDragCoefficient.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j);

					dataListlongitudinalStaticStabilityAndControl.add(cDTotalEquilibriumArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] deltaElevatorEquilibriumArray = new Object[_deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size()+2];
					deltaElevatorEquilibriumArray[0] = "delta_e equilibrium";
					deltaElevatorEquilibriumArray[1] = "deg";
					for(int j=0; j<=indexOfFirstMaximumDeltaElevatorOfEquilibrium; j++) 
						deltaElevatorEquilibriumArray[j+2] = _deltaEEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j).doubleValue(NonSI.DEGREE_ANGLE);

					dataListlongitudinalStaticStabilityAndControl.add(deltaElevatorEquilibriumArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] neutralPointArray = new Object[3];
					neutralPointArray[0] = "Neutral_point";
					neutralPointArray[1] = "";
					for(int j=0; j<=indexOfFirstMaximumDeltaElevatorOfEquilibrium; j++) 
						neutralPointArray[2] = _neutralPointPositionMap.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i));

					dataListlongitudinalStaticStabilityAndControl.add(neutralPointArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] staticStabilityMarginArray = new Object[3];
					staticStabilityMarginArray[0] = "Static_Stability_Margin";
					staticStabilityMarginArray[1] = "";
					staticStabilityMarginArray[2] = _staticStabilityMarginMap.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i));

					dataListlongitudinalStaticStabilityAndControl.add(staticStabilityMarginArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] trimmedEquilibriumEfficiencyArray = new Object[_totalEquilibriumEfficiencyMap.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size()+2];
					trimmedEquilibriumEfficiencyArray[0] = "Efficiency";
					trimmedEquilibriumEfficiencyArray[1] = "";
					for(int j=0; j<=indexOfFirstMaximumDeltaElevatorOfEquilibrium; j++) 
						trimmedEquilibriumEfficiencyArray[j+2] = _totalEquilibriumEfficiencyMap.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j);

					dataListlongitudinalStaticStabilityAndControl.add(trimmedEquilibriumEfficiencyArray);
					currentBoldIndex = currentBoldIndex+1;
					
					dataListlongitudinalStaticStabilityAndControl.add(new Object[] {
						"Maximum efficiency",
						"",
						_totalEquilibriumMaximumEfficiencyMap.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i))
					});
					currentBoldIndex = currentBoldIndex+1;

				}

				//------------------------------------------------------------------------------------------------------------------------
				// CREATING CELLS ...
				//--------------------------------------------------------------------------------
				Row rowlongitudinalStaticStabilityAndControl = longitudinalStaticStabilityAndControlSheet.createRow(0);
				Object[] objArrlongitudinalStaticStabilityAndControl = dataListlongitudinalStaticStabilityAndControl.get(0);
				int cellnumlongitudinalStaticStabilityAndControl = 0;
				for (Object obj : objArrlongitudinalStaticStabilityAndControl) {
					Cell cell = rowlongitudinalStaticStabilityAndControl.createCell(cellnumlongitudinalStaticStabilityAndControl++);
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

				int rownumlongitudinalStaticStabilityAndControl = 1;
				for (int i = 1; i < dataListlongitudinalStaticStabilityAndControl.size(); i++) {
					objArrlongitudinalStaticStabilityAndControl = dataListlongitudinalStaticStabilityAndControl.get(i);
					rowlongitudinalStaticStabilityAndControl = longitudinalStaticStabilityAndControlSheet.createRow(rownumlongitudinalStaticStabilityAndControl++);
					cellnumlongitudinalStaticStabilityAndControl = 0;
					Boolean isBold = Boolean.FALSE;
					for(int bri=0; bri<boldRowIndex.size(); bri++) 
						if(rownumlongitudinalStaticStabilityAndControl == boldRowIndex.get(bri))
							isBold = Boolean.TRUE;
					for (Object obj : objArrlongitudinalStaticStabilityAndControl) {
						Cell cell = rowlongitudinalStaticStabilityAndControl.createCell(cellnumlongitudinalStaticStabilityAndControl++);
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

					longitudinalStaticStabilityAndControlSheet.setDefaultColumnWidth(35);
					longitudinalStaticStabilityAndControlSheet.setColumnWidth(1, 2048);
					for(int k=2; k<100; k++)
						longitudinalStaticStabilityAndControlSheet.setColumnWidth(k, 3840);

				}
			}
			
			//--------------------------------------------------------------------------------
			// DIRECTIONAL STABILITY AND CONTROL RESULTS:
			//--------------------------------------------------------------------------------
			if(_theAerodynamicBuilderInterface.getComponentTaskList().get(ComponentEnum.AIRCRAFT).containsKey(AerodynamicAndStabilityEnum.DIRECTIONAL_STABILITY)) {

				Sheet directionalStabilityAndControlSheet = wb.createSheet("DIRECTIONAL STABILITY AND CONTROL");
				List<Object[]> dataListDirectionalStabilityAndControl = new ArrayList<>();

				List<Integer> boldRowIndex = new ArrayList<>();
				int currentBoldIndex = 1;

				dataListDirectionalStabilityAndControl.add(new Object[] {"Description","Unit","Value"});

				Object[] betaArray = new Object[getBetaList().size()+2];
				betaArray[0] = "Beta";
				betaArray[1] = "deg";
				for(int i=0; i<getBetaList().size(); i++) 
					betaArray[i+2] = getBetaList().get(i).doubleValue(NonSI.DEGREE_ANGLE);			

				dataListDirectionalStabilityAndControl.add(betaArray);
				currentBoldIndex = currentBoldIndex+1;

				for (int i = 0; i < _theAerodynamicBuilderInterface.getXCGAircraft().size(); i++) {

					dataListDirectionalStabilityAndControl.add(new Object[] {""});
					dataListDirectionalStabilityAndControl.add(new Object[] {"Xcg = " + _theAerodynamicBuilderInterface.getXCGAircraft().get(i)*100 + " %"});
					currentBoldIndex = currentBoldIndex+2;
					boldRowIndex.add(currentBoldIndex);

					dataListDirectionalStabilityAndControl.add(new Object[] {""});
					currentBoldIndex = currentBoldIndex+1;
					
					dataListDirectionalStabilityAndControl.add(new Object[] {
							"CN_beta_vertical_tail",
							"1/deg",
							_cNBetaVertical.get(i)._2});
					
					currentBoldIndex = currentBoldIndex+1;
					
					dataListDirectionalStabilityAndControl.add(new Object[] {
							"CN_beta_fuselage",
							"1/deg",
							_cNBetaFuselage.get(i)._2});
					
					currentBoldIndex = currentBoldIndex+1;
					
					dataListDirectionalStabilityAndControl.add(new Object[] {
							"CN_beta_wing",
							"1/deg",
							_cNBetaWing.get(i)._2});
					
					currentBoldIndex = currentBoldIndex+1;
					
					dataListDirectionalStabilityAndControl.add(new Object[] {
							"CN_beta_total",
							"1/deg",
							_cNBetaTotal.get(i)._2});
					
					currentBoldIndex = currentBoldIndex+1;
					
					dataListDirectionalStabilityAndControl.add(new Object[] {""});
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] deltaRudderArray = new Object[_theAerodynamicBuilderInterface.getDeltaRudderList().size()+2];
					deltaRudderArray[0] = "Delta_rudder";
					deltaRudderArray[1] = "deg";
					for(int j=0; j<_theAerodynamicBuilderInterface.getDeltaRudderList().size(); j++) 
						deltaRudderArray[j+2] = _theAerodynamicBuilderInterface.getDeltaRudderList().get(j).doubleValue(NonSI.DEGREE_ANGLE);			
					
					dataListDirectionalStabilityAndControl.add(deltaRudderArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] cNdrArray = new Object[_cNDeltaR.keySet().size()+2];
					
					cNdrArray[0] = "CN_delta_rudder";
					cNdrArray[1] = "1/deg";
					for(int j=0; 
							j<_cNDeltaR.keySet().size()+2;
							j++) 
						cNdrArray[j+2] = _cNDeltaR.keySet().size()+2;
					
					dataListDirectionalStabilityAndControl.add(cNdrArray);
					currentBoldIndex = currentBoldIndex+1;
					
					dataListDirectionalStabilityAndControl.add(new Object[] {""});
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] cNVerticalTailArray = new Object[_cNVertical.size()+2];
					
					cNVerticalTailArray[0] = "CN_vertical_tail";
					cNVerticalTailArray[1] = "";
					for(int j=0; 
							j<_cNVertical.size()+2;
							j++) 
						cNVerticalTailArray[j+2] = _cNVertical.get(i)._2().get(j);
					
					dataListDirectionalStabilityAndControl.add(cNVerticalTailArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] cNFuslegaArray = new Object[_cNFuselage.get(i)._2().size()+2];
					
					cNFuslegaArray[0] = "CN_fuselage";
					cNFuslegaArray[1] = "";
					for(int j=0; 
							j<_cNFuselage.get(i)._2().size(); 
							j++) 
						cNFuslegaArray[j+2] = _cNFuselage.get(i)._2().get(j);
					
					dataListDirectionalStabilityAndControl.add(cNFuslegaArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] cNWingArray = new Object[_cNWing.get(i)._2().size()+2];
					cNWingArray[0] = "CN_wing";
					cNWingArray[1] = "";
					for(int j=0; 
							j<_cNWing.get(i)._2().size(); 
							j++) 
						cNWingArray[j+2] = _cNWing.get(i)._2().get(j);
					
					dataListDirectionalStabilityAndControl.add(cNWingArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] cNTotalArray = new Object[_cNTotal.get(i)._2().size()+2];
					cNTotalArray[0] = "CN_total";
					cNTotalArray[1] = "";
					for(int j=0; 
							j<_cNTotal.get(i)._2().size(); 
							j++) 
						cNTotalArray[j+2] = _cNTotal.get(i)._2().get(j);
					
					dataListDirectionalStabilityAndControl.add(cNTotalArray);
					currentBoldIndex = currentBoldIndex+1;
					
					dataListDirectionalStabilityAndControl.add(new Object[] {""});
					currentBoldIndex = currentBoldIndex+1;
					
					for (int j = 0; j < _theAerodynamicBuilderInterface.getDeltaRudderList().size(); j++) {

						Object[] cNDueToDeltaRudderArray = new Object[_cNDueToDeltaRudder.get(_theAerodynamicBuilderInterface.getDeltaRudderList().get(j)).get(i)._2().size()+2];
						cNDueToDeltaRudderArray[0] = "CN at delta rudder = " + _theAerodynamicBuilderInterface.getDeltaRudderList().get(j).doubleValue(NonSI.DEGREE_ANGLE) + " deg";
						cNDueToDeltaRudderArray[1] = "";
						for(int k=0; 
								k<_cNDueToDeltaRudder.get(_theAerodynamicBuilderInterface.getDeltaRudderList().get(j)).get(i)._2().size(); 
								k++) 
							cNDueToDeltaRudderArray[k+2] = _cNDueToDeltaRudder.get(_theAerodynamicBuilderInterface.getDeltaRudderList().get(j)).get(i)._2().get(k);
						
						dataListDirectionalStabilityAndControl.add(cNDueToDeltaRudderArray);
						currentBoldIndex = currentBoldIndex+1;
						
					}
					
					dataListDirectionalStabilityAndControl.add(new Object[] {""});
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] deltaRudderEquilibriumArray = new Object[_betaOfEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size()+2];
					deltaRudderEquilibriumArray[0] = "Delta_rudder equilibrium";
					deltaRudderEquilibriumArray[1] = "deg";
					for(int j=0; 
							j<_betaOfEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size(); 
							j++) 
						deltaRudderEquilibriumArray[j+2] = _betaOfEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j)
						._1().doubleValue(NonSI.DEGREE_ANGLE);			
					
					dataListDirectionalStabilityAndControl.add(deltaRudderEquilibriumArray);
					currentBoldIndex = currentBoldIndex+1;
					
					Object[] betaEquilibriumArray = new Object[_betaOfEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size()+2];
					betaEquilibriumArray[0] = "Beta equilibrium";
					betaEquilibriumArray[1] = "deg";
					for(int j=0; 
							j<_betaOfEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).size(); 
							j++) 
						
						betaEquilibriumArray[j+2] = _betaOfEquilibrium.get(_theAerodynamicBuilderInterface.getXCGAircraft().get(i)).get(j)
						._2().doubleValue(NonSI.DEGREE_ANGLE);			
					dataListDirectionalStabilityAndControl.add(betaEquilibriumArray);
					currentBoldIndex = currentBoldIndex+1;
					
				}

				//------------------------------------------------------------------------------------------------------------------------
				// CREATING CELLS ...
				//--------------------------------------------------------------------------------
				Row rowDirectionalStabilityAndControl = directionalStabilityAndControlSheet.createRow(0);
				Object[] objArrDirectionalStabilityAndControl = dataListDirectionalStabilityAndControl.get(0);
				int cellnumDirectionalStabilityAndControl = 0;
				for (Object obj : objArrDirectionalStabilityAndControl) {
					Cell cell = rowDirectionalStabilityAndControl.createCell(cellnumDirectionalStabilityAndControl++);
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

				int rownumDirectionalStabilityAndControl = 1;
				for (int i = 1; i < dataListDirectionalStabilityAndControl.size(); i++) {
					objArrDirectionalStabilityAndControl = dataListDirectionalStabilityAndControl.get(i);
					rowDirectionalStabilityAndControl = directionalStabilityAndControlSheet.createRow(rownumDirectionalStabilityAndControl++);
					cellnumDirectionalStabilityAndControl = 0;
					Boolean isBold = Boolean.FALSE;
					for(int bri=0; bri<boldRowIndex.size(); bri++) 
						if(rownumDirectionalStabilityAndControl == boldRowIndex.get(bri))
							isBold = Boolean.TRUE;
					for (Object obj : objArrDirectionalStabilityAndControl) {
						Cell cell = rowDirectionalStabilityAndControl.createCell(cellnumDirectionalStabilityAndControl++);
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

					directionalStabilityAndControlSheet.setDefaultColumnWidth(35);
					directionalStabilityAndControlSheet.setColumnWidth(1, 2048);
					for(int k=2; k<100; k++)
						directionalStabilityAndControlSheet.setColumnWidth(k, 3840);

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
			System.out.println("\n\tAerodynamic and Stability results Excel file has been generated!\n");
	        
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
	public Amount<Temperature> getCurrentDeltaTemperature() {
		return _currentDeltaTemperature;
	}

	public void setCurrentDeltaTemperature(Amount<Temperature> _currentDeltaTemperature) {
		this._currentDeltaTemperature = _currentDeltaTemperature;
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
	public List<Double> getCurrent3DCanardLiftCurve() {
		return _current3DCanardLiftCurve;
	}
	public void setCurrent3DCanardLiftCurve(List<Double> _current3DCanardLiftCurve) {
		this._current3DCanardLiftCurve = _current3DCanardLiftCurve;
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
