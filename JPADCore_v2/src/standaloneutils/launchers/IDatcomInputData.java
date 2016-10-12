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
	
}
