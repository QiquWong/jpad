package calculators.stability;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
//import java.math.*;
//import it.unina.adopt.core.MyAircraft;
//import it.unina.adopt.core.MyOperatingConditions;
//import it.unina.adopt.core.calculators.MyAerodynamicCalculator;
//
//import org.apache.commons.math3.exception.DimensionMismatchException;
//import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
//
public class DynamicsEquations implements FirstOrderDifferentialEquations {
//
//	private MyOperatingConditions _theOperatingConditions;
//	private MyAircraft _theAircraft;
//
//	double g,
//	rho,
//	V,
//	W,
//	S,
//	mac,
//	b,
//	Tmax,
//	mu_T,
//	CD0_mach,
//	K_mach,
//	CL_alpha,
//	CL_alpha_dot,
//	CL_q,
//	CL_delta_e,
//	CL_delta_s,
//	m,
//
//	Cm_0, 
//	Cm_alpha,
//	Cm_T_0,
//	Cm_T_alpha,
//	Cm_delta_s,
//	Cm_delta_e,
//	Cm_q, 
//
//	mu_rel,
//	k_y;
//
//	public MyDynamicsEquations(
//			MyOperatingConditions conditions, 
//			MyAircraft aircraft,
//			MyAerodynamicCalculator calculator) {
//
//		_theOperatingConditions = conditions;
//		_theAircraft = aircraft;
//
//		g = conditions.g0.getEstimatedValue();
//		rho = conditions.get_densityCurrent().getEstimatedValue();
//		V = conditions.get_tas().getEstimatedValue();
//		W = aircraft.get_weights().get_MTOW().getEstimatedValue();
//		S = aircraft.get_wing().get_surface().getEstimatedValue();
//		mac = aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue();
//		b = aircraft.get_wing().get_span().getEstimatedValue();
//		Tmax = aircraft.get_powerPlant().get_T0Total().getEstimatedValue();
//		mu_T = aircraft.get_powerPlant().get_muT().getEstimatedValue();
//		CD0_mach = calculator.get_cD0Total();
//		K_mach = calculator.get_kFactorPolar();
//		CL_alpha = aircraft.get_theAerodynamicCalculator().get_cLAlphaFixed();
//		CL_alpha_dot = ;
//		CL_q = ;
//		CL_delta_e = calculator.get_cLDeltaE();
//		CL_delta_s = calculator.get_cLDeltaS();
//		m = 2.;
//
//		Cm_0 = aircraft.get_theAerodynamicCalculator().get_cM0();
//		Cm_alpha = aircraft.get_theAerodynamicCalculator().get_cMAlphaFixed();
//		Cm_T_0 = 0.;
//		Cm_T_alpha = 0.;
//		Cm_delta_s = 0.;
//		Cm_delta_e = 0.;
//		Cm_q = 0.;
//
//		mu_rel = (W/g)/(rho*S*b);
//		//		k_y = ;
//
//	}
//
//	@Override
//	public void computeDerivatives(double t, double[] y, double[] yDot)
//			throws MaxCountExceededException, DimensionMismatchException {
//
//		double V = y[0];
//		double alpha = y[1];
//		double q = y[2];
//		double x_eg = y[3];
//		double z_eg = y[4];
//		double theta = y[5];
//		//		delta_s_val = ppval(pp.delta_s,time);
//		//		delta_T_val = ppval(pp.delta_T,time);
//		//		delta_e_val = ppval(pp.delta_e,time);   
//
//		double V_dot = ((delta_T_val*Tmax/W)*cos(alpha - mu_x + mu_T) 
//				-sin(theta - alpha + mu_x) 
//				-((rho*V^2)/(2*(W/S))) 
//				*(CD0_mach + K_mach*((CL_alpha*alpha 
//						+ CL_delta_e*delta_e_val 
//						+ CL_delta_s*delta_s_val)^m)))*g;
//
//		double alpha_dot = ((1. - CL_q*(mac/b)/(4*mu_rel))*q 
//				-(g/V)*(delta_T_val*Tmax/W)*sin(alpha - mu_x + mu_T) 
//				+(g/V)*cos(theta - alpha + mu_x) 
//				-(g/V)*((rho*V^2)/(2*(W/S))) 
//				*(CL_alpha*alpha + CL_delta_e*delta_e_val 
//						+ CL_delta_s*delta_s_val))/(1+((mac/b)/4*mu_rel)*CL_alpha_dot);
//
//		//			% (7.58c) N.B.: si considera trascurabile Cm_alpha_dot.
//		double q_dot = (Cm_0 + Cm_alpha*alpha 
//				+ Cm_delta_s*delta_s_val 
//				+ Cm_delta_e*delta_e_val 
//				+ (mac/(2*V))*Cm_q*q 
//				+ Cm_T_0 + Cm_T_alpha*alpha)*(V^2*mac/b)/(2*mu_rel*k_y^2);
//
//		double x_dot = V*cos(theta - alpha + mu_x);
//
//		double z_dot = -V*sin(theta - alpha + mu_x);
//
//		double theta_dot = q;
//
//		yDot[0] = V_dot;
//		yDot[1] = alpha_dot;
//		yDot[2] = q_dot;
//		yDot[3] = x_dot;
//		yDot[4] = z_dot;
//		yDot[5] = theta_dot;
//
//	}

	@Override
	public int getDimension() {
		return 6;
	}

	@Override
	public void computeDerivatives(double arg0, double[] arg1, double[] arg2)
			throws MaxCountExceededException, DimensionMismatchException {
		// TODO Auto-generated method stub
		
	}

}
