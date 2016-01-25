package sandbox.vt.TakeOff_Landing_Test;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcHighLiftDevices;
import calculators.performance.ThrustCalc;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

/**
 * This class have the purpose of calculating the required take-off/landing field length
 * of a given aircraft by evaluating two main phases: 
 *
 * Take-Off:
 * - the ground roll distance (rotation included)
 * - the airborne distance (until the reach of a given obstacle altitude)
 * 
 * Landing:
 * - the airborne distance
 * - the flare distance
 * - the ground roll distance
 * 
 * for each of them a step by step integration is used in solving the dynamic equation;
 * furthermore the class allows to calculate the balanced field length by evaluating every 
 * failure possibility and comparing the required field length in OEI condition with the 
 * aborted take-off distance until they are equal.    
 * 
 * @author Vittorio Trifari
 *
 */

public class CalcTakeOff_Landing {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	
	private Aircraft aircraft;
	private OperatingConditions theConditions;
	private CalcHighLiftDevices highLiftCalculator;
	private Amount<Velocity> v_s_TO, v_R, v_LO, v_wind;
	private Amount<Length> wing_to_ground_distance;
	private Amount<Angle> alpha_ground;
	private List<Double> time, alpha, alpha_dot, cL, cD;
	private List<Amount<Velocity>> speed, mean_speed;
	private List<Amount<Acceleration>> acceleration, mean_acceleration;
	private List<Amount<Force>> thrust, thrust_horizontal, thrust_vertical, lift,
							    drag, friction, total_force;
	private List<Amount<Length>> delta_GroundDistance, ground_distance;
	private double dt, k_alpha_dot, mu, mu_brake, cLmaxTO, k_ground;
	private final double k_Rot = 1.1,
						 k_LO = 1.2,
						 k1 = 0.078,
						 k2 = 0.365,
						 phi = 1.0;
		
	//-------------------------------------------------------------------------------------
	// BUILDER:
	
