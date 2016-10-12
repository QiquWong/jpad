package standaloneutils.launchers;

import java.util.List;

public interface IDatcomInputData {

	boolean getTRIM();
	void setTRIM(boolean val);
	
	boolean getDAMP();
	void setDAMP(boolean val);
	
	boolean getPART();
	void setPART(boolean val);
	
	String getDERIV();
	void setDERIV(String val);
	
	List<Double> getFLTCON_MACH();
	void setFLTCON_MACH(List<Double> vec);
	int getFLTCON_NMACH();

	List<Double> getFLTCON_ALT();
	void setFLTCON_ALT(List<Double> vec);
	int getFLTCON_NALT();

	List<Double> getFLTCON_ALSCHD();
	void setFLTCON_ALSCHD(List<Double> vec);
	int getFLTCON_NALPHA();

	Double getFLTCON_GAMMA();
	void setFLTCON_GAMMA(Double val);

	int getFLTCON_LOOP();
	void setFLTCON_LOOP(int val);

	Double getFLTCON_RNNUB();
	void setFLTCON_RNNUB(Double val);
	
	Double getOPTINS_BLREF();
	void setOPTINS_BLREF(Double val);
	
	Double getOPTINS_SREF();
	void setOPTINS_SREF(Double val);
	
	Double getOPTINS_CBARR();
	void setOPTINS_CBARR(Double val);

	Double getSYNTHS_XW();
	void setSYNTHS_XW(Double val);
	
	Double getSYNTHS_ALIW();
	void setSYNTHS_ALIW(Double val);
	
	Double getSYNTHS_XCG();
	void setSYNTHS_XCG(Double val);
	
	Double getSYNTHS_ZCG();
	void setSYNTHS_ZCG(Double val);
	
	Double getSYNTHS_XH();
	void setSYNTHS_XH(Double val);
	
	Double getSYNTHS_ZH();
	void setSYNTHS_ZH(Double val);
	
	Double getSYNTHS_XV();
	void setSYNTHS_XV(Double val);
	
	Double getSYNTHS_ZV();
	void setSYNTHS_ZV(Double val);
	
	Double getSYNTHS_XVF();
	void setSYNTHS_XVF(Double val);
	
	Double getSYNTHS_ZVF();
	void setSYNTHS_ZVF(Double val);
	
	boolean getSYNTHS_VERTUP();
	void setSYNTHS_VERTUP(Double val);

	int getBODY_NX();
	void setBODY_NX(int val);
	
	List<Double> getBODY_X();
	void setBODY_X(List<Double> vec);
	
	List<Double> getBODY_ZU();
	void setBODY_ZU(List<Double> vec);
	List<Double> getBODY_ZL();
	void setBODY_ZL(List<Double> vec);
	List<Double> getBODY_S();
	void setBODY_S(List<Double> vec);

	Double getWGPLNF_CHRDR();
	void setWGPLNF_CHRDR(Double val);
	
	Double getWGPLNF_CHRDBP();
	void setWGPLNF_CHRDBP(Double val);
	
	Double getWGPLNF_CHRDTP();
	void setWGPLNF_CHRDTP(Double val);
	
	Double getWGPLNF_SSPN();
	void setWGPLNF_SSPN(Double val);
	
	Double getWGPLNF_SSPNE();
	void setWGPLNF_SSPNE(Double val);
	
	Double getWGPLNF_SAVSI();
	void setWGPLNF_SAVSI(Double val);
	
	Double getWGPLNF_SAVSO();
	void setWGPLNF_SAVSO(Double val);
	
	Double getWGPLNF_CHSTAT();
	void setWGPLNF_CHSTAT(Double val);
	
	Double getWGPLNF_TWISTA();
	void setWGPLNF_TWISTA(Double val);
	
	Double getWGPLNF_SSPNDD();
	void setWGPLNF_SSPNDD(Double val);
	
	Double getWGPLNF_DHDADI();
	void setWGPLNF_DHDADI(Double val);
	
	Double getWGPLNF_DHDADO();
	void setWGPLNF_DHDADO(Double val);
	
	int getWGPLNF_TYPE();
	void setWGPLNF_TYPE(int val);

	Double getVTPLNF_CHRDR();
	void setVTPLNF_CHRDR(Double val);
	
	Double getVTPLNF_CHRDBP();
	void setVTPLNF_CHRDBP(Double val);
	
	Double getVTPLNF_CHRDTP();
	void setVTPLNF_CHRDTP(Double val);
	
	Double getVTPLNF_SSPN();
	void setVTPLNF_SSPN(Double val);
	
	Double getVTPLNF_SSPNE();
	void setVTPLNF_SSPNE(Double val);
	
	Double getVTPLNF_SAVSI();
	void setVTPLNF_SAVSI(Double val);
	
	Double getVTPLNF_SAVSO();
	void setVTPLNF_SAVSO(Double val);
	
	Double getVTPLNF_CHSTAT();
	void setVTPLNF_CHSTAT(Double val);
	
	Double getVTPLNF_TWISTA();
	void setVTPLNF_TWISTA(Double val);
	
	Double getVTPLNF_SSPNDD();
	void setVTPLNF_SSPNDD(Double val);
	
	Double getVTPLNF_DHDADI();
	void setVTPLNF_DHDADI(Double val);
	
