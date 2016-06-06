package database.databasefunctions.aerodynamics.fusDes;

import database.databasefunctions.DatabaseReader;
import standaloneutils.MyInterpolatingFunction;

public class FusDesDatabaseReader extends DatabaseReader{

	private MyInterpolatingFunction kn_vs_FRn, kc_vs_FR,kt_vs_FRt,
	CM0_FR_vs_FR, dCM_nose_vs_wshield, dCM_tail_vs_upsweep,
	CMa_FR_vs_FR, dCMa_nose_vs_wshield, dCMa_tail_vs_upsweep,
	CNb_FR_vs_FR, dCNb_nose_vs_FRn, dCNb_tail_vs_FRt;
	
	private double kn, kc, kt, CM0FR, dCMn, dCMt, CMaFR, dCMan, dCMat, CNbFR, dCNbn, dCNbt;


	public FusDesDatabaseReader(String databaseFolderPath, String databaseFileName) {

		super(databaseFolderPath, databaseFileName);

		// Drag Coeff.
		kn_vs_FRn = database.interpolate2DFromDatasetFunction("Kn_vs_FRn");		// 2D: z=f(x,y) 
		kc_vs_FR  = database.interpolate1DFromDatasetFunction("Kc_vs_FR");		// 1D: y=f(x)
		kt_vs_FRt = database.interpolate2DFromDatasetFunction("Kt_vs_FRt");

		// Pitching Moment Coeff.
		CM0_FR_vs_FR 		= database.interpolate2DFromDatasetFunction("CM0_FR_vs_FR");
		dCM_nose_vs_wshield = database.interpolate2DFromDatasetFunction("dCM_nose_vs_wshield");
		dCM_tail_vs_upsweep = database.interpolate2DFromDatasetFunction("dCM_tail_vs_upsweep");

		// Pitching Moment Derivative Coeff.
		CMa_FR_vs_FR 	  	 = database.interpolate2DFromDatasetFunction("CMa_FR_vs_FR");
		dCMa_nose_vs_wshield = database.interpolate2DFromDatasetFunction("dCMa_nose_vs_wshield");
		dCMa_tail_vs_upsweep = database.interpolate2DFromDatasetFunction("dCMa_tail_vs_upsweep");

		// Yawing Moment Derivative Coeff.
		CNb_FR_vs_FR 	 = database.interpolate2DFromDatasetFunction("CNb_FR_vs_FR");
		dCNb_nose_vs_FRn = database.interpolate2DFromDatasetFunction("dCNb_nose_vs_FRn");
		dCNb_tail_vs_FRt = database.interpolate2DFromDatasetFunction("dCNb_tail_vs_FRt");

	}
	
	// @Overload
	/**
	 * 
	 * @param noseFinenessRatio
	 * @param windshieldAngle
	 * @param finenessRatio
	 * @param tailFinenessRatio
	 * @param upsweepAngle
	 * @param xPositionPole
	 * 
	 * @author Vincenzo Cusati
	 */
	
	public void runAnalysis(double noseFinenessRatio,double windshieldAngle, double finenessRatio,
							double tailFinenessRatio, double upsweepAngle, double xPositionPole) {

		kn = get_Kn_vs_FRn(noseFinenessRatio, windshieldAngle);
		kc = get_Kc_vs_FR(finenessRatio);
		kt = get_Kt_vs_FRt(tailFinenessRatio, upsweepAngle);

		CM0FR = get_CM0_FR_vs_FR(finenessRatio, xPositionPole);
		dCMn = get_dCM_nose_vs_wshield(windshieldAngle,noseFinenessRatio);
		dCMt = get_dCM_tail_vs_upsweep(upsweepAngle, tailFinenessRatio) ;

		CMaFR = get_CMa_FR_vs_FR(finenessRatio, xPositionPole);
		dCMan = get_dCMa_nose_vs_wshield(windshieldAngle,noseFinenessRatio);
		dCMat = get_dCMa_tail_vs_upsweep(upsweepAngle, tailFinenessRatio);

		CNbFR = get_CNb_FR_vs_FR(finenessRatio,xPositionPole);
		dCNbn =	get_dCNb_nose_vs_FRn(noseFinenessRatio, xPositionPole);
		dCNbt = get_dCNb_tail_vs_FRt(tailFinenessRatio, xPositionPole);
	}
	
	/**
	 * 
	 * @param noseFinenessRatio
	 * @param finenessRatio
	 * @param tailFinenessRatio
	 * @param xPositionPole
	 */
	public void runAnalysisCNbeta(double noseFinenessRatio, double finenessRatio,
			double tailFinenessRatio, double xPositionPole) {
		CNbFR = get_CNb_FR_vs_FR(finenessRatio,xPositionPole);
		dCNbn =	get_dCNb_nose_vs_FRn(noseFinenessRatio, xPositionPole);
		dCNbt = get_dCNb_tail_vs_FRt(tailFinenessRatio, xPositionPole);
	}

	
	