	/**************************************************************************************
	 * This builder has the purpose of pass input to the class fields and to initialize all
	 * lists with the correct initial values in order to setup the take-off length calculation 
	 * 
	 * @author Vittorio Trifari
	 * @param aircraft
	 * @param theConditions
	 * @param highLiftCalculator instance of LSAerodynamicManager inner class for managing 
	 * 		  and slat effects 
	 * @param dt temporal step
	 * @param k_alpha_dot decrease factor of alpha_dot
	 * @param mu friction coefficient at take-off
	 * @param mu_brake friction coefficient in landing or aborting
	 * @param wing_to_ground_distance
	 * @param v_wind negative is adverse
	 */
	public CalcTakeOff_Landing(
			Aircraft aircraft,
			OperatingConditions theConditions,
			CalcHighLiftDevices highLiftCalculator,
			double dt,
			double k_alpha_dot,
			double mu,
			double mu_brake,
			Amount<Length> wing_to_ground_distance,
			Amount<Velocity> v_wind,
			Amount<Angle> alpha_ground
			) {

		// Required data
		this.aircraft = aircraft;
		this.theConditions = theConditions;
		this.highLiftCalculator = highLiftCalculator;
		this.dt = dt;
		this.k_alpha_dot = k_alpha_dot;
		this.mu = mu;
		this.mu_brake = mu_brake;
		this.wing_to_ground_distance = wing_to_ground_distance;
		this.v_wind = v_wind;
		this.alpha_ground = alpha_ground;
		
		// CalcHighLiftDevices object to manage flap/slat effects
		highLiftCalculator.calculateHighLiftDevicesEffects();
        cLmaxTO = highLiftCalculator.getcL_Max_Flap();
		
		// Reference velocities definition
		v_s_TO = Amount.valueOf(
				SpeedCalc.calculateSpeedStall(
						theConditions.get_altitude().getEstimatedValue(),
						aircraft.get_weights().get_MTOW().getEstimatedValue(),
						aircraft.get_wing().get_surface().getEstimatedValue(),
						cLmaxTO
						),
				SI.METERS_PER_SECOND);
		v_R = v_s_TO.times(k_Rot);
		v_LO = v_s_TO.times(k_LO);
		
		// Ground effect coefficient
		k_ground = (1 - (1.32*(wing_to_ground_distance.getEstimatedValue()/aircraft.get_wing().get_span().getEstimatedValue())))
				/(1.05 + (7.4*(wing_to_ground_distance.getEstimatedValue()/aircraft.get_wing().get_span().getEstimatedValue())));

		// List initialization with known values
		this.time = new ArrayList<Double>();
		time.add(0.0);
		this.speed = new ArrayList<Amount<Velocity>>();
		speed.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		
		this.thrust = new ArrayList<Amount<Force>>();
		this.thrust_horizontal = new ArrayList<Amount<Force>>();
		this.thrust_vertical = new ArrayList<Amount<Force>>();
		if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
			thrust.add(Amount.valueOf(
					ThrustCalc.calculateThrustDatabase(
							aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
							aircraft.get_powerPlant().get_engineNumber(),
							phi,
							aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
							aircraft.get_powerPlant().get_engineType(),
							EngineOperatingConditionEnum.TAKE_OFF,
							theConditions.get_altitude().getEstimatedValue(),
							SpeedCalc.calculateMach(
									theConditions.get_altitude().getEstimatedValue(),
									speed.get(0).getEstimatedValue())
							),
					SI.NEWTON)
					);
			thrust_horizontal.add(Amount.valueOf(
					ThrustCalc.calculateThrustDatabase(
							aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
							aircraft.get_powerPlant().get_engineNumber(),
							phi,
							aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
							aircraft.get_powerPlant().get_engineType(),
							EngineOperatingConditionEnum.TAKE_OFF,
							theConditions.get_altitude().getEstimatedValue(),
							SpeedCalc.calculateMach(
									theConditions.get_altitude().getEstimatedValue(),
									speed.get(0).getEstimatedValue())
							),
					SI.NEWTON)
					);
			thrust_vertical.add(Amount.valueOf(0.0, SI.NEWTON));
		}
		else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
			thrust.add(Amount.valueOf(
					ThrustCalc.calculateThrust(
							aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
							aircraft.get_powerPlant().get_engineNumber(),
							phi,
							theConditions.get_altitude().getEstimatedValue()),
					SI.NEWTON)
					);
			thrust_horizontal.add(Amount.valueOf(
					ThrustCalc.calculateThrust(
							aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
							aircraft.get_powerPlant().get_engineNumber(),
							phi,
							theConditions.get_altitude().getEstimatedValue()),
					SI.NEWTON)
					);
			thrust_vertical.add(Amount.valueOf(0.0, SI.NEWTON));
		}
		
		this.alpha = new ArrayList<Double>();
		alpha.add(0.0);
		this.alpha_dot = new ArrayList<Double>();
		alpha_dot.add(0.0);
		
		this.cL = new ArrayList<Double>();
		cL.add(highLiftCalculator.calcCLatAlphaHighLiftDevice(alpha_ground));
		this.lift = new ArrayList<Amount<Force>>();
		lift.add(Amount.valueOf(0.0, SI.NEWTON));
		
		
		this.cD = new ArrayList<Double>();
		cD.add(aircraft.get_theAerodynamics().get_cD0() 
			   + highLiftCalculator.getDeltaCD()
			   + aircraft.get_landingGear().get_deltaCD0() 
			   + ((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
			   - k_ground*((Math.pow(cL.get(0),2))/(Math.PI*aircraft.get_wing().get_aspectRatio())));
		this.drag = new ArrayList<Amount<Force>>();
		drag.add(Amount.valueOf(0.0, SI.NEWTON));
		
		this.friction = new ArrayList<Amount<Force>>();
		friction.add(aircraft.get_weights().get_MTOW().times(mu));
		this.total_force = new ArrayList<Amount<Force>>();
		total_force.add(thrust_horizontal.get(0).minus(friction.get(0)));
		this.acceleration = new ArrayList<Amount<Acceleration>>();
		acceleration.add(Amount.valueOf(
				AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(total_force.get(0)).getEstimatedValue(),
				SI.METERS_PER_SQUARE_SECOND));
		this.mean_acceleration = new ArrayList<Amount<Acceleration>>(); 
		mean_acceleration.add(acceleration.get(0));
		this.mean_speed = new ArrayList<Amount<Velocity>>();
		mean_speed.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this.delta_GroundDistance = new ArrayList<Amount<Length>>();
		delta_GroundDistance.add(Amount.valueOf(0.0, SI.METER));
		this.ground_distance = new ArrayList<Amount<Length>>();
		ground_distance.add(Amount.valueOf(0.0, SI.METER));
	}
	
	//-------------------------------------------------------------------------------------
	// METHODS:
	
	/**************************************************************************************
	 * This method performs a step by step integration of horizontal equation of motion in
	 * order to calculate the ground roll distance of a given airplane take-off phase. 
	 * Moreover it calculates all accessories quantity in terms of forces and velocities.
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateGroundDistance() {
		
		// Computation starts from the result of first step initialized in the builder
		
		// final instant of the first step
		int i = 1;
		
		// step by step integration
		while(speed.get(i-1).isLessThan(v_R)) {
			
			// increment at actual speed
			speed.add(Amount.valueOf(
					speed.get(i-1).getEstimatedValue() + (mean_acceleration.get(i-1).getEstimatedValue()*dt),
					SI.METERS_PER_SECOND));
			
			// update of all variables
			time.add(time.get(i-1) + dt);
			
			if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
				thrust.add(Amount.valueOf(
						ThrustCalc.calculateThrustDatabase(
								aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
								aircraft.get_powerPlant().get_engineNumber(),
								phi,
								aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
								aircraft.get_powerPlant().get_engineType(),
								EngineOperatingConditionEnum.TAKE_OFF,
								theConditions.get_altitude().getEstimatedValue(),
								SpeedCalc.calculateMach(
										theConditions.get_altitude().getEstimatedValue(),
										speed.get(i).getEstimatedValue())
								),
						SI.NEWTON)
						);
				thrust_horizontal.add(Amount.valueOf(
						ThrustCalc.calculateThrustDatabase(
								aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
								aircraft.get_powerPlant().get_engineNumber(),
								phi,
								aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
								aircraft.get_powerPlant().get_engineType(),
								EngineOperatingConditionEnum.TAKE_OFF,
								theConditions.get_altitude().getEstimatedValue(),
								SpeedCalc.calculateMach(
										theConditions.get_altitude().getEstimatedValue(),
										speed.get(i).getEstimatedValue())
								),
						SI.NEWTON)
						);
				thrust_vertical.add(Amount.valueOf(0.0, SI.NEWTON));
			}
			else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
				thrust.add(Amount.valueOf(
						ThrustCalc.calculateThrust(
								aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
								aircraft.get_powerPlant().get_engineNumber(),
								phi,
								theConditions.get_altitude().getEstimatedValue()),
						SI.NEWTON)
						);
				thrust_horizontal.add(Amount.valueOf(
						ThrustCalc.calculateThrust(
								aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
								aircraft.get_powerPlant().get_engineNumber(),
								phi,
								theConditions.get_altitude().getEstimatedValue()),
						SI.NEWTON)
						);
				thrust_vertical.add(Amount.valueOf(0.0, SI.NEWTON));
			}
			
			alpha.add(alpha.get(i-1));
			alpha_dot.add(alpha_dot.get(i-1));
			
			cL.add(cL.get(i-1));
			lift.add(Amount.valueOf(
					0.5
					*aircraft.get_wing().get_surface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.get_altitude().getEstimatedValue())
					*(Math.pow(speed.get(i).getEstimatedValue(), 2))
					*cL.get(i),
					SI.NEWTON)
					);
			
			cD.add(aircraft.get_theAerodynamics().get_cD0() 
					   + highLiftCalculator.getDeltaCD()
					   + aircraft.get_landingGear().get_deltaCD0() 
					   + ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
					   - k_ground*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio())));
			drag.add(Amount.valueOf(
					0.5
					*aircraft.get_wing().get_surface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.get_altitude().getEstimatedValue())
					*(Math.pow(speed.get(i).getEstimatedValue(), 2))
					*cD.get(i),
					SI.NEWTON)
					);
			
			friction.add((aircraft.get_weights().get_MTOW().minus(lift.get(i))).times(mu));
			total_force.add(thrust_horizontal.get(i).minus(friction.get(i)).minus(drag.get(i)));
			acceleration.add(Amount.valueOf(
					AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(total_force.get(i)).getEstimatedValue(),
					SI.METERS_PER_SQUARE_SECOND));
			mean_acceleration.add((acceleration.get(i-1).plus(acceleration.get(i))).divide(2));
			mean_speed.add(((speed.get(i-1).plus(speed.get(i))).divide(2)).plus(v_wind));
			delta_GroundDistance.add(
					Amount.valueOf(
							mean_speed.get(i).getEstimatedValue()*dt,
							SI.METER
							)
					);
			ground_distance.add(ground_distance.get(i-1).plus(delta_GroundDistance.get(i)));
			
			// step increment
			i += 1;
		}
	}
	
	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:
	
	public Aircraft getAircraft() {
		return aircraft;
	}

	public void setAircraft(Aircraft aircraft) {
		this.aircraft = aircraft;
	}

	public OperatingConditions getTheConditions() {
		return theConditions;
	}

	public void setTheConditions(OperatingConditions theConditions) {
		this.theConditions = theConditions;
	}

	public List<Double> getTime() {
		return time;
	}

	public void setTime(List<Double> time) {
		this.time = time;
	}

	public List<Double> getAlpha() {
		return alpha;
	}

	public void setAlpha(List<Double> alpha) {
		this.alpha = alpha;
	}

	public List<Double> getAlpha_dot() {
		return alpha_dot;
	}

	public void setAlpha_dot(List<Double> alpha_dot) {
		this.alpha_dot = alpha_dot;
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

	public List<Amount<Velocity>> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Amount<Velocity>> speed) {
		this.speed = speed;
	}

	public List<Amount<Velocity>> getMean_speed() {
		return mean_speed;
	}

	public void setMean_speed(List<Amount<Velocity>> mean_speed) {
		this.mean_speed = mean_speed;
	}

	public Amount<Velocity> get_v_s_TO() {
		return v_s_TO;
	}

	public void set_v_s_TO(Amount<Velocity> stall_speed_TO) {
		this.v_s_TO = stall_speed_TO;
	}

	public Amount<Velocity> get_v_R() {
		return v_R;
	}

	public void set_v_R(Amount<Velocity> v_R) {
		this.v_R = v_R;
	}

	public Amount<Velocity> get_v_LO() {
		return v_LO;
	}

	public void set_v_LO(Amount<Velocity> v_LO) {
		this.v_LO = v_LO;
	}

	public List<Amount<Acceleration>> getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(List<Amount<Acceleration>> acceleration) {
		this.acceleration = acceleration;
	}

	public List<Amount<Acceleration>> getMean_acceleration() {
		return mean_acceleration;
	}

	public void setMean_acceleration(List<Amount<Acceleration>> mean_acceleration) {
		this.mean_acceleration = mean_acceleration;
	}

	public List<Amount<Force>> getThrust() {
		return thrust;
	}

	public void setThrust(List<Amount<Force>> thrust) {
		this.thrust = thrust;
	}

	public List<Amount<Force>> getThrust_horizontal() {
		return thrust_horizontal;
	}

	public void setThrust_horizontal(List<Amount<Force>> thrust_horizontal) {
		this.thrust_horizontal = thrust_horizontal;
	}

	public List<Amount<Force>> getThrust_vertical() {
		return thrust_vertical;
	}

	public void setThrust_vertical(List<Amount<Force>> thrust_vertical) {
		this.thrust_vertical = thrust_vertical;
	}

	public List<Amount<Force>> getLift() {
		return lift;
	}

	public void setLift(List<Amount<Force>> lift) {
		this.lift = lift;
	}

	public List<Amount<Force>> getDrag() {
		return drag;
	}

	public void setDrag(List<Amount<Force>> drag) {
		this.drag = drag;
	}

	public List<Amount<Force>> getFriction() {
		return friction;
	}

	public void setFriction(List<Amount<Force>> friction) {
		this.friction = friction;
	}

	public List<Amount<Force>> getTotal_force() {
		return total_force;
	}

	public void setTotal_force(List<Amount<Force>> total_force) {
		this.total_force = total_force;
	}

	public List<Amount<Length>> getDelta_GroundDistance() {
		return delta_GroundDistance;
	}

	public void setDelta_GroundDistance(List<Amount<Length>> delta_GroundDistance) {
		this.delta_GroundDistance = delta_GroundDistance;
	}

	public List<Amount<Length>> getGround_distance() {
		return ground_distance;
	}

	public void setGround_distance(List<Amount<Length>> ground_distance) {
		this.ground_distance = ground_distance;
	}

	public double getDt() {
		return dt;
	}

	public void setDt(double dt) {
		this.dt = dt;
	}

	public double getK_alpha_dot() {
		return k_alpha_dot;
	}

	public void setK_alpha_dot(double k_alpha_dot) {
		this.k_alpha_dot = k_alpha_dot;
	}

	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public double getMu_brake() {
		return mu_brake;
	}

	public void setMu_brake(double mu_brake) {
		this.mu_brake = mu_brake;
	}

	public double getcLmaxTO() {
		return cLmaxTO;
	}

	public void setcLmaxTO(double cLmaxTO) {
		this.cLmaxTO = cLmaxTO;
	}

	public double getK_Rot() {
		return k_Rot;
	}

	public double getK_LO() {
		return k_LO;
	}

	public double getK1() {
		return k1;
	}

	public double getK2() {
		return k2;
	}

	public double getPhi() {
		return phi;
	}

	public Amount<Length> getWing_to_ground_distance() {
		return wing_to_ground_distance;
	}

	public void setWing_to_ground_distance(Amount<Length> wing_to_ground_distance) {
		this.wing_to_ground_distance = wing_to_ground_distance;
	}

	public double getK_ground() {
		return k_ground;
	}

	public void setK_ground(double k_ground) {
		this.k_ground = k_ground;
	}

	public Amount<Velocity> getV_wind() {
		return v_wind;
	}

	public void setV_wind(Amount<Velocity> v_wind) {
		this.v_wind = v_wind;
	}

	public Amount<Angle> getAlpha_ground() {
		return alpha_ground;
	}

	public void setAlpha_ground(Amount<Angle> alpha_ground) {
		this.alpha_ground = alpha_ground;
	}
}