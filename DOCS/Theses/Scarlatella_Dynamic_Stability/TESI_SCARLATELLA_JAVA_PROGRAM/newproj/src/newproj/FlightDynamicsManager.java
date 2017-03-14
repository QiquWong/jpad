package newproj;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

import org.apache.commons.math3.linear.RealVector;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import newproj.StabilityDerivativesCalc.Propulsion;


public class FlightDynamicsManager {

	Propulsion propulsion_system = Propulsion.CONSTANT_TRUST;  // propulsion regime type 

	double rho0;         						// air density
	double surf;								// wing area
	double mass; 								// total mass
	double cbar; 								// mean aerodynamic chord
	double bbar; 								// wingspan
	double u0; 								    // speed of the aircraft
	double q0;               				    // dynamic pressure
	double m0;               				    // Mach number
	double gamma0;          					// ramp angle
	double theta0_rad = Math.toRadians(gamma0); // Euler angle [rad] (assuming gamma0 = theta0)
	double iXX; 								// lateral-directional moment of inertia (IXX)
	double iYY; 								// longitudinal moment of inertia  (IYY)
	double iZZ; 								// lateral-directional moment of inertia (IZZ)
	double iXZ; 								// lateral-directional product of inertia (IXZ)
	double cd0; 								// drag coefficient at null incidence (Cdº) of the aircraft
	double cdAlpha0; 							// linear drag gradient (CdAlphaº) of the aircraft
	double cdM0; 								// drag coefficient with respect to Mach (CdMº) of the aircraft
	double cl0; 								// lift coefficient at null incidence (Clº) of the aircraft
	double clAlpha0; 							// linear lift gradient (ClAlphaº) of the aircraft
	double clAlpha_dot0; 						// linear lift gradient time derivative (ClAlpha_dotº) of the aircraft
	double clM0;								// lift coefficient with respect to Mach (ClMº) of the aircraft
	double clQ0; 								// lift coefficient with respect to q (ClQº) of the aircraft
	double clDelta_T; 							// lift coefficient with respect to delta_T (ClDelta_Tº) of the aircraft
	double clDelta_E; 							// lift coefficient with respect to delta_E (ClDelta_Eº) of the aircraft
	double cMAlpha0; 							// pitching moment coefficient with respect to Alpha (CmAlphaº) of the aircraft
	double cMAlpha_dot0; 						// pitching moment coefficient time derivative (CmAlpha_dotº) of the aircraft
	double cM_m0; 							    // pitching moment coefficient with respect to Mach number
	double cMq; 							    // pitching moment coefficient with respect to q
	double cMDelta_T; 							// pitching moment coefficient with respect to delta_T (CMDelta_Tº) of the aircraft
	double cMDelta_E;							// pitching moment coefficient with respect to delta_E (CMDelta_Eº) of the aircraft
	double cTfix; 							    // thrust coefficient at a fixed point ( U0 = u , delta_T = 1 )
	double kv; 							        // scale factor of the effect on the propulsion due to the speed
	double cyBeta; 							    // lateral force coefficient with respect to beta (CyBeta) of the aircraft
	double cyP; 							    // lateral force coefficient with respect to p (CyP) of the aircraft
	double cyR; 							    // lateral force coefficient with respect to r (CyR) of the aircraft
	double cyDelta_A; 							// lateral force coefficient with respect to delta_A (CyDelta_A) of the aircraft
	double cyDelta_R; 							// lateral force coefficient with respect to delta_R (CyDelta_R) of the aircraft
	double cLBeta; 							    // rolling moment coefficient with respect to beta (CLBeta) of the aircraft
	double cLP; 							    // rolling moment coefficient with respect to a p (CLP) of the aircraft
	double cLR; 							    // rolling moment coefficient with respect to a r (CLR) of the aircraft
	double cLDelta_A; 							// rolling moment coefficient with respect to a delta_A (CLDelta_A) of the aircraft
	double cLDelta_R; 							// rolling moment coefficient with respect to a delta_R (CLDelta_R) of the aircraft
	double cNBeta; 							    // yawing moment coefficient with respect to a beta (CNBeta) of the aircraft
	double cNP; 							    // yawing moment coefficient with respect to p (CNP) of the aircraft
	double cNR; 							    // yawing moment coefficient with respect to r (CNR) of the aircraft
	double cNDelta_A; 						    // yawing moment coefficient with respect to delta_A (CNDelta_A) of the aircraft
	double cNDelta_R; 					        // yawing moment coefficient with respect to delta_R (CNDelta_R) of the aircraft

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

	
	
	public FlightDynamicsManager() {
		
	}

	
	
