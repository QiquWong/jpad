package database.databasefunctions.aerodynamics.fusDes;

public class FusDesDatabaseResults {
	

	
	double kn_vs_FRn, kc_vs_FR,kt_vs_FRt,
	CM0_FR_vs_FR, dCM_nose_vs_wshield, dCM_tail_vs_upsweep,
	CMa_FR_vs_FR, dCMa_nose_vs_wshield, dCMa_tail_vs_upsweep,
	CNb_FR_vs_FR, dCNb_nose_vs_FRn, dCNb_tail_vs_FRt;
	
	/**
	 * @deprecated It has never been used 
	 * 
	 * @author Vincenzo Cusati
	 */
			
	public FusDesDatabaseResults(double kn_vs_FRn, double kc_vs_FR, double kt_vs_FRt, double cM0_FR_vs_FR,
			double dCM_nose_vs_wshield, double dCM_tail_vs_upsweep, double cMa_FR_vs_FR, double dCMa_nose_vs_wshield,
			double dCMa_tail_vs_upsweep, double cNb_FR_vs_FR, double dCNb_nose_vs_FRn, double dCNb_tail_vs_FRt) {
		
		this.kn_vs_FRn = kn_vs_FRn;
		this.kc_vs_FR = kc_vs_FR;
		this.kt_vs_FRt = kt_vs_FRt;
		this.CM0_FR_vs_FR = cM0_FR_vs_FR;
		this.dCM_nose_vs_wshield = dCM_nose_vs_wshield;
		this.dCM_tail_vs_upsweep = dCM_tail_vs_upsweep;
		this.CMa_FR_vs_FR = cMa_FR_vs_FR;
		this.dCMa_nose_vs_wshield = dCMa_nose_vs_wshield;
		this.dCMa_tail_vs_upsweep = dCMa_tail_vs_upsweep;
		this.CNb_FR_vs_FR = cNb_FR_vs_FR;
		this.dCNb_nose_vs_FRn = dCNb_nose_vs_FRn;
		this.dCNb_tail_vs_FRt = dCNb_tail_vs_FRt;
	}

	public double getKn_vs_FRn() {
		return kn_vs_FRn;
	}

	public double getKc_vs_FR() {
		return kc_vs_FR;
	}

	public double getKt_vs_FRt() {
		return kt_vs_FRt;
	}

	public double getCM0_FR_vs_FR() {
		return CM0_FR_vs_FR;
	}

	public double getdCM_nose_vs_wshield() {
		return dCM_nose_vs_wshield;
	}

	public double getdCM_tail_vs_upsweep() {
		return dCM_tail_vs_upsweep;
	}

	public double getCMa_FR_vs_FR() {
		return CMa_FR_vs_FR;
	}

	public double getdCMa_nose_vs_wshield() {
		return dCMa_nose_vs_wshield;
	}

	public double getdCMa_tail_vs_upsweep() {
		return dCMa_tail_vs_upsweep;
	}

	public double getCNb_FR_vs_FR() {
		return CNb_FR_vs_FR;
	}

	public double getdCNb_nose_vs_FRn() {
		return dCNb_nose_vs_FRn;
	}

	public double getdCNb_tail_vs_FRt() {
		return dCNb_tail_vs_FRt;
	}

}
