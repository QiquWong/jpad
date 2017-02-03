package calculators.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.Aircraft;
import calculators.performance.customdata.AircraftPointMassSimulationResultMap;
import configuration.enumerations.EngineOperatingConditionEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyUnits;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

/*
 * See the book by D. K. Schmidt, Modern Flight Dynamics McGraw-Hill
 * Example 8.11 (Chapter 8): Case Study - A Nonlinear Aircraft-Performance Simulation
 * 
 * Assumptions:
 *    -> vehicle's sideslip (β) and aerodynamic side force are zero
 *      (as in steady level flight or in a steady coordinated turn)
 *    -> T·cosα ≈ T, T·sinα ≪ L 
 *
 * Variables:
 *   Vv = inertial vehicle's speed
 *   Va = airspeed
 *   α = angle of attack
 *   β = angle of sideslip (NOT USED)
 *   γ = flight-path angle
 *   ϕ = wind-axes bank (INPUT) <-----------------------
 *   ψ = wind-axes heading angle
 *   h = altitude
 *   Xi = inertial x-coordinate (flat-Earth)
 *   Yi = inertial y-coordinate (flat-Earth)
 *   
 *   m = aircraft mass
 *   ρ = air density
 *   T = thrust (INPUT) <-------------------------------
 *   D = drag
 *   L = lift (INPUT) <---------------------------------
 *   S = side force (NOT USED)
 *   
 *   dot_wf = fuel flow rate
 * 
 * 
 * Equations of motion:
 * 
 *    1) dot( Vv ) = (T - D)/m - g sinγ
 *    2) dot( γ  ) = (L cosϕ - m g cosγ)/(m Vv)
 *    3) dot( ψ  ) = L sinϕ /(m Vv cosγ)
 *    4) dot( Xi ) = Vv cosγ cosψ
 *    5) dot( Yi ) = Vv cosγ sinψ
 *    6) dot( h  ) = Vv sinγ
 *    7) dot( xT ) = m (Vc - Vv)
 *    8) dot( T  ) = -pT T + pT (KTi xT + KTp m (Vc - Vv) )
 *    9) dot( xL ) = m Vc ( sinγc - sinγ)
 *   10) dot( L  ) = -pL L + pL (KLi xL + KLp m Vc ( sinγc - sinγ))
 *   11) dot( ϕ  ) = -pϕ ϕ + pϕ Kϕp Vc (ψc - ψ)/g 
 *   12) dot( m  ) = - dot_wf/g = - Kwf T
 *   
 * In presence of wind:
 * 
 *   vec W = Wx vec Ii + Wy vec Ji - Wh vec Ki
 *   Ii, Ji, Ki = versors of inertial reference frame (flat-Earth)
 *   vec Va = - vec Vv + vec W
 * 
 *   dot( Xw ) = Vv cosγ cosψ - Wx
 *   dot( Yw ) = Vv cosγ sinψ - Wy
 *   dot( hw ) = Vv sinγ      - Wh
 *   
 *   Va = sqrt( dot( Xw )^2 + dot( Yw )^2 + dot( hw )^2 )
 *   
 * Constants:
 *   g = gravity acceleration
 *   Sw = wing reference area
 *   ARw = wing aspect ratio
 *   e = Oswald factor
 *   Kwf = a constant depending on the aircraft
 *   
 * Model:
 *   C_L = C_Lα * (α - α0)
 *   C_D = D_D0 + C_L^2/KD
 *   L = C_L (0.5 ρ Va^2) Sw
 *   D = C_D (0.5 ρ Va^2) Sw
 *   KD = 3.1415/(ARw e)
 *   
 * Inputs:
 *   L(t)
 *     D = KD0 Va^2 + KD1 L^2/Va^2
 *     α = KL L/Va^2 + α0
 *     KD0 = 0.5 ρ Sw CD0
 *     KD1 = 2/(ρ Sw KD)
 *     KL = 2/(ρ Sw C_Lα)
 *   T(t)
 * 
 * Guidance laws:
 * 
 *   Commanded quantities:
 *   
 *   Tc = commanded thrust
 *   Lc = commanded lift
 *   ϕc = commanded bank angle
 *   Vc = commanded velocity (USER DEFINED)
 *   dot(h)c = commanded rate of climb (USER DEFINED)
 *           = Vc sin γc
 *   ψc = commanded heading (USER DEFINED)
 *   
 *   dot( xT ) = m (Vc - Vv)
 *   --> Tc = KTI xT + KTP m(Vc - Vv)
 *   dot( xL ) = m (Vc sin γc - Vc sin γ)
 *   --> Lc = KLI xL + KLP (Vc sin γc - Vc sin γ)
 *   --> ϕc = KϕP (Vc/g) (ψc - ψ)  
 * 
 *   Input laws:
 *   
 *   dot( T ) = -PT T + PT Tc
 *   dot( L ) = -PL L + PL Lc
 *   dot( ϕ ) = -Pϕ ϕ + Pϕ ϕc
 *   
 *   PT, PL, Pϕ = time constants selected to approximate the responses
 *                of the engine and the airframe attitude
 *                 
 *   Limits:
 *   0 <= T <= Tmax
 *   L <= KLmax Vv^2
 *   -ϕmax <= ϕ <= ϕmax
 * 
 * =========================================
 * EXAMPLE
 * =========================================
 * 
 * Aircraft: C-130
 * Vv(0) = 400 mph = 347 kts
 * γ(0) = 0 (constant altitude)
 * ψ(0) = 0 (heading North)
 * 
 * PT = 2 rad/s
 * PL = 2.5 rad/s
 * Pϕ = 1 rad/s
 * 
 * Tmax = 72000 lb
 * KLmax = 2.6 lb/fps^2
 * ϕmax = 30 deg
 * 
 * Kwf = 4e-6 sl/(lb*s)
 * α0 = -0.05 deg
 * g = 9.81 m/s^2
 * 
 * KD0 = 3.8e-2 sl/ft
 * KD1 = 2.48e-2 ft^2/(lb s^2)
 * KL = 5.4 deg ft/sl
 * 
 * KTP = 0.08/s
 * KTI = 0.002/s^2
 * KLP = 0.5/s
 * KLI = 0.01/s^2
 * KϕP = 0.075/s
 * 
 * The vehicle must transition from level flight in northerly direction at 400 mph
 * to a heading of 15 deg at 450 mph while climbing at approximately 3500 ft/min.
 * A steady wind of about 30 mph from southwest is included.
 * 
 * Vc = 450 mph = 391 kts
 * γc = 5 deg --> dot(h)c = 3455 ft/min
 * ψc = 15 deg
 * Wx = 25 mph
 * Wy = 25 mph
 * Wh = 0 mph
 * 
 */
@SuppressWarnings("unchecked")
public class AircraftPointMassPropagator {

	private Aircraft theAircraft;

	private List<MissionEvent> missionEvents = new ArrayList<MissionEvent>();	

	private Amount<?> pT    = Amount.valueOf(2.0, MyUnits.RADIAN_PER_SECOND);
	private Amount<?> pL    = Amount.valueOf(2.5, MyUnits.RADIAN_PER_SECOND);
	private Amount<?> pPhi  = Amount.valueOf(1.0, MyUnits.RADIAN_PER_SECOND);
	private Amount<?> kTp   = Amount.valueOf(0.08, MyUnits.ONE_PER_SECOND);
	private Amount<?> kTi   = Amount.valueOf(0.002, MyUnits.ONE_PER_SECOND_SQUARED);
	private Amount<?> kLp   = Amount.valueOf(0.5, MyUnits.ONE_PER_SECOND);
	private Amount<?> kLi   = Amount.valueOf(0.01, MyUnits.ONE_PER_SECOND_SQUARED);
	private Amount<?> kPhip = Amount.valueOf(0.075, MyUnits.ONE_PER_SECOND);
	private Amount<?> kWDot = Amount.valueOf(4.0e-6, MyUnits.SLUG_PER_SECOND_PER_POUND);
	