	public void calculateAll() {
		// Formats numbers up to 4 decimal places
		DecimalFormat df = new DecimalFormat("#,###,##0.0000");

		////////////////////          LONGITUDINAL DYNAMICS          \\\\\\\\\\\\\\\\\\\\

		// Calculates the longitudinal Stability and Control DERIVATIVES \\
		x_u_CT  = StabilityDerivativesCalc.calcX_u_CT(rho0, surf, mass, u0, q0, cd0, m0, cdM0);
		x_u_CP  = StabilityDerivativesCalc.calcX_u_CP(rho0, surf, mass, u0, q0, cd0, cM_m0, cdM0, cl0, gamma0);
		x_w     = StabilityDerivativesCalc.calcX_w(rho0, surf, mass, u0, q0, cl0, cdAlpha0);
		x_w_dot = 0;
		x_q     = 0;
		z_u     = StabilityDerivativesCalc.calcZ_u(rho0, surf, mass, u0, q0, cM_m0, cl0, clM0);
		z_w     = StabilityDerivativesCalc.calcZ_w(rho0, surf, mass, u0, q0, cM_m0, cd0, clAlpha0);
		z_w_dot = StabilityDerivativesCalc.calcZ_w_dot(rho0, surf, mass, cbar, clAlpha_dot0);
		z_q     = StabilityDerivativesCalc.calcZ_q(rho0, surf, mass, u0, q0, cbar, clQ0);
		m_u     = StabilityDerivativesCalc.calcM_u(rho0, surf, mass, u0, q0, cbar, cdM0, iYY, cM_m0);
		m_w     = StabilityDerivativesCalc.calcM_w(rho0, surf, mass, u0, q0, cbar, iYY, cMAlpha0);
		m_w_dot = StabilityDerivativesCalc.calcM_w_dot(rho0, surf, cbar, iYY, cMAlpha_dot0);
		m_q     = StabilityDerivativesCalc.calcM_q(rho0, mass, u0, q0, surf, cbar, iYY, cMq);

		x_delta_T_CT  = StabilityDerivativesCalc.calcX_delta_T_CT (rho0, surf, mass, u0, q0, cTfix, kv);
		x_delta_T_CP  = StabilityDerivativesCalc.calcX_delta_T_CP (rho0, surf, mass, u0, q0, cTfix, kv);
		x_delta_T_CMF = StabilityDerivativesCalc.calcX_delta_T_CMF (rho0, surf, mass, u0, q0, cTfix, kv);
		x_delta_T_RJ  = StabilityDerivativesCalc.calcX_delta_T_RJ (rho0, surf, mass, u0, q0, cTfix, kv);
		x_delta_T_CT  = StabilityDerivativesCalc.calcX_delta_T_CT (rho0, surf, mass, u0, q0, cTfix, kv);
		x_delta_E     = 0;
		z_delta_T     = StabilityDerivativesCalc.calcZ_delta_T (rho0, surf, mass, u0, q0, clDelta_T);
		z_delta_E     = StabilityDerivativesCalc.calcZ_delta_E (rho0, surf, mass, u0, q0, clDelta_E);
		m_delta_T     = StabilityDerivativesCalc.calcM_delta_T (rho0, surf, cbar, u0, q0, iYY, cMDelta_T);
		m_delta_E     = StabilityDerivativesCalc.calcM_delta_E (rho0, surf, cbar, u0, q0, iYY, cMDelta_E);

		// Prints out the LONGITUDINAL STABILITY AND CONTROL DERIVATIVES LIST \\
		System.out.println("_________________________________________________________________\n");
		System.out.println("LONGITUDINAL STABILITY DERIVATIVES: \n");
		System.out.println(" Xªu_CT  = " + df.format(x_u_CT));
		System.out.println(" Xªu_CP  = " + df.format(x_u_CP));
		System.out.println(" Xªw     = " + df.format(x_w));
		System.out.println(" Xªw_dot = " + df.format(x_w_dot));
		System.out.println(" Xªq     = " + df.format(x_q));
		System.out.println(" Zªu     = " + df.format(z_u));
		System.out.println(" Zªw     = " + df.format(z_w));
		System.out.println(" Zªw_dot = " + df.format(z_w_dot));
		System.out.println(" Zªq     = " + df.format(z_q));
		System.out.println(" Mªu     = " + df.format(m_u));
		System.out.println(" Mªw     = " + df.format(m_w));
		System.out.println(" Mªw_dot = " + df.format(m_w_dot));
		System.out.println(" Mªq     = " + df.format(m_q));
		System.out.println("\n\nLONGITUDINAL CONTROL DERIVATIVES: \n");
		System.out.println(" Xªdelta_T_CT  = " + df.format(x_delta_T_CT));
		System.out.println(" Xªdelta_T_CP  = " + df.format(x_delta_T_CP));
		System.out.println(" Xªdelta_T_CMF = " + df.format(x_delta_T_CMF));
		System.out.println(" Xªdelta_T_RJ  = " + df.format(x_delta_T_RJ));
		System.out.println(" Xªdelta_E     = " + df.format(x_delta_E));
		System.out.println(" Zªdelta_T     = " + df.format(z_delta_T));
		System.out.println(" Zªdelta_E     = " + df.format(z_delta_E));
		System.out.println(" Mªdelta_T     = " + df.format(m_delta_T));
		System.out.println(" Mªdelta_E     = " + df.format(m_delta_E)+"\n");

		// Generates and prints out the [A_Lon] and [B_Lon] MATRICES \\
		System.out.println("_________________________________________________________________\n");
		System.out.println("MATRIX [A_LON]: \n");

		aLon = StabilityDerivativesCalc.build_A_Lon_matrix (propulsion_system, rho0, surf, mass, cbar, u0, q0, cd0, m0, cdM0, cl0,
				clM0, cdAlpha0, gamma0, theta0_rad,clAlpha0, clAlpha_dot0, cMAlpha0, cMAlpha_dot0, clQ0, iYY, cM_m0, cMq);
		
		System.out.println(df.format(aLon[0][0])+"\t\t"+df.format(aLon[0][1])+"\t\t"+df.format(aLon[0][2])+"\t\t"+df.format(aLon[0][3])+"\n");
		System.out.println(df.format(aLon[1][0])+"\t\t"+df.format(aLon[1][1])+"\t\t"+df.format(aLon[1][2])+"\t\t"+df.format(aLon [1][3])+"\n");
		System.out.println(df.format(aLon[2][0])+"\t\t"+df.format(aLon[2][1])+"\t\t"+df.format(aLon[2][2])+"\t\t"+df.format(aLon [2][3])+"\n");
		System.out.println(aLon[3][0]+"\t\t"+aLon[3][1]+"\t\t"+aLon[3][2]+"\t\t"+aLon [3][3]+"\n");

		System.out.println("_________________________________________________________________\n");
		System.out.println("MATRIX [B_LON]: \n");

		bLon = StabilityDerivativesCalc.build_B_Lon_matrix (propulsion_system, rho0, surf, mass, cbar, u0, q0, cd0, m0, cdM0, cl0,
				cdAlpha0, gamma0, theta0_rad, clAlpha0, clAlpha_dot0, cMAlpha0, cMAlpha_dot0, clQ0, iYY, cM_m0, cMq, cTfix,
				kv, clDelta_T, clDelta_E, cMDelta_T, cMDelta_E);

		System.out.println(df.format(bLon[0][0])+"\t\t"+df.format(bLon[0][1])+"\n");
		System.out.println(df.format(bLon[1][0])+"\t\t"+df.format(bLon[1][1])+"\n");
		System.out.println(df.format(bLon[2][0])+"\t\t"+df.format(bLon[2][1])+"\n");
		System.out.println(bLon[3][0]+"\t\t"+bLon[3][1]+"\n");

		// Generates and prints out the Eigenvalues of [A_Lon] matrix \\
		lonEigenvaluesMatrix = DynamicStabilityCalculator.buildEigenValuesMatrix(aLon);

		System.out.println("_________________________________________________________________\n");
		System.out.println("LONGITUDINAL EIGENVALUES\n");
		System.out.println("  SHORT PERIOD: "+df.format(lonEigenvaluesMatrix[0][0])+" ± j"+df.format(lonEigenvaluesMatrix[0][1])+"\n");
		System.out.println("  PHUGOID:      "+df.format(lonEigenvaluesMatrix[2][0])+" ± j"+df.format(lonEigenvaluesMatrix[2][1])+"\n");

		// Generates and prints out the EigenVectors of [A_Lon] matrix \\
		System.out.println("_________________________________________________________________\n");
		System.out.println("LONGITUDINAL EIGENVECTORS:\n");
		
		eigLonVec1 = DynamicStabilityCalculator.buildEigenVector(aLon, 0);
		eigLonVec2 = DynamicStabilityCalculator.buildEigenVector(aLon, 1);
		eigLonVec3 = DynamicStabilityCalculator.buildEigenVector(aLon, 2);
		eigLonVec4 = DynamicStabilityCalculator.buildEigenVector(aLon, 3);
		
		System.out.println("EigenVector 1 = " + eigLonVec1);
		System.out.println("EigenVector 2 = " + eigLonVec2);
		System.out.println("EigenVector 3 = " + eigLonVec3);
		System.out.println("EigenVector 4 = " + eigLonVec4+"\n");
		
		// Generates and prints out all the characteristics for longitudinal SHORT PERIOD and PHUGOID modes \\
		zeta_SP = DynamicStabilityCalculator.calcZeta(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
		zeta_PH = DynamicStabilityCalculator.calcZeta(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);
		omega_n_SP = DynamicStabilityCalculator.calcOmega_n(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
		omega_n_PH = DynamicStabilityCalculator.calcOmega_n(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);
		period_SP = DynamicStabilityCalculator.calcT(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
		period_PH = DynamicStabilityCalculator.calcT(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);
		t_half_SP = DynamicStabilityCalculator.calct_half(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
		t_half_PH = DynamicStabilityCalculator.calct_half(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);
		N_half_SP = DynamicStabilityCalculator.calcN_half(lonEigenvaluesMatrix[0][0],lonEigenvaluesMatrix[0][1]);
		N_half_PH = DynamicStabilityCalculator.calcN_half(lonEigenvaluesMatrix[2][0],lonEigenvaluesMatrix[2][1]);

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
		y_beta = StabilityDerivativesCalc.calcY_beta (rho0, surf, mass, u0, q0, cyBeta);
		y_p    = StabilityDerivativesCalc.calcY_p (rho0, surf, mass, u0, q0, bbar, cyP);
		y_r    = StabilityDerivativesCalc.calcY_r (rho0, surf, mass, u0, q0, bbar, cyR);
		l_beta = StabilityDerivativesCalc.calcL_beta (rho0, surf, bbar, iXX, u0, q0, cLBeta);
		l_p    = StabilityDerivativesCalc.calcL_p (rho0, surf, bbar, iXX, u0, q0, cLP);
		l_r    = StabilityDerivativesCalc.calcL_r (rho0, surf, bbar, iXX, u0, q0, cLR);
		n_beta = StabilityDerivativesCalc.calcN_beta (rho0, surf, bbar, iZZ, u0, q0, cNBeta);
		n_p    = StabilityDerivativesCalc.calcN_p (rho0, surf, bbar, iZZ, u0, q0, cNP);
		n_r    = StabilityDerivativesCalc.calcN_r (rho0, surf, bbar, iZZ, u0, q0, cNR);
		y_delta_A = StabilityDerivativesCalc.calcY_delta_A (rho0, surf, mass, u0, q0, cyDelta_A);
		y_delta_R = StabilityDerivativesCalc.calcY_delta_R (rho0, surf, mass, u0, q0, cyDelta_R);
		l_delta_A = StabilityDerivativesCalc.calcL_delta_A (rho0, surf, bbar, iXX, u0, q0, cLDelta_A);
		l_delta_R = StabilityDerivativesCalc.calcL_delta_R (rho0, surf, bbar, iXX, u0, q0, cLDelta_R);
		n_delta_A = StabilityDerivativesCalc.calcNªdelta_A (rho0, surf, bbar, iZZ, u0, q0, cNDelta_A);
		n_delta_R = StabilityDerivativesCalc.calcN_delta_R (rho0, surf, bbar, iZZ, u0, q0, cNDelta_R);

		// Prints out the LATERAL-DIRECTIONAL STABILITY AND CONTROL DERIVATIVES LIST \\
		System.out.println("_________________________________________________________________\n");
		System.out.println("LATERAL-DIRECTIONAL STABILITY DERIVATIVES: \n");
		System.out.println(" Yªbeta = " + df.format(y_beta));
		System.out.println(" Yªp    = " + df.format(y_p));
		System.out.println(" Yªr    = " + df.format(y_r));
		System.out.println(" Lªbeta = " + df.format(l_beta));
		System.out.println(" Lªp    = " + df.format(l_p));
		System.out.println(" Lªr    = " + df.format(l_r));
		System.out.println(" Nªbeta = " + df.format(n_beta));
		System.out.println(" Nªp    = " + df.format(n_p));
		System.out.println(" Nªr    = " + df.format(n_r));
		System.out.println("\n\nLATERAL-DIRECTIONAL CONTROL DERIVATIVES: \n");
		System.out.println(" Yªdelta_A = " + df.format(y_delta_A));
		System.out.println(" Yªdelta_R = " + df.format(y_delta_R));
		System.out.println(" Lªdelta_A = " + df.format(l_delta_A));
		System.out.println(" Lªdelta_R = " + df.format(l_delta_R));
		System.out.println(" Nªdelta_A = " + df.format(n_delta_A));
		System.out.println(" Nªdelta_R = " + df.format(n_delta_R)+"\n");

		// Generates and prints out the [A_Ld] and [B_Ld] MATRICES \\
		System.out.println("_________________________________________________________________\n");
		System.out.println("MATRIX [A_LD]: \n");

		aLD = StabilityDerivativesCalc.build_A_LD_matrix (rho0, surf, mass, cbar, bbar, u0, q0, 
				theta0_rad, iXX, iZZ, iXZ, cyBeta, cyP, cyR, cyDelta_A, cyDelta_R, cLBeta,
				cLP, cLR, cLDelta_A, cLDelta_R, cNBeta, cNP, cNR, cNDelta_A, cNDelta_R);

		System.out.println(df.format(aLD[0][0])+"\t\t"+df.format(aLD[0][1])+"\t\t"+df.format(aLD[0][2])+"\t\t"+df.format(aLD[0][3])+"\n");
		System.out.println(df.format(aLD[1][0])+"\t\t"+df.format(aLD[1][1])+"\t\t"+df.format(aLD[1][2])+"\t\t"+df.format(aLD [1][3])+"\n");
		System.out.println(df.format(aLD[2][0])+"\t\t"+df.format(aLD[2][1])+"\t\t"+df.format(aLD[2][2])+"\t\t"+df.format(aLD [2][3])+"\n");
		System.out.println(aLD[3][0]+"\t\t"+aLD[3][1]+"\t\t"+aLD[3][2]+"\t\t"+aLD [3][3]+"\n");

		System.out.println("_________________________________________________________________\n");
		System.out.println("MATRIX [B_LD]: \n");

		bLD = StabilityDerivativesCalc.build_B_LD_matrix (rho0, surf, mass, cbar, bbar, u0, q0, 
				iXX, iZZ, iXZ, cyDelta_A, cyDelta_R, cLDelta_A, cLDelta_R, cNDelta_A, cNDelta_R);

		System.out.println(df.format(bLD[0][0])+"\t\t"+df.format(bLD[0][1])+"\n");
		System.out.println(df.format(bLD[1][0])+"\t\t"+df.format(bLD[1][1])+"\n");
		System.out.println(df.format(bLD[2][0])+"\t\t"+df.format(bLD[2][1])+"\n");
		System.out.println(bLD[3][0]+"\t\t"+bLD[3][1]+"\n");

		// Generates and prints out the Eigenvalues of [A_Ld] matrix \\
		ldEigenvaluesMatrix = DynamicStabilityCalculator.buildEigenValuesMatrix(aLD);

		System.out.println("_________________________________________________________________\n");
		System.out.println("LATERAL-DIRECTIONAL EIGENVALUES\n");
		System.out.println("  ROLL:       "+df.format(ldEigenvaluesMatrix[2][0])+"\n");
		System.out.println("  DUTCH-ROLL: "+df.format(ldEigenvaluesMatrix[0][0])+" ± j"+df.format(ldEigenvaluesMatrix[0][1])+"\n");
		System.out.println("  SPIRAL:     "+df.format(ldEigenvaluesMatrix[3][0])+"\n");
		
		// Generates and prints out the EigenVectors of [A_Ld] matrix \\
		System.out.println("_________________________________________________________________\n");
		System.out.println("LATERAL-DIRECTIONAL EIGENVECTORS:\n");

		eigLDVec1 = DynamicStabilityCalculator.buildEigenVector(aLD, 0);
		eigLDVec2 = DynamicStabilityCalculator.buildEigenVector(aLD, 1);
		eigLDVec3 = DynamicStabilityCalculator.buildEigenVector(aLD, 2);
		eigLDVec4 = DynamicStabilityCalculator.buildEigenVector(aLD, 3);

		System.out.println("EigenVector 1 = " + eigLDVec1);
		System.out.println("EigenVector 2 = " + eigLDVec2);
		System.out.println("EigenVector 3 = " + eigLDVec3);
		System.out.println("EigenVector 4 = " + eigLDVec4+"\n");
		
		// Generates and prints out all the characteristics for lateral-directional DUTCH-ROLL mode \\
		zeta_DR = DynamicStabilityCalculator.calcZeta(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);
		omega_n_DR = DynamicStabilityCalculator.calcOmega_n(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);
		period_DR = DynamicStabilityCalculator.calcT(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);
		t_half_DR = DynamicStabilityCalculator.calct_half(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);
		N_half_DR = DynamicStabilityCalculator.calcN_half(ldEigenvaluesMatrix[0][0],ldEigenvaluesMatrix[0][1]);

		System.out.println("_________________________________________________________________\n");
		System.out.println("DUTCH-ROLL MODE CHARACTERISTICS\n");
		System.out.println("Zeta_DR                          = "+df.format(zeta_DR)+"\n");
		System.out.println("Omega_n_DR                       = "+df.format(omega_n_DR)+"\n");
		System.out.println("Period                           = "+df.format(period_DR)+"\n");
		System.out.println("Halving Time                     = "+df.format(t_half_DR)+"\n");
		System.out.println("Number of cycles to Halving Time = "+df.format(N_half_DR)+"\n\n");

	}


	public void readDataFromExcelFile(File excelFile, int sheetNum) {

		// Formats numbers up to 4 decimal places
		DecimalFormat df = new DecimalFormat("#,###,##0.0000");

		try {
			System.out.println("Input file: " + excelFile.getAbsolutePath());
			FileInputStream fis = new FileInputStream(excelFile);
			Workbook wb = WorkbookFactory.create(fis);
			Sheet ws = wb.getSheetAt(sheetNum);
			int rowNum = ws.getLastRowNum() + 1;
			System.out.println("rows number: " + rowNum);

			if(sheetNum == 0){
				System.out.println("---------------------------------------------------\n");
				System.out.println("\n\n BOEING 747 /// Flight Condition (2) ");
				System.out.println("_________________________________________________________________\n");
				System.out.println("DATA LIST: \n");
			}
			else if (sheetNum == 1){
				System.out.println("---------------------------------------------------\n");
				System.out.println("\n\n BOEING 747 /// Flight Condition (5) ");
				System.out.println("_________________________________________________________________\n");
				System.out.println("DATA LIST: \n");
			}

			for (int i = 0 ; i < rowNum ; i++) {
				Row row = ws.getRow(i);
				int colNum = ws.getRow(0).getLastCellNum();
				for (int j = 0 ; j < colNum-2 ; j++) {

					Cell cell = row.getCell(j);
					String value = cellToString(cell);
					switch (sheetNum){
					///////////// 1st sheet /////////////
					case 0:
						if ((i == 1) && (j == 1)) {
							propulsion_system = Propulsion.valueOf(value);
							switch (propulsion_system)
							{
							case CONSTANT_TRUST:
								System.out.println(" PROPULSION SYSTEM: CONSTANT TRUST \n");
								break;
							case CONSTANT_POWER:
								System.out.println(" PROPULSION SYSTEM: CONSTANT POWER \n");
								break;
							case CONSTANT_MASS_FLOW:
								System.out.println(" PROPULSION SYSTEM: CONSTANT MASS FLOW \n");
								break;
							case RAMJET:
								System.out.println(" PROPULSION SYSTEM: RAMJET \n");
								break;
							default:
								System.out.println(" PROPULSION SYSTEM: CONSTANT TRUST \n");
								break;
							}
						}

						if ((i == 2) && (j == 1)) {
							rho0 = Double.parseDouble(value);
							System.out.println(" rho0         = " + rho0);
						}

						if ((i == 3) && (j == 1)) {
							surf = Double.parseDouble(value);
							System.out.println(" surf         = " + surf);
						}

						if ((i == 4) && (j == 1)) {
							mass = Double.parseDouble(value);
							System.out.println(" mass         = " + mass);
						}

						if ((i == 5) && (j == 1)) {
							cbar = Double.parseDouble(value);
							System.out.println(" cbar         = " + cbar);
						}

						if ((i == 6) && (j == 1)) {
							bbar = Double.parseDouble(value);
							System.out.println(" bbar         = " + bbar  );
						}

						if ((i == 7) && (j == 1)) {
							u0 = Double.parseDouble(value);
							System.out.println(" u0           = " + u0);
							q0 = StabilityDerivativesCalc.calcDynamicPressure(rho0, u0);
							System.out.println(" q0           = " + df.format(q0));
						}


						if ((i == 8) && (j == 1)) {
							m0 = Double.parseDouble(value);
							System.out.println(" m0           = " + m0);
						}

						if ((i == 9) && (j == 1)) {
							gamma0 = Double.parseDouble(value);
							System.out.println(" gamma0       = " + gamma0);
						}

						if ((i == 10) && (j == 1)) {
							theta0_rad  = Double.parseDouble(value);
							System.out.println(" theta0_rad   = " + theta0_rad );
						}

						if ((i == 11) && (j == 1)) {
							iXX = Double.parseDouble(value);
							System.out.println(" iXX          = " + iXX);
						}

						if ((i == 12) && (j == 1)) {
							iYY = Double.parseDouble(value);
							System.out.println(" iYY          = " + iYY);
						}

						if ((i == 13) && (j == 1)) {
							iZZ = Double.parseDouble(value);
							System.out.println(" iZZ          = " + iZZ);
						}

						if ((i == 14) && (j == 1)) {
							iXZ = Double.parseDouble(value);
							System.out.println(" iXZ          = " + iXZ);
						}

						if ((i == 15) && (j == 1)) {
							cd0 = Double.parseDouble(value);
							System.out.println(" cd0          = " + cd0);
						}

						if ((i == 16) && (j == 1)) {
							cdAlpha0  = Double.parseDouble(value);
							System.out.println(" cdAlpha0     = " + cdAlpha0 );
						}

						if ((i == 17) && (j == 1)) {
							cdM0  = Double.parseDouble(value);
							System.out.println(" cdM0         = " + cdM0 );
						}

						if ((i == 18) && (j == 1)) {
							cl0 = Double.parseDouble(value);
							System.out.println(" cl0          = " + cl0);
						}

						if ((i == 19) && (j == 1)) {
							clAlpha0 = Double.parseDouble(value);
							System.out.println(" clAlpha0     = " + clAlpha0);
						}

						if ((i == 20) && (j == 1)) {
							clAlpha_dot0 = Double.parseDouble(value);
							System.out.println(" clAlpha_dot0 = " + clAlpha_dot0);
						}

						if ((i == 21) && (j == 1)) {
							clM0 = Double.parseDouble(value);
							System.out.println(" clM0         = " + clM0);
						}
						
						if ((i == 22) && (j == 1)) {
							clQ0 = Double.parseDouble(value);
							System.out.println(" clQ0         = " + clQ0);
						}

						if ((i == 23) && (j == 1)) {
							clDelta_T = Double.parseDouble(value);
							System.out.println(" clDelta_T    = " + clDelta_T);
						}

						if ((i == 24) && (j == 1)) {
							clDelta_E = Double.parseDouble(value);
							System.out.println(" clDelta_E    = " + clDelta_E);
						}

						if ((i == 25) && (j == 1)) {
							cMAlpha0 = Double.parseDouble(value);
							System.out.println(" cMAlpha0     = " + cMAlpha0);
						}

						if ((i == 26) && (j == 1)) {
							cMAlpha_dot0 = Double.parseDouble(value);
							System.out.println(" cMAlpha_dot0 = " + cMAlpha_dot0);
						}

						if ((i == 27) && (j == 1)) {
							cM_m0 = Double.parseDouble(value);
							System.out.println(" cM_m0        = " + cM_m0);
						}

						if ((i == 28) && (j == 1)) {
							cMq = Double.parseDouble(value);
							System.out.println(" cMq          = " + cMq);
						}

						if ((i == 29) && (j == 1)) {
							cMDelta_T = Double.parseDouble(value);
							System.out.println(" cMDelta_T    = " + cMDelta_T);
						}

						if ((i == 30) && (j == 1)) {
							cMDelta_E = Double.parseDouble(value);
							System.out.println(" cMDelta_E    = " + cMDelta_E);
						}

						if ((i == 31) && (j == 1)) {
							cTfix = Double.parseDouble(value);
							System.out.println(" cTfix        = " + cTfix);
						}

						if ((i == 32) && (j == 1)) {
							kv = Double.parseDouble(value);
							System.out.println(" kv           = " + kv);
						}

						if ((i == 33) && (j == 1)) {
							cyBeta = Double.parseDouble(value);
							System.out.println(" cyBeta       = " + cyBeta);
						}

						if ((i == 34) && (j == 1)) {
							cyP = Double.parseDouble(value);
							System.out.println(" cyP          = " + cyP);
						}

						if ((i == 35) && (j == 1)) {
							cyR = Double.parseDouble(value);
							System.out.println(" cyR          = " + cyR);
						}

						if ((i == 36) && (j == 1)) {
							cyDelta_A = Double.parseDouble(value);
							System.out.println(" cyDelta_A    = " + cyDelta_A);
						}

						if ((i == 37) && (j == 1)) {
							cyDelta_R = Double.parseDouble(value);
							System.out.println(" cyDelta_R    = " + cyDelta_R);
						}

						if ((i == 38) && (j == 1)) {
							cLBeta = Double.parseDouble(value);
							System.out.println(" cLBeta       = " + cLBeta);
						}

						if ((i == 39) && (j == 1)) {
							cLP = Double.parseDouble(value);
							System.out.println(" cLP          = " + cLP);
						}

						if ((i == 40) && (j == 1)) {
							cLR = Double.parseDouble(value);
							System.out.println(" cLR          = " + cLR);
						}

						if ((i == 41) && (j == 1)) {
							cLDelta_A = Double.parseDouble(value);
							System.out.println(" cLDelta_A    = " + cLDelta_A);
						}

						if ((i == 42) && (j == 1)) {
							cLDelta_R = Double.parseDouble(value);
							System.out.println(" cLDelta_R    = " + cLDelta_R);
						}

						if ((i == 43) && (j == 1)) {
							cNBeta = Double.parseDouble(value);
							System.out.println(" cNBeta       = " + cNBeta);
						}

						if ((i == 44) && (j == 1)) {
							cNP = Double.parseDouble(value);
							System.out.println(" cNP          = " + cNP);
						}

						if ((i == 45) && (j == 1)) {
							cNR = Double.parseDouble(value);
							System.out.println(" cNR          = " + cNR);
						}

						if ((i == 46) && (j == 1)) {
							cNDelta_A = Double.parseDouble(value);
							System.out.println(" cNDelta_A    = " + cNDelta_A);
						}

						if ((i == 47) && (j == 1)) {
							cNDelta_R = Double.parseDouble(value);
							System.out.println(" cNDelta_R    = " + cNDelta_R);
						}

						break;

						///////////// 2nd sheet /////////////
					case 1:	
						if ((i == 1) && (j == 1)) {
							propulsion_system = Propulsion.valueOf(value);
							switch (propulsion_system)
							{
							case CONSTANT_TRUST:
								System.out.println(" PROPULSION SYSTEM: CONSTANT TRUST \n");
								break;
							case CONSTANT_POWER:
								System.out.println(" PROPULSION SYSTEM: CONSTANT POWER \n");
								break;
							case CONSTANT_MASS_FLOW:
								System.out.println(" PROPULSION SYSTEM: CONSTANT MASS FLOW \n");
								break;
							case RAMJET:
								System.out.println(" PROPULSION SYSTEM: RAMJET \n");
								break;
							default:
								System.out.println(" PROPULSION SYSTEM: CONSTANT TRUST \n");
								break;
							}
						}

						if ((i == 2) && (j == 1)) {
							rho0 = Double.parseDouble(value);
							System.out.println(" rho0         = " + rho0);
						}

						if ((i == 3) && (j == 1)) {
							surf = Double.parseDouble(value);
							System.out.println(" surf         = " + surf);
						}

						if ((i == 4) && (j == 1)) {
							mass = Double.parseDouble(value);
							System.out.println(" mass         = " + mass);
						}

						if ((i == 5) && (j == 1)) {
							cbar = Double.parseDouble(value);
							System.out.println(" cbar         = " + cbar);
						}

						if ((i == 6) && (j == 1)) {
							bbar = Double.parseDouble(value);
							System.out.println(" bbar         = " + bbar  );
						}

						if ((i == 7) && (j == 1)) {
							u0 = Double.parseDouble(value);
							System.out.println(" u0           = " + u0);
							q0 = StabilityDerivativesCalc.calcDynamicPressure(rho0, u0);
							System.out.println(" q0           = " + df.format(q0));
						}


						if ((i == 8) && (j == 1)) {
							m0 = Double.parseDouble(value);
							System.out.println(" m0           = " + m0);
						}

						if ((i == 9) && (j == 1)) {
							gamma0 = Double.parseDouble(value);
							System.out.println(" gamma0       = " + gamma0);
						}

						if ((i == 10) && (j == 1)) {
							theta0_rad  = Double.parseDouble(value);
							System.out.println(" theta0_rad   = " + theta0_rad );
						}

						if ((i == 11) && (j == 1)) {
							iXX = Double.parseDouble(value);
							System.out.println(" iXX          = " + iXX);
						}

						if ((i == 12) && (j == 1)) {
							iYY = Double.parseDouble(value);
							System.out.println(" iYY          = " + iYY);
						}

						if ((i == 13) && (j == 1)) {
							iZZ = Double.parseDouble(value);
							System.out.println(" iZZ          = " + iZZ);
						}

						if ((i == 14) && (j == 1)) {
							iXZ = Double.parseDouble(value);
							System.out.println(" iXZ          = " + iXZ);
						}

						if ((i == 15) && (j == 1)) {
							cd0 = Double.parseDouble(value);
							System.out.println(" cd0          = " + cd0);
						}

						if ((i == 16) && (j == 1)) {
							cdAlpha0  = Double.parseDouble(value);
							System.out.println(" cdAlpha0     = " + cdAlpha0 );
						}

						if ((i == 17) && (j == 1)) {
							cdM0  = Double.parseDouble(value);
							System.out.println(" cdM0         = " + cdM0 );
						}

						if ((i == 18) && (j == 1)) {
							cl0 = Double.parseDouble(value);
							System.out.println(" cl0          = " + cl0);
						}

						if ((i == 19) && (j == 1)) {
							clAlpha0 = Double.parseDouble(value);
							System.out.println(" clAlpha0     = " + clAlpha0);
						}

						if ((i == 20) && (j == 1)) {
							clAlpha_dot0 = Double.parseDouble(value);
							System.out.println(" clAlpha_dot0 = " + clAlpha_dot0);
						}

						if ((i == 21) && (j == 1)) {
							clM0 = Double.parseDouble(value);
							System.out.println(" clM0         = " + clM0);
						}
						
						if ((i == 22) && (j == 1)) {
							clQ0 = Double.parseDouble(value);
							System.out.println(" clQ0         = " + clQ0);
						}

						if ((i == 23) && (j == 1)) {
							clDelta_T = Double.parseDouble(value);
							System.out.println(" clDelta_T    = " + clDelta_T);
						}

						if ((i == 24) && (j == 1)) {
							clDelta_E = Double.parseDouble(value);
							System.out.println(" clDelta_E    = " + clDelta_E);
						}

						if ((i == 25) && (j == 1)) {
							cMAlpha0 = Double.parseDouble(value);
							System.out.println(" cMAlpha0     = " + cMAlpha0);
						}

						if ((i == 26) && (j == 1)) {
							cMAlpha_dot0 = Double.parseDouble(value);
							System.out.println(" cMAlpha_dot0 = " + cMAlpha_dot0);
						}

						if ((i == 27) && (j == 1)) {
							cM_m0 = Double.parseDouble(value);
							System.out.println(" cM_m0        = " + cM_m0);
						}

						if ((i == 28) && (j == 1)) {
							cMq = Double.parseDouble(value);
							System.out.println(" cMq          = " + cMq);
						}

						if ((i == 29) && (j == 1)) {
							cMDelta_T = Double.parseDouble(value);
							System.out.println(" cMDelta_T    = " + cMDelta_T);
						}

						if ((i == 30) && (j == 1)) {
							cMDelta_E = Double.parseDouble(value);
							System.out.println(" cMDelta_E    = " + cMDelta_E);
						}

						if ((i == 31) && (j == 1)) {
							cTfix = Double.parseDouble(value);
							System.out.println(" cTfix        = " + cTfix);
						}

						if ((i == 32) && (j == 1)) {
							kv = Double.parseDouble(value);
							System.out.println(" kv           = " + kv);
						}

						if ((i == 33) && (j == 1)) {
							cyBeta = Double.parseDouble(value);
							System.out.println(" cyBeta       = " + cyBeta);
						}

						if ((i == 34) && (j == 1)) {
							cyP = Double.parseDouble(value);
							System.out.println(" cyP          = " + cyP);
						}

						if ((i == 35) && (j == 1)) {
							cyR = Double.parseDouble(value);
							System.out.println(" cyR          = " + cyR);
						}

						if ((i == 36) && (j == 1)) {
							cyDelta_A = Double.parseDouble(value);
							System.out.println(" cyDelta_A    = " + cyDelta_A);
						}

						if ((i == 37) && (j == 1)) {
							cyDelta_R = Double.parseDouble(value);
							System.out.println(" cyDelta_R    = " + cyDelta_R);
						}

						if ((i == 38) && (j == 1)) {
							cLBeta = Double.parseDouble(value);
							System.out.println(" cLBeta       = " + cLBeta);
						}

						if ((i == 39) && (j == 1)) {
							cLP = Double.parseDouble(value);
							System.out.println(" cLP          = " + cLP);
						}

						if ((i == 40) && (j == 1)) {
							cLR = Double.parseDouble(value);
							System.out.println(" cLR          = " + cLR);
						}

						if ((i == 41) && (j == 1)) {
							cLDelta_A = Double.parseDouble(value);
							System.out.println(" cLDelta_A    = " + cLDelta_A);
						}

						if ((i == 42) && (j == 1)) {
							cLDelta_R = Double.parseDouble(value);
							System.out.println(" cLDelta_R    = " + cLDelta_R);
						}

						if ((i == 43) && (j == 1)) {
							cNBeta = Double.parseDouble(value);
							System.out.println(" cNBeta       = " + cNBeta);
						}

						if ((i == 44) && (j == 1)) {
							cNP = Double.parseDouble(value);
							System.out.println(" cNP          = " + cNP);
						}

						if ((i == 45) && (j == 1)) {
							cNR = Double.parseDouble(value);
							System.out.println(" cNR          = " + cNR);
						}

						if ((i == 46) && (j == 1)) {
							cNDelta_A = Double.parseDouble(value);
							System.out.println(" cNDelta_A    = " + cNDelta_A);
						}

						if ((i == 47) && (j == 1)) {
							cNDelta_R = Double.parseDouble(value);
							System.out.println(" cNDelta_R    = " + cNDelta_R);
						}

						break;
					}

				}
			}
		}

		catch(Exception ioe) {
			ioe.printStackTrace();
		}


	}

	public static String cellToString(Cell cell) {  
		int type;
		Object result = null;
		type = cell.getCellType();

		switch (type) {

		case Cell.CELL_TYPE_NUMERIC: // numeric value in Excel
		case Cell.CELL_TYPE_FORMULA: // precomputed value based on formula
			result = cell.getNumericCellValue();
			break;
		case Cell.CELL_TYPE_STRING: // String Value in Excel 
			result = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_BLANK:
			result = "";
		case Cell.CELL_TYPE_BOOLEAN: //boolean value 
			result = cell.getBooleanCellValue();
			break;
		case Cell.CELL_TYPE_ERROR:
		default:  
			throw new RuntimeException("There is no support for this type of cell");                        
		}

		if (result == null)
			return "";
		else
			return result.toString();
	}

	public static void main(String[] args) {

		
		FlightDynamicsManager theObj = new FlightDynamicsManager();


		System.out.println("---------------------------------------------------");
		System.out.println("Reading input data file (excel format)");
		String inputFileName = "AIRCRAFT_DATA.xlsx";
		File excelFile = new File (inputFileName) ;
		
		
		////// select the excel sheet you want to read \\\\\\
						
						int sheetNumber = 0;       
		

		if (excelFile.exists()){

			System.out.println("File " + inputFileName + " found.");

			System.out.println("\n %%% start reading from file %%% ");

			// Read all data from file
			theObj.readDataFromExcelFile(excelFile, sheetNumber);

			System.out.println("\n %%% end of reading from file %%%");

			theObj.calculateAll();

		}
		else {
			System.out.println("File " + inputFileName + " not found.");			
		}

	}

}