	// ***************************************************** CD *************************************************************************
	/**
	 * This function returns the nose shape factor Kn which represents the contribution of the nose to the global drag coefficient 
	 * and it takes into account the effect of the nose fineness ratio and of the windshield geometric angle. 
	 * 
	 * @param windshieldAngle is the angle of the front window. It varies between [35 deg - 50 deg].
	 * @param noseFinenessRatio is the ratio between nose fuselage length and maximum fuselage diameter. It varies between [1.2-1.7].
	 * @return
	 */
	public double get_Kn_vs_FRn(double noseFinenessRatio, double windshieldAngle) {
		return kn_vs_FRn.value(noseFinenessRatio, windshieldAngle);
		
	}
	
	/**
	 * This function returns the shape factor Kc which represents the contribution of the cabin to the global drag coefficient and it 
	 * takes into account the effect of the cabin length (or cabin fineness ratio).
	 * 
	 * @param finenessRatio is the ratio between fuselage length and diameter. It varies between [7-12].
	 * @return
	 */
	public  double get_Kc_vs_FR(double finenessRatio) {
		return kc_vs_FR.value(finenessRatio);
	}
	
	/**
	 * This function returns the tail shape factor Kt which represents the contribution of the tail to the global drag coefficient and it takes
	 * into account the effect of the upsweep angle.
	 *  
	 * @param tailFinenessRatio is the ratio between tailcone fuselage length and maximum fuselage diameter. It varies between [2.3-3.0].
	 * @param upsweepAngle is the upward curvature angle of the aft fuselage. It varies between [10 deg - 18 deg].
	 * @return
	 */
	public double get_Kt_vs_FRt(double tailFinenessRatio, double upsweepAngle) {
		return kt_vs_FRt.value(tailFinenessRatio, upsweepAngle);
	}
	
	// ***************************************************** CM0 *************************************************************************
	
	/**
	 * This function returns the pitching moment coefficient as function of fuselage fineness ratio.
	 *  
	 * @param finenessRatio is the ratio between fuselage length and diameter. It varies between [7-12].
	 * @param xPositionPole is the position along x-axis of the reference moment point. It can be equal to 0.35, 0.50 or 0.60 which corresponds
	 * to 35%, 50% or 60% of fuselage length respectively.
	 * @return
	 */
	public double get_CM0_FR_vs_FR(double finenessRatio, double xPositionPole) {
		return CM0_FR_vs_FR.value(finenessRatio, xPositionPole);
	}
	
	/**
	 * This function returns the pitching moment nose correction factor. It depends on windshield angle and on the nose fineness ratio FRn.
	 * 
	 * @param noseFinenessRatio is the ratio between nose fuselage length and maximum fuselage diameter. It varies between [1.2-1.7].
	 * @param windshieldAngle is the angle of the front window. It varies between [35 deg - 50 deg].
	 * @return
	 */
	public double get_dCM_nose_vs_wshield(double windshieldAngle,double noseFinenessRatio) {
		return dCM_nose_vs_wshield.value(windshieldAngle, noseFinenessRatio);
	}
	
	/**
	 * This function returns the pitching moment tail correction factor. It depends on upsweep angle and on the tail fineness ratio.
	 * 
	 * @param tailFinenessRatio is the ratio between tailcone fuselage length and maximum fuselage diameter. It varies between [2.3-3.0].
	 * @param upsweepAngle is the upward curvature angle of the aft fuselage. It varies between [10 deg - 18 deg].
	 * @return
	 */
	public double get_dCM_tail_vs_upsweep(double upsweepAngle, double tailFinenessRatio) {
		return dCM_tail_vs_upsweep.value(upsweepAngle, tailFinenessRatio);
	}
	
	// ***************************************************** CMa *************************************************************************
	
	/**
	 * This function returns the pitching moment derivative as function of fuselage fineness ratio.
	 * 
	 * @param finenessRatio is the ratio between fuselage length and diameter. It varies between [7-12].
	 * @param xPositionPole is the position along x-axis of the reference moment point. It can be equal to 0.35, 0.50 or 0.60 which corresponds
	 * to 35%, 50% or 60% of fuselage length respectively.
	 * @return
	 */
	public double get_CMa_FR_vs_FR(double finenessRatio, double xPositionPole) {
		return CMa_FR_vs_FR.value(finenessRatio, xPositionPole);
	}
	
