package analyses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.RealVector;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.Aircraft;
import analyses.ACDynamicStabilityManagerUtils.PropulsionTypeEnum;
import calculators.aerodynamics.DragCalc;
import calculators.stability.IDynamicStabilityCalculator;
import configuration.enumerations.ConditionEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyUnits;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import writers.JPADStaticWriteUtils;

public class ACDynamicStabilityManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private IACDynamicStabilityManager _theDynamicStabilityManagerInterface;

	PropulsionTypeEnum propulsion_system = PropulsionTypeEnum.CONSTANT_TRUST;
	
	// Calculation object
	
	private IDynamicStabilityCalculator _theDynamicStabilityCalculatorInterface;
	
	//------------------------------------------------------------------------------
	// OUTPUT DATA
	//..............................................................................

	private double _currentMachNumber;
	private Amount<Length> _currentAltitude;
	private Amount<Temperature> _currentDeltaTemperature;
	private Amount<Angle> _alphaBodyCurrent;
	
	double rho0;         						// air density
	double reference_area;						// wing area
	double currentMass; 								// total mass
	double cbar; 								// mean aerodynamic chord
	double wing_span; 							// wingspan
	double u0; 								    // speed of the aircraft
	double qbar0;               			    // dynamic pressure
	double mach0;               			    // Mach number
	double gamma0;          					// ramp angle
	double theta0 = Math.toRadians(gamma0); 	// Euler angle [rad] (assuming gamma0 = theta0)
	double iXX; 								// lateral-directional moment of inertia (IXX)
	double iYY; 								// longitudinal moment of inertia  (IYY)
	double iZZ; 								// lateral-directional moment of inertia (IZZ)
	double iXZ; 								// lateral-directional product of inertia (IXZ)
	double cDrag0; 								// drag coefficient at null incidence (Cd�) of the aircraft
	double cDrag_alpha0; 						// linear drag gradient (CdAlpha�) of the aircraft
	double cDrag_Mach0; 						// drag coefficient with respect to Mach (CdM�) of the aircraft
	double cL0; 								// lift coefficient at null incidence (Cl�) of the aircraft
	double cL_alpha0; 							// linear lift gradient (ClAlpha�) of the aircraft
	double cL_alpha_dot0; 						// linear lift gradient time derivative (ClAlpha_dot�) of the aircraft
	double cL_Mach0;							// lift coefficient with respect to Mach (ClM�) of the aircraft
	double cL_q0; 								// lift coefficient with respect to q (ClQ�) of the aircraft
	double cL_delta_T; 							// lift coefficient with respect to delta_T (ClDelta_T�) of the aircraft
	double cL_delta_E; 							// lift coefficient with respect to delta_E (ClDelta_E�) of the aircraft
	double cm_alpha0; 							// pitching moment coefficient with respect to Alpha (CmAlpha�) of the aircraft
	double cm_alpha_dot0; 						// pitching moment coefficient time derivative (CmAlpha_dot�) of the aircraft
	double cm_Mach0; 						    // pitching moment coefficient with respect to Mach number
	double cm_q; 							    // pitching moment coefficient with respect to q
	double cm_delta_T; 							// pitching moment coefficient with respect to delta_T (CMDelta_T�) of the aircraft
	double cm_delta_E;							// pitching moment coefficient with respect to delta_E (CMDelta_E�) of the aircraft
	double c_Tfix; 							    // thrust coefficient at a fixed point ( U0 = u , delta_T = 1 )
	double kv; 							        // scale factor of the effect on the propulsion due to the speed
	double cY_beta; 						    // lateral force coefficient with respect to beta (CyBeta) of the aircraft
	double cY_p; 							    // lateral force coefficient with respect to p (CyP) of the aircraft
	double cY_r; 							    // lateral force coefficient with respect to r (CyR) of the aircraft
	double cY_delta_A; 							// lateral force coefficient with respect to delta_A (CyDelta_A) of the aircraft
	double cY_delta_R; 							// lateral force coefficient with respect to delta_R (CyDelta_R) of the aircraft
	double cRoll_beta; 						    // rolling moment coefficient with respect to beta (CLBeta) of the aircraft
	double cRoll_p; 						    // rolling moment coefficient with respect to a p (CLP) of the aircraft
	double cRoll_r; 						    // rolling moment coefficient with respect to a r (CLR) of the aircraft
	double cRoll_delta_A; 						// rolling moment coefficient with respect to a delta_A (CLDelta_A) of the aircraft
	double cRoll_delta_R; 						// rolling moment coefficient with respect to a delta_R (CLDelta_R) of the aircraft
	double cYaw_beta; 						    // yawing moment coefficient with respect to a beta (CNBeta) of the aircraft
	double cYaw_p; 							    // yawing moment coefficient with respect to p (CNP) of the aircraft
	double cYaw_r; 							    // yawing moment coefficient with respect to r (CNR) of the aircraft
	double cYaw_delta_A; 					    // yawing moment coefficient with respect to delta_A (CNDelta_A) of the aircraft
	double cYaw_delta_R; 				        // yawing moment coefficient with respect to delta_R (CNDelta_R) of the aircraft

	double x_u_CT;                      // dimensional derivative of force component X with respect to "u" for Constant Thrust
	double x_u_CP;                      // dimensional derivative of force component X with respect to "u" for Constant Power
	double x_w;							// dimensional derivative of force component X with respect to "w"
	double x_w_dot;                     // dimensional derivative of force component X with respect to "w_dot"
	double x_q;							// dimensional derivative of force component X with respect to "q"
	double z_u;							// dimensional derivative of force component Z with respect to "u"
	double z_w;							// dimensional derivative of force component Z with respect to "w"
	double z_w_dot;						// dimensional derivative of force component Z with respect to "w_dot"
	double z_q;							// dimensional derivative of force component Z with respect to "q"
	double m_u;							// dimensional derivative of pitching moment M with respect to "u"
	double m_w;							// dimensional derivative of pitching moment M with respect to "w"
	double m_w_dot;						// dimensional derivative of pitching moment M with respect to "w_dot"
	double m_q;						    // dimensional derivative of pitching moment M with respect to "q"

	double x_delta_T_CT;				// dimensional control derivative of force component X with respect to "delta_T" for Constant Thrust
	double x_delta_T_CP;				// dimensional control derivative of force component X with respect to "delta_T" for Constant Power
	double x_delta_T_CMF;				// dimensional control derivative of force component X with respect to "delta_T" for Constant Mass Flow
	double x_delta_T_RJ;				// dimensional control derivative of force component X with respect to "delta_T" for RamJet
	double x_delta_E;				    // dimensional control derivative of force component X with respect to "delta_E"
	double z_delta_T;				    // dimensional control derivative of force component Z with respect to "delta_T"
	double z_delta_E;				    // dimensional control derivative of force component Z with respect to "delta_E"
	double m_delta_T;				    // dimensional control derivative of pitching moment M with respect to "delta_T"
	double m_delta_E;					// dimensional control derivative of pitching moment M with respect to "delta_E"

	double y_beta;						// dimensional derivative of force component Y with respect to "beta"
	double y_p   ;						// dimensional derivative of force component Y with respect to "p"
	double y_r   ;						// dimensional derivative of force component Y with respect to "r"
	double l_beta;						// dimensional derivative of rolling moment L with respect to "beta"
	double l_p   ;						// dimensional derivative of rolling moment L with respect to "p"
	double l_r   ;						// dimensional derivative of rolling moment L with respect to "r"
	double n_beta;						// dimensional derivative of yawing moment N with respect to "beta"
	double n_p   ;						// dimensional derivative of yawing moment N with respect to "p"
	double n_r   ;						// dimensional derivative of yawing moment N with respect to "r"

	double y_delta_A;					// dimensional control derivative of force component Y with respect to "delta_A"
	double y_delta_R;					// dimensional control derivative of force component Y with respect to "delta_R"
	double l_delta_A;					// dimensional control derivative of rolling moment L with respect to "delta_A"
	double l_delta_R;					// dimensional control derivative of rolling moment L with respect to "delta_R"
	double n_delta_A;					// dimensional control derivative of yawing moment N with respect to "delta_A"
	double n_delta_R;					// dimensional control derivative of yawing moment N with respect to "delta_R"

	double [][] aLon = new double [4][4];		// longitudinal coefficients [A_Lon] matrix
	double [][] bLon = new double [4][2];		// longitudinal control coefficients [B_Lon] matrix
	double [][] aLD = new double [4][4];		// lateral-directional coefficients [A_LD] matrix
	double [][] bLD = new double [4][2];		// lateral-directional control coefficients [B_LD] matrix

	double[][] lonEigenvaluesMatrix = new double [4][2];	// longitudinal eigenvalues matrix
	double[][] ldEigenvaluesMatrix = new double [4][2];		// lateral-directional eigenvalues matrix

	RealVector eigLonVec1;						// longitudinal 1st eigenvector
	RealVector eigLonVec2;						// longitudinal 2nd eigenvector
	RealVector eigLonVec3;						// longitudinal 3rd eigenvector
	RealVector eigLonVec4;						// longitudinal 4th eigenvector
	RealVector eigLDVec1;						// lateral-directional 1st eigenvector
	RealVector eigLDVec2;						// lateral-directional 2nd eigenvector
	RealVector eigLDVec3;						// lateral-directional 3rd eigenvector
	RealVector eigLDVec4;						// lateral-directional 4th eigenvector

	double zeta_SP;                             // Short Period mode damping coefficient
	double zeta_PH;                             // Phugoid mode damping coefficient
	double omega_n_SP;                          // Short Period mode natural frequency
	double omega_n_PH;                          // Phugoid mode natural frequency
	double period_SP;                           // Short Period mode period
	double period_PH;      						// Phugoid mode period
	double t_half_SP;							// Short Period mode halving time
	double t_half_PH;							// Phugoid mode halving time
	double N_half_SP;							// Short Period mode number of cycles to halving time
	double N_half_PH;							// Phugoid mode number of cycles to halving time

	double zeta_DR;                             // Dutch-Roll mode damping coefficient                             
	double omega_n_DR;                          // Dutch-Roll mode natural frequency
	double period_DR;                           // Dutch-Roll mode period
	double t_half_DR;							// Dutch-Roll mode halving time
	double N_half_DR;							// Dutch-Roll mode number of cycles to halving time

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------

	private void initializeData() {
		
		// this.rho0 = 
		
		// TODO @agodemar
		
		//..............................................................................
		// initialize containers of results
		

	}

	@SuppressWarnings("unchecked")	
	public static ACDynamicStabilityManager importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			ConditionEnum currentCondition
			) throws IOException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading dynamic stability analysis data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		PropulsionTypeEnum propulsionSystem = PropulsionTypeEnum.valueOf(
				reader.getXMLAttributeByPath("//dynamic_stability/global_data/propulsion_system", "type"));

		//---------------------------------------------------------------------------------------
		// WEIGHTS FROM FILE INSTRUCTION
		Boolean readWeightsFromPreviousAnalysisFlag;
		String readWeightsFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@weights_from_previous_analysis");
		if(readWeightsFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readWeightsFromPreviousAnalysisFlag = Boolean.TRUE;
		else {
			readWeightsFromPreviousAnalysisFlag = Boolean.FALSE;
			if(theAircraft.getTheAnalysisManager().getTheAnalysisManagerInterface().isIterativeLoop() == true) {
				System.err.println("WARNING (IMPORT PERFORMANCE DATA): IF THE ITERATIVE LOOP FLAG IS 'TRUE', THE 'weights_from_previous_analysis' FLAG MUST BE TRUE. TERMINATING ...");
				System.exit(1);
			}
		}

		//---------------------------------------------------------------------------------------
		// BALANCE FROM FILE INSTRUCTION
		Boolean readBalanceFromPreviousAnalysisFlag;
		String readBalanceFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@balance_from_previous_analysis");
		if(readBalanceFromPreviousAnalysisString.equalsIgnoreCase("true")) {			
			readBalanceFromPreviousAnalysisFlag = Boolean.TRUE;
		}
		else {
			readBalanceFromPreviousAnalysisFlag = Boolean.FALSE;
		}
		
		//---------------------------------------------------------------------------------------
		// AERODYNAMICS FROM FILE INSTRUCTION
		Boolean readAerodynamicsFromPreviousAnalysisFlag;
		String readAerodynamicsFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@aerodynamics_from_previous_analysis");
		if(readAerodynamicsFromPreviousAnalysisString.equalsIgnoreCase("true"))
			readAerodynamicsFromPreviousAnalysisFlag = Boolean.TRUE;
		else {
			readAerodynamicsFromPreviousAnalysisFlag = Boolean.FALSE;
			if(theAircraft.getTheAnalysisManager().getTheAnalysisManagerInterface().isIterativeLoop() == true) {
				System.err.println("WARNING (IMPORT PERFORMANCE DATA): IF THE ITERATIVE LOOP FLAG IS 'TRUE', THE 'aerodynamics_from_previous_analysis' FLAG MUST BE TRUE. TERMINATING ...");
				System.exit(1);
			}
		}
		
		//===========================================================================================
		// READING WEIGHTS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the ACWeightsManager and ignores the assigned
		 * data inside the xml file.
		 * Otherwise it ignores the manager file and reads the input data from the xml.
		 */
		Amount<Mass> maximumTakeOffMass = null;
		Amount<Mass> operatingEmptyMass = null;
		Amount<Mass> maximumFuelMass = null;
		Amount<Mass> maximumPayload = null;
		Amount<Mass> singlePassengerMass = null;
		
		if(readWeightsFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheWeights() != null) {

					//...............................................................
					// MAXIMUM TAKE-OFF MASS
					maximumTakeOffMass = theAircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(SI.KILOGRAM);

					//...............................................................
					// OPERATING EMPTY MASS
					operatingEmptyMass = theAircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().to(SI.KILOGRAM);

					//...............................................................
					// MAXIMUM FUEL MASS
					if(theAircraft.getFuelTank() != null) 
						maximumFuelMass = theAircraft.getFuelTank().getFuelMass().to(SI.KILOGRAM);
					else {
						System.err.println("WARNING!! THE FUEL TANK DOES NOT EXIST ... TERMINATING");
						System.exit(1);
					}
					
					//...............................................................
					// MAXIMUM PAYLOAD
					maximumPayload = theAircraft.getTheAnalysisManager().getTheWeights().getMaxPayload().to(SI.KILOGRAM);
					
					//...............................................................
					// SINGLE PASSENGER MASS
					singlePassengerMass = theAircraft.getTheAnalysisManager().getTheWeights().getTheWeightsManagerInterface().getSinglePassengerMass().to(SI.KILOGRAM);
						
				}
				else {
					System.err.println("WARNING!! THE WEIGHTS ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
			}
		}
		else {
			//...............................................................
			// MAXIMUM TAKE-OFF MASS
			String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//dynamic_stability/weights/maximum_take_off_mass");
			if(maximumTakeOffMassProperty != null)
				maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//dynamic_stability/weights/maximum_take_off_mass");

			//...............................................................
			// OPERATING EMPTY MASS
			String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//dynamic_stability/weights/operating_empty_mass");
			if(operatingEmptyMassProperty != null)
				operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//dynamic_stability/weights/operating_empty_mass");

			//...............................................................
			// MAXIMUM FUEL MASS
			String maximumFuelMassProperty = reader.getXMLPropertyByPath("//dynamic_stability/weights/maximum_fuel_mass");
			if(maximumFuelMassProperty != null)
				maximumFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//dynamic_stability/weights/maximum_fuel_mass");
			
			//...............................................................
			// MAXIMUM PAYLOAD
			String maximumPayloadProperty = reader.getXMLPropertyByPath("//dynamic_stability/weights/maximum_payload");
			if(maximumPayloadProperty != null)
				maximumPayload = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//dynamic_stability/weights/maximum_payload");
			else
				maximumPayload =  Amount.valueOf( 
						(theAircraft.getCabinConfiguration().getDesignPassengerNumber()*121)+700,
						SI.KILOGRAM
						);

			//...............................................................
			// SINGLE PASSENGER MASS
			String singlePassengerMassProperty = reader.getXMLPropertyByPath("//dynamic_stability/weights/single_passenger_mass");
			if(singlePassengerMassProperty != null)
				singlePassengerMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//dynamic_stability/weights/single_passenger_mass");
		}
		
		//===========================================================================================
		// READING INERTIA DATA ...
		/********************************************************************************************/
		Map<Double, Double> iXX = new HashMap<>();
		Map<Double, Double> iYY = new HashMap<>();
		Map<Double, Double> iZZ = new HashMap<>();
		Map<Double, Double> iXZ = new HashMap<>();
		
		if (readBalanceFromPreviousAnalysisFlag == Boolean.TRUE) {
			
			// TODO @agodemar
		}
		else {
			
			// get the node <aerodynamics/>
			NodeList nodeListBalance = MyXMLReaderUtils
					.getXMLNodeListByPath(reader.getXmlDoc(), "//analysis/dynamic_stability/balance");
			
			if (nodeListBalance.getLength() == 0) throw new AssertionError("Must have a node analysis/dynamic_stability/balance !");
			
			// take first occurrence
			Node nodeBalance = nodeListBalance.item(0);
			
			// get the node list of <xcg/>'s
			NodeList xcgNodelist = MyXMLReaderUtils
					.getXMLNodeListByPath(nodeBalance, "//xcg");

			if (xcgNodelist.getLength() == 0) throw new AssertionError("Must have at least one node analysis/dynamic_stability/balance/xcg !");
			
			// System.out.println("balance/xcg nodes found: " + xcgNodelist.getLength());
			
			for(int i = 0; i < xcgNodelist.getLength(); i++) {

				Node node = xcgNodelist.item(i);
				Double xcg = MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "/");

				iXX
					.put(xcg, MyXMLReaderUtils.getXMLAmountFromAttributeValueWithUnitByPath(node, "//iXX")
						.doubleValue(MyUnits.KILOGRAM_METER_SQUARED));
				iYY
					.put(xcg, MyXMLReaderUtils.getXMLAmountFromAttributeValueWithUnitByPath(node, "//iYY")
						.doubleValue(MyUnits.KILOGRAM_METER_SQUARED));
				iZZ
					.put(xcg, MyXMLReaderUtils.getXMLAmountFromAttributeValueWithUnitByPath(node, "//iZZ")
							.doubleValue(MyUnits.KILOGRAM_METER_SQUARED));
				iXZ
					.put(xcg, MyXMLReaderUtils.getXMLAmountFromAttributeValueWithUnitByPath(node, "//iXZ")
						.doubleValue(MyUnits.KILOGRAM_METER_SQUARED));
				
				/*
				System.out.println("xcg = " + xcg);
				System.out.println("IXX = " + iXX.get(xcg));
				System.out.println("IYY = " + iYY.get(xcg));
				System.out.println("IZZ = " + iZZ.get(xcg));
				System.out.println("IXZ = " + iXZ.get(xcg));
				*/
				
			}
			
			
		}
		
		//===========================================================================================
		// READING AERODYNAMICS DATA ...
		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the ACAerodynamicAndStabilityManager and ignores the assigned
		 * data inside the xml file.
		 * Otherwise it ignores the manager file and reads the input data from the xml.
		 */
		
		List<Double> centerOfGravityList = new ArrayList<>();
		
		Map<Double,Double> cLmaxClean = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaClean  = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaTakeOff = new HashMap<>();
		Map<Double,Amount<?>> cLAlphaLanding = new HashMap<>();
		Map<Double,Double> cLmaxTakeOff = new HashMap<>();
		Map<Double,Double> cLZeroTakeOff = new HashMap<>();
		Map<Double,Double> cLmaxLanding = new HashMap<>();
		Map<Double,Double> cLZeroLanding = new HashMap<>();
		Map<Double,Double> deltaCD0TakeOff = new HashMap<>();
		Map<Double,Double> deltaCD0Landing = new HashMap<>();
		Map<Double,Double> deltaCD0LandingGears = new HashMap<>();
		
		Map<Double, Double> cD0 = new HashMap<>();
		Map<Double,Double> oswaldCruise = new HashMap<>();
		Map<Double,Double> oswaldClimb = new HashMap<>();
		Map<Double,Double> oswaldTakeOff = new HashMap<>();
		Map<Double,Double> oswaldLanding = new HashMap<>();

		Map<Double,double[]> polarCLCruise = new HashMap<>();
		Map<Double,double[]> polarCDCruise = new HashMap<>();
		Map<Double,double[]> polarCLClimb = new HashMap<>();
		Map<Double,double[]> polarCDClimb = new HashMap<>();
		Map<Double,double[]> polarCLTakeOff = new HashMap<>();
		Map<Double,double[]> polarCDTakeOff = new HashMap<>();
		Map<Double,double[]> polarCLLanding = new HashMap<>();
		Map<Double,double[]> polarCDLanding = new HashMap<>();
		
		Map<Double, Double> cDrag0 = new HashMap<>();
		Map<Double, Double> cDragAlpha0 = new HashMap<>();
		Map<Double, Double> cDragMach0 = new HashMap<>();
		
		Map<Double, Double> cLift0 = new HashMap<>();
		Map<Double, Double> cLiftAlpha0 = new HashMap<>();
		Map<Double, Double> cLiftAlphaDot0 = new HashMap<>();
		Map<Double, Double> cLiftMach0 = new HashMap<>();
		Map<Double, Double> cLiftQ0 = new HashMap<>();
		Map<Double, Double> cLiftDeltaT = new HashMap<>();
		Map<Double, Double> cLiftDeltaE = new HashMap<>();
		
		Map<Double, Double> cPitchAlpha0 = new HashMap<>();
		Map<Double, Double> cPitchAlphaDot0 = new HashMap<>();
		Map<Double, Double> cPitchMach0 = new HashMap<>();
		Map<Double, Double> cPitchQ0 = new HashMap<>();
		Map<Double, Double> cPitchDeltaT = new HashMap<>();
		Map<Double, Double> cPitchDeltaE = new HashMap<>();
		
		Map<Double, Double> cThrustFix = new HashMap<>();
		Map<Double, Double> kVThrust = new HashMap<>();
		
		Map<Double, Double> cSideBeta = new HashMap<>();
		Map<Double, Double> cSideP = new HashMap<>();
		Map<Double, Double> cSideR = new HashMap<>();
		Map<Double, Double> cSideDeltaA = new HashMap<>();
		Map<Double, Double> cSideDeltaR = new HashMap<>();

		Map<Double, Double> cRollBeta = new HashMap<>();
		Map<Double, Double> cRollP = new HashMap<>();
		Map<Double, Double> cRollR = new HashMap<>();
		Map<Double, Double> cRollDeltaA = new HashMap<>();
		Map<Double, Double> cRollDeltaR = new HashMap<>();
		
		Map<Double, Double> cYawBeta = new HashMap<>();
		Map<Double, Double> cYawP = new HashMap<>();
		Map<Double, Double> cYawR = new HashMap<>();
		Map<Double, Double> cYawDeltaA = new HashMap<>();
		Map<Double, Double> cYawDeltaR = new HashMap<>();
		
		if(readAerodynamicsFromPreviousAnalysisFlag == Boolean.TRUE) {
			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheAerodynamicAndStability() != null) {
					
					// TODO @agodemar
					
				}
			}
		}
		else { // read aero data from xml, not from previous analysis
			
			
			Boolean parabolicDragPolarFlag = Boolean.FALSE;
			String paraboliDragPolarProperty = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//dynamic_stability/aerodynamics/@parabolicDragPolar");
			if(paraboliDragPolarProperty.equalsIgnoreCase("TRUE"))
				parabolicDragPolarFlag = Boolean.TRUE;
			else if(paraboliDragPolarProperty.equalsIgnoreCase("FALSE"))
				parabolicDragPolarFlag = Boolean.FALSE;
			else {
				System.err.println("ERROR : parabolicDragPolar HAS TO BE TRUE/FALSE !");
				System.exit(1);
			}

			//...............................................................
			// Xcg LIST
			
			
			// Treat <xcg/> as a NodeList under node <aerodynamics/>
			
			// get the node <aerodynamics/>
			Node nodeAerodynamics = MyXMLReaderUtils
					.getXMLNodeListByPath(reader.getXmlDoc(), "//dynamic_stability/aerodynamics")
					.item(0);
			
			// get the node list of <xcg/>'s
			NodeList xcgNodelist = MyXMLReaderUtils
					.getXMLNodeListByPath(nodeAerodynamics, "//xcg");
			
			// TODO refactor the following code in order to treat dynamic_stability/aerodynamics/xcg as a node list
			
			List<String> xCGListProperty = MyXMLReaderUtils.getXMLPropertiesByPath(reader.getXmlDoc(), reader.getXpath(),"//dynamic_stability/aerodynamics/xcg/@value");
			if(!xCGListProperty.isEmpty())
				xCGListProperty.stream().forEach(xcg -> centerOfGravityList.add(Double.valueOf(xcg)));
			
			//...............................................................
			// CLmax CLEAN
			List<String> cLmaxCleanProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cLmax_clean_configuration");
			if(!cLmaxCleanProperty.isEmpty())
				for(int i=0; i<cLmaxCleanProperty.size(); i++)
					cLmaxClean.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxCleanProperty.get(i))
							);
			//...............................................................
			// CLalpha CLEAN
			List<String> cLAlphaCleanProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cL_alpha_clean_configuration");
			List<Amount<?>> cLAlphaCleanList = new ArrayList<>();
			if(!cLAlphaCleanProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaCleanList.add(
							reader.getXMLAmountWithUnitByPath("//dynamic_stability/aerodynamics/xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_clean_configuration")
							);
			}
			for(int i=0; i<cLAlphaCleanList.size(); i++)
				cLAlphaClean.put(
						centerOfGravityList.get(i), 
						cLAlphaCleanList.get(i)
						);
				
			//...............................................................
			// CLalpha TAKE-OFF
			List<String> cLAlphaTakeOffProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cL_alpha_take_off");
			List<Amount<?>> cLAlphaTakeOffList = new ArrayList<>();
			if(!cLAlphaTakeOffProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaTakeOffList.add(
							reader.getXMLAmountWithUnitByPath("//dynamic_stability/aerodynamics/xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_take_off")
							);
			}
			for(int i=0; i<cLAlphaTakeOffList.size(); i++)
				cLAlphaTakeOff.put(
						centerOfGravityList.get(i), 
						cLAlphaTakeOffList.get(i)
						);
			//...............................................................
			// CLalpha LANDING
			List<String> cLAlphaLandingProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cL_alpha_landing");
			List<Amount<?>> cLAlphaLandingList = new ArrayList<>();
			if(!cLAlphaLandingProperty.isEmpty()) {
				for(int i=0; i<centerOfGravityList.size(); i++) 
					cLAlphaLandingList.add(
							reader.getXMLAmountWithUnitByPath("//dynamic_stability/aerodynamics/xcg[@value='" + centerOfGravityList.get(i) + "']/cL_alpha_landing")
							);
			}
			for(int i=0; i<cLAlphaLandingList.size(); i++)
				cLAlphaLanding.put(
						centerOfGravityList.get(i), 
						cLAlphaLandingList.get(i)
						);
			//...............................................................
			// CLmax TAKE-OFF
			List<String> cLmaxTakeOffProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cLmax_take_off_configuration");
			if(!cLmaxTakeOffProperty.isEmpty())
				for(int i=0; i<cLmaxTakeOffProperty.size(); i++)
					cLmaxTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxTakeOffProperty.get(i))
							);
			//...............................................................
			// CL0 TAKE-OFF
			List<String> cL0TakeOffProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cL0_take_off_configuration");
			if(!cL0TakeOffProperty.isEmpty())
				for(int i=0; i<cL0TakeOffProperty.size(); i++)
					cLZeroTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(cL0TakeOffProperty.get(i))
							);
			//...............................................................
			// CLmax LANDING
			List<String> cLmaxLandingProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cLmax_landing_configuration");
			if(!cLmaxLandingProperty.isEmpty())
				for(int i=0; i<cLmaxLandingProperty.size(); i++)
					cLmaxLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(cLmaxLandingProperty.get(i))
							);
			//...............................................................
			// CL0 LANDING
			List<String> cL0LandingProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cL0_landing_configuration");
			if(!cL0LandingProperty.isEmpty())
				for(int i=0; i<cL0LandingProperty.size(); i++)
					cLZeroLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(cL0LandingProperty.get(i))
							);
			
			//...............................................................
			// DeltaCD0 TAKE-OFF
			List<String> deltaCD0TakeOffProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/delta_CD0_flap_take_off");
			if(!deltaCD0TakeOffProperty.isEmpty())
				for(int i=0; i<deltaCD0TakeOffProperty.size(); i++)
					deltaCD0TakeOff.put(
						centerOfGravityList.get(i),
						Double.valueOf(deltaCD0TakeOffProperty.get(i))
						);
			//...............................................................
			// DeltaCD0 LANDING
			List<String> deltaCD0LandingProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/delta_CD0_flap_landing");
			if(!deltaCD0LandingProperty.isEmpty())
				for(int i=0; i<deltaCD0LandingProperty.size(); i++)
					deltaCD0Landing.put(
						centerOfGravityList.get(i),
						Double.valueOf(deltaCD0LandingProperty.get(i))
						);
			//...............................................................
			// DeltaCD0 LANDING GEARS
			List<String> deltaCD0LandingGearsProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/delta_CD0_landing_gears");
			if(!deltaCD0LandingGearsProperty.isEmpty())
				for(int i=0; i<deltaCD0LandingGearsProperty.size(); i++)
					deltaCD0LandingGears.put(
						centerOfGravityList.get(i),
						Double.valueOf(deltaCD0LandingGearsProperty.get(i))
						);
			
			if(parabolicDragPolarFlag == Boolean.FALSE) {
				
				// TODO @agodemar
				
				// ...
				
				// ...
				
			}
			else {
				//...............................................................
				// CD0
				List<String> cD0Property = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/cD0");
				if(!cD0Property.isEmpty())
					for(int i=0; i<cD0Property.size(); i++)
						cD0.put(
							centerOfGravityList.get(i),
							Double.valueOf(cD0Property.get(i))
							);
				//...............................................................
				// OSWALD CRUISE
				List<String> oswladCruiseProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/oswald_cruise");
				if(!oswladCruiseProperty.isEmpty())
					for(int i=0; i<oswladCruiseProperty.size(); i++)
						oswaldCruise.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladCruiseProperty.get(i))
							);
				//...............................................................
				// OSWALD CLIMB
				List<String> oswladClimbProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/oswald_climb");
				if(!oswladClimbProperty.isEmpty())
					for(int i=0; i<oswladClimbProperty.size(); i++)
						oswaldClimb.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladClimbProperty.get(i))
							);
				//...............................................................
				// OSWALD TO
				List<String> oswladTOProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/oswald_take_off");
				if(!oswladTOProperty.isEmpty())
					for(int i=0; i<oswladTOProperty.size(); i++)
						oswaldTakeOff.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladTOProperty.get(i))
							);
				//...............................................................
				// OSWALD LND
				List<String> oswladLNDProperty = reader.getXMLPropertiesByPath("//dynamic_stability/aerodynamics/xcg/oswald_landing");
				if(!oswladLNDProperty.isEmpty())
					for(int i=0; i<oswladLNDProperty.size(); i++)
						oswaldLanding.put(
							centerOfGravityList.get(i),
							Double.valueOf(oswladLNDProperty.get(i))
							);
					
				int numberOfPolarPoints = 50;

				centerOfGravityList.stream().forEach(xcg -> {

					//...............................................................
					// POLAR CURVE CRUISE
					polarCLCruise.put(
							xcg, 
							MyArrayUtils.linspace(-0.2, cLmaxClean.get(xcg), numberOfPolarPoints)
							);				
					polarCDCruise.put(
							xcg,
							new double[polarCLCruise.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE CLIMB
					polarCLClimb.put(
							xcg, 
							MyArrayUtils.linspace(-0.2, cLmaxClean.get(xcg), numberOfPolarPoints)
							);				
					polarCDClimb.put(
							xcg,
							new double[polarCLClimb.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE TAKE-OFF
					polarCLTakeOff.put(
							xcg, 
							MyArrayUtils.linspace(-0.2, cLmaxTakeOff.get(xcg), numberOfPolarPoints)
							);				
					polarCDTakeOff.put(
							xcg,
							new double[polarCLTakeOff.get(xcg).length]
							);

					//...............................................................
					// POLAR CURVE LANDING
					polarCLLanding.put(
							xcg, 
							MyArrayUtils.linspace(-0.2, cLmaxLanding.get(xcg), numberOfPolarPoints)
							);				
					polarCDLanding.put(
							xcg,
							new double[polarCLLanding.get(xcg).length]
							);

					//...............................................................
					// building the CD arrays...
					for(int i=0; i<numberOfPolarPoints; i++) {
						polarCDClimb.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg),
								polarCLClimb.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldClimb.get(xcg), 
								theOperatingConditions.getMachClimb(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLCruise.get(xcg)[i],
										theOperatingConditions.getMachClimb(),
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										theAircraft.getWing().getAirfoilList().get(0).getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getType()
										)
								);
						polarCDCruise.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg),
								polarCLCruise.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldCruise.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLCruise.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										theAircraft.getWing().getAirfoilList().get(0).getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getType()
										)
								);
						polarCDTakeOff.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg) + deltaCD0TakeOff.get(xcg) + deltaCD0LandingGears.get(xcg),
								polarCLTakeOff.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldTakeOff.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLTakeOff.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										theAircraft.getWing().getAirfoilList().get(0).getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getType()
										)
								);
						polarCDLanding.get(xcg)[i] = DragCalc.calculateCDTotal(
								cD0.get(xcg) + deltaCD0Landing.get(xcg) + deltaCD0LandingGears.get(xcg),
								polarCLLanding.get(xcg)[i],
								theAircraft.getWing().getAspectRatio(),
								oswaldLanding.get(xcg), 
								theOperatingConditions.getMachCruise(),
								DragCalc.calculateCDWaveLockKornCriticalMachKroo(
										polarCLLanding.get(xcg)[i],
										theOperatingConditions.getMachCruise(),
										theAircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord(),
										theAircraft.getWing().getAirfoilList().get(0).getThicknessToChordRatio(),
										theAircraft.getWing().getAirfoilList().get(0).getType()
										)
								);
					}
				});
			}
			
			// System.out.println("analysis/dynamic_stability/aerodynamics/xcg nodes: " + xcgNodelist.getLength());
			// System.out.println("analysis/dynamic_stability/dynamic_stability/xcg nodes: " + centerOfGravityList.size());
			
			if (centerOfGravityList.size() != xcgNodelist.getLength()) throw new AssertionError("Inconsistent nodes analysis/dynamic_stability/aerodynamics/xcg and analysis/dynamic_stability/dynamic_stability/xcg !");

			for(int i = 0; i < xcgNodelist.getLength(); i++) {
				
				Double xcg = centerOfGravityList.get(i);
				Node node = xcgNodelist.item(i);
				
				cDrag0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cD0"));
				cDragAlpha0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cD_Alpha0"));
				cDragMach0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cD_Mach0"));
				cLift0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cL0"));
				cLiftAlpha0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cL_Alpha0"));
				cLiftAlphaDot0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cL_Alpha_dot0"));
				cLiftMach0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cL_Mach0"));
				cLiftQ0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cL_q0"));
				cLiftDeltaT
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cL_delta_T"));
				cLiftDeltaE
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cL_delta_E"));

				cPitchAlpha0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cm_alpha0"));
				cPitchAlphaDot0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cm_alpha_dot0"));
				cPitchMach0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cm_Mach0"));
				cPitchQ0
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cm_q"));
				cPitchDeltaE
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cm_delta_E"));
				cPitchDeltaT
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cm_delta_T"));
				
				cThrustFix
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//c_Tfix"));
				kVThrust
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//k_TV"));
				
				cSideBeta
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cY_beta"));
				cSideP
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cY_p"));
				cSideR
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cY_r"));
				cSideDeltaA
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cY_delta_A"));
				cSideDeltaR
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cY_delta_R"));

				cRollBeta
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cRoll_beta"));
				cRollP
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cRoll_p"));
				cRollR
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cRoll_r"));
				cRollDeltaA
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cRoll_delta_A"));
				cRollDeltaR
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cRoll_delta_R"));

				cYawBeta
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cYaw_beta"));
				cYawP
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cYaw_p"));
				cYawR
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cYaw_r"));
				cYawDeltaA
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cYaw_delta_A"));
				cYawDeltaR				
					.put(xcg, MyXMLReaderUtils.getXMLDoubleFromAttributeValueByPath(node, "//cYaw_delta_A"));
			}
		}		
		
		// TODO @agodemar
		//===========================================================================================
		// BUILDING THE CALCULATOR ...
		IACDynamicStabilityManager theDynamicsManagerInterface = new IACDynamicStabilityManager.Builder()
				.setId(id)
				.setTheAircraft(theAircraft)
				.setTheOperatingConditions(theOperatingConditions)
				.setCurrentCondition(currentCondition)
				.setPropulsionSystem(propulsionSystem)
				.setMaximumTakeOffMass(maximumTakeOffMass.to(SI.KILOGRAM))
				.setOperatingEmptyMass(operatingEmptyMass.to(SI.KILOGRAM))
				.setMaximumFuelMass(maximumFuelMass.to(SI.KILOGRAM))
				.setMaximumPayload(maximumPayload.to(SI.KILOGRAM))
				.setSinglePassengerMass(singlePassengerMass.to(SI.KILOGRAM))	
				.addAllXcgPositionList(centerOfGravityList)
				.putAllCLmaxClean(cLmaxClean)
				.putAllCLAlphaClean(cLAlphaClean)
				.putAllCLAlphaTakeOff(cLAlphaTakeOff)
				.putAllCLAlphaLanding(cLAlphaLanding)
				.putAllCLmaxTakeOff(cLmaxTakeOff)
				.putAllCLZeroTakeOff(cLZeroTakeOff)
				.putAllCLmaxLanding(cLmaxLanding)
				.putAllCLZeroLanding(cLZeroLanding)
				.putAllDeltaCD0LandingGears(deltaCD0LandingGears)
				.putAllDeltaCD0FlapTakeOff(deltaCD0TakeOff)
				.putAllDeltaCD0FlapLanding(cLZeroLanding)
				.putAllPolarCLCruise(polarCLCruise)
				.putAllPolarCDCruise(polarCDCruise)
				.putAllPolarCLClimb(polarCLClimb)
				.putAllPolarCDClimb(polarCDClimb)
				.putAllPolarCLTakeOff(polarCLTakeOff)
				.putAllPolarCDTakeOff(polarCDTakeOff)
				.putAllPolarCLLanding(polarCLLanding)
				.putAllPolarCDLanding(polarCDLanding)
				.putAllIXX(iXX) // TODO ...
				.putAllIYY(iYY)
				.putAllIZZ(iZZ)
				.putAllIXZ(iXZ)
				.putAllCDrag0(cDrag0)
				.putAllCDragAlpha0(cDragAlpha0)
				.putAllCDragMach0(cDragMach0)
				.putAllCLift0(cLift0)
				.putAllCLiftAlpha0(cLiftAlpha0)
				.putAllCLiftAlphaDot0(cLiftAlphaDot0)
				.putAllCLiftMach0(cLiftMach0)
				.putAllCLiftQ0(cLiftQ0)
				.putAllCLiftDeltaE(cLiftDeltaE)
				.putAllCLiftDeltaT(cLiftDeltaT)
				.putAllCPitchAlpha0(cPitchAlpha0)
				.putAllCPitchAlphaDot0(cPitchAlphaDot0)
				.putAllCPitchMach0(cPitchMach0)
				.putAllCPitchQ0(cPitchQ0)
				.putAllCPitchDeltaE(cPitchDeltaE)
				.putAllCPitchDeltaT(cPitchDeltaT)
				.putAllCThrustFix(cThrustFix)
				.putAllKVThrust(kVThrust)
				.putAllCSideBeta(cSideBeta)
				.putAllCSideP(cSideP)
				.putAllCSideR(cSideR)
				.putAllCSideDeltaA(cSideDeltaA)
				.putAllCSideDeltaR(cSideDeltaR)
				.putAllCRollBeta(cRollBeta)
				.putAllCRollP(cRollP)
				.putAllCRollR(cRollR)
				.putAllCRollDeltaA(cRollDeltaA)
				.putAllCRollDeltaR(cRollDeltaR)
				.putAllCYawBeta(cYawBeta)
				.putAllCYawP(cYawP)
				.putAllCYawR(cYawR)
				.putAllCYawDeltaA(cYawDeltaA)
				.putAllCYawDeltaR(cYawDeltaR)
				// TODO: add the rest... ???
				.build();

		ACDynamicStabilityManager theDynamicsManager = new ACDynamicStabilityManager();
		theDynamicsManager.setTheDynamicsInterface(theDynamicsManagerInterface);

		return theDynamicsManager;
	}

	/**
	 * This method initializes the related calculators inner classes and 
	 * performs the required calculation
	 */
	public void calculate(String resultsFolderPath) {

		initializeData();
		
		String dynamicStabilityFolderPath = JPADStaticWriteUtils.createNewFolder(
				resultsFolderPath 
				+ "DYNAMIC_STABILITY"
				+ File.separator
				);
		
		for(int i=0; i<_theDynamicStabilityManagerInterface.getXcgPositionList().size(); i++) {
			
			
			String xcgFolderPath = JPADStaticWriteUtils.createNewFolder(
					dynamicStabilityFolderPath 
					+ "XCG_" + _theDynamicStabilityManagerInterface.getXcgPositionList().get(i)
					+ File.separator
					);

			
			// Air density at current operating condition
			
			switch (_theDynamicStabilityManagerInterface.getCurrentCondition()) {
			case TAKE_OFF:
				this._currentMachNumber = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getMachTakeOff();
				this._currentAltitude = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getAltitudeTakeOff();
				this._currentDeltaTemperature = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getDeltaTemperatureTakeOff();
				this._alphaBodyCurrent = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getAlphaTakeOff();
				this.currentMass = _theDynamicStabilityManagerInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM);
				this.u0 = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getTasTakeOff().doubleValue(SI.METERS_PER_SECOND);
				this.qbar0 = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getDynamicPressureTakeOff().doubleValue(SI.PASCAL);
				break;
			case CLIMB:
				this._currentMachNumber = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getMachClimb();
				this._currentAltitude = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getAltitudeClimb();
				this._currentDeltaTemperature = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getDeltaTemperatureClimb();
				this._alphaBodyCurrent = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getAlphaClimb();
				this.currentMass = _theDynamicStabilityManagerInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM);
				this.u0 = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getTasClimb().doubleValue(SI.METERS_PER_SECOND);
				this.qbar0 = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getDynamicPressureClimb().doubleValue(SI.PASCAL);
				break;
			case CRUISE:
				this._currentMachNumber = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getMachCruise();
				this._currentAltitude = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getAltitudeCruise();
				this._currentDeltaTemperature = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getDeltaTemperatureCruise();
				this._alphaBodyCurrent = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getAlphaCruise();
				this.currentMass = _theDynamicStabilityManagerInterface.getMaximumTakeOffMass().doubleValue(SI.KILOGRAM)*0.80; // TODO check this assumption
				this.u0 = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getTasCruise().doubleValue(SI.METERS_PER_SECOND);
				this.qbar0 = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getDynamicPressureCruise().doubleValue(SI.PASCAL);
				break;
			case LANDING:
				this._currentMachNumber = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getMachLanding();
				this._currentAltitude = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getAltitudeLanding();
				this._currentDeltaTemperature = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getDeltaTemperatureLanding();
				this._alphaBodyCurrent = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getAlphaLanding();
				this.currentMass = _theDynamicStabilityManagerInterface.getMaximumTakeOffMass()
						.minus(_theDynamicStabilityManagerInterface.getMaximumFuelMass().times(0.80)) // TODO check this assumption
						.doubleValue(SI.KILOGRAM); 
				this.u0 = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getTasLanding().doubleValue(SI.METERS_PER_SECOND);
				this.qbar0 = _theDynamicStabilityManagerInterface.getTheOperatingConditions().getDynamicPressureLanding().doubleValue(SI.PASCAL);
				break;
			default:
				break;
			}
			
			// TODO refactor, clean the code below and use _theDynamicStabilityCalculatorInterface instead
			
			this.rho0 				= AtmosphereCalc.getDensity(_currentAltitude.doubleValue(SI.METER), _currentDeltaTemperature.doubleValue(SI.CELSIUS));
			this.mach0 				= this._currentMachNumber;
			this.reference_area 	= _theDynamicStabilityManagerInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE);
			this.cbar 				= _theDynamicStabilityManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER);
			this.wing_span 			= _theDynamicStabilityManagerInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER);
			
			this.gamma0 = 0.0;
			this.theta0 = gamma0;

			Double xcg = _theDynamicStabilityManagerInterface.getXcgPositionList().get(i);
			this.iXX = _theDynamicStabilityManagerInterface.getIXX().get(xcg);
			this.iYY = _theDynamicStabilityManagerInterface.getIYY().get(xcg);
			this.iZZ = _theDynamicStabilityManagerInterface.getIZZ().get(xcg);
			this.iXZ = _theDynamicStabilityManagerInterface.getIXZ().get(xcg);
			this.cDrag0  = _theDynamicStabilityManagerInterface.getCDrag0().get(xcg);
			this.cDrag_alpha0 = _theDynamicStabilityManagerInterface.getCDragAlpha0().get(xcg);
			this.cDrag_Mach0 = _theDynamicStabilityManagerInterface.getCDragMach0().get(xcg);
			this.cL0  = _theDynamicStabilityManagerInterface.getCLift0().get(xcg);
			this.cL_alpha0  = _theDynamicStabilityManagerInterface.getCLiftAlpha0().get(xcg);
			this.cL_alpha_dot0 = _theDynamicStabilityManagerInterface.getCLiftAlphaDot0().get(xcg);
			this.cL_Mach0 = _theDynamicStabilityManagerInterface.getCLiftMach0().get(xcg);
			this.cL_q0 = _theDynamicStabilityManagerInterface.getCLiftQ0().get(xcg);
			this.cL_delta_E = _theDynamicStabilityManagerInterface.getCLiftDeltaE().get(xcg);
			this.cL_delta_T = _theDynamicStabilityManagerInterface.getCLiftDeltaT() .get(xcg);
			this.cm_alpha0 = _theDynamicStabilityManagerInterface.getCPitchAlpha0().get(xcg);
			this.cm_alpha_dot0 = _theDynamicStabilityManagerInterface.getCPitchAlphaDot0().get(xcg);
			this.cm_Mach0 = _theDynamicStabilityManagerInterface.getCPitchMach0().get(xcg);
			this.cm_q = _theDynamicStabilityManagerInterface.getCPitchQ0().get(xcg);
			this.cm_delta_E = _theDynamicStabilityManagerInterface.getCPitchDeltaE().get(xcg);
			this.cm_delta_T = _theDynamicStabilityManagerInterface.getCPitchDeltaT().get(xcg);
			this.c_Tfix = _theDynamicStabilityManagerInterface.getCThrustFix().get(xcg);
			this.kv = _theDynamicStabilityManagerInterface.getKVThrust().get(xcg);
			this.cY_beta = _theDynamicStabilityManagerInterface.getCSideBeta().get(xcg);
			this.cY_p = _theDynamicStabilityManagerInterface.getCSideP().get(xcg);
			this.cY_r = _theDynamicStabilityManagerInterface.getCSideR().get(xcg);
			this.cY_delta_A = _theDynamicStabilityManagerInterface.getCSideDeltaA().get(xcg);
			this.cY_delta_R = _theDynamicStabilityManagerInterface.getCSideDeltaR().get(xcg);
			this.cRoll_beta = _theDynamicStabilityManagerInterface.getCRollBeta().get(xcg);
			this.cRoll_p = _theDynamicStabilityManagerInterface.getCRollP().get(xcg);
			this.cRoll_r = _theDynamicStabilityManagerInterface.getCRollR().get(xcg);
			this.cRoll_delta_A = _theDynamicStabilityManagerInterface.getCRollDeltaA().get(xcg);
			this.cRoll_delta_R = _theDynamicStabilityManagerInterface.getCRollDeltaR().get(xcg);
			this.cYaw_beta = _theDynamicStabilityManagerInterface.getCYawBeta().get(xcg);
			this.cYaw_p = _theDynamicStabilityManagerInterface.getCYawP().get(xcg);
			this.cYaw_r = _theDynamicStabilityManagerInterface.getCYawR().get(xcg);
			this.cYaw_delta_A = _theDynamicStabilityManagerInterface.getCYawDeltaA().get(xcg);
			this.cYaw_delta_R = _theDynamicStabilityManagerInterface.getCYawDeltaR().get(xcg);
			
			// TODO
			this._theDynamicStabilityCalculatorInterface = new IDynamicStabilityCalculator.Builder()
					.setDensity0(AtmosphereCalc.getDensity(_currentAltitude.doubleValue(SI.METER), _currentDeltaTemperature.doubleValue(SI.CELSIUS)))
					.setReferenceArea(_theDynamicStabilityManagerInterface.getTheAircraft().getWing().getSurfacePlanform().doubleValue(SI.SQUARE_METRE))
					.setReferenceChord(_theDynamicStabilityManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER))
					.setWingSpan(_theDynamicStabilityManagerInterface.getTheAircraft().getWing().getSpan().doubleValue(SI.METER))
					.setMass(currentMass)
					.setSpeed0(this.u0)
					.setDynamicPressure0(this.qbar0)
					.setMach0(this._currentMachNumber)
					.setFlightPathAngle0(0.0)
					.setElevationAngle0(0.0)
					.setXCGPercentMAC(_theDynamicStabilityManagerInterface.getXcgPositionList().get(i))
					.setIXX(_theDynamicStabilityManagerInterface.getIXX().get(xcg))
					.setIYY(_theDynamicStabilityManagerInterface.getIYY().get(xcg))
					.setIZZ(_theDynamicStabilityManagerInterface.getIZZ().get(xcg))
					.setIXZ(_theDynamicStabilityManagerInterface.getIXZ().get(xcg))
					.setCDrag0 (_theDynamicStabilityManagerInterface.getCDrag0().get(xcg))
					.setCDragAlpha0(_theDynamicStabilityManagerInterface.getCDragAlpha0().get(xcg))
					.setCDragMach0(_theDynamicStabilityManagerInterface.getCDragMach0().get(xcg))
					.setCLift0 (_theDynamicStabilityManagerInterface.getCLift0().get(xcg))
					.setCLiftAlpha0 (_theDynamicStabilityManagerInterface.getCLiftAlpha0().get(xcg))
					.setCLiftAlphaDot0(_theDynamicStabilityManagerInterface.getCLiftAlphaDot0().get(xcg))
					.setCLiftMach0(_theDynamicStabilityManagerInterface.getCLiftMach0().get(xcg))
					.setCLiftQ0(_theDynamicStabilityManagerInterface.getCLiftQ0().get(xcg))
					.setCLiftDeltaE(_theDynamicStabilityManagerInterface.getCLiftDeltaE().get(xcg))
					.setCLiftDeltaT(_theDynamicStabilityManagerInterface.getCLiftDeltaT() .get(xcg))
					.setCPitchAlpha0(_theDynamicStabilityManagerInterface.getCPitchAlpha0().get(xcg))
					.setCPitchAlphaDot0(_theDynamicStabilityManagerInterface.getCPitchAlphaDot0().get(xcg))
					.setCPitchMach0(_theDynamicStabilityManagerInterface.getCPitchMach0().get(xcg))
					.setCPitchQ0(_theDynamicStabilityManagerInterface.getCPitchQ0().get(xcg))
					.setCPitchDeltaE(_theDynamicStabilityManagerInterface.getCPitchDeltaE().get(xcg))
					.setCPitchDeltaT(_theDynamicStabilityManagerInterface.getCPitchDeltaT().get(xcg))
					.setCThrustFix(_theDynamicStabilityManagerInterface.getCThrustFix().get(xcg))
					.setKVThrust(_theDynamicStabilityManagerInterface.getKVThrust().get(xcg))
					.setCSideBeta0(_theDynamicStabilityManagerInterface.getCSideBeta().get(xcg))
					.setCSideP0(_theDynamicStabilityManagerInterface.getCSideP().get(xcg))
					.setCSideR0(_theDynamicStabilityManagerInterface.getCSideR().get(xcg))
					.setCSideDeltaA(_theDynamicStabilityManagerInterface.getCSideDeltaA().get(xcg))
					.setCSideDeltaR(_theDynamicStabilityManagerInterface.getCSideDeltaR().get(xcg))
					.setCRollBeta0(_theDynamicStabilityManagerInterface.getCRollBeta().get(xcg))
					.setCRollP0(_theDynamicStabilityManagerInterface.getCRollP().get(xcg))
					.setCRollR0(_theDynamicStabilityManagerInterface.getCRollR().get(xcg))
					.setCRollDeltaA(_theDynamicStabilityManagerInterface.getCRollDeltaA().get(xcg))
					.setCRollDeltaR(_theDynamicStabilityManagerInterface.getCRollDeltaR().get(xcg))
					.setCYawBeta0(_theDynamicStabilityManagerInterface.getCYawBeta().get(xcg))
					.setCYawP0(_theDynamicStabilityManagerInterface.getCYawP().get(xcg))
					.setCYawR0(_theDynamicStabilityManagerInterface.getCYawR().get(xcg))
					.setCYawDeltaA(_theDynamicStabilityManagerInterface.getCYawDeltaA().get(xcg))
					.setCYawDeltaR(_theDynamicStabilityManagerInterface.getCYawDeltaR().get(xcg))
					.build();
			
			// Formats numbers up to 4 decimal places
			NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
	        DecimalFormat df = (DecimalFormat)nf;
	        df.applyPattern("#,###,##0.0000");

			////////////////////          LONGITUDINAL DYNAMICS          \\\\\\\\\\\\\\\\\\\\

	        // TODO: make a calculator class
	        // >>> new CalcACDynamicStability(_theDynamicStabilityCalculatorInterface)
	        // that makes all calculations
	        
			// Calculates the longitudinal Stability and Control DERIVATIVES \\
			x_u_CT  = ACDynamicStabilityManagerUtils.calcX_u_CT(rho0, reference_area, currentMass, u0, qbar0, cDrag0, mach0, cDrag_Mach0);
			x_u_CP  = ACDynamicStabilityManagerUtils.calcX_u_CP(rho0, reference_area, currentMass, u0, qbar0, cDrag0, cm_Mach0, cDrag_Mach0, cL0, gamma0);
			x_w     = ACDynamicStabilityManagerUtils.calcX_w(rho0, reference_area, currentMass, u0, qbar0, cL0, cDrag_alpha0);
			x_w_dot = 0;
			x_q     = 0;
			z_u     = ACDynamicStabilityManagerUtils.calcZ_u(rho0, reference_area, currentMass, u0, qbar0, cm_Mach0, cL0, cL_Mach0);
			z_w     = ACDynamicStabilityManagerUtils.calcZ_w(rho0, reference_area, currentMass, u0, qbar0, cm_Mach0, cDrag0, cL_alpha0);
			z_w_dot = ACDynamicStabilityManagerUtils.calcZ_w_dot(rho0, reference_area, currentMass, cbar, cL_alpha_dot0);
			z_q     = ACDynamicStabilityManagerUtils.calcZ_q(rho0, reference_area, currentMass, u0, qbar0, cbar, cL_q0);
			m_u     = ACDynamicStabilityManagerUtils.calcM_u(rho0, reference_area, currentMass, u0, qbar0, cbar, cDrag_Mach0, iYY, cm_Mach0);
			m_w     = ACDynamicStabilityManagerUtils.calcM_w(rho0, reference_area, currentMass, u0, qbar0, cbar, iYY, cm_alpha0);
			m_w_dot = ACDynamicStabilityManagerUtils.calcM_w_dot(rho0, reference_area, cbar, iYY, cm_alpha_dot0);
			m_q     = ACDynamicStabilityManagerUtils.calcM_q(rho0, currentMass, u0, qbar0, reference_area, cbar, iYY, cm_q);

			x_delta_T_CT  = ACDynamicStabilityManagerUtils.calcX_delta_T_CT (rho0, reference_area, currentMass, u0, qbar0, c_Tfix, kv);
			x_delta_T_CP  = ACDynamicStabilityManagerUtils.calcX_delta_T_CP (rho0, reference_area, currentMass, u0, qbar0, c_Tfix, kv);
			x_delta_T_CMF = ACDynamicStabilityManagerUtils.calcX_delta_T_CMF (rho0, reference_area, currentMass, u0, qbar0, c_Tfix, kv);
			x_delta_T_RJ  = ACDynamicStabilityManagerUtils.calcX_delta_T_RJ (rho0, reference_area, currentMass, u0, qbar0, c_Tfix, kv);
			x_delta_T_CT  = ACDynamicStabilityManagerUtils.calcX_delta_T_CT (rho0, reference_area, currentMass, u0, qbar0, c_Tfix, kv);
			x_delta_E     = 0;
			z_delta_T     = ACDynamicStabilityManagerUtils.calcZ_delta_T (rho0, reference_area, currentMass, u0, qbar0, cL_delta_T);
			z_delta_E     = ACDynamicStabilityManagerUtils.calcZ_delta_E (rho0, reference_area, currentMass, u0, qbar0, cL_delta_E);
			m_delta_T     = ACDynamicStabilityManagerUtils.calcM_delta_T (rho0, reference_area, cbar, u0, qbar0, iYY, cm_delta_T);
			m_delta_E     = ACDynamicStabilityManagerUtils.calcM_delta_E (rho0, reference_area, cbar, u0, qbar0, iYY, cm_delta_E);

			// Prints out the LONGITUDINAL STABILITY AND CONTROL DERIVATIVES LIST \\
			System.out.println("_________________________________________________________________\n");
			System.out.println("LONGITUDINAL STABILITY DERIVATIVES: \n");
			System.out.println(" X_u_CT  = " + df.format(x_u_CT));
			System.out.println(" X_u_CP  = " + df.format(x_u_CP));
			System.out.println(" X_w     = " + df.format(x_w));
			System.out.println(" X_w_dot = " + df.format(x_w_dot));
			System.out.println(" X_q     = " + df.format(x_q));
			System.out.println(" Z_u     = " + df.format(z_u));
			System.out.println(" Z_w     = " + df.format(z_w));
			System.out.println(" Z_w_dot = " + df.format(z_w_dot));
			System.out.println(" Z_q     = " + df.format(z_q));
			System.out.println(" M_u     = " + df.format(m_u));
			System.out.println(" M_w     = " + df.format(m_w));
			System.out.println(" M_w_dot = " + df.format(m_w_dot));
			System.out.println(" M_q     = " + df.format(m_q));
			System.out.println("\n\nLONGITUDINAL CONTROL DERIVATIVES: \n");
			System.out.println(" X_delta_T_CT  = " + df.format(x_delta_T_CT));
			System.out.println(" X_delta_T_CP  = " + df.format(x_delta_T_CP));
			System.out.println(" X_delta_T_CMF = " + df.format(x_delta_T_CMF));
			System.out.println(" X_delta_T_RJ  = " + df.format(x_delta_T_RJ));
			System.out.println(" X_delta_E     = " + df.format(x_delta_E));
			System.out.println(" Z_delta_T     = " + df.format(z_delta_T));
			System.out.println(" Z_delta_E     = " + df.format(z_delta_E));
			System.out.println(" M_delta_T     = " + df.format(m_delta_T));
			System.out.println(" M_delta_E     = " + df.format(m_delta_E)+"\n");

			// Generates and prints out the [A_Lon] and [B_Lon] MATRICES \\
			System.out.println("_________________________________________________________________\n");
			System.out.println("MATRIX [A_LON]: \n");

			aLon = ACDynamicStabilityManagerUtils.build_A_Lon_matrix (propulsion_system, rho0, reference_area, currentMass, cbar, u0, qbar0, cDrag0, mach0, cDrag_Mach0, cL0,
					cL_Mach0, cDrag_alpha0, gamma0, theta0,cL_alpha0, cL_alpha_dot0, cm_alpha0, cm_alpha_dot0, cL_q0, iYY, cm_Mach0, cm_q);
			
			System.out.println(df.format(aLon[0][0])+"\t\t"+df.format(aLon[0][1])+"\t\t"+df.format(aLon[0][2])+"\t\t"+df.format(aLon[0][3])+"\n");
			System.out.println(df.format(aLon[1][0])+"\t\t"+df.format(aLon[1][1])+"\t\t"+df.format(aLon[1][2])+"\t\t"+df.format(aLon [1][3])+"\n");
			System.out.println(df.format(aLon[2][0])+"\t\t"+df.format(aLon[2][1])+"\t\t"+df.format(aLon[2][2])+"\t\t"+df.format(aLon [2][3])+"\n");
			System.out.println(aLon[3][0]+"\t\t"+aLon[3][1]+"\t\t"+aLon[3][2]+"\t\t"+aLon [3][3]+"\n");

			System.out.println("_________________________________________________________________\n");
			System.out.println("MATRIX [B_LON]: \n");

			bLon = ACDynamicStabilityManagerUtils.build_B_Lon_matrix (propulsion_system, rho0, reference_area, currentMass, cbar, u0, qbar0, cDrag0, mach0, cDrag_Mach0, cL0,
					cDrag_alpha0, gamma0, theta0, cL_alpha0, cL_alpha_dot0, cm_alpha0, cm_alpha_dot0, cL_q0, iYY, cm_Mach0, cm_q, c_Tfix,
					kv, cL_delta_T, cL_delta_E, cm_delta_T, cm_delta_E);

			System.out.println(df.format(bLon[0][0])+"\t\t"+df.format(bLon[0][1])+"\n");
			System.out.println(df.format(bLon[1][0])+"\t\t"+df.format(bLon[1][1])+"\n");
			System.out.println(df.format(bLon[2][0])+"\t\t"+df.format(bLon[2][1])+"\n");
			System.out.println(bLon[3][0]+"\t\t"+bLon[3][1]+"\n");

			// Generates and prints out the Eigenvalues of [A_Lon] matrix \\
			lonEigenvaluesMatrix = ACDynamicStabilityManagerUtils.buildEigenValuesMatrix(aLon);

			System.out.println("_________________________________________________________________\n");
			System.out.println("LONGITUDINAL EIGENVALUES\n");
			System.out.println("  SHORT PERIOD: "+df.format(lonEigenvaluesMatrix[0][0])+" � j"+df.format(lonEigenvaluesMatrix[0][1])+"\n");
			System.out.println("  PHUGOID:      "+df.format(lonEigenvaluesMatrix[2][0])+" � j"+df.format(lonEigenvaluesMatrix[2][1])+"\n");

			// Generates and prints out the EigenVectors of [A_Lon] matrix \\
			System.out.println("_________________________________________________________________\n");
			System.out.println("LONGITUDINAL EIGENVECTORS:\n");
			
			eigLonVec1 = ACDynamicStabilityManagerUtils.buildEigenVector(aLon, 0);
			eigLonVec2 = ACDynamicStabilityManagerUtils.buildEigenVector(aLon, 1);
			eigLonVec3 = ACDynamicStabilityManagerUtils.buildEigenVector(aLon, 2);
			eigLonVec4 = ACDynamicStabilityManagerUtils.buildEigenVector(aLon, 3);
			
			System.out.println("EigenVector 1 = " + eigLonVec1);
			System.out.println("EigenVector 2 = " + eigLonVec2);
			System.out.println("EigenVector 3 = " + eigLonVec3);
			System.out.println("EigenVector 4 = " + eigLonVec4+"\n");
			
			// Generates and prints out all the characteristics for longitudinal SHORT PERIOD and PHUGOID modes \\
			zeta_SP    = ACDynamicStabilityManagerUtils.calcDampingCoefficient(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
			zeta_PH    = ACDynamicStabilityManagerUtils.calcDampingCoefficient(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);
			omega_n_SP = ACDynamicStabilityManagerUtils.calcNaturalPulsation(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
			omega_n_PH = ACDynamicStabilityManagerUtils.calcNaturalPulsation(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);
			period_SP  = ACDynamicStabilityManagerUtils.calcPeriod(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
			period_PH  = ACDynamicStabilityManagerUtils.calcPeriod(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);
			t_half_SP  = ACDynamicStabilityManagerUtils.calcTimeToHalf(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
			t_half_PH  = ACDynamicStabilityManagerUtils.calcTimeToHalf(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);
			N_half_SP  = ACDynamicStabilityManagerUtils.calcNCyclesToHalf(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
			N_half_PH  = ACDynamicStabilityManagerUtils.calcNCyclesToHalf(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);

			System.out.println("_________________________________________________________________\n");
			System.out.println("SHORT PERIOD MODE CHARACTERISTICS\n");
			System.out.println("Zeta_SP                          = "+df.format(zeta_SP)+"\n");
			System.out.println("Omega_n_SP                       = "+df.format(omega_n_SP)+"\n");
			System.out.println("Period                           = "+df.format(period_SP)+"\n");
			System.out.println("Halving Time                     = "+df.format(t_half_SP)+"\n");
			System.out.println("Number of cycles to Halving Time = "+df.format(N_half_SP)+"\n\n");

			System.out.println("PHUGOID MODE CHARACTERISTICS\n");		
			System.out.println("Zeta_PH                          = "+df.format(zeta_PH)+"\n");
			System.out.println("Omega_n_PH                       = "+df.format(omega_n_PH)+"\n");
			System.out.println("Period                           = "+df.format(period_PH)+"\n");
			System.out.println("Halving Time                     = "+df.format(t_half_PH)+"\n");
			System.out.println("Number of cycles to Halving Time = "+df.format(N_half_PH)+"\n");

	        ////////////////////          LATERAL-DIRECTIONAL DYNAMICS          \\\\\\\\\\\\\\\\\\\\
			
			// Calculates the lateral-directional Stability and Control DERIVATIVES \\
			y_beta = ACDynamicStabilityManagerUtils.calcY_beta (rho0, reference_area, currentMass, u0, qbar0, cY_beta);
			y_p    = ACDynamicStabilityManagerUtils.calcY_p (rho0, reference_area, currentMass, u0, qbar0, wing_span, cY_p);
			y_r    = ACDynamicStabilityManagerUtils.calcY_r (rho0, reference_area, currentMass, u0, qbar0, wing_span, cY_r);
			l_beta = ACDynamicStabilityManagerUtils.calcL_beta (rho0, reference_area, wing_span, iXX, u0, qbar0, cRoll_beta);
			l_p    = ACDynamicStabilityManagerUtils.calcL_p (rho0, reference_area, wing_span, iXX, u0, qbar0, cRoll_p);
			l_r    = ACDynamicStabilityManagerUtils.calcL_r (rho0, reference_area, wing_span, iXX, u0, qbar0, cRoll_r);
			n_beta = ACDynamicStabilityManagerUtils.calcN_beta (rho0, reference_area, wing_span, iZZ, u0, qbar0, cYaw_beta);
			n_p    = ACDynamicStabilityManagerUtils.calcN_p (rho0, reference_area, wing_span, iZZ, u0, qbar0, cYaw_p);
			n_r    = ACDynamicStabilityManagerUtils.calcN_r (rho0, reference_area, wing_span, iZZ, u0, qbar0, cYaw_r);
			y_delta_A = ACDynamicStabilityManagerUtils.calcY_delta_A (rho0, reference_area, currentMass, u0, qbar0, cY_delta_A);
			y_delta_R = ACDynamicStabilityManagerUtils.calcY_delta_R (rho0, reference_area, currentMass, u0, qbar0, cY_delta_R);
			l_delta_A = ACDynamicStabilityManagerUtils.calcL_delta_A (rho0, reference_area, wing_span, iXX, u0, qbar0, cRoll_delta_A);
			l_delta_R = ACDynamicStabilityManagerUtils.calcL_delta_R (rho0, reference_area, wing_span, iXX, u0, qbar0, cRoll_delta_R);
			n_delta_A = ACDynamicStabilityManagerUtils.calcN_delta_A (rho0, reference_area, wing_span, iZZ, u0, qbar0, cYaw_delta_A);
			n_delta_R = ACDynamicStabilityManagerUtils.calcN_delta_R (rho0, reference_area, wing_span, iZZ, u0, qbar0, cYaw_delta_R);

			// Prints out the LATERAL-DIRECTIONAL STABILITY AND CONTROL DERIVATIVES LIST \\
			System.out.println("_________________________________________________________________\n");
			System.out.println("LATERAL-DIRECTIONAL STABILITY DERIVATIVES: \n");
			System.out.println(" Y_beta = " + df.format(y_beta));
			System.out.println(" Y_p    = " + df.format(y_p));
			System.out.println(" Y_r    = " + df.format(y_r));
			System.out.println(" L_beta = " + df.format(l_beta));
			System.out.println(" L_p    = " + df.format(l_p));
			System.out.println(" L_r    = " + df.format(l_r));
			System.out.println(" N_beta = " + df.format(n_beta));
			System.out.println(" N_p    = " + df.format(n_p));
			System.out.println(" N_r    = " + df.format(n_r));
			System.out.println("\n\nLATERAL-DIRECTIONAL CONTROL DERIVATIVES: \n");
			System.out.println(" Y_delta_A = " + df.format(y_delta_A));
			System.out.println(" Y_delta_R = " + df.format(y_delta_R));
			System.out.println(" L_delta_A = " + df.format(l_delta_A));
			System.out.println(" L_delta_R = " + df.format(l_delta_R));
			System.out.println(" N_delta_A = " + df.format(n_delta_A));
			System.out.println(" N_delta_R = " + df.format(n_delta_R)+"\n");

			// Generates and prints out the [A_Ld] and [B_Ld] MATRICES \\
			System.out.println("_________________________________________________________________\n");
			System.out.println("MATRIX [A_LD]: \n");

			aLD = ACDynamicStabilityManagerUtils.build_A_LD_matrix (rho0, reference_area, currentMass, cbar, wing_span, u0, qbar0, 
					theta0, iXX, iZZ, iXZ, cY_beta, cY_p, cY_r, cY_delta_A, cY_delta_R, cRoll_beta,
					cRoll_p, cRoll_r, cRoll_delta_A, cRoll_delta_R, cYaw_beta, cYaw_p, cYaw_r, cYaw_delta_A, cYaw_delta_R);

			System.out.println(df.format(aLD[0][0])+"\t\t"+df.format(aLD[0][1])+"\t\t"+df.format(aLD[0][2])+"\t\t"+df.format(aLD[0][3])+"\n");
			System.out.println(df.format(aLD[1][0])+"\t\t"+df.format(aLD[1][1])+"\t\t"+df.format(aLD[1][2])+"\t\t"+df.format(aLD [1][3])+"\n");
			System.out.println(df.format(aLD[2][0])+"\t\t"+df.format(aLD[2][1])+"\t\t"+df.format(aLD[2][2])+"\t\t"+df.format(aLD [2][3])+"\n");
			System.out.println(aLD[3][0]+"\t\t"+aLD[3][1]+"\t\t"+aLD[3][2]+"\t\t"+aLD [3][3]+"\n");

			System.out.println("_________________________________________________________________\n");
			System.out.println("MATRIX [B_LD]: \n");

			bLD = ACDynamicStabilityManagerUtils.build_B_LD_matrix (rho0, reference_area, currentMass, cbar, wing_span, u0, qbar0, 
					iXX, iZZ, iXZ, cY_delta_A, cY_delta_R, cRoll_delta_A, cRoll_delta_R, cYaw_delta_A, cYaw_delta_R);

			System.out.println(df.format(bLD[0][0])+"\t\t"+df.format(bLD[0][1])+"\n");
			System.out.println(df.format(bLD[1][0])+"\t\t"+df.format(bLD[1][1])+"\n");
			System.out.println(df.format(bLD[2][0])+"\t\t"+df.format(bLD[2][1])+"\n");
			System.out.println(bLD[3][0]+"\t\t"+bLD[3][1]+"\n");

			// Generates and prints out the Eigenvalues of [A_Ld] matrix \\
			ldEigenvaluesMatrix = ACDynamicStabilityManagerUtils.buildEigenValuesMatrix(aLD);

			System.out.println("_________________________________________________________________\n");
			System.out.println("LATERAL-DIRECTIONAL EIGENVALUES\n");
			System.out.println("  ROLL:       "+df.format(ldEigenvaluesMatrix[2][0])+"\n");
			System.out.println("  DUTCH-ROLL: "+df.format(ldEigenvaluesMatrix[0][0])+" � j"+df.format(ldEigenvaluesMatrix[0][1])+"\n");
			System.out.println("  SPIRAL:     "+df.format(ldEigenvaluesMatrix[3][0])+"\n");
			
			// Generates and prints out the EigenVectors of [A_Ld] matrix \\
			System.out.println("_________________________________________________________________\n");
			System.out.println("LATERAL-DIRECTIONAL EIGENVECTORS:\n");

			eigLDVec1 = ACDynamicStabilityManagerUtils.buildEigenVector(aLD, 0);
			eigLDVec2 = ACDynamicStabilityManagerUtils.buildEigenVector(aLD, 1);
			eigLDVec3 = ACDynamicStabilityManagerUtils.buildEigenVector(aLD, 2);
			eigLDVec4 = ACDynamicStabilityManagerUtils.buildEigenVector(aLD, 3);

			System.out.println("EigenVector 1 = " + eigLDVec1);
			System.out.println("EigenVector 2 = " + eigLDVec2);
			System.out.println("EigenVector 3 = " + eigLDVec3);
			System.out.println("EigenVector 4 = " + eigLDVec4+"\n");
			
			// Generates and prints out all the characteristics for lateral-directional DUTCH-ROLL mode \\
			zeta_DR    = ACDynamicStabilityManagerUtils.calcDampingCoefficient(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);
			omega_n_DR = ACDynamicStabilityManagerUtils.calcNaturalPulsation(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);
			period_DR  = ACDynamicStabilityManagerUtils.calcPeriod(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);
			t_half_DR  = ACDynamicStabilityManagerUtils.calcTimeToHalf(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);
			N_half_DR  = ACDynamicStabilityManagerUtils.calcNCyclesToHalf(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);

			System.out.println("_________________________________________________________________\n");
			System.out.println("DUTCH-ROLL MODE CHARACTERISTICS\n");
			System.out.println("Zeta_DR                          = "+df.format(zeta_DR)+"\n");
			System.out.println("Omega_n_DR                       = "+df.format(omega_n_DR)+"\n");
			System.out.println("Period                           = "+df.format(period_DR)+"\n");
			System.out.println("Halving Time                     = "+df.format(t_half_DR)+"\n");
			System.out.println("Number of cycles to Halving Time = "+df.format(N_half_DR)+"\n\n");
			
			
			
			// TODO
			
			
			// PRINT RESULTS
			try {
				toXLSFile(
						xcgFolderPath + "DynamicStability_" + _theDynamicStabilityManagerInterface.getXcgPositionList().get(i),
						_theDynamicStabilityManagerInterface.getXcgPositionList().get(i));
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 		
			
			// TODO @agodemar make dynamic stability calculations
		}
	}

	@SuppressWarnings("unchecked")
	public void toXLSFile(String filenameWithPathAndExt, Double xcg) throws InvalidFormatException, IOException {
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

        //--------------------------------------------------------------------------------
        // TAKE-OFF ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_theDynamicStabilityManagerInterface.getTaskList().contains(ConditionEnum.TAKE_OFF)) {
        	Sheet sheet = wb.createSheet("TAKE-OFF");
        	List<Object[]> dataListTakeOff = new ArrayList<>();
        	
        	dataListTakeOff.add(new Object[] {"Description","Unit","Value"});
        	
        	// TODO @agodemar
        	
        	Row rowTakeOff = sheet.createRow(0);
        	Object[] objArrTakeOff = dataListTakeOff.get(0);
        	int cellnumTakeOff = 0;
        	for (Object obj : objArrTakeOff) {
        		Cell cell = rowTakeOff.createCell(cellnumTakeOff++);
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
        		sheet.setDefaultColumnWidth(35);
        		sheet.setColumnWidth(1, 2048);
        		sheet.setColumnWidth(2, 3840);
        	}
        	int rownumTakeOff = 1;
        	for (int i = 1; i < dataListTakeOff.size(); i++) {
        		objArrTakeOff = dataListTakeOff.get(i);
        		rowTakeOff = sheet.createRow(rownumTakeOff++);
        		cellnumTakeOff = 0;
        		for (Object obj : objArrTakeOff) {
        			Cell cell = rowTakeOff.createCell(cellnumTakeOff++);
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
        	}
        }
		//--------------------------------------------------------------------------------
		// CLIMB ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_theDynamicStabilityManagerInterface.getTaskList().contains(ConditionEnum.CLIMB)) {
        	Sheet sheet = wb.createSheet("CLIMB");
        	List<Object[]> dataListClimb = new ArrayList<>();
        	
        	dataListClimb.add(new Object[] {"Description","Unit","Value"});
        	
        	// TODO @agodemar
        	
        	Row rowClimb = sheet.createRow(0);
        	Object[] objArrClimb = dataListClimb.get(0);
        	int cellnumClimb = 0;
        	for (Object obj : objArrClimb) {
        		Cell cell = rowClimb.createCell(cellnumClimb++);
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
        		sheet.setDefaultColumnWidth(35);
        		sheet.setColumnWidth(1, 2048);
        		sheet.setColumnWidth(2, 3840);
        	}
        	int rownumClimb = 1;
        	for (int i = 1; i < dataListClimb.size(); i++) {
        		objArrClimb = dataListClimb.get(i);
        		rowClimb = sheet.createRow(rownumClimb++);
        		cellnumClimb = 0;
        		for (Object obj : objArrClimb) {
        			Cell cell = rowClimb.createCell(cellnumClimb++);
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
        	}
        }

		//--------------------------------------------------------------------------------
		// CRUISE ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_theDynamicStabilityManagerInterface.getTaskList().contains(ConditionEnum.CRUISE)) {
        	Sheet sheet = wb.createSheet("CRUISE");
        	List<Object[]> dataListCruise = new ArrayList<>();
        	
        	dataListCruise.add(new Object[] {"Description","Unit","Value"});
        	
        	// TODO @agodemar
        	
        	Row rowCruise = sheet.createRow(0);
        	Object[] objArrCruise = dataListCruise.get(0);
        	int cellnumCruise = 0;
        	for (Object obj : objArrCruise) {
        		Cell cell = rowCruise.createCell(cellnumCruise++);
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
        		sheet.setDefaultColumnWidth(35);
        		sheet.setColumnWidth(1, 2048);
        		sheet.setColumnWidth(2, 3840);
        	}
        	int rownumCruise = 1;
        	for (int i = 1; i < dataListCruise.size(); i++) {
        		objArrCruise = dataListCruise.get(i);
        		rowCruise = sheet.createRow(rownumCruise++);
        		cellnumCruise = 0;
        		for (Object obj : objArrCruise) {
        			Cell cell = rowCruise.createCell(cellnumCruise++);
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
        	}
        }
        
        //--------------------------------------------------------------------------------
        // LANDING ANALYSIS RESULTS:
        //--------------------------------------------------------------------------------
        if(_theDynamicStabilityManagerInterface.getTaskList().contains(ConditionEnum.LANDING)) {
        	Sheet sheet = wb.createSheet("LANDING");
        	List<Object[]> dataListLanding = new ArrayList<>();
        	
        	dataListLanding.add(new Object[] {"Description","Unit","Value"});
        	
        	// TODO @agodemar
        	
        	Row rowLanding = sheet.createRow(0);
        	Object[] objArrLanding = dataListLanding.get(0);
        	int cellnumLanding = 0;
        	for (Object obj : objArrLanding) {
        		Cell cell = rowLanding.createCell(cellnumLanding++);
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
        		sheet.setDefaultColumnWidth(35);
        		sheet.setColumnWidth(1, 2048);
        		sheet.setColumnWidth(2, 3840);
        	}
        	int rownumLanding = 1;
        	for (int i = 1; i < dataListLanding.size(); i++) {
        		objArrLanding = dataListLanding.get(i);
        		rowLanding = sheet.createRow(rownumLanding++);
        		cellnumLanding = 0;
        		for (Object obj : objArrLanding) {
        			Cell cell = rowLanding.createCell(cellnumLanding++);
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
        	}
        }
        
		
	}
	
	// GETTERS/SETTERS
	private void setTheDynamicsInterface(IACDynamicStabilityManager theDynamicsManagerInterface) {
		this._theDynamicStabilityManagerInterface = theDynamicsManagerInterface;
	}

	public IACDynamicStabilityManager getTheDynamicStabilityManagerInterface() {
		return _theDynamicStabilityManagerInterface;
	}
}
