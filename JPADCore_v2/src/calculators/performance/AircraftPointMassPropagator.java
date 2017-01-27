package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
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
import analyses.OperatingConditions;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyUnits;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

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
	private Amount<Angle> bankAngleMax = Amount.valueOf(30.0, NonSI.DEGREE_ANGLE);

	private Amount<Velocity> commandedSpeed;
	private Amount<Angle> commandedFlightpathAngle;
	private Amount<Angle> commandedHeadingAngle;
	
	// Initial state variables
	private double
		speedInertial0, flightpathAngle0, headingAngle0,
		xInertial0, yInertial0, altitude0, 
		xThrust0, thrust0, xLift0, lift0, 
		bankAngle0, mass0;
	
	private double timeFinal = 10.0;
	
	public AircraftPointMassPropagator(Aircraft ac) {
		this.theAircraft = ac;
	}
	
	public void readMissionEvents(String pathToXML) {
		
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

		System.out.println("Reading mission events ...");
		
		NodeList nodelistEvents = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//mission_simulation/event");
		
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

		double kAlpha, cD0, oswald, ar;
		MyInterpolatingFunction thrustMax;
		
		// state variables
		private double time,
			speedInertial, flightpathAngle, heading, xInertial, yInertial, altitude, 
			xThrust, thrust, xLift, lift, bankAngle, mass;
		
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
			this.time            = t;
			this.speedInertial   = x[0];
			this.flightpathAngle = x[1];
			this.heading         = x[2];
			this.xInertial       = x[3];
			this.yInertial       = x[4];
			this.altitude        = x[5];
			this.xThrust         = x[6];
			this.thrust          = x[7];
			this.xLift           = x[8];
			this.lift            = x[9];
			this.bankAngle       = x[10];
			this.mass            = x[11];
			
			// TODO: steady wind velocity components in the inertial frame
			double windXI = 0.0; // TODO: getActualWindXI
			double windYI = 0.0; // TODO: getActualWindYI
			double windZI = 0.0; // TODO: getActualWindZI (positive downwards)
			
			// intermediate variables
			double xDotI = speedInertial*Math.cos(flightpathAngle)*Math.cos(heading);
			double yDotI = speedInertial*Math.cos(flightpathAngle)*Math.sin(heading);
			double hDot  = speedInertial*Math.sin(flightpathAngle);
			double airspeed = Math.sqrt(
					Math.pow(xDotI - windXI, 2)	
					+ Math.pow(yDotI - windYI, 2)
					+ Math.pow(hDot - windZI, 2)
					);

			// TODO: calculate these quantities accordingly
//			this.thrust    = 1.0; // TODO: getActualThrust
//			this.lift      = 0.5; // TODO: getActualLift
//			this.drag      = 0.0; // TODO: getActualDrag
//			this.bankAngle = 0.0; // getActualBank
			
			// Assign the derivatives
			xDot[ 0] = ((thrust - drag)/mass) - g0*Math.sin(flightpathAngle);
			xDot[ 1] = (lift*Math.cos(bankAngle) - mass*g0*Math.cos(flightpathAngle))/(mass * speedInertial);
			xDot[ 2] = (lift*Math.sin(bankAngle))/(mass*speedInertial*Math.cos(flightpathAngle));
			xDot[ 3] = xDotI;
			xDot[ 4] = yDotI;
			xDot[ 5] = hDot;
			xDot[ 6] = mass*(commandedSpeed.doubleValue(SI.METERS_PER_SECOND) - speedInertial);
			xDot[ 7] = pT.doubleValue(MyUnits.RADIAN_PER_SECOND)*( 
						+ kTi.doubleValue(MyUnits.ONE_PER_SECOND_SQUARED)*x[6] 
						+ kTp.doubleValue(MyUnits.ONE_PER_SECOND)*xDot[6]
						- thrust);
			xDot[ 8] = mass*commandedSpeed.doubleValue(SI.METERS_PER_SECOND)*(
						Math.sin(commandedFlightpathAngle.doubleValue(SI.RADIAN)) - Math.sin(flightpathAngle));
			xDot[ 9] = pL.doubleValue(MyUnits.RADIAN_PER_SECOND)*( 
						+ kLi.doubleValue(MyUnits.ONE_PER_SECOND_SQUARED)*x[8] 
						+ kLp.doubleValue(MyUnits.ONE_PER_SECOND)*xDot[8]
						- lift);
			xDot[10] = pPhi.doubleValue(MyUnits.RADIAN_PER_SECOND)*( 
					kPhip.doubleValue(MyUnits.ONE_PER_SECOND)
						*commandedSpeed.doubleValue(SI.METERS_PER_SECOND)*(
							commandedHeadingAngle.doubleValue(SI.RADIAN) - x[2])/g0
					- bankAngle);
			xDot[11] = 0.0; // TODO: make a variable mass
			
		}

		public double getSpeedInertial() {
			return speedInertial;
		}

		public double getFlightpathAngle() {
			return flightpathAngle;
		}

		public double getHeading() {
			return heading;
		}

		public double getxInertial() {
			return xInertial;
		}

		public double getyInertial() {
			return yInertial;
		}

		public double getAltitude() {
			return altitude;
		}

		public double getxThrust() {
			return xThrust;
		}

		public double getThrust() {
			return thrust;
		}

		public double getxLift() {
			return xLift;
		}

		public double getLift() {
			return lift;
		}

		public double getBankAngle() {
			return bankAngle;
		}

		public double getMass() {
			return mass;
		}

		public double getWindSpeedXE() {
			return windSpeedXE;
		}

		public double getWindSpeedYE() {
			return windSpeedYE;
		}

		public double getWindSpeedZE() {
			return windSpeedZE;
		}

		public double getAirspeed() {
			return airspeed;
		}

		public double getDrag() {
			return drag;
		}

		public double getAngleOfAttack() {
			return angleOfAttack;
		}

		public double getAirDensity() {
			return airDensity;
		}

		public void setSpeedInertial(double speedInertial) {
			this.speedInertial = speedInertial;
		}

		public void setFlightpathAngle(double flightpathAngle) {
			this.flightpathAngle = flightpathAngle;
		}

		public void setHeading(double heading) {
			this.heading = heading;
		}

		public void setxInertial(double xInertial) {
			this.xInertial = xInertial;
		}

		public void setyInertial(double yInertial) {
			this.yInertial = yInertial;
		}

		public void setAltitude(double altitude) {
			this.altitude = altitude;
		}

		public void setxThrust(double xThrust) {
			this.xThrust = xThrust;
		}

		public void setThrust(double thrust) {
			this.thrust = thrust;
		}

		public void setxLift(double xLift) {
			this.xLift = xLift;
		}

		public void setLift(double lift) {
			this.lift = lift;
		}

		public void setBankAngle(double bankAngle) {
			this.bankAngle = bankAngle;
		}

		public void setMass(double mass) {
			this.mass = mass;
		}

		public void setWindSpeedXE(double windSpeedXE) {
			this.windSpeedXE = windSpeedXE;
		}

		public void setWindSpeedYE(double windSpeedYE) {
			this.windSpeedYE = windSpeedYE;
		}

		public void setWindSpeedZE(double windSpeedZE) {
			this.windSpeedZE = windSpeedZE;
		}

		public void setAirspeed(double airspeed) {
			this.airspeed = airspeed;
		}

		public void setDrag(double drag) {
			this.drag = drag;
		}

		public void setAngleOfAttack(double angleOfAttack) {
			this.angleOfAttack = angleOfAttack;
		}

		public void setAirDensity(double airDensity) {
			this.airDensity = airDensity;
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
		
//		List<EventHandler> eventHandlers = new ArrayList<EventHandler>();

		missionEvents.stream()
			.forEach(me -> {
				EventHandler eventHandler = new EventHandler() {

					@Override
					public void init(double t0, double[] y0, double t) {
						// TODO Auto-generated method stub
						
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
						
						return  Action.STOP;
					}

					@Override
					public void resetState(double t, double[] y) {
						// TODO Auto-generated method stub
						
					}
					
				};
				theIntegrator.addEventHandler(eventHandler, 1.0, 1e-3, 20);
			});
		
		// TODO: add a STOP event at the end

		// handle detailed info
		StepHandler stepHandler = new StepHandler() {

			@Override
			public void init(double t0, double[] y0, double t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {
				
				double   t = interpolator.getCurrentTime();
				double[] x = interpolator.getInterpolatedState();

				System.out.println("-------------------------"+
						"\n\tt = " + t + " s" +
						"\n\tx[0] = V = " + x[0] + " m/s" +
						"\n\tx[1] = gamma = " + x[1] + " rad" + 
						"\n\tx[2] = psi = " + x[2] + " rad" +
						"\n\tx[3] = XI = " + x[3] + " m" +
						"\n\tx[4] = YI = " + x[4] + " m" +
						"\n\tx[5] = h  = " + x[5] + " m"
						);
			}
			
		};

		theIntegrator.addStepHandler(stepHandler);

		// Initial values
		commandedSpeed = Amount.valueOf(this.speedInertial0,SI.METERS_PER_SECOND);
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

}