	Double getVTPLNF_DHDADO();
	void setVTPLNF_DHDADO(Double val);
	
	int getVTPLNF_TYPE();
	void setVTPLNF_TYPE(int val);

	Double getHTPLNF_CHRDR();
	void setHTPLNF_CHRDR(Double val);
	
	Double getHTPLNF_CHRDBP();
	void setHTPLNF_CHRDBP(Double val);
	
	Double getHTPLNF_CHRDTP();
	void setHTPLNF_CHRDTP(Double val);
	
	Double getHTPLNF_SSPN();
	void setHTPLNF_SSPN(Double val);
	
	Double getHTPLNF_SSPNE();
	void setHTPLNF_SSPNE(Double val);
	
	Double getHTPLNF_SAVSI();
	void setHTPLNF_SAVSI(Double val);
	
	Double getHTPLNF_SAVSO();
	void setHTPLNF_SAVSO(Double val);
	
	Double getHTPLNF_CHSTAT();
	void setHTPLNF_CHSTAT(Double val);
	
	Double getHTPLNF_TWISTA();
	void setHTPLNF_TWISTA(Double val);
	
	Double getHTPLNF_SSPNDD();
	void setHTPLNF_SSPNDD(Double val);
	
	Double getHTPLNF_DHDADI();
	void setHTPLNF_DHDADI(Double val);
	
	Double getHTPLNF_DHDADO();
	void setHTPLNF_DHDADO(Double val);
	
	int getHTPLNF_TYPE();
	void setHTPLNF_TYPE(int val);

	Double getJETPWR_AIETLJ();
	void setJETPWR_AIETLJ(Double val);
	
	Double getJETPWR_AMBSTP();
	void setJETPWR_AMBSTP(Double val);
	
	Double getJETPWR_AMBTMP();
	void setJETPWR_AMBTMP(Double val);
	
	List<Double> getJETPWR_JEALOC();
	void setJETPWR_JEALOC(List<Double> vec);
	
	Double getJETPWR_JELLOC();
	void setJETPWR_JELLOC(Double val);
	
	Double getJETPWR_JERAD();
	void setJETPWR_JERAD(Double val);
	
	Double getJETPWR_JEVLOC();
	void setJETPWR_JEVLOC(Double val);
	
	Double getJETPWR_JIALOC();
	void setJETPWR_JIALOC(Double val);
	
	Double getJETPWR_JINLTA();
	void setJETPWR_JINLTA(Double val);
	
	Double getJETPWR_THSTCJ();
	void setJETPWR_THSTCJ(Double val);
	
	Double getJETPWR_JEANGL();
	void setJETPWR_JEANGL(Double val);
	
	int getJETPWR_NENGSJ();
	void setJETPWR_NENGSJ(int val);

	int getPROPWR_NENGSP();
	void setPROPWR_NENGSP(int val);
	
	Double getPROPWR_AIETLP();
	void setPROPWR_AIETLP(Double val);
	
	Double getPROPWR_THSTCP();
	void setPROPWR_THSTCP(Double val);
	
	Double getPROPWR_PHALOC();
	void setPROPWR_PHALOC(Double val);
	
	Double getPROPWR_PHVLOC();
	void setPROPWR_PHVLOC(Double val);
	
	Double getPROPWR_PRPRAD();
	void setPROPWR_PRPRAD(Double val);
	
	Double getPROPWR_ENGFCT();
	void setPROPWR_ENGFCT(Double val);
	
	Double getPROPWR_BWAPR3();
	void setPROPWR_BWAPR3(Double val);
	
	Double getPROPWR_BWAPR6();
	void setPROPWR_BWAPR6(Double val);
	
	Double getPROPWR_BWAPR9();
	void setPROPWR_BWAPR9(Double val);
	
	int getPROPWR_NOPBPE();
	void setPROPWR_NOPBPE(int val);
	
	Double getPROPWR_BAPR75();
	void setPROPWR_BAPR75(Double val);
	
	Double getPROPWR_YP();
	void setPROPWR_YP(Double val);
	
	boolean getPROPWR_CROT();
	void setPROPWR_CROT(boolean val);

	int getSYMFLP_FTYPE();
	void setSYMFLP_FTYPE(int val);
	
	int getSYMFLP_NDELTA();
	void setSYMFLP_NDELTA(int val);
	
	List<Double> getSYMFLP_DELTA();
	void setSYMFLP_DELTA(List<Double> vec);
	
	Double getSYMFLP_SPANFI();
	void setSYMFLP_SPANFI(Double val);
	
	Double getSYMFLP_SPANFO();
	void setSYMFLP_SPANFO(Double val);
	
	Double getSYMFLP_CHRDFI();
	void setSYMFLP_CHRDFI(Double val);
	
	Double getSYMFLP_CHRDFO();
	void setSYMFLP_CHRDFO(Double val);
	
	int getSYMFLP_NTYPE();
	void setSYMFLP_NTYPE(int val);
	
	Double getSYMFLP_CB();
	void setSYMFLP_CB(Double val);
	
	Double getSYMFLP_TC();
	void setSYMFLP_TC(Double val);
	
	Double getSYMFLP_PHETE();
	void setSYMFLP_PHETE(Double val);
	
	Double getSYMFLP_PHETEP();
	void setSYMFLP_PHETEP(Double val);

}