	private Amount<Angle> bankAngleMax = Amount.valueOf(30.0, NonSI.DEGREE_ANGLE);
	private double thrustMax;
	private double signDeltaThrustAtMax = 1.0;
	private double thrustMin = 0.0;
	private double signDeltaThrustAtMin = 1.0;
	
	private boolean resetThrustDerivative = false;
	private double thrustDerivativeResetValue;
	
	private Amount<Velocity> commandedSpeed;
	private Amount<Angle> commandedFlightpathAngle;
	private Amount<Angle> commandedHeadingAngle;
	
	private Amount<Velocity> windVelocityXI = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
	private Amount<Velocity> windVelocityYI = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
	private Amount<Velocity> windVelocityZI = Amount.valueOf(0.0, SI.METERS_PER_SECOND);
	
	// Initial state variables
	private double
		speedInertial0, flightpathAngle0, headingAngle0,
		xInertial0, yInertial0, altitude0, 
		xThrust0, thrust0, xLift0, lift0, 
		bankAngle0, mass0;
	
	private double timeFinal = 10.0;
	
	// Simulation outputs
	private List<Amount<Duration>> time;
	private List<Amount<Mass>> mass;
	private List<Amount<Force>> thrust, lift, drag, totalForce;
	private List<Amount<Force>> commandedThrust, commandedLift;
	private List<Amount<?>> xT, xL;
	private List<Amount<?>> xTDot, xLDot;
	private List<Amount<Velocity>> speedInertial, airspeed, rateOfClimb;
	private List<Amount<Acceleration>> acceleration;
	private List<Amount<Length>> altitude, groundDistanceX, groundDistanceY;
	private List<Amount<Angle>> angleOfAttack, flightpathAngle, headingAngle, bankAngle;
	private List<Double> cL, cD, loadFactor;	
	private AircraftPointMassSimulationResultMap simulationResults = new AircraftPointMassSimulationResultMap();
	
	private Boolean chartsEnabled = false;
	private String outputChartDir;

	public AircraftPointMassPropagator(Aircraft ac) {
		this.theAircraft = ac;
		
		this.time = new ArrayList<Amount<Duration>>();
		this.mass = new ArrayList<Amount<Mass>>();
		this.thrust = new ArrayList<Amount<Force>>();
		this.commandedThrust = new ArrayList<Amount<Force>>();
		this.lift = new ArrayList<Amount<Force>>();
		this.commandedLift = new ArrayList<Amount<Force>>();
		this.xT = new ArrayList<Amount<?>>();
		this.xL = new ArrayList<Amount<?>>();
		this.xTDot = new ArrayList<Amount<?>>();
		this.xLDot = new ArrayList<Amount<?>>();
		this.drag = new ArrayList<Amount<Force>>();
		this.totalForce = new ArrayList<Amount<Force>>();
		this.speedInertial = new ArrayList<Amount<Velocity>>();
		this.airspeed = new ArrayList<Amount<Velocity>>();
		this.rateOfClimb = new ArrayList<Amount<Velocity>>();
		this.acceleration = new ArrayList<Amount<Acceleration>>();
		this.altitude = new ArrayList<Amount<Length>>();
		this.groundDistanceX = new ArrayList<Amount<Length>>();
		this.groundDistanceY = new ArrayList<Amount<Length>>();
		this.angleOfAttack = new ArrayList<Amount<Angle>>();
		this.flightpathAngle = new ArrayList<Amount<Angle>>();
		this.headingAngle = new ArrayList<Amount<Angle>>();
		this.bankAngle = new ArrayList<Amount<Angle>>();
		this.cL = new ArrayList<Double>();
		this.cD = new ArrayList<Double>();
		this.loadFactor = new ArrayList<Double>();
		
		simulationResults.initialize();
	}
	
	public void readMissionScript(String pathToXML) {
		
		// clear old events, if present
		missionEvents.clear();
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		String missionId = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//mission_simulation/@id");

		if (missionId != null)
			missionId = "Mission-Untitled-";
		System.out.println("Mission id: " + missionId);

		//--------------------------------------------------------------------------------------
		System.out.println("Reading mission CONSTANTS (TODO) ...");

		// TODO
		
		//--------------------------------------------------------------------------------------
		System.out.println("Reading mission initial conditions ...");

		this.speedInertial0   =
				((Amount<Velocity>)
					MyXMLReaderUtils.getXMLAmountWithUnitByPath(reader.getXmlDoc(), 
							"//mission_simulation/initial_conditions/speed")
				).doubleValue(SI.METERS_PER_SECOND);
		System.out.println("Initial vehicle speed (m/s) = " + this.speedInertial0);
		this.flightpathAngle0 = 
				((Amount<Angle>)
						MyXMLReaderUtils.getXMLAmountWithUnitByPath(reader.getXmlDoc(), 
								"//mission_simulation/initial_conditions/flightpath_angle")
					).doubleValue(SI.RADIAN);
		System.out.println("Initial flight-path angle (rad) = " + this.flightpathAngle0);
		this.headingAngle0    =
				((Amount<Angle>)
						MyXMLReaderUtils.getXMLAmountWithUnitByPath(reader.getXmlDoc(), 
								"//mission_simulation/initial_conditions/heading_angle")
					).doubleValue(SI.RADIAN);				
		System.out.println("Initial heading angle (rad) = " + this.flightpathAngle0);
		this.bankAngle0    =
				((Amount<Angle>)
						MyXMLReaderUtils.getXMLAmountWithUnitByPath(reader.getXmlDoc(), 
								"//mission_simulation/initial_conditions/bank_angle")
					).doubleValue(SI.RADIAN);				
		System.out.println("Initial bank angle (rad) = " + this.flightpathAngle0);
		this.xInertial0       =
				((Amount<Length>)
						MyXMLReaderUtils.getXMLAmountWithUnitByPath(reader.getXmlDoc(), 
								"//mission_simulation/initial_conditions/position_XE")
					).doubleValue(SI.METER);				
		this.yInertial0       =
				((Amount<Length>)
						MyXMLReaderUtils.getXMLAmountWithUnitByPath(reader.getXmlDoc(), 
								"//mission_simulation/initial_conditions/position_YE")
					).doubleValue(SI.METER);				
		this.altitude0        =
				((Amount<Length>)
						MyXMLReaderUtils.getXMLAmountWithUnitByPath(reader.getXmlDoc(), 
								"//mission_simulation/initial_conditions/altitude")
					).doubleValue(SI.METER);				
		System.out.println("Initial position (xE, yE, h) = (" 
					+ this.xInertial0 + "m , " + this.yInertial0 + " m, " + this.altitude0 +" m)");

		// TODO
		// xThrust0, thrust0, xLift0, lift0, 
		// this.mass0            =
		

		//--------------------------------------------------------------------------------------
		System.out.println("Reading mission events ...");
		
		NodeList nodelistEvents = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//mission_simulation/events/event");
		
		System.out.println("No. events found: " + nodelistEvents.getLength());
		
		for (int i = 0; i < nodelistEvents.getLength(); i++) {
			Node nodeEvent  = nodelistEvents.item(i); // .getNodeValue();
			Element elementEvent = (Element) nodeEvent;
			String eventID = elementEvent.getAttribute("id");
			System.out.println("[" + i + "]\nEvent id: " + eventID);
			// get data from the current <event/> node
			MissionEvent event = AircraftPointMassPropagator.importFromEventNode(nodeEvent);
			if (event != null) {
				System.out.println(event);
				missionEvents.add(event);
			}
		}
	}
	
