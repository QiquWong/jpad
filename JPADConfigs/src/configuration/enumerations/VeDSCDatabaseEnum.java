package configuration.enumerations;

public enum VeDSCDatabaseEnum {

	/*
	 * Input variable names
	 */
	Mach_number,
	Wing_Aspect_Ratio,
	Wing_span,
	Wing_position,
	Vertical_Tail_Aspect_Ratio,
	Vertical_Tail_span,
	Vertical_Tail_Arm,
	Vertical_Tail_Sweep_at_half_chord,
	Vertical_tail_airfoil_lift_curve_slope,
	Horizontal_position_over_vertical,
	Diameter_at_vertical_MAC,
	Tailcone_shape,
	
	/*
	 * Output variable names
	 */
	KFv_vs_bv_over_dfv,
	KHf_vs_zh_over_bv1,
	KHv_vs_zh_over_bv1,
	KVf_vs_bv_over_dfv,
	KWf_vs_zw_over_rf,
	KWv_vs_zw_over_rf,
	CN_beta_vertical;
	
}
