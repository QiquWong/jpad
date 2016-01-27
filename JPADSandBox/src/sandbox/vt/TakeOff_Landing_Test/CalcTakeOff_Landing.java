package sandbox.vt.TakeOff_Landing_Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
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
import calculators.performance.customdata.TakeOffResultsMap;
import configuration.MyConfiguration;
import configuration.enumerations.EngineOperatingConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;
import writers.JPADStaticWriteUtils;

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
	private Amount<Duration> dt, dtRot, dtHold;
	private Amount<Velocity> v_s_TO, v_R, v_LO, v_wind;
	private Amount<Length> wing_to_ground_distance, obstacle;
	private Amount<Angle> alpha_ground, iw;
	private List<Double> alpha_dot, gamma_dot, cL, cD, loadFactor;
	private List<Amount<Angle>> alpha, theta, gamma;
	private List<Amount<Duration>> time;
	private List<Amount<Velocity>> speed, mean_speed, rateOfClimb, meanRateOfClimb;
	private List<Amount<Acceleration>> acceleration, mean_acceleration;
	private List<Amount<Force>> thrust, thrust_horizontal, thrust_vertical, lift,
							    drag, friction, total_force;
	private List<Amount<Length>> delta_GroundDistance, ground_distance, delta_VerticalDistance, vertical_distance;
	private double k_alpha_dot, mu, mu_brake, cLmaxTO, k_ground, alphaDotInitial, alphaRed;
	private final double k_Rot = 1.05,
						 k_LO = 1.1,
						 k_obs = 1.2,
						 k_clMax = 0.9,
						 k1 = 0.078,
						 k2 = 0.365,
						 phi = 1.0;
	
	// Statistics to be collected at every phase: (initialization of the lists through the builder
	private TakeOffResultsMap groundRollResults = new TakeOffResultsMap();
		
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
			Amount<Duration> dt,
			Amount<Duration> dtRot, 
			Amount<Duration> dtHold,
			double k_alpha_dot,
			double alphaRed,
			double mu,
			double mu_brake,
			Amount<Length> wing_to_ground_distance,
			Amount<Length> obstacle,
			Amount<Velocity> v_wind,
			Amount<Angle> alpha_ground,
			Amount<Angle> iw
			) {

		// Required data
		this.aircraft = aircraft;
		this.theConditions = theConditions;
		this.highLiftCalculator = highLiftCalculator;
		this.dt = dt;
		this.dtRot = dtRot;
		this.dtHold = dtHold;
		this.k_alpha_dot = k_alpha_dot;
		this.alphaRed = alphaRed;
		this.mu = mu;
		this.mu_brake = mu_brake;
		this.wing_to_ground_distance = wing_to_ground_distance;
		this.obstacle = obstacle;
		this.v_wind = v_wind;
		this.alpha_ground = alpha_ground;
		this.iw = iw;
		
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
		
		// Ground effect coefficient
		k_ground = (1 - (1.32*(wing_to_ground_distance.getEstimatedValue()/aircraft.get_wing().get_span().getEstimatedValue())))
				/(1.05 + (7.4*(wing_to_ground_distance.getEstimatedValue()/aircraft.get_wing().get_span().getEstimatedValue())));

		// List initialization with known values
		this.time = new ArrayList<Amount<Duration>>();
		time.add(Amount.valueOf(0.0, SI.SECOND));
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
									speed.get(0).getEstimatedValue() + v_wind.getEstimatedValue())
							),
					SI.NEWTON)
					);
			thrust_horizontal.add(thrust.get(0));
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
			thrust_horizontal.add(thrust.get(0));
			thrust_vertical.add(Amount.valueOf(0.0, SI.NEWTON));
		}
		
		this.alpha = new ArrayList<Amount<Angle>>();
		alpha.add(alpha_ground);
		this.alpha_dot = new ArrayList<Double>();
		alpha_dot.add(0.0);
		this.gamma = new ArrayList<Amount<Angle>>();
		gamma.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		this.gamma_dot = new ArrayList<Double>();
		gamma_dot.add(0.0);
		this.theta = new ArrayList<Amount<Angle>>();
		theta.add(alpha.get(0).plus(gamma.get(0)));
		
		this.cL = new ArrayList<Double>();
		cL.add(highLiftCalculator.calcCLatAlphaHighLiftDevice(alpha_ground));
		this.lift = new ArrayList<Amount<Force>>();
		lift.add(Amount.valueOf(0.0, SI.NEWTON));
		
		this.loadFactor = new ArrayList<Double>();
		loadFactor.add(0.0);
		
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
		this.rateOfClimb = new ArrayList<Amount<Velocity>>();
		rateOfClimb.add(speed.get(0).plus(v_wind).times(Math.sin(gamma.get(0).getEstimatedValue())));
		this.meanRateOfClimb = new ArrayList<Amount<Velocity>>();
		meanRateOfClimb.add(Amount.valueOf(0.0, SI.METERS_PER_SECOND));
		this.delta_GroundDistance = new ArrayList<Amount<Length>>();
		delta_GroundDistance.add(Amount.valueOf(0.0, SI.METER));
		this.ground_distance = new ArrayList<Amount<Length>>();
		ground_distance.add(Amount.valueOf(0.0, SI.METER));
		this.delta_VerticalDistance = new ArrayList<Amount<Length>>();
		delta_VerticalDistance.add(Amount.valueOf(0.0, SI.METER));
		this.vertical_distance = new ArrayList<Amount<Length>>();
		vertical_distance.add(Amount.valueOf(0.0, SI.METER));
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
	public void calculateTakeOffDistance() {
		
		// Computation starts from the result of first step initialized in the builder
		System.out.println("\n---------------------GROUND ROLL PHASE-------------------------");
		// final instant of the first step
		int i = 1;
		
		// step by step integration
		while(speed.get(i-1).isLessThan(v_R)) {
			
			// update of all variables
			time.add(time.get(i-1).plus(dt));
			// increment at actual speed
			speed.add(Amount.valueOf(
					speed.get(i-1).getEstimatedValue() + (mean_acceleration.get(i-1).getEstimatedValue()*dt.getEstimatedValue()),
					SI.METERS_PER_SECOND));
			
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
										speed.get(i).getEstimatedValue() + v_wind.getEstimatedValue())
								),
						SI.NEWTON)
						);
				thrust_horizontal.add(thrust.get(i));
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
				thrust_horizontal.add(thrust.get(i));
				thrust_vertical.add(Amount.valueOf(0.0, SI.NEWTON));
			}
			
			alpha.add(alpha.get(i-1));
			alpha_dot.add(alpha_dot.get(i-1));
			gamma_dot.add(gamma_dot.get(i-1));
			gamma.add(gamma.get(i-1));
			theta.add(alpha.get(i).plus(gamma.get(i)));
			
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
			
			loadFactor.add(lift.get(i).divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());
			
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
			rateOfClimb.add(speed.get(i).plus(v_wind).times(Math.sin(gamma.get(i).getEstimatedValue())));
			meanRateOfClimb.add((rateOfClimb.get(i).plus(rateOfClimb.get(i-1)).divide(2)));
			delta_GroundDistance.add(
					Amount.valueOf(
							mean_speed.get(i).getEstimatedValue()*dt.getEstimatedValue(),
							SI.METER
							)
					);
			ground_distance.add(ground_distance.get(i-1).plus(delta_GroundDistance.get(i)));
			delta_VerticalDistance.add(Amount.valueOf(meanRateOfClimb.get(i).times(dt).getEstimatedValue(), SI.METER));
			vertical_distance.add(vertical_distance.get(i-1).plus(delta_VerticalDistance.get(i)));
			
			// step increment
			i += 1;
		}
		System.out.println("\n-------------------END GROUND ROLL PHASE-----------------------");
		
		// Collecting data at the end of ground roll phase
		System.out.println("\n\tCOLLECTING DATA AT THE END OF GROUND ROLL PHASE ...");
		groundRollResults.collectResults(
				time.get(time.size()-1),
				thrust.get(thrust.size()-1),
				thrust_horizontal.get(thrust_horizontal.size()-1),
				thrust_vertical.get(thrust_vertical.size()-1),
				friction.get(friction.size()-1),
				lift.get(lift.size()-1),
				drag.get(drag.size()-1),
				total_force.get(total_force.size()-1),
				loadFactor.get(loadFactor.size()-1),
				speed.get(speed.size()-1),
				mean_speed.get(mean_speed.size()-1),
				rateOfClimb.get(rateOfClimb.size()-1),
				meanRateOfClimb.get(meanRateOfClimb.size()-1),
				acceleration.get(acceleration.size()-1),
				mean_acceleration.get(mean_acceleration.size()-1),
				delta_GroundDistance.get(delta_GroundDistance.size()-1),
				ground_distance.get(ground_distance.size()-1),
				delta_VerticalDistance.get(vertical_distance.size()-1),
				vertical_distance.get(vertical_distance.size()-1),
				alpha.get(alpha.size()-1),
				alpha_dot.get(alpha_dot.size()-1),
				gamma.get(gamma.size()-1),
				gamma_dot.get(gamma_dot.size()-1),
				theta.get(theta.size()-1),
				cL.get(cL.size()-1),
				cD.get(cD.size()-1)
				);
		System.out.println("\n---------------------------DONE!-------------------------------");
		
		double cL0 = highLiftCalculator.calcCLatAlphaHighLiftDevice(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		double cLatLiftOff = highLiftCalculator.getcL_Max_Flap()/(Math.pow(k_LO, 2));
		double alphaLiftOff = (cLatLiftOff - cL0)/highLiftCalculator.getcLalpha_new();
		alphaDotInitial = (((alphaLiftOff + iw.getEstimatedValue()) - alpha.get(0).getEstimatedValue())/(dtRot.getEstimatedValue()));
		
		System.out.println("\n\n-----------------------ROTATION PHASE--------------------------");
		
		while(loadFactor.get(i-1) < 1.0) {
			
			// temporal steps in rotation are smaller in order to track this short phase in a better way
			time.add(time.get(i-1).plus(dt.times(0.05)));
			speed.add(Amount.valueOf(
					speed.get(i-1).getEstimatedValue() + (mean_acceleration.get(i-1).getEstimatedValue()*dt.getEstimatedValue()*0.05),
					SI.METERS_PER_SECOND));

			alpha_dot.add(alphaDotInitial*(1-(k_alpha_dot*(alpha.get(i-1).getEstimatedValue() + iw.getEstimatedValue()))));
			alpha.add(Amount.valueOf(
					alpha.get(i-1).getEstimatedValue() + (alpha_dot.get(i)*dt.getEstimatedValue()*0.05),
					NonSI.DEGREE_ANGLE)
					);
			gamma.add(gamma.get(i-1));
			gamma_dot.add(gamma_dot.get(i-1));
			theta.add(alpha.get(i).plus(gamma.get(i)));
			
			cL.add(cL0 + ((highLiftCalculator.getcLalpha_new())*(alpha.get(i).getEstimatedValue() + iw.getEstimatedValue())));
			lift.add(Amount.valueOf(
					0.5
					*aircraft.get_wing().get_surface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.get_altitude().getEstimatedValue())
					*(Math.pow(speed.get(i).getEstimatedValue(), 2))
					*cL.get(i),
					SI.NEWTON)
					);
			
			if(cL.get(i) <= 1.2)
				cD.add(aircraft.get_theAerodynamics().get_cD0() 
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0() 
						+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
						- k_ground*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio())));
			else
				cD.add(aircraft.get_theAerodynamics().get_cD0() 
						+ highLiftCalculator.getDeltaCD()
						+ aircraft.get_landingGear().get_deltaCD0() 
						+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
						- k_ground*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()))
						+ k1*(cL.get(i)-1.2)
						+ k2*(Math.pow((cL.get(i)-1.2), 2))
						);
			
			drag.add(Amount.valueOf(
					0.5
					*aircraft.get_wing().get_surface().getEstimatedValue()
					*AtmosphereCalc.getDensity(
							theConditions.get_altitude().getEstimatedValue())
					*(Math.pow(speed.get(i).getEstimatedValue(), 2))
					*cD.get(i),
					SI.NEWTON)
					);
			
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
										speed.get(i).getEstimatedValue() + v_wind.getEstimatedValue())
								),
						SI.NEWTON)
						);
				thrust_horizontal.add(thrust.get(i).times(Math.cos(alpha.get(i).to(SI.RADIAN).getEstimatedValue())));
				thrust_vertical.add(thrust.get(i).times(Math.sin(alpha.get(i).to(SI.RADIAN).getEstimatedValue())));
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
				thrust_horizontal.add(thrust.get(i).times(Math.cos(alpha.get(i).to(SI.RADIAN).getEstimatedValue())));
				thrust_vertical.add(thrust.get(i).times(Math.sin(alpha.get(i).to(SI.RADIAN).getEstimatedValue())));
			}
			
			friction.add(
					(aircraft.get_weights().get_MTOW()
							.minus(lift.get(i).plus(thrust_vertical.get(i))))
							.times(mu));
			if (friction.get(i).isLessThan(Amount.valueOf(0.0, SI.NEWTON))) {
				friction.remove(i);
				friction.add(Amount.valueOf(0.0, SI.NEWTON));
			}
				
			total_force.add(thrust_horizontal.get(i)
					.minus(friction.get(i))
					.minus(drag.get(i)));
			
			loadFactor.add((lift.get(i)
					.plus(thrust_vertical.get(i)))
					.divide(aircraft.get_weights().get_MTOW()).getEstimatedValue());
			acceleration.add(Amount.valueOf(
					(AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW())).times(total_force.get(i)).getEstimatedValue(),
					SI.METERS_PER_SQUARE_SECOND));
			mean_acceleration.add((acceleration.get(i-1).plus(acceleration.get(i))).divide(2));
			mean_speed.add(((speed.get(i-1).plus(speed.get(i))).divide(2)).plus(v_wind));
			rateOfClimb.add(speed.get(i).plus(v_wind).times(Math.sin(gamma.get(i).getEstimatedValue())));
			meanRateOfClimb.add((rateOfClimb.get(i).plus(rateOfClimb.get(i-1)).divide(2)));
			delta_GroundDistance.add(
					Amount.valueOf(
							mean_speed.get(i).getEstimatedValue()*dt.getEstimatedValue()*0.05,
							SI.METER
							)
					);
			ground_distance.add(ground_distance.get(i-1).plus(delta_GroundDistance.get(i)));
			delta_VerticalDistance.add(Amount.valueOf(meanRateOfClimb.get(i).times(dt).getEstimatedValue(), SI.METER));
			vertical_distance.add(vertical_distance.get(i-1).plus(delta_VerticalDistance.get(i)));
		
			// step increment
			i += 1;
		}
		System.out.println("\n--------------------END ROTATION PHASE-------------------------");
		
		// Collecting data at the end of ground roll phase
		System.out.println("\n\tCOLLECTING DATA AT THE END OF ROTATION PHASE ...");
		groundRollResults.collectResults(
				time.get(time.size()-1),
				thrust.get(thrust.size()-1),
				thrust_horizontal.get(thrust_horizontal.size()-1),
				thrust_vertical.get(thrust_vertical.size()-1),
				friction.get(friction.size()-1),
				lift.get(lift.size()-1),
				drag.get(drag.size()-1),
				total_force.get(total_force.size()-1),
				loadFactor.get(loadFactor.size()-1),
				speed.get(speed.size()-1),
				mean_speed.get(mean_speed.size()-1),
				rateOfClimb.get(rateOfClimb.size()-1),
				meanRateOfClimb.get(meanRateOfClimb.size()-1),
				acceleration.get(acceleration.size()-1),
				mean_acceleration.get(mean_acceleration.size()-1),
				delta_GroundDistance.get(delta_GroundDistance.size()-1),
				ground_distance.get(ground_distance.size()-1),
				delta_VerticalDistance.get(vertical_distance.size()-1),
				vertical_distance.get(vertical_distance.size()-1),
				alpha.get(alpha.size()-1),
				alpha_dot.get(alpha_dot.size()-1),
				gamma.get(gamma.size()-1),
				gamma_dot.get(gamma_dot.size()-1),
				theta.get(theta.size()-1),
				cL.get(cL.size()-1),
				cD.get(cD.size()-1)
				);
		System.out.println("\n---------------------------DONE!-------------------------------");
		
		
