package standaloneutils.launchers;

import java.util.Arrays;
import java.util.List;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface IDatcomInputData {
	
	String getDescription();
	
	DatcomEngineType getEngineType();
	
	String getDIM();
	boolean getTRIM();
	boolean getDAMP();
	boolean getPART();
	String getDERIV();
	
	List<Double> getFLTCON_MACH();
	// int getFLTCON_NMACH();
	List<Double> getFLTCON_ALT();
	// int getFLTCON_NALT();
	List<Double> getFLTCON_ALSCHD();
	// int getFLTCON_NALPHA();
	Double getFLTCON_GAMMA();
	int getFLTCON_LOOP();
	Double getFLTCON_RNNUB();

	Double getOPTINS_BLREF();
	Double getOPTINS_SREF();
	Double getOPTINS_CBARR();

	Double getSYNTHS_XW();
	Double getSYNTHS_ZW();
	Double getSYNTHS_ALIW();
	Double getSYNTHS_XCG();
	Double getSYNTHS_ZCG();
	Double getSYNTHS_XH();
	Double getSYNTHS_ZH();
	Double getSYNTHS_XV();
	Double getSYNTHS_ZV();
	Double getSYNTHS_XVF();
	Double getSYNTHS_ZVF();
	boolean getSYNTHS_VERTUP();

	// int getBODY_NX();
	Double getBODY_BNOSE();
	Double getBODY_BTAIL();
	Double getBODY_BLA();
	List<Double> getBODY_X();
	List<Double> getBODY_ZU();
	List<Double> getBODY_ZL();
	List<Double> getBODY_S();

	Double getWGPLNF_CHRDR();
	Double getWGPLNF_CHRDBP();
	Double getWGPLNF_CHRDTP();
	Double getWGPLNF_SSPN();
	Double getWGPLNF_SSPNE();
	Double getWGPLNF_SAVSI();
	Double getWGPLNF_SAVSO();
	Double getWGPLNF_CHSTAT();
	Double getWGPLNF_TWISTA();
	Double getWGPLNF_SSPNDD();
	Double getWGPLNF_DHDADI();
	Double getWGPLNF_DHDADO();
	int getWGPLNF_TYPE();

	Double getVTPLNF_CHRDR();
	Double getVTPLNF_CHRDBP();
	Double getVTPLNF_CHRDTP();
	Double getVTPLNF_SSPN();
	Double getVTPLNF_SSPNE();
	Double getVTPLNF_SAVSI();
	Double getVTPLNF_SAVSO();
	Double getVTPLNF_CHSTAT();
	Double getVTPLNF_TWISTA();
	Double getVTPLNF_SSPNDD();
	Double getVTPLNF_DHDADI();
	Double getVTPLNF_DHDADO();
	int getVTPLNF_TYPE();

	Double getHTPLNF_CHRDR();
	Double getHTPLNF_CHRDBP();
	Double getHTPLNF_CHRDTP();
	Double getHTPLNF_SSPN();
	Double getHTPLNF_SSPNE();
	Double getHTPLNF_SAVSI();
	Double getHTPLNF_SAVSO();
	Double getHTPLNF_CHSTAT();
	Double getHTPLNF_TWISTA();
	Double getHTPLNF_SSPNDD();
	Double getHTPLNF_DHDADI();
	Double getHTPLNF_DHDADO();
	int getHTPLNF_TYPE();

	Double getJETPWR_AIETLJ();
	Double getJETPWR_AMBSTP();
	Double getJETPWR_AMBTMP();
	List<Double> getJETPWR_JEALOC();
	Double getJETPWR_JELLOC();
	Double getJETPWR_JERAD();
	Double getJETPWR_JEVLOC();
	Double getJETPWR_JIALOC();
	Double getJETPWR_JINLTA();
	Double getJETPWR_THSTCJ();
	Double getJETPWR_JEANGL();
	int getJETPWR_NENGSJ();

	int getPROPWR_NENGSP();
	Double getPROPWR_AIETLP();
	Double getPROPWR_THSTCP();
	Double getPROPWR_PHALOC();
	Double getPROPWR_PHVLOC();
	Double getPROPWR_PRPRAD();
	Double getPROPWR_ENGFCT();
	Double getPROPWR_BWAPR3();
	Double getPROPWR_BWAPR6();
	Double getPROPWR_BWAPR9();
	int getPROPWR_NOPBPE();
	Double getPROPWR_BAPR75();
	Double getPROPWR_YP();
	boolean getPROPWR_CROT();

	int getSYMFLP_FTYPE();
	int getSYMFLP_NDELTA();
	List<Double> getSYMFLP_DELTA();
	Double getSYMFLP_SPANFI();
	Double getSYMFLP_SPANFO();
	Double getSYMFLP_CHRDFI();
	Double getSYMFLP_CHRDFO();
	int getSYMFLP_NTYPE();
	Double getSYMFLP_CB();
	Double getSYMFLP_TC();
	Double getSYMFLP_PHETE();
	Double getSYMFLP_PHETEP();
	
	/** Builder of IDatcomInputData instances. */
	class Builder extends IDatcomInputData_Builder { 
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("(c) Agostino De Marco - X-airplane");
			setEngineType(DatcomEngineType.JET);
			setDIM("FT");
			setTRIM(true);
			setDAMP(true);
			setPART(true);
			setDERIV("RAD");

			addAllFLTCON_MACH(Arrays.asList( // list of Mach numbers
					0.3));
			addAllFLTCON_MACH(Arrays.asList( // list of Altitudes
					1500.0));
			addAllFLTCON_ALSCHD(Arrays.asList( // list of Altitudes
					-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0,
						10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0));
			setFLTCON_GAMMA(0.0);
			setFLTCON_RNNUB(20120887.0);
			
			setOPTINS_BLREF(93.0);
			setOPTINS_SREF(1329.0);
			setOPTINS_CBARR(14.3);

			setSYNTHS_XW(28.3);
			setSYNTHS_ZW(-1.4);
			setSYNTHS_ALIW(1.0);
			setSYNTHS_XCG(41.3);
			setSYNTHS_ZCG(0.0);
			setSYNTHS_XH(76.6);
			setSYNTHS_ZH(6.2);
			setSYNTHS_XV(71.1);
			setSYNTHS_ZV(7.6);
			setSYNTHS_XVF(66.2);
			setSYNTHS_ZVF(13.1);
			setSYNTHS_VERTUP(true);

			setBODY_BNOSE(2.0);
			setBODY_BTAIL(2.0);
			setBODY_BLA(2.0);
			addAllBODY_X(Arrays.asList( // list of X
					0., 1.38, 4.83, 6.90, 8.97, 13.8, 27.6, 55.2, 65.6, 69.0, 75.9, 82.8, 89.7, 90.4));
			addAllBODY_ZU(Arrays.asList( // list of ZU
					0.69, 2.07, 3.45, 4.38, 5.87, 6.90, 8.28, 8.28, 8.28, 8.28, 7.94, 7.59, 7.50, 6.9));
			addAllBODY_ZL(Arrays.asList( // list of ZL
					-0.35, -1.73, -3.45, -3.80, -4.14, -4.49, -4.83, -4.83, -3.45, -2.76, -0.81, 1.04, 4.14, 6.21));
			addAllBODY_ZL(Arrays.asList( // list of S
					0.55, 8.23, 28.89, 44.31, 65.06, 92.63, 127.81, 127.81, 108.11, 95.68, 56.88, 28.39, 3.64, 0.11));
			
		}
//		@Override
//		public String getDescription() {
//			return ">>>"+super.getDescription()+"<<<";
//		}
		@Override
		public Builder setSYNTHS_XW(Double val) {
			// Check single-field (argument) constraints in the setter method.
			checkArgument(val >= 0);
			return super.setSYNTHS_XW(val);
		}
		@Override 
		public IDatcomInputData build() {
			// Check cross-field (state) constraints in the build method.
			IDatcomInputData data = super.build();
			checkState(!data.getDescription().contains("Agostino De Marco"));
			return data;
		}
	}

}