	public static MissionEvent importFromEventNode(Node nodeEvent) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(nodeEvent, true);
			doc.appendChild(importedNode);
			return AircraftPointMassPropagator.importFromEventNodeImpl(doc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	private static MissionEvent importFromEventNodeImpl(Document doc) {
		System.out.println("Reading mission event data from XML doc node ...");
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						doc, xpath,
						"//event/@id");
		Amount<Duration> time = MyXMLReaderUtils.getXMLAmountTimeByPath(doc, xpath, "//time");
		Amount<Velocity> commandedSpeed = MyXMLReaderUtils.getXMLAmountVelocityByPath(doc, xpath, "//commanded_speed");
		Amount<Angle> commandedFlightpathAngle = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//commanded_flightpath_angle");
		Amount<Angle> commandedHeadingAngle = MyXMLReaderUtils.getXMLAmountAngleByPath(doc, xpath, "//commanded_heading_angle");
		Amount<Velocity> windSpeedXE = MyXMLReaderUtils.getXMLAmountVelocityByPath(doc, xpath, "//wind_speed_XE");
		Amount<Velocity> windSpeedYE = MyXMLReaderUtils.getXMLAmountVelocityByPath(doc, xpath, "//wind_speed_YE");
		Amount<Velocity> windSpeedZE = MyXMLReaderUtils.getXMLAmountVelocityByPath(doc, xpath, "//wind_speed_ZE");

		// create the MissionEvent object
		MissionEvent event = new MissionEvent
				.Builder()
				.setDescription(id)
				.setTime(time.doubleValue(SI.SECOND))
				.setCommandedSpeed(commandedSpeed.doubleValue(SI.METERS_PER_SECOND))
				.setCommandedFlightpathAngle(commandedFlightpathAngle.doubleValue(SI.RADIAN))
				.setCommandedHeadingAngle(commandedHeadingAngle.doubleValue(SI.RADIAN))
				.setWindSpeedXE(windSpeedXE.doubleValue(SI.METERS_PER_SECOND))
				.setWindSpeedYE(windSpeedYE.doubleValue(SI.METERS_PER_SECOND))
				.setWindSpeedZE(windSpeedZE.doubleValue(SI.METERS_PER_SECOND))
				.build();
		return event;
	}
	
	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsAircraftPointMass implements FirstOrderDifferentialEquations {

		double g0 = AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND);

		// MyInterpolatingFunction thrustMax; // TODO
		
//		// state variables
//		private double time,
//			speedInertial, flightpathAngle, heading, xInertial, yInertial, altitude, 
//			xThrust, thrust, xLift, lift, bankAngle, mass;
		
		// auxiliary variables
		private double 
			windSpeedXE, windSpeedYE, windSpeedZE,
			airspeed, 
			drag, angleOfAttack, airDensity;
		
		public DynamicsEquationsAircraftPointMass() {
			
		}
		
		@Override
		public int getDimension() {
			return 12;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {
			
			/*
			 * x0  = Vv
			 * x1  = γ
			 * x2  = ψ
			 * x3  = Xi
			 * x4  = Yi
			 * x5  = h
			 * x6  = xT
			 * x7  = T
			 * x8  = xL
			 * x9  = L
			 * x10 = ϕ
			 * x11 = m
			 * 
			 * Equations of motion:
			 * 
			 *    1) dot( Vv ) = (T - D)/m - g sinγ
			 *    2) dot( γ  ) = (L cosϕ - m g cosγ)/(m Vv)
			 *    3) dot( ψ  ) = L sinϕ /(m Vv cosγ)
			 *    4) dot( Xi ) = Vv cosγ cosψ
			 *    5) dot( Yi ) = Vv cosγ sinψ
			 *    6) dot( h  ) = Vv sinγ
			 *    7) dot( xT ) = m (Vc - Vv)
			 *    8) dot( T  ) = -pT T + pT (KTi xT + KTp m (Vc - Vv) )
			 *    9) dot( xL ) = m Vc ( sinγc - sinγ)
			 *   10) dot( L  ) = -pL L + pL (KLi xL + KLp m Vc ( sinγc - sinγ))
			 *   11) dot( ϕ  ) = -pϕ ϕ + pϕ Kϕp Vc (ψc - ψ)/g 
			 *   12) dot( m  ) = - dot_wf/g = - Kwf T 
			 */
			double time            = t;
			double speedInertial   = x[0];
			double flightpathAngle = x[1];
			double heading         = x[2];
			double xInertial       = x[3];
			double yInertial       = x[4];
			double altitude        = x[5];
			double xThrust         = x[6];
			double thrust          = x[7];
			double xLift           = x[8];
			double lift            = x[9];
			double bankAngle       = x[10];
			double mass            = x[11];
			
			// TODO: steady wind velocity components in the inertial frame
			double windXI = windVelocityXI.doubleValue(SI.METERS_PER_SECOND); // TODO: getActualWindXI
			double windYI = windVelocityYI.doubleValue(SI.METERS_PER_SECOND); // TODO: getActualWindYI
			double windZI = windVelocityZI.doubleValue(SI.METERS_PER_SECOND); // TODO: getActualWindZI (positive downwards)
			
			// intermediate variables
			double xDotI = speedInertial*Math.cos(flightpathAngle)*Math.cos(heading);
			double yDotI = speedInertial*Math.cos(flightpathAngle)*Math.sin(heading);
			double hDot  = speedInertial*Math.sin(flightpathAngle);
			double airspeed = Math.sqrt(
					Math.pow(xDotI - windXI, 2)	
					+ Math.pow(yDotI - windYI, 2)
					+ Math.pow(hDot + windZI, 2)
					);

			// drag
			double cD0 = 0.03;
			double aspectRatio = theAircraft.getWing().getAspectRatio();
			double oswaldFactor = 0.85;
			double kD = Math.PI * aspectRatio * oswaldFactor;
			double airDensity = AtmosphereCalc.getDensity(altitude);
			double surfaceWing = theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE);
			double kD0 = 0.5 * airDensity * surfaceWing * cD0;
			double kD1 = 2.0/(airDensity * surfaceWing * kD);
			this.drag = kD0 * Math.pow(airspeed, 2) 
					+ kD1 * Math.pow(lift, 2)/Math.pow(airspeed, 2);
			
			// alpha
			// TODO

			//=======================================================================
			// ASSIGN THE DERIVATIVES
			// DERIVATIVES-RESET LOGIC GO HERE
			
			/* STATE VARIABLES
			 *
			 * x0  = Vv,  x1  = γ,  x2  = ψ
			 * x3  = Xi,  x4  = Yi, x5  = h
			 * x6  = xT,  x7  = T,  x8  = xL
			 * x9  = L,   x10 = ϕ,  x11 = m
			 * 
			 */

			// Vv, speed
			xDot[ 0] = ((thrust - drag)/mass) - g0*Math.sin(flightpathAngle);
			// gamma, flight-path angle
			xDot[ 1] = (lift*Math.cos(bankAngle) - mass*g0*Math.cos(flightpathAngle))/(mass * speedInertial);
			// psi, heading
			xDot[ 2] = (lift*Math.sin(bankAngle))/(mass*speedInertial*Math.cos(flightpathAngle));
			// XI, ground distance X-
			xDot[ 3] = xDotI;
			// YI, ground distance Y-
			xDot[ 4] = yDotI;
			// h, altitude
			xDot[ 5] = hDot;
			// xT, int(m(Vc - Vv))
			xDot[ 6] = mass*(commandedSpeed.doubleValue(SI.METERS_PER_SECOND) - speedInertial);
			
			// T, thrust
			if (resetThrustDerivative) {
				xDot[7] = thrustDerivativeResetValue;
				System.out.println("-------> t       = " + t);
				System.out.println("-------> x[7]    = " + x[7]);
				System.out.println("-------> xDot[7] = " + xDot[7]);
				resetThrustDerivative = false;
			} else {
				double commandedThrust = kTi.doubleValue(MyUnits.ONE_PER_SECOND_SQUARED)*x[6]
						+ kTp.doubleValue(MyUnits.ONE_PER_SECOND)*xDot[6];
				
				xDot[ 7] = - pT.doubleValue(MyUnits.RADIAN_PER_SECOND)*thrust
						+ pT.doubleValue(MyUnits.RADIAN_PER_SECOND)*commandedThrust;
			}

			// xL, int(m(hdotc - hdot))
			xDot[ 8] = mass*commandedSpeed.doubleValue(SI.METERS_PER_SECOND)*(
						Math.sin(commandedFlightpathAngle.doubleValue(SI.RADIAN)) - Math.sin(flightpathAngle));
			// L, lift
			double commandedLift = kLi.doubleValue(MyUnits.ONE_PER_SECOND_SQUARED)*x[8]
					+ kLp.doubleValue(MyUnits.ONE_PER_SECOND)*xDot[8];
			xDot[ 9] = -pL.doubleValue(MyUnits.RADIAN_PER_SECOND)*lift
					+ pL.doubleValue(MyUnits.RADIAN_PER_SECOND)*commandedLift;

			// phi, bank angle
			xDot[10] = pPhi.doubleValue(MyUnits.RADIAN_PER_SECOND)*( 
					kPhip.doubleValue(MyUnits.ONE_PER_SECOND)
						*commandedSpeed.doubleValue(SI.METERS_PER_SECOND)*(
							commandedHeadingAngle.doubleValue(SI.RADIAN) - x[2])/g0
					- bankAngle);
			// m, mass
			xDot[11] = -AircraftPointMassPropagator.this.kWDot.doubleValue(MyUnits.KILOGRAM_PER_SECOND_PER_NEWTON) * x[7];
			
		}
		