	/**
	 * This function returns the pitching moment nose derivative correction factor. It depends on windshield angle and on the nose fineness ratio.
	 * 
	 * @param noseFinenessRatio is the ratio between nose fuselage length and maximum fuselage diameter. It varies between [1.2-1.7].
	 * @param windshieldAngle is the angle of the front window. It varies between [35 deg - 50 deg].
	 * @return
	 */
	public double get_dCMa_nose_vs_wshield(double windshieldAngle,double noseFinenessRatio) {
		return dCMa_nose_vs_wshield.value(windshieldAngle, noseFinenessRatio);
	}
	
	/**
	 * 
	 * This function returns the pitching moment tail derivative correction factor. It depends on upsweep angle and on the tailcone fineness ratio.
	 * @param tailFinenessRatio is the ratio between tailcone fuselage length and maximum fuselage diameter. It varies between [2.3-3.0].
	 * @param upsweepAngle is the upward curvature angle of the aft fuselage. It varies between [10 deg - 18 deg].
	 * @return
	 */
	public double get_dCMa_tail_vs_upsweep(double upsweepAngle, double tailFinenessRatio) {
		return dCMa_tail_vs_upsweep.value(upsweepAngle,  tailFinenessRatio);
	}
	
	// ***************************************************** CNb *************************************************************************
	/**
	 * This function returns the yawing moment derivative coefficient as function of fuselage fineness ratio.
	 * 
	 * @param finenessRatio is the ratio between fuselage length and diameter. It varies between [7-12].
	 * @param xPositionPole is the position along x-axis of the reference moment point. It can be equal to 0.35, 0.50 or 0.60 which corresponds
	 * to 35%, 50% or 60% of fuselage length respectively.
	 * @return
	 */
	public double get_CNb_FR_vs_FR(double finenessRatio, double xPositionPole) {
		return CNb_FR_vs_FR.value(finenessRatio, xPositionPole);
	}
	
	/**
	 * This function returns the yawing moment nose correction factor. It depends on the nose fineness ratio
	 * 
	 * @param noseFinenessRatio is the ratio between nose fuselage length and maximum fuselage diameter. It varies between [1.2-1.7].
	 * @param xPositionPole is the position along x-axis of the reference moment point. It can be equal to 0.35, 0.50 or 0.60 which corresponds
	 * to 35%, 50% or 60% of fuselage length respectively.
	 * @return
	 */
	public double get_dCNb_nose_vs_FRn(double noseFinenessRatio, double xPositionPole) {
		return dCNb_nose_vs_FRn.value(noseFinenessRatio, xPositionPole);
	}
	
	/**
	 * This function returns the yawing moment tail correction factor. It depends on the tailcone fineness ratio.
	 * 
	 * @param tailFinenessRatio is the ratio between tailcone fuselage length and maximum fuselage diameter. It varies between [2.3-3.0].
	 * @param xPositionPole is the position along x-axis of the reference moment point. It can be equal to 0.35, 0.50 or 0.60 which corresponds
	 * to 35%, 50% or 60% of fuselage length respectively.
	 * @return
	 */
	public double get_dCNb_tail_vs_FRt(double tailFinenessRatio, double xPositionPole) {
		return dCNb_tail_vs_FRt.value(tailFinenessRatio, xPositionPole);
	}
	
	public double getKn() {
		return kn;
	}

	public void setKn(double kn) {
		this.kn = kn;
	}

	public double getKc() {
		return kc;
	}

	public void setKc(double kc) {
		this.kc = kc;
	}

	public double getKt() {
		return kt;
	}

	public void setKt(double kt) {
		this.kt = kt;
	}

	public double getCM0FR() {
		return CM0FR;
	}

	public void setCM0FR(double cM0FR) {
		CM0FR = cM0FR;
	}

	public double getdCMn() {
		return dCMn;
	}

	public void setdCMn(double dCMn) {
		this.dCMn = dCMn;
	}

	public double getdCMt() {
		return dCMt;
	}

	public void setdCMt(double dCMt) {
		this.dCMt = dCMt;
	}

	public double getCMaFR() {
		return CMaFR;
	}

	public void setCMaFR(double cMaFR) {
		CMaFR = cMaFR;
	}

	public double getdCMan() {
		return dCMan;
	}

	public void setdCMan(double dCMan) {
		this.dCMan = dCMan;
	}

	public double getdCMat() {
		return dCMat;
	}

	public void setdCMat(double dCMat) {
		this.dCMat = dCMat;
	}

	public double getCNbFR() {
		return CNbFR;
	}

	public void setCNbFR(double cNbFR) {
		CNbFR = cNbFR;
	}

	public double getdCNbn() {
		return dCNbn;
	}

