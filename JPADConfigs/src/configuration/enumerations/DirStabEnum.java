package configuration.enumerations;

public enum DirStabEnum {
	
	/*
	 * Input variable names
	 */
	Mach_number,
	Reynolds_number,
	LiftCoefficient,
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
	FuselageDiameter,
	FuselageLength,
	NoseFinenessRatio,
	FinenessRatio,
	TailFinenessRatio, 
	WindshieldAngle,
	UpsweepAngle,
	xPercentPositionPole,
	WingSweepAngle,
	xACwMACratio,
	xCGMACratio,
		
	/*
	 * Output variable names
	 */
	KFv_vs_bv_over_dfv,
	KHf_vs_zh_over_bv1,
	KHv_vs_zh_over_bv1,
	KVf_vs_bv_over_dfv,
	KWf_vs_zw_over_rf,
	KWv_vs_zw_over_rf,
	CN_beta_vertical,
	CNb_FR_vs_FR,
	dCNb_nose_vs_FRn,
	dCNb_tail_vs_FRt,
	CNb_fuselage,
	CNb_wing,
	CNb_AC;
}
