package calculators.performance;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import aircraft.components.Aircraft;
import standaloneutils.MyInterpolatingFunction;
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

public class AircraftPointMassPropagator {

	private Aircraft aircraft;
	
	public AircraftPointMassPropagator(Aircraft ac) {
		this.aircraft = ac;
		// TODO
		
	}
	
	//-------------------------------------------------------------------------------------
	//										NESTED CLASS
	//-------------------------------------------------------------------------------------
	// ODE integration
	// see: https://commons.apache.org/proper/commons-math/userguide/ode.html

	public class DynamicsEquationsAircraftPointMass implements FirstOrderDifferentialEquations {

		double mass, g0, kAlpha, cD0, oswald, ar, kGround, vWind;
		MyInterpolatingFunction thrustMax;
		
		// visible variables
		public double speedInertial, flightPathAngle, heading, xInertial, yInertial, altitude;
		
		public DynamicsEquationsAircraftPointMass() {
			
		}
		
		@Override
		public int getDimension() {
			return 6;
		}

		@Override
		public void computeDerivatives(double t, double[] x, double[] xDot)
				throws MaxCountExceededException, DimensionMismatchException {
			
			speedInertial   = x[0];
			flightPathAngle = x[1];
			heading         = x[2];
			xInertial       = x[3];
			yInertial       = x[4];
			altitude        = x[5];
			// TODO: consider an augmented state vector
			// thrust, lift, bank, xDotT, xDotL, 

			// TODO: steady wind velocity components in the inertial frame
			double windXI = 0.0; // TODO: getActualWindXI
			double windYI = 0.0; // TODO: getActualWindYI
			double windZI = 0.0; // TODO: getActualWindZI (positive downwards)
			
			// intermediate variables
			double xDotI = speedInertial*Math.cos(flightPathAngle)*Math.cos(heading);
			double yDotI = speedInertial*Math.cos(flightPathAngle)*Math.sin(heading);
			double hDot  = speedInertial*Math.sin(flightPathAngle);
			double airspeed = Math.sqrt(
					Math.pow(xDotI - windXI, 2)	
					+ Math.pow(yDotI - windYI, 2)
					+ Math.pow(hDot - windZI, 2)
					);

			// TODO: calculate these quantities accordingly
			double mass = 1.0; // TODO: getActualMass
			double thrust = 1.0; // TODO: getActualThrust
			double lift = 0.5; // TODO: getActualLift
			double drag = 0.0; // TODO: getActualDrag
			double bank = 0.0; // getActualBank
			double g0 = AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND);
			
			// Assign the derivatives
			xDot[0] = ((thrust - drag)/mass) - g0*Math.sin(flightPathAngle);
			xDot[1] = (lift*Math.cos(bank) - mass*g0*Math.cos(flightPathAngle))/(mass * speedInertial);
			xDot[2] = (lift*Math.sin(bank))/(mass*speedInertial*Math.cos(flightPathAngle));
			xDot[3] = xDotI;
			xDot[4] = yDotI;
			xDot[5] = hDot;
			
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
	public void propagate(String eventsFileName) {

		System.out.println("---------------------------------------------------");
		System.out.println("AircraftPointMassPropagator :: ODE integration\n\n");
		
		FirstOrderIntegrator theIntegrator = new HighamHall54Integrator(
				1e-6,
				1,
				1e-15,
				1e-15
				);
		
		FirstOrderDifferentialEquations ode = new DynamicsEquationsAircraftPointMass();
		
		List<EventHandler> eventHandlers = new ArrayList<EventHandler>();
		// TODO: readEvents(eventsFileName);
		// TODO: eventHandlers.stream.map(e -> theIntegrator.addEventHandler(e, 1.0, 1e-3, 20))
		
		
		// TODO: modify accordingly
		EventHandler commandedEvents = new EventHandler() {

			@Override
			public void init(double t0, double[] y0, double t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public double g(double t, double[] y) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Action eventOccurred(double t, double[] y, boolean increasing) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void resetState(double t, double[] y) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		theIntegrator.addEventHandler(commandedEvents, 1.0, 1e-3, 20);

		// handle detailed info
		StepHandler stepHandler = new StepHandler() {

			@Override
			public void init(double t0, double[] y0, double t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void handleStep(StepInterpolator interpolator, boolean isLast) throws MaxCountExceededException {
				// TODO Auto-generated method stub
				
				System.out.println("step");
				
			}
			
		};

		theIntegrator.addStepHandler(stepHandler);

		double[] xAt0 = new double[] { // initial state
				0.0, // Vv 
				0.0, // gamma
				0.0, // psi
				0.0, // XI
				0.0, // YI
				0.0  // h
				};
		double tInitial = 0.0, tFinal = 1000.0;
		theIntegrator.integrate(ode, tInitial, xAt0, tFinal, xAt0); // now xAt0 contains final state

		theIntegrator.clearEventHandlers();
		theIntegrator.clearStepHandlers();		
		
		System.out.println("\n---------------------------END!!-------------------------------");
	}	
	
}
