package configuration.enumerations;

public enum AerodynamicAndStabilityPlotEnum {

	//..............................................
	// Wing
	WING_LIFT_CURVE_CLEAN,
	WING_STALL_PATH,
	WING_CL_DISTRIBUTION,
	WING_CL_ADDITIONAL_DISTRIBUTION,
	WING_CL_BASIC_DISTRIBUTION,
	WING_cCL_DISTRIBUTION,
	WING_cCL_ADDITIONAL_DISTRIBUTION,
	WING_cCL_BASIC_DISTRIBUTION,
	WING_GAMMA_DISTRIBUTION,
	WING_GAMMA_ADDITIONAL_DISTRIBUTION,
	WING_GAMMA_BASIC_DISTRIBUTION,
	WING_TOTAL_LOAD_DISTRIBUTION,
	WING_ADDITIONAL_LOAD_DISTRIBUTION,
	WING_BASIC_LOAD_DISTRIBUTION,
	
	WING_LIFT_CURVE_HIGH_LIFT,
	WING_POLAR_CURVE_HIGH_LIFT,
	WING_MOMENT_CURVE_HIGH_LIFT,
	
	WING_POLAR_CURVE_CLEAN,
	WING_DRAG_DISTRIBUTION,
	
	WING_MOMENT_CURVE_CLEAN,
	WING_MOMENT_DISTRIBUTION,
	
	//..............................................
	// HTail
	HTAIL_LIFT_CURVE_CLEAN,
	HTAIL_STALL_PATH,
	HTAIL_CL_DISTRIBUTION,
	HTAIL_CL_ADDITIONAL_DISTRIBUTION,
	HTAIL_CL_BASIC_DISTRIBUTION,
	HTAIL_cCL_DISTRIBUTION,
	HTAIL_cCL_ADDITIONAL_DISTRIBUTION,
	HTAIL_cCL_BASIC_DISTRIBUTION,
	HTAIL_GAMMA_DISTRIBUTION,
	HTAIL_GAMMA_ADDITIONAL_DISTRIBUTION,
	HTAIL_GAMMA_BASIC_DISTRIBUTION,
	HTAIL_TOTAL_LOAD_DISTRIBUTION,
	HTAIL_ADDITIONAL_LOAD_DISTRIBUTION,
	HTAIL_BASIC_LOAD_DISTRIBUTION,
	
	HTAIL_POLAR_CURVE_CLEAN_BREAKDOWN,
	HTAIL_DRAG_DISTRIBUTION,
	
	HTAIL_MOMENT_CURVE_CLEAN,
	HTAIL_MOMENT_DISTRIBUTION,
	
	//..............................................
	// VTail
	VTAIL_LIFT_CURVE_CLEAN,
	VTAIL_STALL_PATH,
	VTAIL_CL_DISTRIBUTION,
	VTAIL_CL_ADDITIONAL_DISTRIBUTION,
	VTAIL_CL_BASIC_DISTRIBUTION,
	VTAIL_cCL_DISTRIBUTION,
	VTAIL_cCL_ADDITIONAL_DISTRIBUTION,
	VTAIL_cCL_BASIC_DISTRIBUTION,
	VTAIL_GAMMA_DISTRIBUTION,
	VTAIL_GAMMA_ADDITIONAL_DISTRIBUTION,
	VTAIL_GAMMA_BASIC_DISTRIBUTION,
	VTAIL_TOTAL_LOAD_DISTRIBUTION,
	VTAIL_ADDITIONAL_LOAD_DISTRIBUTION,
	VTAIL_BASIC_LOAD_DISTRIBUTION,
	
	VTAIL_POLAR_CURVE_CLEAN_BREAKDOWN,
	VTAIL_DRAG_DISTRIBUTION,
	
	VTAIL_MOMENT_CURVE_CLEAN,
	VTAIL_MOMENT_DISTRIBUTION,
	
	//..............................................
	// Fuselage
	FUSELAGE_POLAR_CURVE,
	FUSELAGE_MOMENT_CURVE,
	
	//..............................................
	// Nacelle
	NACELLE_POLAR_CURVE,
	NACELLE_MOMENT_CURVE,
	
	//..............................................
	// Aircraft
	DOWNWASH_GRADIENT,
	DOWNWASH_ANGLE,
	TOTAL_LIFT_CURVE,
	TOTAL_POLAR_CURVE,
	TOTAL_MOMENT_CURVE_VS_ALPHA,
	TOTAL_MOMENT_CURVE_VS_CL,
	TRIMMED_LIFT_CURVE,
	TRIMMED_LIFT_CURVE_HTAIL,
	TRIMMED_POLAR_CURVE,
	DELTA_ELEVATOR_EQUILIBRIUM,
	TOTAL_CM_BREAKDOWN,
	TOTAL_CN_BREAKDOWN,
	TOTAL_CN_VS_BETA_VS_DELTA_RUDDER,
	DELTA_RUDDER_EQUILIBRIUM,
	
	// STABILITY EXECUTABLE
	WING_CL_CURVE_CLEAN,
	WING_CL_CURVE_HIGH_LIFT,
	HTAIL_CL_CURVE_CLEAN,
	HTAIL_CL_CURVE_ELEVATOR,
	WING_POLAR_CURVE,
	WING_POLAR_CURVE_HIGHLIFT,
	HTAIL_POLAR_CURVE_CLEAN,
	WING_CM_QUARTER_CHORD,
	WING_CM_AERODYNAMIC_CENTER,
	HTAIL_CM_QUARTER_CHORD,
	HTAIL_CM_AERODYNAMIC_CENTER,
	FUSELAGE_CM_PLOT,
	AIRCRAFT_CM_VS_ALPHA_BODY_COMPONENTS,
	AIRCRAFT_CM_VS_ALPHA_BODY,
	AIRCRAFT_CM_VS_CL_DELTAE,
	DELTA_E_EQUILIBRIUM,
	CL_DISTRIBUTION_WING,
	CM_DISTRIBUTION_WING,
	CL_DISTRIBUTION_HORIZONTAL_TAIL,
	CM_DISTRIBUTION_HORIZONTAL_TAIL,
	CL_TOTAL,
	PROPULSIVE_SYSTEM_CM_DIRECT_EFFECTS,
	PROPULSIVE_SYSTEM_CM_NON_DIRECT_EFFECTS,
	AIRCRAFT_CM_VS_ALPHA_BODY_DELTAE,
	AIRCRAFT_CL_VS_ALPHA_BODY_DELTAE,
	NEUTRAL_POINT,
	INDUCED_ALPHA_DISTRIBUTION_WING,
	CENTER_OF_PRESSURE_DISTRIBUTION_WING,
	INDUCED_ALPHA_DISTRIBUTION_HORIZONTAL_TAIL,
	CENTER_OF_PRESSURE_DISTRIBUTION_HORIZONTAL_TAIL,

	// TODO : ADD OTHER IF NEEDED

}