//		System.out.println("\n\n-----------------------AIRBORNE PHASE--------------------------");
//		
//		while (vertical_distance.get(i-1).isLessThan(obstacle)) {
//			
//			time.add(time.get(i-1).plus(dt.times(0.05)));
//			speed.add(Amount.valueOf(
//					speed.get(i-1).getEstimatedValue() + (mean_acceleration.get(i-1).getEstimatedValue()*dt.getEstimatedValue()*0.05),
//					SI.METERS_PER_SECOND));
//			
//			// TODO: ADD IF CASES TO ALPHA DOT, ALPHA, CL, LIFT
//			alpha_dot.add(alphaDotInitial*(1-(k_alpha_dot*(alpha.get(i-1).getEstimatedValue() + iw.getEstimatedValue()))));
//			alpha.add(Amount.valueOf(
//					alpha.get(i-1).getEstimatedValue() + (alpha_dot.get(i)*dt.getEstimatedValue()*0.05),
//					NonSI.DEGREE_ANGLE)
//					);
//			
//			cL.add(cL0 + ((highLiftCalculator.getcLalpha_new())*(alpha.get(i).getEstimatedValue() + iw.getEstimatedValue())));
//			lift.add(Amount.valueOf(
//					0.5
//					*aircraft.get_wing().get_surface().getEstimatedValue()
//					*AtmosphereCalc.getDensity(
//							theConditions.get_altitude().getEstimatedValue())
//					*(Math.pow(speed.get(i).getEstimatedValue(), 2))
//					*cL.get(i),
//					SI.NEWTON)
//					);
//			
//			if(cL.get(i) <= 1.2)
//				cD.add(aircraft.get_theAerodynamics().get_cD0() 
//						+ highLiftCalculator.getDeltaCD()
//						+ aircraft.get_landingGear().get_deltaCD0() 
//						+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
//						- k_ground*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio())));
//			else
//				cD.add(aircraft.get_theAerodynamics().get_cD0() 
//						+ highLiftCalculator.getDeltaCD()
//						+ aircraft.get_landingGear().get_deltaCD0() 
//						+ ((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()*aircraft.get_theAerodynamics().get_oswald()))
//						- k_ground*((Math.pow(cL.get(i),2))/(Math.PI*aircraft.get_wing().get_aspectRatio()))
//						+ k1*(cL.get(i)-1.2)
//						+ k2*(Math.pow((cL.get(i)-1.2), 2))
//						);
//			
//			drag.add(Amount.valueOf(
//					0.5
//					*aircraft.get_wing().get_surface().getEstimatedValue()
//					*AtmosphereCalc.getDensity(
//							theConditions.get_altitude().getEstimatedValue())
//					*(Math.pow(speed.get(i).getEstimatedValue(), 2))
//					*cD.get(i),
//					SI.NEWTON)
//					);
//			
//			if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP) {
//				thrust.add(Amount.valueOf(
//						ThrustCalc.calculateThrustDatabase(
//								aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
//								aircraft.get_powerPlant().get_engineNumber(),
//								phi,
//								aircraft.get_powerPlant().get_engineList().get(0).get_bpr(),
//								aircraft.get_powerPlant().get_engineType(),
//								EngineOperatingConditionEnum.TAKE_OFF,
//								theConditions.get_altitude().getEstimatedValue(),
//								SpeedCalc.calculateMach(
//										theConditions.get_altitude().getEstimatedValue(),
//										speed.get(i).getEstimatedValue() + v_wind.getEstimatedValue())
//								),
//						SI.NEWTON)
//						);
//				thrust_horizontal.add(thrust.get(i).times(Math.cos(alpha.get(i).to(SI.RADIAN).getEstimatedValue())));
//				thrust_vertical.add(thrust.get(i).times(Math.sin(alpha.get(i).to(SI.RADIAN).getEstimatedValue())));
//			}
//			else if(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOFAN) {
//				thrust.add(Amount.valueOf(
//						ThrustCalc.calculateThrust(
//								aircraft.get_powerPlant().get_engineList().get(0).get_t0().getEstimatedValue(),
//								aircraft.get_powerPlant().get_engineNumber(),
//								phi,
//								theConditions.get_altitude().getEstimatedValue()),
//						SI.NEWTON)
//						);
//				thrust_horizontal.add(thrust.get(i).times(Math.cos(alpha.get(i).to(SI.RADIAN).getEstimatedValue())));
//				thrust_vertical.add(thrust.get(i).times(Math.sin(alpha.get(i).to(SI.RADIAN).getEstimatedValue())));
//			}
//			
//			gamma_dot.add((AtmosphereCalc.g0.getEstimatedValue()/(speed.get(i).plus(v_wind).getEstimatedValue()))
//					*((lift.get(i)
//							.plus(thrust_vertical.get(i))
//							.minus(aircraft.get_weights().get_MTOW().times(Math.cos(gamma.get(i).getEstimatedValue()))).getEstimatedValue())
//							/aircraft.get_weights().get_MTOW().getEstimatedValue()));
//			gamma.add(Amount.valueOf(
//					gamma.get(i-1).getEstimatedValue() + (gamma_dot.get(i)*dt.getEstimatedValue()),
//					NonSI.DEGREE_ANGLE)
//					);
//			theta.add(alpha.get(i).plus(gamma.get(i)));
//
//			friction.add(friction.get(i-1));
//
//			total_force.add(
//					thrust_horizontal.get(i)
//					.minus(drag.get(i))
//					.minus(aircraft.get_weights().get_MTOW()
//							.times(Math.cos(gamma.get(i).getEstimatedValue()))
//							)
//					);
//			loadFactor.add(lift.get(i)
//					.divide(aircraft.get_weights().get_MTOW()
//							.times(Math.cos(gamma.get(i).getEstimatedValue()))
//							)
//					.getEstimatedValue()
//					);
//			if(loadFactor.get(i) < 1.0) {
//				loadFactor.remove(i);
//				loadFactor.add(1.0);
//			}
//			
//			acceleration.add(Amount.valueOf(
//					AtmosphereCalc.g0.divide(aircraft.get_weights().get_MTOW()).times(total_force.get(i)).getEstimatedValue(),
//					SI.METERS_PER_SQUARE_SECOND));
//			mean_acceleration.add((acceleration.get(i-1).plus(acceleration.get(i))).divide(2));
//			mean_speed.add(((speed.get(i-1).plus(speed.get(i))).divide(2)).plus(v_wind));
//			rateOfClimb.add(speed.get(i).plus(v_wind).times(Math.sin(gamma.get(i).getEstimatedValue())));
//			meanRateOfClimb.add((rateOfClimb.get(i).plus(rateOfClimb.get(i-1)).divide(2)));
//			delta_GroundDistance.add(
//					Amount.valueOf(
//							mean_speed.get(i).getEstimatedValue()*dt.getEstimatedValue()*0.05,
//							SI.METER
//							)
//					);
//			ground_distance.add(ground_distance.get(i-1).plus(delta_GroundDistance.get(i)));
//			delta_VerticalDistance.add(Amount.valueOf(meanRateOfClimb.get(i).times(dt).getEstimatedValue(), SI.METER));
//			vertical_distance.add(vertical_distance.get(i-1).plus(delta_VerticalDistance.get(i)));
//
//			// step increment
//			i += 1;
//		}
//		
//		System.out.println("\n--------------------END AIRBORNE PHASE-------------------------");
//		
//		// Collecting data at the end of ground roll phase
//		System.out.println("\n\tCOLLECTING DATA AT THE END OF AIRBORNE PHASE ...");
//		groundRollResults.collectResults(
//				time.get(time.size()-1),
//				thrust.get(thrust.size()-1),
//				thrust_horizontal.get(thrust_horizontal.size()-1),
//				thrust_vertical.get(thrust_vertical.size()-1),
//				friction.get(friction.size()-1),
//				lift.get(lift.size()-1),
//				drag.get(drag.size()-1),
//				total_force.get(total_force.size()-1),
//				loadFactor.get(loadFactor.size()-1),
//				speed.get(speed.size()-1),
//				mean_speed.get(mean_speed.size()-1),
//				rateOfClimb.get(rateOfClimb.size()-1),
//				meanRateOfClimb.get(meanRateOfClimb.size()-1),
//				acceleration.get(acceleration.size()-1),
//				mean_acceleration.get(mean_acceleration.size()-1),
//				delta_GroundDistance.get(delta_GroundDistance.size()-1),
//				ground_distance.get(ground_distance.size()-1),
//				delta_VerticalDistance.get(vertical_distance.size()-1),
//				vertical_distance.get(vertical_distance.size()-1),
//				alpha.get(alpha.size()-1),
//				alpha_dot.get(alpha_dot.size()-1),
//				gamma.get(gamma.size()-1),
//				gamma_dot.get(gamma_dot.size()-1),
//				theta.get(theta.size()-1),
//				cL.get(cL.size()-1),
//				cD.get(cD.size()-1)
//				);
//		System.out.println("\n---------------------------DONE!-------------------------------");
	}
	
	/**
	 * This method allows users to plot all take-off performance producing several output charts
	 * which have time or ground distance as independent variables.
	 * @author Vittorio Trifari
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void createTakeOffCharts() throws InstantiationException, IllegalAccessException {
		
		System.out.println("\n----------WRITING TAKE-OFF PERFORMANCE CHARTS TO FILE------------");

		String folderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		String subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "Take-Off_Performance" + File.separator);
		
		// data setup
		double[] time = new double[getTime().size()];
		for(int i=0; i<time.length; i++)
			time[i] = getTime().get(i).getEstimatedValue();
		
		double[] verticalDistance = new double[getVertical_distance().size()];
		for(int i=0; i<verticalDistance.length; i++)
			verticalDistance[i] = getVertical_distance().get(i).getEstimatedValue();
		
		double[] groundDistance = new double[getGround_distance().size()];
		for(int i=0; i<groundDistance.length; i++)
			groundDistance[i] = getGround_distance().get(i).getEstimatedValue();
		
		double[] thrust = new double[getThrust().size()];
		for(int i=0; i<thrust.length; i++)
			thrust[i] = getThrust().get(i).getEstimatedValue();
		
		double[] thrustHorizontal = new double[getThrust_horizontal().size()];
		for(int i=0; i<thrustHorizontal.length; i++)
			thrustHorizontal[i] = getThrust_horizontal().get(i).getEstimatedValue();
		
		double[] thrustVertical = new double[getThrust_vertical().size()];
		for(int i=0; i<thrustVertical.length; i++)
			thrustVertical[i] = getThrust_vertical().get(i).getEstimatedValue();
		
		double[] lift = new double[getLift().size()];
		for(int i=0; i<lift.length; i++)
			lift[i] = getLift().get(i).getEstimatedValue();
		
		double[] drag = new double[getDrag().size()];
		for(int i=0; i<drag.length; i++)
			drag[i] = getDrag().get(i).getEstimatedValue();
		
		double[] friction = new double[getFriction().size()];
		for(int i=0; i<friction.length; i++)
			friction[i] = getFriction().get(i).getEstimatedValue();
		
		double[] totalForce = new double[getTotal_force().size()];
		for(int i=0; i<totalForce.length; i++)
			totalForce[i] = getTotal_force().get(i).getEstimatedValue();
		
		double[] loadFactor = new double[getLoadFactor().size()];
		for(int i=0; i<loadFactor.length; i++)
			loadFactor[i] = getLoadFactor().get(i);
		
		double[] acceleration = new double[getAcceleration().size()];
		for(int i=0; i<acceleration.length; i++)
			acceleration[i] = getAcceleration().get(i).getEstimatedValue();
		
		double[] speed = new double[getSpeed().size()];
		for(int i=0; i<speed.length; i++)
			speed[i] = getSpeed().get(i).getEstimatedValue();
		
		double[] alpha = new double[getAlpha().size()];
		for(int i=0; i<alpha.length; i++)
			alpha[i] = getAlpha().get(i).getEstimatedValue();
		
		double[] gamma = new double[getGamma().size()];
		for(int i=0; i<gamma.length; i++)
			gamma[i] = getGamma().get(i).getEstimatedValue();
		
		double[] theta = new double[getTime().size()];
		for(int i=0; i<theta.length; i++)
			theta[i] = getTheta().get(i).getEstimatedValue();
		
		double[] alphaDot = new double[getAlpha_dot().size()];
		for(int i=0; i<alphaDot.length; i++)
			alphaDot[i] = getAlpha_dot().get(i);
		
		double[] gammaDot = new double[getGamma_dot().size()];
		for(int i=0; i<gammaDot.length; i++)
			gammaDot[i] = getGamma_dot().get(i);
		
		double[] cL = new double[getcL().size()];
		for(int i=0; i<cL.length; i++)
			cL[i] = getcL().get(i);
		
		double[] rateOfClimb = new double[getRateOfClimb().size()];
		for(int i=0; i<rateOfClimb.length; i++)
			rateOfClimb[i] = getRateOfClimb().get(i).getEstimatedValue();
		
		double[] weight = new double[getTime().size()];
		for(int i=0; i<weight.length; i++)
			weight[i] = aircraft.get_weights().get_MTOW().getEstimatedValue()
						*Math.cos(getGamma().get(i).getEstimatedValue());
		
		// take-off trajectory
		MyChartToFileUtils.plotNoLegend(
				groundDistance, verticalDistance,
				0.0, null, 0.0, 15.0,
				"Ground Distance", "Altitude", "m", "m",
				subfolderPath, "TakeOff_Trajectory");
		
		// vertical distance v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, verticalDistance,
				0.0, null, 0.0, 15.0,
				"Time", "Altitude", "s", "m",
				subfolderPath, "Altitude_evolution");
		
		// speed v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, speed,
				0.0, null, 0.0, null,
				"Time", "Speed", "s", "m/s",
				subfolderPath, "Speed_evolution");
		
		// speed v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, speed,
				0.0, null, 0.0, null,
				"Ground Distance", "Speed", "m", "m/s",
				subfolderPath, "Speed_vs_GroundDistance");
		
		// acceleration v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, acceleration,
				0.0, null, 0.0, null,
				"Time", "Acceleration", "s", "m/(s^2)",
				subfolderPath, "Acceleration_evolution");
		
		// acceleration v.s. time
		MyChartToFileUtils.plotNoLegend(
				groundDistance, acceleration,
				0.0, null, 0.0, null,
				"Ground Distance", "Acceleration", "m", "m/(s^2)",
				subfolderPath, "Acceleration_vs_GroundDistance");
		
		// load factor v.s. time
		MyChartToFileUtils.plotNoLegend(
				time, loadFactor,
				0.0, null, 0.0, null,
				"Time", "Load Factor", "s", "",
				subfolderPath, "LoadFactor_evolution");
		
		// load factor v.s. ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, loadFactor,
				0.0, null, 0.0, null,
				"Ground distance", "Load Factor", "m", "",
				subfolderPath, "LoadFactor_vs_GroundDistance");
		
		// Rate of Climb v.s. Time
		MyChartToFileUtils.plotNoLegend(
				time, rateOfClimb,
				0.0, null, 0.0, 10.0,
				"Time", "Rate of Climb", "s", "m/s",
				subfolderPath, "RateOfClimb_evolution");
		
		// Rate of Climb v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, rateOfClimb,
				0.0, null, 0.0, 10.0,
				"Ground distance", "Rate of Climb", "m", "m/s",
				subfolderPath, "RateOfClimb_vs_GroundDistance");

		// CL v.s. Time
		MyChartToFileUtils.plotNoLegend(
				time, cL,
				0.0, null, 0.0, null,
				"Time", "CL", "s", "",
				subfolderPath, "CL_evolution");
		
		// CL v.s. Ground distance
		MyChartToFileUtils.plotNoLegend(
				groundDistance, cL,
				0.0, null, 0.0, null,
				"Ground distance", "CL", "m", "",
				subfolderPath, "CL_vs_GroundDistance");
		
		// Horizontal Forces v.s. Time
		double[][] xMatrix1 = new double[4][totalForce.length];
		for(int i=0; i<xMatrix1.length; i++)
			xMatrix1[i] = time;
		
		double[][] yMatrix1 = new double[4][totalForce.length];
		yMatrix1[0] = totalForce;
		yMatrix1[1] = thrustHorizontal;
		yMatrix1[2] = drag;
		yMatrix1[3] = friction;
		
		MyChartToFileUtils.plot(
				xMatrix1, yMatrix1,
				0.0, null, 0.0, null,
				"Time", "Horizontal Forces", "s", "N",
				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction"},
				subfolderPath, "HorizontalForces_evolution");
		
		// Horizontal Forces v.s. Ground Distance
		double[][] xMatrix2 = new double[4][totalForce.length];
		for(int i=0; i<xMatrix2.length; i++)
			xMatrix2[i] = groundDistance;

		double[][] yMatrix2 = new double[4][totalForce.length];
		yMatrix2[0] = totalForce;
		yMatrix2[1] = thrustHorizontal;
		yMatrix2[2] = drag;
		yMatrix2[3] = friction;

		MyChartToFileUtils.plot(
				xMatrix2, yMatrix2,
				0.0, null, 0.0, null,
				"Ground Distance", "Horizontal Forces", "m", "N",
				new String[] {"Total Force", "Thrust Horizontal", "Drag", "Friction"},
				subfolderPath, "HorizontalForces_vs_GroundDistance");
				
		// Vertical Forces v.s. Time
		double[][] xMatrix3 = new double[3][totalForce.length];
		for(int i=0; i<xMatrix3.length; i++)
			xMatrix3[i] = time;

		double[][] yMatrix3 = new double[3][totalForce.length];
		yMatrix3[0] = lift;
		yMatrix3[1] = thrustVertical;
		yMatrix3[2] = weight;

		MyChartToFileUtils.plot(
				xMatrix3, yMatrix3,
				0.0, null, 0.0, null,
				"Time", "Vertical Forces", "s", "N",
				new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
				subfolderPath, "VerticalForces_evolution");
		
		// Vertical Forces v.s. ground distance
		double[][] xMatrix4 = new double[3][totalForce.length];
		for(int i=0; i<xMatrix4.length; i++)
			xMatrix4[i] = groundDistance;

		double[][] yMatrix4 = new double[3][totalForce.length];
		yMatrix4[0] = lift;
		yMatrix4[1] = thrustVertical;
		yMatrix4[2] = weight;

		MyChartToFileUtils.plot(
				xMatrix4, yMatrix4,
				0.0, null, 0.0, null,
				"Ground distance", "Vertical Forces", "m", "N",
				new String[] {"Lift", "Thrust Vertical", "W*cos(gamma)"},
				subfolderPath, "VerticalForces_vs_GroundDistance");
		
		// Angles v.s. time
		double[][] xMatrix5 = new double[3][totalForce.length];
		for(int i=0; i<xMatrix5.length; i++)
			xMatrix5[i] = time;

		double[][] yMatrix5 = new double[3][totalForce.length];
		yMatrix5[0] = alpha;
		yMatrix5[1] = theta;
		yMatrix5[2] = gamma;

		MyChartToFileUtils.plot(
				xMatrix5, yMatrix5,
				0.0, null, 0.0, null,
				"Time", "Angles", "s", "deg",
				new String[] {"Alpha", "Theta", "Gamma"},
				subfolderPath, "Angles_evolution");
		
		// Angles v.s. Ground Distance
		double[][] xMatrix6 = new double[3][totalForce.length];
		for(int i=0; i<xMatrix6.length; i++)
			xMatrix6[i] = groundDistance;

		double[][] yMatrix6 = new double[3][totalForce.length];
		yMatrix6[0] = alpha;
		yMatrix6[1] = theta;
		yMatrix6[2] = gamma;

		MyChartToFileUtils.plot(
				xMatrix6, yMatrix6,
				0.0, null, 0.0, null,
				"Ground Distance", "Angles", "m", "deg",
				new String[] {"Alpha", "Theta", "Gamma"},
				subfolderPath, "Angles_vs_GroundDistance");
		
		// Angular velocity v.s. time
		double[][] xMatrix7 = new double[2][totalForce.length];
		for(int i=0; i<xMatrix7.length; i++)
			xMatrix7[i] = time;

		double[][] yMatrix7 = new double[2][totalForce.length];
		yMatrix7[0] = alphaDot;
		yMatrix7[1] = gammaDot;

		MyChartToFileUtils.plot(
				xMatrix7, yMatrix7,
				0.0, null, 0.0, null,
				"Time", "Angular Velocity", "s", "deg/s",
				new String[] {"Alpha_dot", "Gamma_dot"},
				subfolderPath, "AngularVelocity_evolution");
		
		// Angular velocity v.s. Ground Distance
		double[][] xMatrix8 = new double[2][totalForce.length];
		for(int i=0; i<xMatrix8.length; i++)
			xMatrix8[i] = groundDistance;

		double[][] yMatrix8 = new double[2][totalForce.length];
		yMatrix8[0] = alphaDot;
		yMatrix8[1] = gammaDot;

		MyChartToFileUtils.plot(
				xMatrix8, yMatrix8,
				0.0, null, 0.0, null,
				"Ground Distance", "Angular Velocity", "m", "deg/s",
				new String[] {"Alpha_dot", "Gamma_dot"},
				subfolderPath, "AngularVelocity_vs_GroundDistance");
		
		System.out.println("\n---------------------------DONE!-------------------------------");
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

	public List<Amount<Duration>> getTime() {
		return time;
	}

	public void setTime(List<Amount<Duration>> time) {
		this.time = time;
	}

	public List<Amount<Angle>> getAlpha() {
		return alpha;
	}

	public void setAlpha(List<Amount<Angle>> alpha) {
		this.alpha = alpha;
	}

	public List<Amount<Angle>> getTheta() {
		return theta;
	}

	public void setTheta(List<Amount<Angle>> theta) {
		this.theta = theta;
	}

	public List<Amount<Angle>> getGamma() {
		return gamma;
	}

	public void setGamma(List<Amount<Angle>> gamma) {
		this.gamma = gamma;
	}

	public List<Double> getAlpha_dot() {
		return alpha_dot;
	}

	public void setAlpha_dot(List<Double> alpha_dot) {
		this.alpha_dot = alpha_dot;
	}

	public double getAlphaDotInitial() {
		return alphaDotInitial;
	}

	public void setAlphaDotInitial(double alphaDotInitial) {
		this.alphaDotInitial = alphaDotInitial;
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

	public List<Double> getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(List<Double> loadFactor) {
		this.loadFactor = loadFactor;
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

	public List<Amount<Length>> getDelta_VerticalDistance() {
		return delta_VerticalDistance;
	}

	public void setDelta_VerticalDistance(List<Amount<Length>> delta_VerticalDistance) {
		this.delta_VerticalDistance = delta_VerticalDistance;
	}

	public List<Amount<Length>> getGround_distance() {
		return ground_distance;
	}

	public void setGround_distance(List<Amount<Length>> ground_distance) {
		this.ground_distance = ground_distance;
	}

	public List<Amount<Length>> getVertical_distance() {
		return vertical_distance;
	}

	public void setVertical_distance(List<Amount<Length>> vertical_distance) {
		this.vertical_distance = vertical_distance;
	}

	public Amount<Duration> getDt() {
		return dt;
	}

	public void setDt(Amount<Duration> dt) {
		this.dt = dt;
	}

	public Amount<Duration> getDtRot() {
		return dtRot;
	}

	public void setDtRot(Amount<Duration> dtRot) {
		this.dtRot = dtRot;
	}

	public Amount<Duration> getDtHold() {
		return dtHold;
	}

	public void setDtHold(Amount<Duration> dtHold) {
		this.dtHold = dtHold;
	}

	public double getK_alpha_dot() {
		return k_alpha_dot;
	}

	public void setK_alpha_dot(double k_alpha_dot) {
		this.k_alpha_dot = k_alpha_dot;
	}

	public double getAlphaRed() {
		return alphaRed;
	}

	public void setAlphaRed(double alphaRed) {
		this.alphaRed = alphaRed;
	}

	public List<Double> getGamma_dot() {
		return gamma_dot;
	}

	public void setGamma_dot(List<Double> gamma_dot) {
		this.gamma_dot = gamma_dot;
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

	public double getK_obs() {
		return k_obs;
	}

	public double getK_clMax() {
		return k_clMax;
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

	public Amount<Length> getObstacle() {
		return obstacle;
	}

	public void setObstacle(Amount<Length> obstacle) {
		this.obstacle = obstacle;
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

	public Amount<Angle> getIw() {
		return iw;
	}

	public void setIw(Amount<Angle> iw) {
		this.iw = iw;
	}

	public TakeOffResultsMap getGroundRollResults() {
		return groundRollResults;
	}

	public void setGroundRollResults(TakeOffResultsMap groundRollResults) {
		this.groundRollResults = groundRollResults;
	}

	public List<Amount<Velocity>> getRateOfClimb() {
		return rateOfClimb;
	}

	public void setRateOfClimb(List<Amount<Velocity>> rateOfClimb) {
		this.rateOfClimb = rateOfClimb;
	}

	public List<Amount<Velocity>> getMeanRateOfClimb() {
		return meanRateOfClimb;
	}

	public void setMeanRateOfClimb(List<Amount<Velocity>> meanRateOfClimb) {
		this.meanRateOfClimb = meanRateOfClimb;
	}
}