	public void setdCNbn(double dCNbn) {
		this.dCNbn = dCNbn;
	}

	public double getdCNbt() {
		return dCNbt;
	}

	public void setdCNbt(double dCNbt) {
		this.dCNbt = dCNbt;
	}
	

	/*
	// TEST EACH METHOD
	public static void main(String[] args) {
		System.out.println("kn=" + get_Kn_vs_FRn(1.4,45.));		// Kn = 1.8846
		System.out.println();
		System.out.println("kc=" + get_Kc_vs_FR(8.69));			// Kc = 1.0551
		System.out.println();
		System.out.println("kt=" + get_Kt_vs_FRt(2.6,11));	// Kt = 0.718095967
		System.out.println();
		System.out.println("CM0_FR=" + get_CM0_FR_vs_FR(10,0.5));// -0.2952
		System.out.println();
		System.out.println("dCM_nose=" + get_dCM_nose_vs_wshield(45,1.4)); //-0.0129
		System.out.println();
		System.out.println("dCM_tail=" + get_dCM_tail_vs_upsweep(18,2.83));// 0.0492
		System.out.println();
		System.out.println("CMa=" + get_CMa_FR_vs_FR(12,0.5)); // 0.2775
		System.out.println();
		System.out.println("dCMa_nose=" + get_dCMa_nose_vs_wshield(50,1.7)); // 0.0035
		System.out.println();
		System.out.println("dCMa_tail=" + get_dCMa_tail_vs_upsweep(10,2.5)); // 0.0111
		System.out.println();
		System.out.println("CNb=" + get_CNb_FR_vs_FR(8,0.6)); //-0.1707
 		System.out.println();
		System.out.println("dCNb_nose=" +get_dCNb_nose_vs_FRn(1.7,0.35)); //0.000176805415604761
		System.out.println();
		System.out.println("dCNb_tail=" +get_dCNb_tail_vs_FRt(2.9,0.6)); // -0.0024
	}*/
	
}



// ------------------------------------------ Old snippet -----------------------------------------------------------------------------

/*	public static final String FusDesDatabaseFileName = "FusDes_database.h5";

private static final URL FusDesDatabaseFileNameWithPath = AerodynamicDatabaseReader.class.getResource(MyConfiguration.databaseFolderPath);
private static final MyHDFReader FusDesDatabase = new MyHDFReader(FusDesDatabaseFileNameWithPath.getPath() + File.separator + FusDesDatabaseFileName);

// Drag Coeff.
private static final MyInterpolatingFunction kn_vs_FRn = FusDesDatabase.interpolate2DFromDatasetFunction("Kn_vs_FRn");		// 2D: z=f(x,y) 
private static final MyInterpolatingFunction kc_vs_FR  = FusDesDatabase.interpolate1DFromDatasetFunction("Kc_vs_FR");		// 1D: y=f(x)
private static final MyInterpolatingFunction kt_vs_FRt = FusDesDatabase.interpolate2DFromDatasetFunction("Kt_vs_FRt");

// Pitching Moment Coeff.
private static final MyInterpolatingFunction CM0_FR_vs_FR 		 = FusDesDatabase.interpolate2DFromDatasetFunction("CM0_FR_vs_FR");
private static final MyInterpolatingFunction dCM_nose_vs_wshield = FusDesDatabase.interpolate2DFromDatasetFunction("dCM_nose_vs_wshield");
private static final MyInterpolatingFunction dCM_tail_vs_upsweep = FusDesDatabase.interpolate2DFromDatasetFunction("dCM_tail_vs_upsweep");

// Pitching Moment Derivative Coeff.
private static final MyInterpolatingFunction CMa_FR_vs_FR 	  	  = FusDesDatabase.interpolate2DFromDatasetFunction("CMa_FR_vs_FR");
private static final MyInterpolatingFunction dCMa_nose_vs_wshield = FusDesDatabase.interpolate2DFromDatasetFunction("dCMa_nose_vs_wshield");
private static final MyInterpolatingFunction dCMa_tail_vs_upsweep = FusDesDatabase.interpolate2DFromDatasetFunction("dCMa_tail_vs_upsweep");

// Yawing Moment Derivative Coeff.
private static final MyInterpolatingFunction CNb_FR_vs_FR 	  = FusDesDatabase.interpolate2DFromDatasetFunction("CNb_FR_vs_FR");
private static final MyInterpolatingFunction dCNb_nose_vs_FRn = FusDesDatabase.interpolate2DFromDatasetFunction("dCNb_nose_vs_FRn");
private static final MyInterpolatingFunction dCNb_tail_vs_FRt = FusDesDatabase.interpolate2DFromDatasetFunction("dCNb_tail_vs_FRt");
*/	
