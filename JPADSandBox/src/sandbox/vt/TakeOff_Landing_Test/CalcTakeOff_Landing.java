package sandbox.vt.TakeOff_Landing_Test;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Acceleration;
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
	private Amount<Velocity> v_s_TO, v_R, v_LO;
	private double phi;
	private List<Double> time, alpha, alpha_dot, cL, cD;
	private List<Amount<Velocity>> speed, mean_speed;
	private List<Amount<Acceleration>> acceleration, mean_acceleration;
	private List<Amount<Force>> thrust, thrust_horizontal, thrust_vertical, lift,
							    drag, friction, total_force;
	private List<Amount<Length>> delta_GroundDistance, ground_distance;
	private double dt, k_alpha_dot, mu, mu_brake, cLmaxTO;
	private final double k_Rot = 1.1,
						 k_LO = 1.2,
						 k1 = 0.078,
						 k2 = 0.365;
		
	//-------------------------------------------------------------------------------------
	// BUILDER:
	
	public CalcTakeOff_Landing(
			Aircraft aircraft,
			OperatingConditions theConditions,
			CalcHighLiftDevices highLiftCalculator,
			double dt,
			double k_alpha_dot,
			double mu,
			double mu_brake
			) {

		// Required data
		this.aircraft = aircraft;
		this.theConditions = theConditions;
		this.dt = dt;
		this.k_alpha_dot = k_alpha_dot;
		this.mu = mu;
		this.mu_brake = mu_brake;

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
		this.cL = new ArrayList<Double>();
		cL.add(highLiftCalculator.calcCLatAlphaHighLiftDevice(Amount.valueOf(alpha.get(0), NonSI.DEGREE_ANGLE)));
		this.ground_distance = new ArrayList<Amount<Length>>();
		ground_distance.add(Amount.valueOf(0.0, SI.METER));
		
		// List initialization
		
		// TODO: INITIALIZE WITH CORRECT VALUES
		
		this.acceleration = new ArrayList<Amount<Acceleration>>();
		this.mean_acceleration = new ArrayList<Amount<Acceleration>>(); 
		this.mean_speed = new ArrayList<Amount<Velocity>>();
		this.alpha_dot = new ArrayList<Double>();
		this.cD = new ArrayList<Double>();
		this.lift = new ArrayList<Amount<Force>>();
		this.drag = new ArrayList<Amount<Force>>();
		this.friction = new ArrayList<Amount<Force>>();
		this.total_force = new ArrayList<Amount<Force>>();
		this.delta_GroundDistance = new ArrayList<Amount<Length>>();
	}
	
	//-------------------------------------------------------------------------------------
	// METHODS:
	
	/**************************************************************************************
	 * 
	 * @author Vittorio Trifari
	 */
	public void calculateGroundRollDistance() {
		
		// Computation starts from the result of first step initialized in the builder
		int i = 1;
		while(speed.get(i).isLessThan(v_R)) {
			// TO BE FILLED
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

	public void setPhi(double phi) {
		this.phi = phi;
	}
}