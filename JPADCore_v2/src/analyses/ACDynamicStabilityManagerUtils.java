package analyses;

public class ACDynamicStabilityManagerUtils {

	public enum PropulsionTypeEnum { CONSTANT_TRUST, CONSTANT_POWER, CONSTANT_MASS_FLOW, RAMJET }
	
	public static double calcDynamicPressure(double rho0, double u0) {
		
		return 0.5*rho0*Math.pow(u0,2);
		
	}
	
	public static double calcMassVehicleParameter(double rho0, double surf, double mass,
			double cbar) {
		
		return 2*mass/(rho0*surf*cbar);
		
	}
	
	
	//////////////////////          LONGITUDINAL DYNAMICS          //////////////////////
	
	// Longitudinal Dimensional Stability Derivatives
	
	/**
	 *   calculates the dimensional stability derivative of force component X with respect to "u", 
     *   divided by the mass, for Constant Thrust (appropriate for jet aircraft or
     *   for unpowered flight)
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cd0 drag coefficient at null incidence (Cd�) of the aircraft
	 * @param m0 Mach number
	 * @param cdM0 drag coefficient with respect to Mach (CdM�) of the aircraft
	 * @return X_uCT dimensional derivative [s^(-1)]
	 */
	public static double calcX_u_CT (double rho0, double surf, double mass, double u0, double q0,
			double cd0, double m0, double cdM0) {
		
		return -q0*surf*(2*cd0 + m0*cdM0)/(mass*u0);	
	
	}
	
	/**  
	 *   calculates the dimensional stability derivative of force component X with respect to "u", 
     *   divided by the mass, for Constant Power (appropriate for propeller aircraft
     *   with automatic pitch control and constant-speed propeller)
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cd0 drag coefficient at null incidence (Cd�) of the aircraft
	 * @param m0 Mach number
	 * @param cdM0 drag coefficient with respect to Mach (CdM�) of the aircraft
	 * @param cl0 lift coefficient at null incidence (Cl�) of the aircraft
	 * @param gamma0 ramp angle
	 * @return X�uCP dimensional derivative [s^(-1)]
	 */
	public static double calcX_u_CP (double rho0, double surf, double mass, double u0, double q0,
			double cd0, double m0, double cdM0, double cl0, double gamma0) {
		
		double gamma0_rad = Math.toRadians(gamma0); // angolo di salita (in rad)
		return -q0*surf*(3*cd0+cl0*Math.tan(gamma0_rad)+m0*cdM0)/(mass*u0);
	
	}
	
	/**  
	 *   calculates the dimensional stability derivative of force component X with respect to "w",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param cl0 lift coefficient at null incidence (Cl�) of the aircraft
	 * @param cdAlpha0 linear drag gradient (CdAlpha�) of the aircraft
	 * @return X�w dimensional derivative [s^(-1)]
	 */
	public static double calcX_w (double rho0, double surf, double mass, double u0, double q0,
			double cl0, double cdAlpha0) {
		
		
		return q0*surf*(cl0-cdAlpha0)/(mass*u0);	
	
	}
		
	/**  
	 *   calculates the dimensional stability derivative of force component Z with respect to "u",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param m0 Mach number
	 * @param cl0 lift coefficient at null incidence (Cl�) of the aircraft
	 * @param clM0 lift coefficient with respect to Mach (ClM�) of the aircraft
	 * @return Z�u dimensional derivative [s^(-1)]
	 */
	public static double calcZ_u (double rho0, double surf, double mass, double u0, double q0, double m0,
			double cl0, double clM0) {
		
		
		return -q0*surf*(2*cl0 + Math.pow(m0,2)*clM0/(1-Math.pow(m0,2)) )/(mass*u0);	
	
	}

	/**  
	 *   calculates the dimensional stability derivative of force component Z with respect to "w",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param m0 Mach number
	 * @param cd0 drag coefficient at null incidence (Cd�) of the aircraft
	 * @param clAlpha0 linear lift gradient (ClAlpha�) of the aircraft
	 * @return Z�w dimensional derivative [s^(-1)]
	 */
	public static double calcZ_w (double rho0, double surf, double mass, double u0, double q0, double m0,
			double cd0, double clAlpha0) {
		
		
		return -q0*surf*(cd0 + clAlpha0 )/(mass*u0);	
	
	}

	/**  
	 *   calculates the dimensional stability derivative of force component Z with respect to "w_dot",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param cbar mean aerodynamic chord
	 * @param clAlpha_dot0 linear lift gradient time derivative (ClAlpha_dot�) of the aircraft
	 * @return Z�w_dot adimensional derivative
	 */
	public static double calcZ_w_dot (double rho0, double surf, double mass, double cbar,
			double clAlpha_dot0) {
		
		return -clAlpha_dot0/(2*calcMassVehicleParameter(rho0, surf, mass, cbar));	
	
	}
		