		public double getWindSpeedXE() {
			return windSpeedXE;
		}
		public void setWindSpeedXE(double windSpeedXE) {
			this.windSpeedXE = windSpeedXE;
		}

		public double getWindSpeedYE() {
			return windSpeedYE;
		}
		public void setWindSpeedYE(double windSpeedYE) {
			this.windSpeedYE = windSpeedYE;
		}

		public double getWindSpeedZE() {
			return windSpeedZE;
		}
		public void setWindSpeedZE(double windSpeedZE) {
			this.windSpeedZE = windSpeedZE;
		}

	}	

	/***************************************************************************************
	 * This method performs the integration of the total following systema of equations:
	 * 
	 *   dot( Vv ) = (T - D)/m - g sinγ
	 *   dot( γ  ) = (L cosϕ - m g cosγ)/(m Vv)
	 *   dot( ψ  ) = L sinϕ /(m Vv cosγ)
	 *   dot( Xi ) = Vv cosγ cosψ
	 *   dot( Yi ) = Vv cosγ sinψ
	 *   dot( h  ) = Vv sinγ
	 *   dot( m  ) = - dot_wf/g = - Kwf T
	 *   
	 * In presence of wind:
	 * 
	 *   vec W = Wx vec Ii + Wy vec Ji - Wh vec Ki
	 *   Ii, Ji, Ki = versors of inertial reference frame (flat-Earth)
	 *   vec Va = - vec Vv + vec W
	 * 
	 *   dot( Xw ) = Vv cosγ cosψ - Wx
	 *   dot( Yw ) = Vv cosγ sinψ - Wy
	 *   dot( hw ) = Vv sinγ      - Wh
	 *   
	 *   Va = sqrt( dot( Xw )^2 + dot( Yw )^2 + dot( hw )^2 )	 * 
	 * 
	 * ODE with a HighamHall54Integrator. The library used is the Apache Math3. 
	 * 
	 * see: https://commons.apache.org/proper/commons-math/userguide/ode.html
	 * 
	 * @author Agostino De Marco
	 */
	public void propagate() {

		if (missionEvents.isEmpty()) {
			System.out.println("---------------------------------------------------");
			System.out.println("AircraftPointMassPropagator :: empty list of mission events, time propagation aborted!\n\n");
			return;
		}
		
		System.out.println("---------------------------------------------------");
		System.out.println("AircraftPointMassPropagator :: ODE integration\n\n");
		
		FirstOrderIntegrator theIntegrator = new HighamHall54Integrator(
				1e-6,
				1,
				1e-15,
				1e-15
				);
		
		FirstOrderDifferentialEquations ode = new DynamicsEquationsAircraftPointMass();
		
		//=============================================================================
		// EVENT HANDLERS AS READ FROM FILE, AS COMMANDED MISSION EVENTS 
		missionEvents.stream()
			.forEach(me -> {
				EventHandler eventHandler = new EventHandler() {

					@Override
					public void init(double t0, double[] y0, double t) {
					}

					@Override
					public double g(double t, double[] y) {
						return t - me.getTime();
					}

					@Override
					public Action eventOccurred(double t, double[] y, boolean increasing) {
						commandedSpeed = Amount.valueOf(me.getCommandedSpeed(), SI.METERS_PER_SECOND);
						commandedFlightpathAngle = Amount.valueOf(me.getCommandedFlightpathAngle(), SI.RADIAN);
						commandedHeadingAngle = Amount.valueOf(me.getCommandedHeadingAngle(), SI.RADIAN);
						
						System.out.println("EVENT OCCURRED_____________________________ " + me.getDescription());
						
						return  Action.CONTINUE;
					}

					@Override
					public void resetState(double t, double[] y) {
					}
					
				};
				theIntegrator.addEventHandler(eventHandler, 1.0, 1e-3, 20);
			});
		
		//=============================================================================
		// HANDLERS AS LIMITERS
		
		EventHandler handlerMaxThrustLimiter = new EventHandler() {

			/* STATE VARIABLES
			 *
			 * x0  = Vv,  x1  = γ,  x2  = ψ
			 * x3  = Xi,  x4  = Yi, x5  = h
			 * x6  = xT,  x7  = T,  x8  = xL
			 * x9  = L,   x10 = ϕ,  x11 = m
			 * 
			 */

			@Override
			public void init(double t0, double[] x0, double t) {
			}

			@Override
			public double g(double t, double[] x) {
				
				// max available thrust
				AircraftPointMassPropagator.this.thrustMax = AircraftPointMassPropagator.this.getThrustMax(
						AircraftPointMassPropagator.this.windVelocityXI.doubleValue(SI.METERS_PER_SECOND), // TODO: getActualWindXI
						AircraftPointMassPropagator.this.windVelocityYI.doubleValue(SI.METERS_PER_SECOND), // TODO: getActualWindYI
						AircraftPointMassPropagator.this.windVelocityZI.doubleValue(SI.METERS_PER_SECOND), // TODO: getActualWindZI
						x[0], // V 
						x[1], // gamma
						x[2], // psi
						x[5], // altitude
						AircraftPointMassPropagator.this.theAircraft);

				// switching condition 
//				if ( 1.0 //AircraftPointMassPropagator.this.signDeltaThrustAtMax
//						* (x[7] - AircraftPointMassPropagator.this.thrustMax) < 0) {
//					System.out.println("sign = " + signDeltaThrustAtMax);					
//					System.out.println("x[7] = " + x[7]);					
//					System.out.println("Tmax = " + AircraftPointMassPropagator.this.thrustMax);					
//				}
				return 
						1.0 //AircraftPointMassPropagator.this.signDeltaThrustAtMax
						* (x[7] - AircraftPointMassPropagator.this.thrustMax);
			}

			@Override
			public Action eventOccurred(double t, double[] x, boolean increasing) {
//				AircraftPointMassPropagator.this.signDeltaThrustAtMax = -AircraftPointMassPropagator.this.signDeltaThrustAtMax;
				return  Action.RESET_STATE;
			}

			@Override
			public void resetState(double t, double[] x) {
				System.out.println("handlerMaxThrustLimiter - STATE RESET - t="+t);
				// max available thrust
				AircraftPointMassPropagator.this.thrustMax = AircraftPointMassPropagator.this.getThrustMax(
						AircraftPointMassPropagator.this.windVelocityXI.doubleValue(SI.METERS_PER_SECOND), // TODO: getActualWindXI
						AircraftPointMassPropagator.this.windVelocityYI.doubleValue(SI.METERS_PER_SECOND), // TODO: getActualWindYI
						AircraftPointMassPropagator.this.windVelocityZI.doubleValue(SI.METERS_PER_SECOND), // TODO: getActualWindZI
						x[0], // V 
						x[1], // gamma
						x[2], // psi
						x[5], // altitude
						AircraftPointMassPropagator.this.theAircraft);
				System.out.println("T0 = " + AircraftPointMassPropagator.this.theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON));
				System.out.println("engineNumber = " + AircraftPointMassPropagator.this.theAircraft.getPowerPlant().getEngineNumber());
				System.out.println("BPR = " + AircraftPointMassPropagator.this.theAircraft.getPowerPlant().getEngineList().get(0).getBPR());
				System.out.println("engineType = " + AircraftPointMassPropagator.this.theAircraft.getPowerPlant().getEngineType());
				System.out.println("altitude = " + x[5]);
				System.out.println("Tmax = " + AircraftPointMassPropagator.this.thrustMax);

				x[7] = AircraftPointMassPropagator.this.thrustMax;
			}
		};
		theIntegrator.addEventHandler(handlerMaxThrustLimiter, 1.0, 1e-3, 20);		

		EventHandler handlerMinThrustLimiter = new EventHandler() {

			/* STATE VARIABLES
			 *
			 * x0  = Vv,  x1  = γ,  x2  = ψ
			 * x3  = Xi,  x4  = Yi, x5  = h
			 * x6  = xT,  x7  = T,  x8  = xL
			 * x9  = L,   x10 = ϕ,  x11 = m
			 * 
			 */
			
			double sign = 1.0;

			@Override
			public void init(double t0, double[] x0, double t) {
			}

			@Override
			public double g(double t, double[] x) {
				// minimum available thrust
				AircraftPointMassPropagator.this.thrustMin = AircraftPointMassPropagator.this.getThrustMin();
				//System.out.println(x[7] + " ___ " + AircraftPointMassPropagator.this.thrustMin);
				return
						x[7] - AircraftPointMassPropagator.this.thrustMin;
			}

			@Override
			public Action eventOccurred(double t, double[] x, boolean increasing) {
				sign = -sign;
				System.out.println("sign = " + sign);
				AircraftPointMassPropagator.this.resetThrustDerivative = true;
				AircraftPointMassPropagator.this.thrustDerivativeResetValue = 0.0;
				System.out.println(x[7] + " ___ " + AircraftPointMassPropagator.this.thrustMin);
				return  Action.RESET_STATE;
			}

			@Override
			public void resetState(double t, double[] x) {
				System.out.println("handlerMinThrustLimiter - STATE RESET - t="+t);
				// max available thrust
				AircraftPointMassPropagator.this.thrustMin = AircraftPointMassPropagator.this.getThrustMin();
				System.out.println("T0 = " + AircraftPointMassPropagator.this.theAircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON));
				System.out.println("engineNumber = " + AircraftPointMassPropagator.this.theAircraft.getPowerPlant().getEngineNumber());
				System.out.println("BPR = " + AircraftPointMassPropagator.this.theAircraft.getPowerPlant().getEngineList().get(0).getBPR());
				System.out.println("engineType = " + AircraftPointMassPropagator.this.theAircraft.getPowerPlant().getEngineType());
				System.out.println("altitude = " + x[5]);
				System.out.println("Tmin = " + AircraftPointMassPropagator.this.thrustMin);
				x[7] = AircraftPointMassPropagator.this.thrustMin;
			}
		};
		theIntegrator.addEventHandler(handlerMinThrustLimiter, 1.0, 1e-6, 20);		
		
		// TODO: add a STOP event at the end

		// handle detailed info
		StepHandler stepHandler = new StepHandler() {

			@Override
			public void init(double t0, double[] y0, double t) {
			}

			@Override
			public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {
				
				/* STATE VARIABLES
				 *
				 * x0  = Vv,  x1  = γ,  x2  = ψ
				 * x3  = Xi,  x4  = Yi, x5  = h
				 * x6  = xT,  x7  = T,  x8  = xL
				 * x9  = L,   x10 = ϕ,  x11 = m
				 * 
				 */
				double   t = interpolator.getCurrentTime();
				double[] x = interpolator.getInterpolatedState();
				double[] xdot = interpolator.getInterpolatedDerivatives();

//				System.out.println("-------------------------"+
//						"\n\tt = " + t + " s" +
//						"\n\tx[0] = V = " + x[0] + " m/s" +
//						"\n\tx[1] = gamma = " + x[1] + " rad" + 
//						"\n\tx[2] = psi = " + x[2] + " rad" +
//						"\n\tx[3] = XI = " + x[3] + " m" +
//						"\n\tx[4] = YI = " + x[4] + " m" +
//						"\n\tx[5] = h  = " + x[5] + " m"
//						);
				
				/* PICKING UP ALL DATA AT EVERY STEP
				 *
				 * x0  = Vv,  x1  = γ,  x2  = ψ
				 * x3  = Xi,  x4  = Yi, x5  = h
				 * x6  = xT,  x7  = T,  x8  = xL
				 * x9  = L,   x10 = ϕ,  x11 = m
				 * 
				 */
				//----------------------------------------------------------------------------------------
				// TIME:
				AircraftPointMassPropagator.this.getTime().add(Amount.valueOf(t, SI.SECOND));
				//----------------------------------------------------------------------------------------
				// GROUND DISTANCE -XI:
				AircraftPointMassPropagator.this.getGroundDistanceX().add(Amount.valueOf(x[3], SI.METER));
				// GROUND DISTANCE -YI:
				AircraftPointMassPropagator.this.getGroundDistanceY().add(Amount.valueOf(x[4], SI.METER));
				//----------------------------------------------------------------------------------------
				// ALTITUDE:
				AircraftPointMassPropagator.this.getAltitude().add(Amount.valueOf(x[5], SI.METER));
				//----------------------------------------------------------------------------------------
				// FLIGHTPATH ANGLE:
				AircraftPointMassPropagator.this.getFlightpathAngle().add(Amount.valueOf(x[1], SI.RADIAN));
				//----------------------------------------------------------------------------------------
				// RATE OF CLIMB:
				AircraftPointMassPropagator.this.getRateOfClimb().add(Amount.valueOf(xdot[5], SI.METERS_PER_SECOND));
				//----------------------------------------------------------------------------------------
				// HEADING ANGLE:
				AircraftPointMassPropagator.this.getHeadingAngle().add(Amount.valueOf(x[2], SI.RADIAN));
				//----------------------------------------------------------------------------------------
				// BANK ANGLE:
				AircraftPointMassPropagator.this.getBankAngle().add(Amount.valueOf(x[10], SI.RADIAN));
				//----------------------------------------------------------------------------------------
				// SPEED:
				AircraftPointMassPropagator.this.getSpeedInertial().add(Amount.valueOf(x[0], SI.METERS_PER_SECOND));
				//----------------------------------------------------------------------------------------
				// AIRSPEED:
				double windXI = AircraftPointMassPropagator.this.windVelocityXI.doubleValue(SI.METERS_PER_SECOND);
				double windYI = AircraftPointMassPropagator.this.windVelocityYI.doubleValue(SI.METERS_PER_SECOND);
				double windZI = AircraftPointMassPropagator.this.windVelocityZI.doubleValue(SI.METERS_PER_SECOND);				
				double xDotI = xdot[3];
				double yDotI = xdot[4];
				double hDot  = xdot[5];
				double airspeed = Math.sqrt(Math.pow(xDotI - windXI, 2)	
						+ Math.pow(yDotI - windYI, 2) 
						+ Math.pow(hDot  + windZI, 2));				
				AircraftPointMassPropagator.this.getAirspeed().add(Amount.valueOf(airspeed, SI.METERS_PER_SECOND));
				//----------------------------------------------------------------------------------------
				// MASS:
				AircraftPointMassPropagator.this.getMass().add(Amount.valueOf(x[11], SI.KILOGRAM));
				//----------------------------------------------------------------------------------------
				// THRUST:
				AircraftPointMassPropagator.this.getThrust().add(Amount.valueOf(x[7],SI.NEWTON));
				//----------------------------------------------------------------------------------------
				// x-THRUST:
				AircraftPointMassPropagator.this.getXThrust().add(Amount.valueOf(x[6],MyUnits.KILOGRAM_METER));
				// x-THRUST-DOT:
				AircraftPointMassPropagator.this.getXThrustDot().add(Amount.valueOf(xdot[6],MyUnits.KILOGRAM_METER_PER_SECOND));
				//----------------------------------------------------------------------------------------
				// LIFT:
				AircraftPointMassPropagator.this.getLift().add(Amount.valueOf(x[9],SI.NEWTON));
				//----------------------------------------------------------------------------------------
				// x-LIFT:
				AircraftPointMassPropagator.this.getXLift().add(Amount.valueOf(x[8],MyUnits.KILOGRAM_METER));
				// x-LIFT-DOT:
				AircraftPointMassPropagator.this.getXLiftDot().add(Amount.valueOf(xdot[8],MyUnits.KILOGRAM_METER_PER_SECOND));
				//----------------------------------------------------------------------------------------
				// DRAG:
				double cD0 = 0.03;
				double aspectRatio = theAircraft.getWing().getAspectRatio();
				double oswaldFactor = 0.85; // TODO
				double kD = Math.PI * aspectRatio * oswaldFactor;
				double airDensity = AtmosphereCalc.getDensity(x[5]); // f(altitude)
				double surfaceWing = theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE);
				double kD0 = 0.5 * airDensity * surfaceWing * cD0;
				double kD1 = 2.0/(airDensity * surfaceWing * kD);
				double d = kD0 * Math.pow(airspeed, 2) 
						+ kD1 * Math.pow(x[9], 2)/Math.pow(airspeed, 2);
				AircraftPointMassPropagator.this.getDrag().add(Amount.valueOf(d, SI.NEWTON));
				
				//----------------------------------------------------------------------------------------
				// COMMANDED THRUST & LIFT
				double commandedThrust = 
						kTi.doubleValue(MyUnits.ONE_PER_SECOND_SQUARED)*x[6]
						+ kTp.doubleValue(MyUnits.ONE_PER_SECOND)*xdot[6];
				AircraftPointMassPropagator.this.getCommandedThrust().add(Amount.valueOf(commandedThrust,SI.NEWTON));
				double commandedLift = 
						kLi.doubleValue(MyUnits.ONE_PER_SECOND_SQUARED)*x[8]
						+ kLp.doubleValue(MyUnits.ONE_PER_SECOND)*xdot[8];
				AircraftPointMassPropagator.this.getCommandedLift().add(Amount.valueOf(commandedLift,SI.NEWTON));

			}
			
		};

		theIntegrator.addStepHandler(stepHandler);

		// Initial values
