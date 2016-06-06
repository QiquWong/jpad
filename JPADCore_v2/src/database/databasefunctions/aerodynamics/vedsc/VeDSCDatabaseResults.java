package database.databasefunctions.aerodynamics.vedsc;

public class VeDSCDatabaseResults {

	double kFv_vs_bv_over_dfv,
	kVf_vs_zw_over_dfv,
	kWv_vs_zw_over_rf,
	kWf_vs_zw_over_rf,
	kHv_vs_zh_over_bv1,
	kHf_vs_zh_over_bv1;
	
	/**
	 * @deprecated It has never been used 
	 * 
	 * @author Vincenzo Cusati
	 */

	public VeDSCDatabaseResults(
			double kFv_vs_bv_over_dfv, double kVf_vs_zw_over_dfv, double kWv_vs_zw_over_rf,
			double kWf_vs_zw_over_rf, double kHv_vs_zh_over_bv1, double kHf_vs_zh_over_bv1) {
		this.kFv_vs_bv_over_dfv = kFv_vs_bv_over_dfv;
		this.kVf_vs_zw_over_dfv = kVf_vs_zw_over_dfv;
		this.kWv_vs_zw_over_rf = kWv_vs_zw_over_rf;
		this.kWf_vs_zw_over_rf = kWf_vs_zw_over_rf;
		this.kHv_vs_zh_over_bv1 = kHv_vs_zh_over_bv1;
		this.kHf_vs_zh_over_bv1 = kHf_vs_zh_over_bv1;
	}
	
	public double getkFv_vs_bv_over_dfv() {
		return kFv_vs_bv_over_dfv;
	}

	public double getkVf_vs_zw_over_dfv() {
		return kVf_vs_zw_over_dfv;
	}

	public double getkWv_vs_zw_over_rf() {
		return kWv_vs_zw_over_rf;
	}

	public double getkWf_vs_zw_over_rf() {
		return kWf_vs_zw_over_rf;
	}

	public double getkHv_vs_zh_over_bv1() {
		return kHv_vs_zh_over_bv1;
	}

	public double getkHf_vs_zh_over_bv1() {
		return kHf_vs_zh_over_bv1;
	}


}