	/**  
	 *   calculates the dimensional stability derivative of force component Z with respect to "q",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cbar mean aerodynamic chord
	 * @param clQ0 lift coefficient with respect to q (ClQ�) of the aircraft
	 * @return Z�q dimensional derivative [s^(-1)]
	 */
	public static double calcZ_q (double rho0, double surf, double mass, double u0, double q0, double cbar,
			double clQ0) {
		
		return -u0*clQ0/(2*calcMassVehicleParameter(rho0, surf,mass, cbar));	
	
	}
		
	/**
	 *   calculates the dimensional stability derivative of pitching moment M with respect to "u", 
	 *   divided by the longitudinal moment of inertia Iyy
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param cbar mean aerodynamic chord
	 * @param m0 Mach number
	 * @param iYY longitudinal moment of inertia  (IYY)
	 * @param cM_m0 pitching moment coefficient with respect to Mach number
	 * @return cM�u dimensional derivative [m^(-1) * s^(-1)]
	 */
	public static double calcM_u (double rho0, double surf, double mass, double u0, double q0, double cbar,
			double m0, double iYY, double cM_m0) {
		
		
		return q0*surf*cbar*m0*cM_m0/(iYY*u0);
		
	}

	/**
	 *   calculates the dimensional stability derivative of pitching moment M with respect to "w", 
	 *   divided by the longitudinal moment of inertia Iyy
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param cbar mean aerodynamic chord
	 * @param iYY longitudinal moment of inertia  (IYY)
	 * @param cMAlpha0 pitching moment coefficient with respect to Alpha (CmAlpha�) of the aircraft
	 * @return calcM�w dimensional derivative [m^(-1) * s^(-1)]
	 */
	public static double calcM_w (double rho0, double surf, double mass, double u0, double q0, double cbar,
			double iYY, double cMAlpha0) {
		
		
		return q0*surf*cbar*cMAlpha0/(iYY*u0);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of pitching moment M with respect to "w_dot", 
	 *   divided by the longitudinal moment of inertia Iyy
	 * @param rho0 air density
	 * @param mass total mass
	 * @param surf wing area
	 * @param cbar mean aerodynamic chord
	 * @param iYY longitudinal moment of inertia  (IYY)
	 * @param cMAlpha_dot0 pitching moment coefficient with respect to Alpha (CmAlpha�) of the aircraft_dot
	 * @return calcM�w_dot dimensional derivative [m^(-1)]
	 */
	public static double calcM_w_dot (double rho0, double surf, double cbar,
			double iYY, double cMAlpha_dot0) {
		
		return rho0*surf*Math.pow(cbar, 2)*cMAlpha_dot0/(4*iYY);
		
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of pitching moment M with respect to "q",
	 *   divided by the longitudinal moment of inertia Iyy
	 * @param rho0 air density
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param surf wing area
	 * @param cbar mean aerodynamic chord
	 * @param iYY longitudinal moment of inertia  (IYY)
	 * @return calcM�q dimensional derivative [s^(-1)]
	 */
	public static double calcM_q (double rho0, double mass, double u0, double q0, double surf, double cbar,
			double iYY, double cMq) {
		
	    return rho0*u0*surf*Math.pow(cbar, 2)*cMq/(4*iYY);
	    
	}
	
	// Longitudinal Dimensional Control Derivatives
	
	/**
	 *   calculates the dimensional control derivative of force component X with respect to "delta_T", 
	 *   divided by the mass, for Constant Thrust (appropriate for jet aircraft or
     *   for unpowered flight)
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cTfix thrust coefficient at a fixed point ( U0 = u , delta_T = 1 )
	 * @param kv scale factor of the effect on the propulsion due to the speed
	 * @return X�delta_T_CT dimensional derivative [m * s^(-2)]
	 */
	public static double calcX_delta_T_CT (double rho0, double surf, double mass, double u0, double q0,
			double cTfix, double kv) {
		
		
		return -q0*surf*(cTfix+kv*Math.pow(u0, -2))/(mass);
	
	}
	
	/**
	 *   calculates the dimensional control derivative of force component X with respect to "delta_T",
	 *   divided by the mass, for Constant Power (appropriate for propeller aircraft
     *   with automatic pitch control and constant-speed propeller)
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cTfix thrust coefficient at a fixed point ( U0 = u , delta_T = 1 )
	 * @param kv scale factor of the effect on the propulsion due to the speed
	 * @return X�delta_T_CP dimensional derivative [m * s^(-2)]
	 */
	public static double calcX_delta_T_CP (double rho0, double surf, double mass, double u0, double q0,
			double cTfix, double kv) {
		
		
		return -q0*surf*(cTfix+kv*Math.pow(u0, -3))/(mass);
	
	}
	
	/**
	 *   calculates the dimensional control derivative of force component X with respect to "delta_T",
	 *   divided by the mass, for Constant Mass Flow propulsion
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cTfix thrust coefficient at a fixed point ( U0 = u , delta_T = 1 )
	 * @param kv scale factor of the effect on the propulsion due to the speed
	 * @return X�delta_T_CMF dimensional derivative [m * s^(-2)]
	 */
	public static double calcX_delta_T_CMF (double rho0, double surf, double mass, double u0, double q0,
			double cTfix, double kv) {
		
		
		return -q0*surf*(cTfix+kv*Math.pow(u0, -1))/(mass);
	
	}
	
	/**
	 *   calculates the dimensional control derivative of force component X with respect to "delta_T",
	 *   divided by the mass, for Ramjet propulsion
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cTfix thrust coefficient at a fixed point ( U0 = u , delta_T = 1 )
	 * @param kv scale factor of the effect on the propulsion due to the speed
	 * @return X�delta_T_RJ dimensional derivative [m * s^(-2)]
	 */
	public static double calcX_delta_T_RJ (double rho0, double surf, double mass, double u0, double q0,
			double cTfix, double kv) {
		
		
		return -q0*surf*(cTfix+kv*Math.pow(u0, 0))/(mass);
	
	}
	
	/**
	 *   calculates the dimensional control derivative of force component X with respect to "delta_T",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param clDelta_T lift coefficient with respect to delta_T (ClDelta_T�) of the aircraft 
	 * @return Z�delta_T dimensional derivative [m * s^(-2)]
	 */
	public static double calcZ_delta_T (double rho0, double surf, double mass, double u0, double q0,
			double clDelta_T) {
		
		return 0;
		
	}
	
	/**
	 *   calculates the dimensional control derivative of force component Z with respect to "delta_E",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param clDelta_E lift coefficient with respect to delta_E (ClDelta_E�) of the aircraft  
	 * @return Z�delta_E dimensional derivative [m * s^(-2)]
	 */
	public static double calcZ_delta_E (double rho0, double surf, double mass, double u0, double q0,
			double clDelta_E) {
		
		return -q0*surf*(1/mass)*clDelta_E;
		
	}

	/**
	 *   calculates the dimensional control derivative of pitching moment M with respect to "delta_T",
	 *   divided by the longitudinal moment of inertia Iyy
	 * @param rho0 air density
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param surf wing area
	 * @param cbar mean aerodynamic chord
	 * @param iYY longitudinal moment of inertia  (IYY)
	 * @return calcM�delta_T dimensional derivative [s^(-2)]
	 */
	public static double calcM_delta_T (double rho0, double surf, double cbar, double u0, double q0,
			double iYY, double cMDelta_T) {
		
		return 0;
		
	}
		
	/**
	 *   calculates the dimensional control derivative of pitching moment M with respect to "delta_E",
	 *   divided by the longitudinal moment of inertia Iyy
	 * @param rho0 air density
	 * @param mass total mass
	 * @param u0 speed of the aircraft 
	 * @param q0 dynamic pressure
	 * @param surf wing area
	 * @param cbar mean aerodynamic chord
	 * @param iYY longitudinal moment of inertia  (IYY)
	 * @return calcM�delta_E dimensional derivative [s^(-2)]
	 */
	public static double calcM_delta_E (double rho0, double surf, double cbar, double u0, double q0,
			double iYY, double cMDelta_E) {
		
		return q0*surf*cbar*cMDelta_E/(iYY);
		
	}
	
	// Longitudinal Matrices
	
	/**
	 *   generates the longitudinal coefficients matrix [A_Lon] of linearized equations of dynamics
	 * @param propulsion_system propulsion regime type
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param cbar mean aerodynamic chord
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param m0 Mach number
	 * @param gamma0 ramp angle
	 * @param theta0_rad Euler angle [rad] (assuming gamma0 = theta0)
	 * @param iYY longitudinal moment of inertia  (IYY)
	 * @param cd0 drag coefficient at null incidence (Cd�) of the aircraft
	 * @param cdM0 drag coefficient with respect to Mach (CdM�) of the aircraft
	 * @param cdAlpha0 linear drag gradient (CdAlpha�) of the aircraft
	 * @param cl0 lift coefficient at null incidence (Cl�) of the aircraft
	 * @param clAlpha0 linear lift gradient (ClAlpha�) of the aircraft
	 * @param clAlpha_dot0 linear lift gradient time derivative (ClAlpha_dot�) of the aircraft
	 * @param clQ0 lift coefficient with respect to q (ClQ�) of the aircraft
	 * @param cMAlpha0 pitching moment coefficient with respect to Alpha (CmAlpha�) of the aircraft
	 * @param cMAlpha0_dot pitching moment coefficient time derivative (CmAlpha_dot�) of the aircraft
	 * @param cM_m0 pitching moment coefficient with respect to Mach number
	 * @param cMq pitching moment coefficient with respect to q
	 * @return matrix [A_Lon]
	 */
	public static double[][] build_A_Lon_matrix (PropulsionTypeEnum propulsion_system,
			double rho0, double surf, double mass, double cbar, double u0, double q0,
			double cd0, double m0, double cdM0, double cl0,	double clM0, double cdAlpha0,
			double gamma0, double theta0_rad, double clAlpha0, double clAlpha_dot0,
			double cMAlpha0, double cMAlpha0_dot, double clQ0, double iYY, double cM_m0,
			double cMq) {
		
		double [][] aLon = new double [4][4];
		
		// Propulsion type in the X�u calculation 
		switch (propulsion_system)
			{
			case CONSTANT_TRUST:
				aLon [0][0] = calcX_u_CT (rho0, surf, mass, u0, q0, cd0, m0, cdM0);
				break;
			case CONSTANT_POWER:
				aLon [0][0] = calcX_u_CP (rho0, surf, mass, u0, q0, cd0, m0, cdM0, cl0, gamma0);
				break;
			default:
				aLon [0][0] = calcX_u_CT (rho0, surf, mass, u0, q0, cd0, m0, cdM0);
				break;
			}
		
		double k = calcM_w_dot (rho0, surf, cbar, iYY, cMAlpha0_dot)/(1 - calcZ_w_dot (rho0, surf,
				   mass, cbar, clAlpha_dot0));
	
		// Construction of the Matrix [A Lon]
		aLon [0][1] = calcX_w(rho0, surf, mass, u0, q0, cl0, cdAlpha0);
		
		aLon [0][2] = 0;
		
		aLon [0][3] = -(9.8100)*Math.cos(theta0_rad);
		
		aLon [1][0] = calcZ_u (rho0, surf, mass, u0, q0, m0, cl0, clM0)/(1 - calcZ_w_dot (rho0, surf,
			    mass, cbar, clAlpha_dot0));
		
		aLon [1][1] = calcZ_w (rho0, surf, mass, u0, q0, m0, cd0, clAlpha0)/(1 - calcZ_w_dot (rho0, surf,
			    mass, cbar, clAlpha_dot0));
		
		aLon [1][2] = (calcZ_q (rho0, surf, mass, u0, q0, cbar, clQ0) + u0)/(1 - calcZ_w_dot (rho0, surf,
			    mass, cbar, clAlpha_dot0));
		
		aLon [1][3] = -(9.8100)*Math.sin(theta0_rad)/(1 - calcZ_w_dot (rho0, surf,
			    mass, cbar, clAlpha_dot0));
		
		aLon [2][0] = calcM_u (rho0, surf, mass, u0, q0, cbar, m0, iYY, cM_m0) + k*calcZ_u (rho0, surf,
				mass, u0, q0, m0, cl0, clM0);
		
		aLon [2][1] = calcM_w (rho0, surf, mass, u0, q0, cbar, iYY, cMAlpha0) + k*calcZ_w (rho0, surf,
				mass, u0, q0, m0, cd0, clAlpha0);
		
		aLon [2][2] = calcM_q (rho0, mass, u0, q0, surf, cbar, iYY, cMq) + k * ( calcZ_q (rho0, surf, mass,
				u0, q0, cbar, clQ0)  + u0);
		
		aLon [2][3] = -k*(9.8100)*Math.sin(theta0_rad);
		
		aLon [3][0] = 0;
		
		aLon [3][1] = 0;
		
		aLon [3][2] = 1;
		
		aLon [3][3] = 0;	
		
		return aLon;
	}
	
	/**
	 * generates the longitudinal control coefficients matrix [B_Lon] of linearized equations of dynamics
	 * @param propulsion_system propulsion regime type
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param cbar mean aerodynamic chord
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cd0 drag coefficient at null incidence (Cd�) of the aircraft
	 * @param m0 Mach number
	 * @param cdM0 drag coefficient with respect to Mach (CdM�) of the aircraft
	 * @param cl0 lift coefficient at null incidence (Cl�) of the aircraft
	 * @param cdAlpha0 linear drag gradient (CdAlpha�) of the aircraft
	 * @param gamma0 ramp angle
	 * @param theta0_rad Euler angle [rad] (assuming gamma0 = theta0)
	 * @param clAlpha0 linear lift gradient (ClAlpha�) of the aircraft
	 * @param clAlpha_dot0 linear lift gradient time derivative (ClAlpha_dot�) of the aircraft
	 * @param cMAlpha0 pitching moment coefficient with respect to Alpha (CmAlpha�) of the aircraft
	 * @param cMAlpha0_dot pitching moment coefficient time derivative (CmAlpha_dot�) of the aircraft
	 * @param clQ0 lift coefficient with respect to q (ClQ�) of the aircraft
	 * @param iYY longitudinal moment of inertia  (IYY)
	 * @param cM_m0 pitching moment coefficient with respect to Mach number
	 * @param cMq pitching moment coefficient with respect to q
	 * @param cTfix thrust coefficient at a fixed point ( U0 = u , delta_T = 1 )
	 * @param kv scale factor of the effect on the propulsion due to the speed
	 * @param clDelta_T lift coefficient with respect to delta_T (ClDelta_T�) of the aircraft
	 * @param clDelta_E lift coefficient with respect to delta_E (ClDelta_E�) of the aircraft
	 * @param cMDelta_T pitching moment coefficient with respect to delta_T (CMDelta_T�) of the aircraft
	 * @param cMDelta_E pitching moment coefficient with respect to delta_E (CMDelta_E�) of the aircraft
	 * @return
	 */
	public static double[][] build_B_Lon_matrix (PropulsionTypeEnum propulsion_system, double rho0, 
			double surf, double mass, double cbar, double u0, double q0, double cd0, double m0, double cdM0,
			double cl0,	double cdAlpha0, double gamma0, double theta0_rad, double clAlpha0,
			double clAlpha_dot0, double cMAlpha0, double cMAlpha_dot0, double clQ0, double iYY,
			double cM_m0, double cMq, double cTfix, double kv, double clDelta_T, double clDelta_E,
			double cMDelta_T, double cMDelta_E) {
		
		double [][] bLon = new double [4][2];
		
		// Propulsion type in the X�delta_T calculation
		switch (propulsion_system)
		{
		case CONSTANT_TRUST:
			bLon [0][0] = calcX_delta_T_CT (rho0, surf, mass, u0, q0, cTfix, kv);
			break;
		case CONSTANT_POWER:
			bLon [0][0] = calcX_delta_T_CP (rho0, surf, mass, u0, q0, cTfix, kv);
			break;
		case CONSTANT_MASS_FLOW:
			bLon [0][0] = calcX_delta_T_CMF (rho0, surf, mass, u0, q0, cTfix, kv);
			break;
		case RAMJET:
			bLon [0][0] = calcX_delta_T_RJ (rho0, surf, mass, u0, q0, cTfix, kv);
			break;
		default:
			bLon [0][0] = calcX_delta_T_CT (rho0, surf, mass, u0, q0, cTfix, kv);
			break;
		}
		
		// Coefficient k� calculation
		double k = calcM_w_dot (rho0, surf, cbar, iYY, cMAlpha_dot0)/(1 - calcZ_w_dot (rho0, surf,
				    mass, cbar, clAlpha_dot0));
		
		// Construction of the Matrix [B Lon]
		bLon [0][1] = 0;
		
		bLon [1][0] = calcZ_delta_T (rho0, surf, mass, u0, q0, clDelta_T) / (1 - calcZ_w_dot (rho0, surf,
			    mass, cbar, clAlpha_dot0));
		
		bLon [1][1] = calcZ_delta_E (rho0, surf, mass, u0, q0, clDelta_E) / (1 - calcZ_w_dot (rho0, surf,
			    mass, cbar, clAlpha_dot0));
		
		bLon [2][0] = calcM_delta_T (rho0, surf, cbar, u0, q0, iYY, cMDelta_T) + k * calcZ_delta_T (rho0, surf,
				mass, u0, q0, clDelta_T);
		
		bLon [2][1] = calcM_delta_E (rho0, surf, cbar, u0, q0, iYY, cMDelta_E) + k * calcZ_delta_E (rho0, surf,
				mass, u0, q0, clDelta_E);
		
		bLon [3][0] = 0;
		
		bLon [3][1] = 0;
		
		return bLon;
				
	}
	
	
    //////////////////////       LATERAL-DIRECTIONAL DYNAMICS       //////////////////////
	
	// Lateral-Directional Dimensional Stability Derivatives
	
	/**
	 *   calculates the dimensional stability derivative of force component Y with respect to "beta", 
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cyBeta lateral force coefficient with respect to beta (CyBeta) of the aircraft
	 * @return Y�beta dimensional derivative [m * s^(-2)]
	 */
	public static double calcY_beta (double rho0, double surf, double mass, double u0, double q0,
			double cyBeta) {
		
		return q0*surf*cyBeta/(mass);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of force component Y with respect to "p", 
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param bbar wingspan
	 * @param cyP lateral force coefficient with respect to p (CyP) of the aircraft
	 * @return Y�p dimensional derivative [m * s^(-1)]
	 */
	public static double calcY_p (double rho0, double surf, double mass, double u0, double q0,
			double bbar, double cyP) {
		
		return q0*surf*bbar*cyP/(2*u0*mass);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of force component Y with respect to "r",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param bbar wingspan
	 * @param cyR lateral force coefficient with respect to r (CyR) of the aircraft
	 * @return Y�r dimensional derivative [m * s^(-1)]
	 */
	public static double calcY_r (double rho0, double surf, double mass, double u0, double q0,
			double bbar, double cyR) {
		
		return q0*surf*bbar*cyR/(2*u0*mass);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of rolling moment L with respect to "beta",
	 *   divided for the lateral-directional moment of inertia I_xx
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iXX lateral-directional moment of inertia I_xx
	 * @param cLBeta rolling moment coefficient with respect to beta (CLBeta) of the aircraft
	 * @return L�beta dimensional derivative [s^(-2)]
	 */
	public static double calcL_beta (double rho0, double surf, double bbar, double iXX,
			double u0, double q0, double cLBeta) {
		
		return q0*surf*bbar*cLBeta/(iXX);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of rolling moment L with respect to "p",
	 *   divided for the lateral-directional moment of inertia I_xx
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iXX lateral-directional moment of inertia I_xx
	 * @param cLP rolling moment coefficient with respect to a p (CLP) of the aircraft
	 * @return L�p dimensional derivative [s^(-1)]
	 */
	public static double calcL_p (double rho0, double surf, double bbar, double iXX,
			double u0, double q0, double cLP) {
		
		return q0*surf*bbar*(bbar/(2*u0))*cLP/(iXX);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of rolling moment L with respect to "r",
	 *   divided for the lateral-directional moment of inertia I_xx
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iXX lateral-directional moment of inertia I_xx
	 * @param cLR rolling moment coefficient with respect to a r (CLR) of the aircraft
	 * @return L�r dimensional derivative [s^(-1)]
	 */
	public static double calcL_r (double rho0, double surf, double bbar, double iXX,
			double u0, double q0, double cLR) {
		
		return q0*surf*bbar*(bbar/(2*u0))*cLR/(iXX);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of yawing moment N with respect to "beta",
	 *   divided for the lateral-directional moment of inertia I_zz
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iZZ lateral-directional moment of inertia I_zz
	 * @param cNBeta yawing moment coefficient with respect to a beta (CNBeta) of the aircraft
	 * @return N�beta dimensional derivative [s^(-2)]
	 */
	public static double calcN_beta (double rho0, double surf, double bbar, double iZZ,
			double u0, double q0, double cNBeta) {
		
		return q0*surf*bbar*cNBeta/(iZZ);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of yawing moment N with respect to "p",
	 *   divided for the lateral-directional moment of inertia I_zz
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iZZ lateral-directional moment of inertia I_zz
	 * @param cNP yawing moment coefficient with respect to p (CNP) of the aircraft
	 * @return N�p dimensional derivative [s^(-1)]
	 */
	public static double calcN_p (double rho0, double surf, double bbar, double iZZ,
			double u0, double q0, double cNP) {
		
		return q0*surf*bbar*(bbar/(2*u0))*cNP/(iZZ);
		
	}
	
	/**
	 *   calculates the dimensional stability derivative of yawing moment N with respect to "r",
	 *   divided for the lateral-directional moment of inertia I_zz
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iZZ lateral-directional moment of inertia I_zz
	 * @param cNR yawing moment coefficient with respect to p (CNP) of the aircraft
	 * @return N�r dimensional derivative [s^(-1)]
	 */
	public static double calcN_r (double rho0, double surf, double bbar, double iZZ,
			double u0, double q0, double cNR) {
		
		return q0*surf*bbar*(bbar/(2*u0))*cNR/(iZZ);
		
	}
	
	// Lateral-Directional Dimensional Control Derivatives
	
	/**
	 *   calculates the dimensional control derivative of force component Y with respect to "delta_A",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cyDelta_A lateral force coefficient with respect to delta_A (CyDelta_A) of the aircraft
	 * @return Y�delta_A dimensional derivative [m * s^(-2)]
	 */
	public static double calcY_delta_A (double rho0, double surf, double mass, double u0, double q0,
			double cyDelta_A) {
		
		return q0*surf*cyDelta_A/(mass);
		
	}
	
	/**
	 *   calculates the dimensional control derivative of force component Y with respect to "delta_R",
	 *   divided by the mass
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param cyDelta_R lateral force coefficient with respect to delta_R (CyDelta_R) of the aircraft
	 * @return Y�delta_R dimensional derivative [m * s^(-2)]
	 */
	public static double calcY_delta_R (double rho0, double surf, double mass, double u0, double q0,
			double cyDelta_R) {
		
		return q0*surf*cyDelta_R/(mass);
		
	}
	
	/**
	 *   calculates the dimensional control derivative of rolling moment L with respect to "delta_A",
	 *   divided for the lateral-directional moment of inertia I_xx
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iXX lateral-directional moment of inertia I_xx
	 * @param cLDelta_A rolling moment coefficient with respect to a delta_A (CLDelta_A) of the aircraft
	 * @return L�delta_A dimensional derivative [s^(-2)]
	 */
	public static double calcL_delta_A (double rho0, double surf, double bbar, double iXX,
			double u0, double q0, double cLDelta_A) {
		
		return q0*surf*bbar*cLDelta_A/(iXX);
		
	}
	
	/**
	 *   calculates the dimensional control derivative of rolling moment L with respect to "delta_R",
	 *   divided for the lateral-directional moment of inertia I_xx
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iXX lateral-directional moment of inertia I_xx
	 * @param cLDelta_R rolling moment coefficient with respect to a delta_R (CLDelta_R) of the aircraft
	 * @return L�delta_R dimensional derivative [s^(-2)]
	 */
	public static double calcL_delta_R (double rho0, double surf, double bbar, double iXX,
			double u0, double q0, double cLDelta_R) {
		
		return q0*surf*bbar*cLDelta_R/(iXX);
		
	}
	
	/**
	 *   calculates the dimensional control derivative of yawing moment N with respect to "delta_A",
	 *   divided for the lateral-directional moment of inertia I_zz
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iZZ lateral-directional moment of inertia I_zz
	 * @param cNDelta_A yawing moment coefficient with respect to delta_A (CNDelta_A) of the aircraft
	 * @return N�delta_A dimensional derivative [s^(-2)]
	 */
	public static double calcN_delta_A (double rho0, double surf, double bbar, double iZZ,
			double u0, double q0, double cNDelta_A) {
		
		return q0*surf*bbar*cNDelta_A/(iZZ);
		
	}
	
	/**
	 *   calculates the dimensional control derivative of yawing moment N with respect to "delta_R",
	 *   divided for the lateral-directional moment of inertia I_zz
	 * @param rho0 air density
	 * @param surf wing area
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iZZ lateral-directional moment of inertia I_zz
	 * @param cNDelta_R yawing moment coefficient with respect to delta_R (CNDelta_R) of the aircraft
	 * @return N�delta_R dimensional derivative [s^(-2)]
	 */
	public static double calcN_delta_R (double rho0, double surf, double bbar, double iZZ,
			double u0, double q0, double cNDelta_R) {
		
		return q0*surf*bbar*cNDelta_R/(iZZ);
		
	}
	
	// Lateral-Directional Matrices
	
	/**
	 *   generates the lateral-directional coefficients matrix [A_LD] of linearized equations of dynamics
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param cbar mean aerodynamic chord
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param theta0_rad Euler angle [rad] (assuming gamma0 = theta0)
	 * @param iXX lateral-directional moment of inertia I_xx
	 * @param iZZ lateral-directional moment of inertia I_zz
	 * @param iXZ lateral-directional product of inertia I_xz
	 * @param cyBeta lateral force coefficient with respect to beta (CyBeta) of the aircraft
	 * @param cyP lateral force coefficient with respect to p (CyP) of the aircraft
	 * @param cyR lateral force coefficient with respect to r (CyR) of the aircraft
	 * @param cLBeta rolling moment coefficient with respect to beta (CLBeta) of the aircraft
	 * @param cLP rolling moment coefficient with respect to a p (CLP) of the aircraft
	 * @param cLR rolling moment coefficient with respect to a r (CLR) of the aircraft
	 * @param cNBeta yawing moment coefficient with respect to a beta (CNBeta) of the aircraft
	 * @param cNP yawing moment coefficient with respect to p (CNP) of the aircraft
	 * @param cNR yawing moment coefficient with respect to r (CNR) of the aircraft
	 * @return matrix [A_LD]
	 */
	public static double[][] build_A_LD_matrix (double rho0, double surf, double mass,
			double cbar, double bbar, double u0, double q0, double theta0_rad, double iXX, double iZZ,
			double iXZ,	double cyBeta, double cyP, double cyR, double cyDelta_A,
			double cyDelta_R, double cLBeta, double cLP, double cLR, double cLDelta_A,
			double cLDelta_R, double cNBeta, double cNP, double cNR, double cNDelta_A,
			double cNDelta_R) {
		
		double [][] aLD = new double [4][4];
		
		// Inertia coefficient calculation
		double i1 = iXZ/iXX;
		double i2 = iXZ/iZZ;
		
		// Primed Derivatives calculation
		double Y_beta_1 = calcY_beta (rho0, surf, mass, u0, q0, cyBeta);
		double Y_p_1 = calcY_p (rho0, surf, mass, u0, q0, bbar, cyP);
		double Y_r_1 = calcY_r (rho0, surf, mass, u0, q0, bbar, cyR);
		double L_beta_1 = (calcL_beta (rho0, surf, bbar, iXX, u0, q0, cLBeta) +
				i1*calcN_beta (rho0, surf, bbar, iZZ, u0, q0, cNBeta))/(1-i1*i2);
		double L_p_1 = (calcL_p (rho0, surf, bbar, iXX, u0, q0, cLP) +
				i1*calcN_p (rho0, surf, bbar, iZZ, u0, q0, cNP))/(1-i1*i2);
		double L_r_1 = (calcL_r (rho0, surf, bbar, iXX, u0, q0, cLR) +
				i1*calcN_r (rho0, surf, bbar, iZZ, u0, q0, cNR))/(1-i1*i2);
		double N_beta_1 = (i2*calcL_beta (rho0, surf, bbar, iXX, u0, q0, cLBeta) +
				calcN_beta (rho0, surf, bbar, iZZ, u0, q0, cNBeta))/(1-i1*i2);
		double N_p_1 = (i2*calcL_p (rho0, surf, bbar, iXX, u0, q0, cLP) +
				calcN_p (rho0, surf, bbar, iZZ, u0, q0, cNP))/(1-i1*i2);
		double N_r_1 = (i2*calcL_r (rho0, surf, bbar, iXX, u0, q0, cLR) +
				calcN_r (rho0, surf, bbar, iZZ, u0, q0, cNR))/(1-i1*i2);
		
		// Construction of the Matrix [A_LD]
		aLD [0][0] = N_r_1;
		
		aLD [0][1] = N_beta_1;
		
		aLD [0][2] = N_p_1;
		
		aLD [0][3] = 0;
		
		aLD [1][0] = Y_r_1/u0 - 1;
		
		aLD [1][1] = Y_beta_1/u0;
		
		aLD [1][2] = Y_p_1/u0;
		
		aLD [1][3] = (9.8100)*Math.cos(theta0_rad)/u0;
		
		aLD [2][0] = L_r_1;
		
		aLD [2][1] = L_beta_1;
		
		aLD [2][2] = L_p_1;
		
		aLD [2][3] = 0;
		
		aLD [3][0] = Math.sin(theta0_rad)/Math.cos(theta0_rad);
		
		aLD [3][1] = 0;
		
		aLD [3][2] = 1;
		
		aLD [3][3] = 0;	
		
		return aLD;
	}
	
	/**
	 *   generates the lateral-directional control coefficients matrix [B_LD] of linearized equations of dynamics
	 * @param rho0 air density
	 * @param surf wing area
	 * @param mass total mass
	 * @param cbar mean aerodynamic chord
	 * @param bbar wingspan
	 * @param u0 speed of the aircraft
	 * @param q0 dynamic pressure
	 * @param iXX lateral-directional moment of inertia I_xx
	 * @param iZZ lateral-directional moment of inertia I_zz
	 * @param iXZ lateral-directional product of inertia I_xz
	 * @param cyDelta_A lateral force coefficient with respect to delta_A (CyDelta_A) of the aircraft
	 * @param cyDelta_R lateral force coefficient with respect to delta_R (CyDelta_R) of the aircraft
	 * @param cLDelta_A rolling moment coefficient with respect to a delta_A (CLDelta_A) of the aircraft
	 * @param cLDelta_R rolling moment coefficient with respect to a delta_R (CLDelta_R) of the aircraft
	 * @param cNDelta_A yawing moment coefficient with respect to a delta_A (CLDelta_A) of the aircraft
	 * @param cNDelta_R yawing moment coefficient with respect to a delta_R (CLDelta_R) of the aircraft
	 * @return matrix [B_LD]
	 */
	public static double[][] build_B_LD_matrix (double rho0, double surf, double mass,
			double cbar, double bbar, double u0, double q0, double iXX, double iZZ, double iXZ,
			double cyDelta_A, double cyDelta_R, double cLDelta_A, double cLDelta_R,
			double cNDelta_A, double cNDelta_R) {
		
		double [][] bLD = new double [4][2];
		
		// Inertia coefficient calculation
		double i1 = iXZ/iXX;
		double i2 = iXZ/iZZ;
		
		//Primed Derivatives calculation
		double Y_delta_A_1 = calcY_delta_A (rho0, surf, mass, u0, q0, cyDelta_A);
		double Y_delta_R_1 = calcY_delta_R (rho0, surf, mass, u0, q0, cyDelta_R);
		double L_delta_A_1 = (calcL_delta_A (rho0, surf, bbar, iXX, u0, q0, cLDelta_A) +
				i1*calcN_delta_A (rho0, surf, bbar, iZZ, u0, q0, cNDelta_A))/(1-i1*i2);
		double L_delta_R_1 = (calcL_delta_R (rho0, surf, bbar, iXX, u0, q0, cLDelta_R) +
				i1*calcN_delta_R (rho0, surf, bbar, iZZ, u0, q0, cNDelta_R))/(1-i1*i2);
		double N_delta_A_1 = (i2*calcL_delta_A (rho0, surf, bbar, iXX, u0, q0, cLDelta_A) +
				calcN_delta_A (rho0, surf, bbar, iZZ, u0, q0, cNDelta_A))/(1-i1*i2);
		double N_delta_R_1 = (i2*calcL_delta_R (rho0, surf, bbar, iXX, u0, q0, cLDelta_R) +
				calcN_delta_R (rho0, surf, bbar, iZZ, u0, q0, cNDelta_R))/(1-i1*i2);
	
		// Construction of the Matrix [B_LD]
		bLD [0][0] = N_delta_A_1;
		
		bLD [0][1] = N_delta_R_1;
		
		bLD [1][0] = Y_delta_A_1/u0;
		
		bLD [1][1] = Y_delta_R_1/u0;
		
		bLD [2][0] = L_delta_A_1;
		
		bLD [2][1] = L_delta_R_1;
		
		bLD [3][0] = 0;
		
		bLD [3][1] = 0;
		
		return bLD;
	}
	
}
