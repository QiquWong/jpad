package configuration.enumerations;

public enum AerodynamicAndStabilityEnum {

	////////////////////////////////////////////////
	// Lifting Surface (Wing/HTail/VTail/Canard)
	CRITICAL_MACH,
	AERODYNAMIC_CENTER,
	CL_AT_ALPHA,
	CL_ALPHA,
	CL_ZERO,
	CL_STAR,
	CL_MAX,
	ALPHA_ZERO_LIFT,
	ALPHA_STAR,
	ALPHA_STALL,
	LIFT_CURVE_3D,
	LIFT_DISTRIBUTION,
	CD0,
	CD_INDUCED_LIFTING_SURFACE,
	CD_WAVE,
	OSWALD_FACTOR,
	POLAR_CURVE_3D_LIFTING_SURFACE,
	DRAG_DISTRIBUTION,
	CD_AT_ALPHA_LIFTING_SURFACE,
	HIGH_LIFT_DEVICES_EFFECTS,
	HIGH_LIFT_CURVE_3D,
	CL_AT_ALPHA_HIGH_LIFT,
	CD_AT_ALPHA_HIGH_LIFT,
	CM_AT_ALPHA_HIGH_LIFT,
	CM_AC_LIFTING_SURFACE,
	CM_ALPHA_LIFTING_SURFACE,
	CM_AT_ALPHA_LIFTING_SURFACE,
	MOMENT_DISTRIBUTION_LIFTING_SURFACE,
	MOMENT_CURVE_3D,
	
	////////////////////////////////////////////////
	// Fuselage
	CD0_PARASITE_FUSELAGE,
	CD0_BASE_FUSELAGE,
	CD0_UPSWEEP_FUSELAGE,
	CD0_WINDSHIELD_FUSELAGE,
	CD0_TOTAL_FUSELAGE,
	CD_INDUCED_FUSELAGE,
	POLAR_CURVE_3D_FUSELAGE,
	CD_AT_ALPHA_FUSELAGE,
	CM0_FUSELAGE,
	CM_ALPHA_FUSELAGE,
	CM_AT_ALPHA_FUSELAGE,
	MOMENT_CURVE_3D_FUSELAGE,
	
	////////////////////////////////////////////////
	// Nacelle
	CD0_PARASITE_NACELLE,
	CD0_BASE_NACELLE,
	CD0_TOTAL_NACELLE,
	CD_INDUCED_NACELLE,
	POLAR_CURVE_3D_NACELLE,
	CD_AT_ALPHA_NACELLE,
	CM0_NACELLE,
	CM_ALPHA_NACELLE,
	CM_AT_ALPHA_NACELLE,
	MOMENT_CURVE_3D_NACELLE,
	
	////////////////////////////////////////////////
	// Aircraft
	DOWNWASH,
	CL_TOTAL,
	CD_TOTAL,
	CM_TOTAL,
	LONGITUDINAL_STABILITY,
	DIRECTIONAL_STABILITY,
	BUFFET_BARRIER
	
	
}