//		commandedSpeed = Amount.valueOf(this.speedInertial0,SI.METERS_PER_SECOND);
		
		commandedSpeed = Amount.valueOf(this.getMissionEvents().get(0).getCommandedSpeed(),SI.METERS_PER_SECOND);
		
		commandedFlightpathAngle = Amount.valueOf(this.flightpathAngle0,SI.RADIAN);
		commandedHeadingAngle = Amount.valueOf(headingAngle0,SI.RADIAN);
		
		// initial state vector
		double[] xAt0 = new double[] { // initial state
				this.speedInertial0,
				this.flightpathAngle0,
				this.headingAngle0,
				this.xInertial0, 
				this.yInertial0,
				this.altitude0, 
				this.xThrust0,
				this.thrust0,
				this.xLift0,
				this.lift0, 
				this.bankAngle0,
				this.mass0
				};
		
		double tInitial = 0.0;
		double tFinal = this.timeFinal;
		
		theIntegrator.integrate(ode, tInitial, xAt0, tFinal, xAt0); // now xAt0 contains final state

		theIntegrator.clearEventHandlers();
		theIntegrator.clearStepHandlers();		
		
		System.out.println("\n---------------------------END!!-------------------------------");
	}

	/**************************************************************************************
	 * This method allows users to plot all take-off performance producing several output charts
	 * which have time or ground distance as independent variables.
	 *
	 * @author Agostino De Marco
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void createOutputCharts() throws InstantiationException, IllegalAccessException {

		if (chartsEnabled && (outputChartDir != null)) {
			System.out.println("\n---------WRITING MISSION SIM CHARTS TO FILE-----------");

			// speed vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getSpeedInertial().stream()
							.map(vV -> vV.doubleValue(SI.METERS_PER_SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, 0.0, null,
					"Time", "Speed", "s", "m/s",
					outputChartDir, "Speed_Inertial");

			// speed, airspeed vs. time
			double[][] xMatrix2 = new double[2][getTime().size()];
			for(int i=0; i<xMatrix2.length; i++)
				xMatrix2[i] = Arrays.stream(
						getTime().stream()
						.map(t -> t.doubleValue(SI.SECOND))
						.toArray(size -> new Double[size])
						).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];

			double[][] yMatrix2 = new double[2][getTime().size()];
			yMatrix2[0] = Arrays.stream(
					getSpeedInertial().stream()
					.map(vV -> vV.doubleValue(SI.METERS_PER_SECOND))
					.toArray(size -> new Double[size])
					).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];
			yMatrix2[1] = Arrays.stream(
					getAirspeed().stream()
					.map(vVa -> vVa.doubleValue(SI.METERS_PER_SECOND))
					.toArray(size -> new Double[size])
					).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];
			
			MyChartToFileUtils.plot(
							xMatrix2, yMatrix2,
							0.0, null, null, null,
							"Time", "Vv, Va", "s", "m/s",
							new String[] {"Velocity Inertial", "Airspeed"},
							outputChartDir, "Velocities");
			
			// altitude vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getAltitude().stream()
							.map(h -> h.doubleValue(SI.METER))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, 0.0, null,
					"Time", "Altitude", "s", "m",
					outputChartDir, "Altitude");

			// rate-of-climb vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getRateOfClimb().stream()
							.map(roc -> roc.doubleValue(SI.METERS_PER_SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, null, null,
					"Time", "Rate Of Climb", "s", "m/s",
					outputChartDir, "Rate_of_Climb");
			
			// flightpath angle vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getFlightpathAngle().stream()
							.map(gamma -> gamma.doubleValue(NonSI.DEGREE_ANGLE))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, null, null,
					"Time", "Flightpath Angle", "s", "deg",
					outputChartDir, "Flightpath_Angle");
			
			// heading angle vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getHeadingAngle().stream()
							.map(psi -> psi.doubleValue(NonSI.DEGREE_ANGLE))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, -180.0, 180.0,
					"Time", "Heading Angle", "s", "deg",
					outputChartDir, "Heading_Angle");

			// bank angle vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getBankAngle().stream()
							.map(phi -> phi.doubleValue(NonSI.DEGREE_ANGLE))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, -180.0, 180.0,
					"Time", "Bank Angle", "s", "deg",
					outputChartDir, "Bank_Angle");

			// int( m(Vc - V) ) angle vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getXThrust().stream()
							.map(xt -> xt.doubleValue(MyUnits.KILOGRAM_METER))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, null, null,
					"Time", "xT = int(m(Vc - V))", "s", "kg*m",
					outputChartDir, "xT");

			// m(Vc - V) angle vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getXThrustDot().stream()
							.map(xtd -> xtd.doubleValue(MyUnits.KILOGRAM_METER_PER_SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, null, null,
					"Time", "xTDot = m(Vc - V)", "s", "kg*m/s",
					outputChartDir, "xTDot");
			
			// thrust vs. time
//			MyChartToFileUtils.plotNoLegend(
//					Arrays.stream(
//							getTime().stream()
//							.map(t -> t.doubleValue(SI.SECOND))
//							.toArray(size -> new Double[size])
//							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
//					Arrays.stream(
//							getThrust().stream()
//							.map(thrust -> thrust.doubleValue(SI.NEWTON))
//							.toArray(size -> new Double[size])
//							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
//					0.0, null, null, null,
//					"Time", "Thrust", "s", "N",
//					outputChartDir, "Thrust");
			
			// int( m (hdotc - hdot)) angle vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getXLift().stream()
							.map(xl -> xl.doubleValue(MyUnits.KILOGRAM_METER))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, null, null,
					"Time", "xL = int(m(hdotc - hdot))", "s", "kg*m",
					outputChartDir, "xL");

			// m(hdotc - hdot) angle vs. time
			MyChartToFileUtils.plotNoLegend(
					Arrays.stream(
							getTime().stream()
							.map(t -> t.doubleValue(SI.SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					Arrays.stream(
							getXLiftDot().stream()
							.map(xld -> xld.doubleValue(MyUnits.KILOGRAM_METER_PER_SECOND))
							.toArray(size -> new Double[size])
							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
					0.0, null, null, null,
					"Time", "xLDot = m(hdotc - hdot)", "s", "kg*m/s",
					outputChartDir, "xLDot");
			
			// drag vs. time
//			MyChartToFileUtils.plotNoLegend(
//					Arrays.stream(
//							getTime().stream()
//							.map(t -> t.doubleValue(SI.SECOND))
//							.toArray(size -> new Double[size])
//							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
//					Arrays.stream(
//							getDrag().stream()
//							.map(thrust -> thrust.doubleValue(SI.NEWTON))
//							.toArray(size -> new Double[size])
//							).mapToDouble(Double::doubleValue).toArray(), // list-of-Amount --> double[]
//					0.0, null, null, null,
//					"Time", "Drag", "s", "N",
//					outputChartDir, "Drag");
			
			// thrust, commandedThrust drag vs. time
			double[][] xMatrix3 = new double[3][getTime().size()];
			double[][] yMatrix3 = new double[3][getTime().size()];
			for(int i=0; i<xMatrix3.length; i++)
				xMatrix3[i] = Arrays.stream(
						getTime().stream()
						.map(t -> t.doubleValue(SI.SECOND))
						.toArray(size -> new Double[size])
						).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];

			yMatrix3[0] = Arrays.stream(
					getThrust().stream()
					.map(th -> th.doubleValue(SI.NEWTON))
					.toArray(size -> new Double[size])
					).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];
			yMatrix3[1] = Arrays.stream(
					getCommandedThrust().stream()
					.map(thc -> thc.doubleValue(SI.NEWTON))
					.toArray(size -> new Double[size])
					).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];
			yMatrix3[2] = Arrays.stream(
					getDrag().stream()
					.map(d -> d.doubleValue(SI.NEWTON))
					.toArray(size -> new Double[size])
					).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];
			
			MyChartToFileUtils.plot(
							xMatrix3, yMatrix3,
							0.0, null, null, null,
							"Time", "T, Tc, D", "s", "N",
							new String[] {"Thrust", "Commanded-Thrust", "Drag"},
							outputChartDir, "Thrust_Drag");

			// lift, commandedLift vs. time
			xMatrix2 = null; yMatrix2 = null;
			xMatrix2 = new double[2][getTime().size()];
			yMatrix2 = new double[2][getTime().size()];
			for(int i=0; i<xMatrix2.length; i++)
				xMatrix2[i] = Arrays.stream(
						getTime().stream()
						.map(t -> t.doubleValue(SI.SECOND))
						.toArray(size -> new Double[size])
						).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];

			yMatrix2[0] = Arrays.stream(
					getLift().stream()
					.map(l -> l.doubleValue(SI.NEWTON))
					.toArray(size -> new Double[size])
					).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];
			yMatrix2[1] = Arrays.stream(
					getCommandedLift().stream()
					.map(lc -> lc.doubleValue(SI.NEWTON))
					.toArray(size -> new Double[size])
					).mapToDouble(Double::doubleValue).toArray(); // list-of-Amount --> double[];
			
			MyChartToFileUtils.plot(
							xMatrix2, yMatrix2,
							0.0, null, null, null,
							"Time", "L, Lc", "s", "N",
							new String[] {"Lift", "Commanded-Lift"},
							outputChartDir, "Lift");
			
		}
	}
	
	public void setInitialConditions(
			double v0, double gamma0, double psi0,
			double x0, double y0, double h0,
			double xT0, double thr0, double xL0, double lft0,
			double phi0, double m0
			) {
		this.speedInertial0   = v0;
		this.flightpathAngle0 = gamma0;
		this.headingAngle0    = psi0;
		this.xInertial0       = x0; 
		this.yInertial0       = y0;
		this.altitude0        = h0; 
		this.xThrust0         = xT0;
		this.thrust0          = thr0;
		this.xLift0           = xL0;
		this.lift0            = lft0; 
		this.bankAngle0       = phi0;
		this.mass0            = m0;
	}

	public List<MissionEvent> getMissionEvents() {
		return missionEvents;
	}

	public void setMissionEvents(List<MissionEvent> missionEvents) {
		this.missionEvents = missionEvents;
	}

	public double getTimeFinal() {
		return timeFinal;
	}

	public void setTimeFinal(double timeFinal) {
		this.timeFinal = timeFinal;
	}

	public List<Amount<Duration>> getTime() {
		return time;
	}

	public void setTime(List<Amount<Duration>> time) {
		this.time = time;
	}

	public List<Amount<Mass>> getMass() {
		return mass;
	}

	public void setMass(List<Amount<Mass>> mass) {
		this.mass = mass;
	}

	public List<Amount<Force>> getThrust() {
		return thrust;
	}

	public void setThrust(List<Amount<Force>> thrust) {
		this.thrust = thrust;
	}

	public List<Amount<Force>> getCommandedThrust() {
		return this.commandedThrust;
	}

	public void setCommandedThrust(List<Amount<Force>> cthrust) {
		this.commandedThrust = cthrust;
	}
	
	public List<Amount<?>> getXThrust() {
		return xT;
	}

	public void setXThrust(List<Amount<?>> x) {
		this.xT = x;
	}

	public List<Amount<?>> getXThrustDot() {
		return xTDot;
	}

	public void setXThrustDot(List<Amount<?>> xd) {
		this.xTDot = xd;
	}
	
	public List<Amount<Force>> getLift() {
		return lift;
	}

	public void setLift(List<Amount<Force>> lift) {
		this.lift = lift;
	}

	public List<Amount<Force>> getCommandedLift() {
		return this.commandedLift;
	}

	public void setCommandedLift(List<Amount<Force>> clift) {
		this.commandedLift = clift;
	}
	
	public List<Amount<?>> getXLift() {
		return xL;
	}

	public void setXLift(List<Amount<?>> x) {
		this.xL = x;
	}

	public List<Amount<?>> getXLiftDot() {
		return xLDot;
	}

	public void setXLiftDot(List<Amount<?>> xd) {
		this.xLDot = xd;
	}
	
	public List<Amount<Force>> getDrag() {
		return drag;
	}

	public void setDrag(List<Amount<Force>> drag) {
		this.drag = drag;
	}

	public List<Amount<Force>> getTotalForce() {
		return totalForce;
	}

	public void setTotalForce(List<Amount<Force>> totalForce) {
		this.totalForce = totalForce;
	}

	public List<Amount<Velocity>> getSpeedInertial() {
		return speedInertial;
	}

	public void setSpeedInertial(List<Amount<Velocity>> speedInertial) {
		this.speedInertial = speedInertial;
	}

	public List<Amount<Velocity>> getAirspeed() {
		return airspeed;
	}

	public void setAirspeed(List<Amount<Velocity>> airspeed) {
		this.airspeed = airspeed;
	}

	public List<Amount<Velocity>> getRateOfClimb() {
		return rateOfClimb;
	}

	public void setRateOfClimb(List<Amount<Velocity>> rateOfClimb) {
		this.rateOfClimb = rateOfClimb;
	}

	public List<Amount<Acceleration>> getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(List<Amount<Acceleration>> acceleration) {
		this.acceleration = acceleration;
	}

	public List<Amount<Length>> getAltitude() {
		return altitude;
	}

	public void setAltitude(List<Amount<Length>> altitude) {
		this.altitude = altitude;
	}

	public List<Amount<Length>> getGroundDistanceX() {
		return groundDistanceX;
	}

	public void setGroundDistanceX(List<Amount<Length>> groundDistanceX) {
		this.groundDistanceX = groundDistanceX;
	}

	public List<Amount<Length>> getGroundDistanceY() {
		return groundDistanceY;
	}

	public void setGroundDistanceY(List<Amount<Length>> groundDistanceY) {
		this.groundDistanceY = groundDistanceY;
	}

	public List<Amount<Angle>> getAngleOfAttack() {
		return angleOfAttack;
	}

	public void setAngleOfAttack(List<Amount<Angle>> angleOfAttack) {
		this.angleOfAttack = angleOfAttack;
	}

	public List<Amount<Angle>> getFlightpathAngle() {
		return flightpathAngle;
	}

	public void setFlightpathAngle(List<Amount<Angle>> flightpathAngle) {
		this.flightpathAngle = flightpathAngle;
	}

	public List<Amount<Angle>> getHeadingAngle() {
		return headingAngle;
	}

	public void setHeadingAngle(List<Amount<Angle>> headingAngle) {
		this.headingAngle = headingAngle;
	}

	public List<Amount<Angle>> getBankAngle() {
		return bankAngle;
	}

	public void setBankAngle(List<Amount<Angle>> bankAngle) {
		this.bankAngle = bankAngle;
	}

	public List<Double> getcL() {
		return cL;
	}

	public void setcL(List<Double> cL) {
		this.cL = cL;
	}

	public List<Double> getcD() {
		return cD;
	}

	public void setcD(List<Double> cD) {
		this.cD = cD;
	}

	public List<Double> getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(List<Double> loadFactor) {
		this.loadFactor = loadFactor;
	}

	public Boolean chartsEnabled() {
		return chartsEnabled;
	}

	public void enableCharts(Boolean chartsEnabled) {
		this.chartsEnabled = chartsEnabled;
	}

	public void enableCharts(Boolean chartsEnabled, String outputChartDir) {
		this.chartsEnabled = chartsEnabled;
		this.outputChartDir = outputChartDir;
	}
	
	public String getOutputChartDir() {
		return outputChartDir;
	}

	public void setOutputChartDir(String outputChartDir) {
		this.outputChartDir = outputChartDir;
	}

	public double getThrustMax(
			double windXI, double windYI, double windZI,
			double vV, double gamma, double psi, double altitude,
			Aircraft aircraft
			) {
		// intermediate variables
		double xDotI = vV*Math.cos(gamma)*Math.cos(psi);
		double yDotI = vV*Math.cos(gamma)*Math.sin(psi);
		double hDot  = vV*Math.sin(gamma);
		double airspeed = Math.sqrt(
				Math.pow(xDotI - windXI, 2)	
				+ Math.pow(yDotI - windYI, 2)
				+ Math.pow(hDot + windZI, 2)
				);
		// max available thrust
		return ThrustCalc.calculateThrustDatabase(
				aircraft.getPowerPlant().getEngineList().get(0).getT0().doubleValue(SI.NEWTON), 
				aircraft.getPowerPlant().getEngineNumber(), 
				1.0, // throttle setting at max 
				aircraft.getPowerPlant().getEngineList().get(0).getBPR(), 
				aircraft.getPowerPlant().getEngineType(), 
				EngineOperatingConditionEnum.CRUISE, // TODO: set a condition linked to the mission event  type 
				aircraft.getPowerPlant(), 
				altitude, // altitude in meters 
				SpeedCalc.calculateMach(altitude, airspeed) 
				);
	}

	public double getThrustMin() {
		// minimum thrust TODO: see if idle thrust makes sense
		return 0.0;
	}
	
}
