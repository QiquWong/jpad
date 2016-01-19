package configuration.enumerations;

public enum FusDesDatabaseEnum {
	/*
	 * Input variable names
	 */
	MachNumber,
	ReynoldsNumber,
	MAC,
	WingSpan,
	WingSurface,
	FuselageDiameter,
	FuselageLength,
	NoseFinenessRatio,
	FinenessRatio,
	TailFinenessRatio, 
	WindshieldAngle,
	UpsweepAngle,
	xPercentPositionPole,
	/*
	 * Output variable names
	 */
	kn_vs_FRn,
	kc_vs_FR,
	kt_vs_FRt,
	CD0_fuselage,
	CM0_FR_vs_FR,
	dCM_nose_vs_wshield,
	dCM_tail_vs_upsweep,
	CM0_fuselage,
	CMa_FR_vs_FR,
	dCMa_nose_vs_wshield,
	dCMa_tail_vs_upsweep,
	CMa_fuselage,
	CNb_FR_vs_FR,
	dCNb_nose_vs_FRn,
	dCNb_tail_vs_FRt,
	CNb_fuselage;
}
