package standaloneutils.launchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

public class DatcomInputData0 implements IDatcomInputData0 {

	private DatcomEngineType _engineType;
	
	private String _DIM;
	private boolean _TRIM;
	private boolean _DAMP;
	private boolean _PART;
	private String _DERIV;
	
	private int _FLTCON_NMACH = 0;
	private List<Double> _FLTCON_MACH = new ArrayList<Double>();
	private int _FLTCON_NALT = 0;
	private List<Double> _FLTCON_ALT = new ArrayList<Double>();
	private int _FLTCON_NALPHA = 0;
	private List<Double> _FLTCON_ALSCHD = new ArrayList<Double>();
	private Double _FLTCON_GAMMA = 0.0;
	private int _FLTCON_LOOP = 1;
	private Double _FLTCON_RNNUB = 1000000.0;

	private Double _OPTINS_BLREF;
	private Double _OPTINS_SREF;
	private Double _OPTINS_CBARR;

	private Double _SYNTHS_XW;
	private Double _SYNTHS_ZW;
	private Double _SYNTHS_ALIW;
	private Double _SYNTHS_XCG;
	private Double _SYNTHS_ZCG;
	private Double _SYNTHS_XH;
	private Double _SYNTHS_ZH;
	private Double _SYNTHS_XV;
	private Double _SYNTHS_ZV;
	private Double _SYNTHS_XVF;
	private Double _SYNTHS_ZVF;
	private boolean _SYNTHS_VERTUP;

	private int _BODY_NX;
	private Double _BODY_BNOSE;
	private Double _BODY_BTAIL;
	private Double _BODY_BLA;
	private List<Double> _BODY_X = new ArrayList<Double>();
	private List<Double> _BODY_ZU = new ArrayList<Double>();
	private List<Double> _BODY_ZL = new ArrayList<Double>();
	private List<Double> _BODY_S = new ArrayList<Double>();
	
    private Double _WGPLNF_CHRDR;
    private Double _WGPLNF_CHRDBP;
    private Double _WGPLNF_CHRDTP;
    private Double _WGPLNF_SSPN;
    private Double _WGPLNF_SSPNE;
    private Double _WGPLNF_SAVSI;
    private Double _WGPLNF_SAVSO;
    private Double _WGPLNF_CHSTAT;
    private Double _WGPLNF_TWISTA;
    private Double _WGPLNF_SSPNDD;
    private Double _WGPLNF_DHDADI;
    private Double _WGPLNF_DHDADO;
    private int _WGPLNF_TYPE;

    private Double _VTPLNF_CHRDR;
    private Double _VTPLNF_CHRDBP;
    private Double _VTPLNF_CHRDTP;
    private Double _VTPLNF_SSPN;
    private Double _VTPLNF_SSPNE;
    private Double _VTPLNF_SAVSI;
    private Double _VTPLNF_SAVSO;
    private Double _VTPLNF_CHSTAT;
    private Double _VTPLNF_TWISTA;
    private Double _VTPLNF_SSPNDD;
    private Double _VTPLNF_DHDADI;
    private Double _VTPLNF_DHDADO;
    private int _VTPLNF_TYPE;

    private Double _HTPLNF_CHRDR;
    private Double _HTPLNF_CHRDBP;
    private Double _HTPLNF_CHRDTP;
    private Double _HTPLNF_SSPN;
    private Double _HTPLNF_SSPNE;
    private Double _HTPLNF_SAVSI;
    private Double _HTPLNF_SAVSO;
    private Double _HTPLNF_CHSTAT;
    private Double _HTPLNF_TWISTA;
    private Double _HTPLNF_SSPNDD;
    private Double _HTPLNF_DHDADI;
    private Double _HTPLNF_DHDADO;
    private int _HTPLNF_TYPE;
    
    private Double _JETPWR_AIETLJ;
    private Double _JETPWR_AMBSTP;
    private Double _JETPWR_AMBTMP;
    private List<Double> _JETPWR_JEALOC;
    private Double _JETPWR_JELLOC;
    private Double _JETPWR_JERAD;
    private Double _JETPWR_JEVLOC;
    private Double _JETPWR_JIALOC;
    private Double _JETPWR_JINLTA;
    private Double _JETPWR_THSTCJ;
    private Double _JETPWR_JEANGL;
    private int _JETPWR_NENGSJ;

    private int _PROPWR_NENGSP;
    private Double _PROPWR_AIETLP;
    private Double _PROPWR_THSTCP;
    private Double _PROPWR_PHALOC;
    private Double _PROPWR_PHVLOC;
    private Double _PROPWR_PRPRAD;
    private Double _PROPWR_ENGFCT;
    private Double _PROPWR_BWAPR3;
    private Double _PROPWR_BWAPR6;
    private Double _PROPWR_BWAPR9;
    private int _PROPWR_NOPBPE;
    private Double _PROPWR_BAPR75;
    private Double _PROPWR_YP;
    private boolean _PROPWR_CROT;
    
    private int _SYMFLP_FTYPE;
    private int _SYMFLP_NDELTA;
    private List<Double> _SYMFLP_DELTA;
    private Double _SYMFLP_SPANFI;
    private Double _SYMFLP_SPANFO;
    private Double _SYMFLP_CHRDFI;
    private Double _SYMFLP_CHRDFO;
    private int _SYMFLP_NTYPE;
    private Double _SYMFLP_CB;
    private Double _SYMFLP_TC;
    private Double _SYMFLP_PHETE;
    private Double _SYMFLP_PHETEP;

	@Override
	public DatcomEngineType getEngineType() {
		return _engineType;
	}

	@Override
	public void setEngineType(DatcomEngineType type) {
		_engineType = type;
	}
    
	@Override
	public String getDIM() {
		return _DIM;
	}

	@Override
	public void setDIM(String val) {
		String val1 = val.toUpperCase().trim();
		if (val1.equals("FT") || val1.equals("IN")
				|| val1.equals("M") || val1.equals("CM"))
		_DIM = val1.toUpperCase();
	}

	@Override
	public boolean getTRIM() {
		return _TRIM;
	}

	@Override
	public void setTRIM(boolean val) {
		_TRIM = val;
	}

	@Override
	public boolean getDAMP() {
		return _DAMP;
	}

	@Override
	public void setDAMP(boolean val) {
		_DAMP = val;
	}

	@Override
	public boolean getPART() {
		return _PART;
	}

	@Override
	public void setPART(boolean val) {
		_PART = val;
	}

	@Override
	public String getDERIV() {
		return _DERIV;
	}

	@Override
	public void setDERIV(String val) {
		switch(val.toUpperCase()) {
		case "RAD": case "DEG":
			_DERIV = val;
		}
	}

	@Override
	public List<Double> getFLTCON_MACH() {
		return _FLTCON_MACH;
	}

	@Override
	public void setFLTCON_MACH(List<Double> vec) {
		_FLTCON_MACH.clear();
		_FLTCON_MACH = vec;
		_FLTCON_NMACH = vec.size();		
	}
	
	
	@Override
	public int getFLTCON_NMACH() {
		return _FLTCON_NMACH;
	}

	@Override
	public List<Double> getFLTCON_ALT() {
		return _FLTCON_ALT;
	}

	@Override
	public void setFLTCON_ALT(List<Double> vec) {
		_FLTCON_ALT.clear();
		_FLTCON_ALT = vec;
		_FLTCON_NALT = vec.size();		
	}

	@Override
	public int getFLTCON_NALT() {
		return _FLTCON_NALT;
	}

	@Override
	public List<Double> getFLTCON_ALSCHD() {
		return _FLTCON_ALSCHD;
	}

	@Override
	public void setFLTCON_ALSCHD(List<Double> vec) {
		_FLTCON_ALSCHD.clear();
		_FLTCON_ALSCHD = vec;
		_FLTCON_NALPHA = vec.size();		
	}

	@Override
	public int getFLTCON_NALPHA() {
		return _FLTCON_NALPHA;
	}

	@Override
	public Double getFLTCON_GAMMA() {
		return _FLTCON_GAMMA;
	}

	@Override
	public void setFLTCON_GAMMA(Double val) {
		_FLTCON_GAMMA = val;
	}

	@Override
	public int getFLTCON_LOOP() {
		return _FLTCON_LOOP;
	}

	@Override
	public void setFLTCON_LOOP(int val) {
		_FLTCON_LOOP = val;
	}

	@Override
	public Double getFLTCON_RNNUB() {
		return _FLTCON_RNNUB;
	}

	@Override
	public void setFLTCON_RNNUB(Double val) {
		_FLTCON_RNNUB = val;
		
	}

	@Override
	public Double getOPTINS_BLREF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOPTINS_BLREF(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getOPTINS_SREF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOPTINS_SREF(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getOPTINS_CBARR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOPTINS_CBARR(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_XW() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_XW(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_ZW() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_ZW(Double val) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Double getSYNTHS_ALIW() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_ALIW(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_XCG() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_XCG(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_ZCG() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_ZCG(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_XH() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_XH(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_ZH() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_ZH(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_XV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_XV(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_ZV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_ZV(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_XVF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_XVF(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYNTHS_ZVF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYNTHS_ZVF(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getSYNTHS_VERTUP() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSYNTHS_VERTUP(Double val) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public List<Double> getBODY_BNOSE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBODY_BNOSE(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getBODY_BTAIL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBODY_BTAIL(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getBODY_BLA() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBODY_BLA(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getBODY_NX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBODY_NX(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getBODY_X() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBODY_X(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getBODY_ZU() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBODY_ZU(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getBODY_ZL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBODY_ZL(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getBODY_S() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBODY_S(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_CHRDR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_CHRDR(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_CHRDBP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_CHRDBP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_CHRDTP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_CHRDTP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_SSPN() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_SSPN(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_SSPNE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_SSPNE(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_SAVSI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_SAVSI(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_SAVSO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_SAVSO(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_CHSTAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_CHSTAT(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_TWISTA() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_TWISTA(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_SSPNDD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_SSPNDD(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_DHDADI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_DHDADI(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getWGPLNF_DHDADO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWGPLNF_DHDADO(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getWGPLNF_TYPE() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setWGPLNF_TYPE(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_CHRDR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_CHRDR(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_CHRDBP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_CHRDBP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_CHRDTP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_CHRDTP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_SSPN() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_SSPN(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_SSPNE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_SSPNE(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_SAVSI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_SAVSI(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_SAVSO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_SAVSO(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_CHSTAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_CHSTAT(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_TWISTA() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_TWISTA(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_SSPNDD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_SSPNDD(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_DHDADI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_DHDADI(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getVTPLNF_DHDADO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVTPLNF_DHDADO(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getVTPLNF_TYPE() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVTPLNF_TYPE(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_CHRDR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_CHRDR(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_CHRDBP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_CHRDBP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_CHRDTP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_CHRDTP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_SSPN() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_SSPN(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_SSPNE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_SSPNE(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_SAVSI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_SAVSI(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_SAVSO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_SAVSO(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_CHSTAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_CHSTAT(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_TWISTA() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_TWISTA(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_SSPNDD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_SSPNDD(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_DHDADI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_DHDADI(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getHTPLNF_DHDADO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHTPLNF_DHDADO(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getHTPLNF_TYPE() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setHTPLNF_TYPE(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_AIETLJ() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_AIETLJ(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_AMBSTP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_AMBSTP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_AMBTMP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_AMBTMP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getJETPWR_JEALOC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_JEALOC(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_JELLOC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_JELLOC(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_JERAD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_JERAD(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_JEVLOC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_JEVLOC(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_JIALOC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_JIALOC(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_JINLTA() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_JINLTA(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_THSTCJ() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_THSTCJ(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getJETPWR_JEANGL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJETPWR_JEANGL(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getJETPWR_NENGSJ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setJETPWR_NENGSJ(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPROPWR_NENGSP() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPROPWR_NENGSP(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_AIETLP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_AIETLP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_THSTCP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_THSTCP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_PHALOC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_PHALOC(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_PHVLOC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_PHVLOC(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_PRPRAD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_PRPRAD(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_ENGFCT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_ENGFCT(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_BWAPR3() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_BWAPR3(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_BWAPR6() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_BWAPR6(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_BWAPR9() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_BWAPR9(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPROPWR_NOPBPE() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPROPWR_NOPBPE(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_BAPR75() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_BAPR75(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getPROPWR_YP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPROPWR_YP(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getPROPWR_CROT() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPROPWR_CROT(boolean val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSYMFLP_FTYPE() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSYMFLP_FTYPE(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSYMFLP_NDELTA() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSYMFLP_NDELTA(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getSYMFLP_DELTA() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_DELTA(List<Double> vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYMFLP_SPANFI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_SPANFI(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYMFLP_SPANFO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_SPANFO(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYMFLP_CHRDFI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_CHRDFI(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYMFLP_CHRDFO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_CHRDFO(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSYMFLP_NTYPE() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSYMFLP_NTYPE(int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYMFLP_CB() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_CB(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYMFLP_TC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_TC(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYMFLP_PHETE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_PHETE(Double val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getSYMFLP_PHETEP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSYMFLP_PHETEP(Double val) {
		// TODO Auto-generated method stub
		
	}	
	
	// Builder pattern via a nested public static class

	public static class DatcomInputData0Builder {
		
		// required parameters
		DatcomEngineType __engineType;

		// optional parameters ... defaults
		private String __DIM;		
		private boolean __TRIM;
		private boolean __DAMP;
		private boolean __PART;
		private String __DERIV;

		private int __FLTCON_NMACH;
		private List<Double> __FLTCON_MACH = new ArrayList<Double>();
		private int __FLTCON_NALT;
		private List<Double> __FLTCON_ALT = new ArrayList<Double>();
		private int __FLTCON_NALPHA;
		private List<Double> __FLTCON_ALSCHD = new ArrayList<Double>();
		private Double __FLTCON_GAMMA;
		private int __FLTCON_LOOP;
		private Double __FLTCON_RNNUB;
		
		private Double __OPTINS_BLREF;
		private Double __OPTINS_SREF;
		private Double __OPTINS_CBARR;

		private Double __SYNTHS_XW;
		private Double __SYNTHS_ZW;
		private Double __SYNTHS_ALIW;
		private Double __SYNTHS_XCG;
		private Double __SYNTHS_ZCG;
		private Double __SYNTHS_XH;
		private Double __SYNTHS_ZH;
		private Double __SYNTHS_XV;
		private Double __SYNTHS_ZV;
		private Double __SYNTHS_XVF;
		private Double __SYNTHS_ZVF;
		private boolean __SYNTHS_VERTUP;

		private int __BODY_NX;
		private Double __BODY_BNOSE;
		private Double __BODY_BTAIL;
		private Double __BODY_BLA;
		private List<Double> __BODY_X = new ArrayList<Double>();
		private List<Double> __BODY_ZU = new ArrayList<Double>();
		private List<Double> __BODY_ZL = new ArrayList<Double>();
		private List<Double> __BODY_S = new ArrayList<Double>();
		
	    private Double __WGPLNF_CHRDR;
	    private Double __WGPLNF_CHRDBP;
	    private Double __WGPLNF_CHRDTP;
	    private Double __WGPLNF_SSPN;
	    private Double __WGPLNF_SSPNE;
	    private Double __WGPLNF_SAVSI;
	    private Double __WGPLNF_SAVSO;
	    private Double __WGPLNF_CHSTAT;
	    private Double __WGPLNF_TWISTA;
	    private Double __WGPLNF_SSPNDD;
	    private Double __WGPLNF_DHDADI;
	    private Double __WGPLNF_DHDADO;
	    private int __WGPLNF_TYPE;

	    private Double __VTPLNF_CHRDR;
	    private Double __VTPLNF_CHRDBP;
	    private Double __VTPLNF_CHRDTP;
	    private Double __VTPLNF_SSPN;
	    private Double __VTPLNF_SSPNE;
	    private Double __VTPLNF_SAVSI;
	    private Double __VTPLNF_SAVSO;
	    private Double __VTPLNF_CHSTAT;
	    private Double __VTPLNF_TWISTA;
	    private Double __VTPLNF_SSPNDD;
	    private Double __VTPLNF_DHDADI;
	    private Double __VTPLNF_DHDADO;
	    private int __VTPLNF_TYPE;

	    private Double __HTPLNF_CHRDR;
	    private Double __HTPLNF_CHRDBP;
	    private Double __HTPLNF_CHRDTP;
	    private Double __HTPLNF_SSPN;
	    private Double __HTPLNF_SSPNE;
	    private Double __HTPLNF_SAVSI;
	    private Double __HTPLNF_SAVSO;
	    private Double __HTPLNF_CHSTAT;
	    private Double __HTPLNF_TWISTA;
	    private Double __HTPLNF_SSPNDD;
	    private Double __HTPLNF_DHDADI;
	    private Double __HTPLNF_DHDADO;
	    private int __HTPLNF_TYPE;
	    
	    private Double __JETPWR_AIETLJ;
	    private Double __JETPWR_AMBSTP;
	    private Double __JETPWR_AMBTMP;
	    private List<Double> __JETPWR_JEALOC = new ArrayList<Double>();
	    private Double __JETPWR_JELLOC;
	    private Double __JETPWR_JERAD;
	    private Double __JETPWR_JEVLOC;
	    private Double __JETPWR_JIALOC;
	    private Double __JETPWR_JINLTA;
	    private Double __JETPWR_THSTCJ;
	    private Double __JETPWR_JEANGL;
	    private int __JETPWR_NENGSJ;

	    private int __PROPWR_NENGSP;
	    private Double __PROPWR_AIETLP;
	    private Double __PROPWR_THSTCP;
	    private Double __PROPWR_PHALOC;
	    private Double __PROPWR_PHVLOC;
	    private Double __PROPWR_PRPRAD;
	    private Double __PROPWR_ENGFCT;
	    private Double __PROPWR_BWAPR3;
	    private Double __PROPWR_BWAPR6;
	    private Double __PROPWR_BWAPR9;
	    private int __PROPWR_NOPBPE;
	    private Double __PROPWR_BAPR75;
	    private Double __PROPWR_YP;
	    private boolean __PROPWR_CROT;
	    
	    private int __SYMFLP_FTYPE;
	    private int __SYMFLP_NDELTA;
	    private List<Double> __SYMFLP_DELTA = new ArrayList<Double>();
	    private Double __SYMFLP_SPANFI;
	    private Double __SYMFLP_SPANFO;
	    private Double __SYMFLP_CHRDFI;
	    private Double __SYMFLP_CHRDFO;
	    private int __SYMFLP_NTYPE;
	    private Double __SYMFLP_CB;
	    private Double __SYMFLP_TC;
	    private Double __SYMFLP_PHETE;
	    private Double __SYMFLP_PHETEP;
		
		public DatcomInputData0Builder(
				DatcomEngineType type
				){
			this.__engineType = type; 
			initializeDefaultVariables();
		}

		public DatcomInputData0Builder commandTRIM(boolean val) {
			this.__TRIM = val;
			return this;
		}

		public DatcomInputData0Builder commandDAMP(boolean val) {
			this.__DAMP = val;
			return this;
		}
		
		public DatcomInputData0Builder commandPART(boolean val) {
			this.__PART = val;
			return this;
		}

		public DatcomInputData0Builder commandDERIV(String val) {
			this.__DERIV = val;
			return this;
		}
		
		public DatcomInputData0Builder fltconMACH(List<Double> vec) {
			this.__FLTCON_NMACH = vec.size();
			this.__FLTCON_MACH = vec;
			return this;
		}

		public DatcomInputData0Builder fltconALT(List<Double> vec) {
			this.__FLTCON_NALT = vec.size();
			this.__FLTCON_ALT = vec;
			return this;
		}

		public DatcomInputData0Builder fltconALSCHD(List<Double> vec) {
			this.__FLTCON_NALPHA = vec.size();
			this.__FLTCON_ALSCHD = vec;
			return this;
		}

		public DatcomInputData0Builder fltconGAMMA(Double val) {
			this.__FLTCON_GAMMA = val;
			return this;
		}

		public DatcomInputData0Builder fltconLOOP(int val) {
			this.__FLTCON_LOOP = val;
			return this;
		}

		public DatcomInputData0Builder fltconRNNUB(Double val) {
			this.__FLTCON_RNNUB = val;
			return this;
		}
		
		private void initializeDefaultVariables() {
			this.__engineType = DatcomEngineType.JET;
			this.__DIM = "FT";
			this.__TRIM = true;
			this.__DAMP = true;
			this.__PART = true;
			this.__DERIV = "RAD";

			this.__FLTCON_MACH.clear();
			this.__FLTCON_MACH = Arrays.asList( // list of Mach
					0.3);
			this.__FLTCON_NMACH = this.__FLTCON_MACH.size(); 

			this.__FLTCON_ALT.clear();
			this.__FLTCON_ALT = Arrays.asList( // list of Mach
					1500.0);
			this.__FLTCON_NALT = this.__FLTCON_ALT.size(); 
			
			this.__FLTCON_ALSCHD.clear();
			this.__FLTCON_ALSCHD = Arrays.asList( // list of AoA
					-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0,
						10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0);
			this.__FLTCON_NALPHA = this.__FLTCON_ALSCHD.size(); 

			this.__FLTCON_GAMMA = 0.0;
			this.__FLTCON_RNNUB = 20120887.0;
			
			this.__OPTINS_BLREF = 93.0;
			this.__OPTINS_SREF = 1329.9;
			this.__OPTINS_CBARR = 14.3;
			
			this.__SYNTHS_XW = 28.3;
			this.__SYNTHS_ZW = -1.4;
			this.__SYNTHS_ALIW = 1.0;
			this.__SYNTHS_XCG = 41.3;
			this.__SYNTHS_ZCG = 0.0;
			this.__SYNTHS_XH = 76.6;
			this.__SYNTHS_ZH = 6.2;
			this.__SYNTHS_XV = 71.1;
			this.__SYNTHS_ZV = 7.6;
			this.__SYNTHS_XVF = 66.2;
			this.__SYNTHS_ZVF = 13.1;
			this.__SYNTHS_VERTUP = true;
			
			this.__BODY_BNOSE = 2.0;
			this.__BODY_BTAIL = 2.0;
			this.__BODY_BLA = 2.0;
			this.__BODY_X.clear();
			this.__BODY_X = Arrays.asList( // list of X
					0., 1.38, 4.83, 6.90, 8.97, 13.8, 27.6, 55.2, 65.6, 69.0, 75.9, 82.8, 89.7, 90.4);
			this.__BODY_NX = this.__BODY_X.size(); // 14
			this.__BODY_ZU = Arrays.asList( // list of ZU
					0.69, 2.07, 3.45, 4.38, 5.87, 6.90, 8.28, 8.28, 8.28, 8.28, 7.94, 7.59, 7.50, 6.9);
			this.__BODY_ZL = Arrays.asList( // list of ZL
					-0.35, -1.73, -3.45, -3.80, -4.14, -4.49, -4.83, -4.83, -3.45, -2.76, -0.81, 1.04, 4.14, 6.21);
			this.__BODY_S = Arrays.asList( // list of ZL
					0.55, 8.23, 28.89, 44.31, 65.06, 92.63, 127.81, 127.81, 108.11, 95.68, 56.88, 28.39, 3.64, 0.11);
			
		}
		
		public DatcomInputData0 build() {
			return new DatcomInputData0(this);
		}
		
	}
	
	private DatcomInputData0(DatcomInputData0Builder builder) {
		_engineType =    builder.__engineType;

		_DIM =            builder.__DIM;
		_TRIM =           builder.__TRIM;
		_DAMP =           builder.__DAMP;
		_PART =           builder.__PART;
		_DERIV =          builder.__DERIV;
		
		_FLTCON_NMACH =   builder.__FLTCON_NMACH;
		_FLTCON_MACH =    builder.__FLTCON_MACH;
		_FLTCON_NALT =    builder.__FLTCON_NALT;
		_FLTCON_ALT =     builder.__FLTCON_ALT;
		_FLTCON_NALPHA =  builder.__FLTCON_NALPHA;
		_FLTCON_ALSCHD =  builder.__FLTCON_ALSCHD;
		_FLTCON_GAMMA =   builder.__FLTCON_GAMMA;
		_FLTCON_LOOP =    builder.__FLTCON_LOOP;
		_FLTCON_RNNUB =   builder.__FLTCON_RNNUB;

		_OPTINS_BLREF =   builder.__OPTINS_BLREF;
		_OPTINS_SREF =    builder.__OPTINS_SREF;
		_OPTINS_CBARR =   builder.__OPTINS_CBARR;
		    
		_SYNTHS_XW =      builder.__SYNTHS_XW;
		_SYNTHS_ALIW =    builder.__SYNTHS_ALIW;
		_SYNTHS_XCG =     builder.__SYNTHS_XCG;
		_SYNTHS_ZCG =     builder.__SYNTHS_ZCG;
		_SYNTHS_XH =      builder.__SYNTHS_XH;
		_SYNTHS_ZH =      builder.__SYNTHS_ZH;
		_SYNTHS_XV =      builder.__SYNTHS_XV;
		_SYNTHS_ZV =      builder.__SYNTHS_ZV;
		_SYNTHS_XVF =     builder.__SYNTHS_XVF;
		_SYNTHS_ZVF =     builder.__SYNTHS_ZVF;
		_SYNTHS_VERTUP =  builder.__SYNTHS_VERTUP;
		    
		_BODY_NX =        builder.__BODY_NX;
		_BODY_X =         builder.__BODY_X;
		_BODY_ZU =        builder.__BODY_ZU;
		_BODY_ZL =        builder.__BODY_ZL;
		_BODY_S =         builder.__BODY_S;
		    
		_WGPLNF_CHRDR =   builder.__WGPLNF_CHRDR;
		_WGPLNF_CHRDBP =  builder.__WGPLNF_CHRDBP;
		_WGPLNF_CHRDTP =  builder.__WGPLNF_CHRDTP;
		_WGPLNF_SSPN =    builder.__WGPLNF_SSPN;
		_WGPLNF_SSPNE =   builder.__WGPLNF_SSPNE;
		_WGPLNF_SAVSI =   builder.__WGPLNF_SAVSI;
		_WGPLNF_SAVSO =   builder.__WGPLNF_SAVSO;
		_WGPLNF_CHSTAT =  builder.__WGPLNF_CHSTAT;
		_WGPLNF_TWISTA =  builder.__WGPLNF_TWISTA;
		_WGPLNF_SSPNDD =  builder.__WGPLNF_SSPNDD;
		_WGPLNF_DHDADI =  builder.__WGPLNF_DHDADI;
		_WGPLNF_DHDADO =  builder.__WGPLNF_DHDADO;
		_WGPLNF_TYPE =    builder.__WGPLNF_TYPE;
		    
		_VTPLNF_CHRDR =   builder.__VTPLNF_CHRDR;
		_VTPLNF_CHRDBP =  builder.__VTPLNF_CHRDBP;
		_VTPLNF_CHRDTP =  builder.__VTPLNF_CHRDTP;
		_VTPLNF_SSPN =    builder.__VTPLNF_SSPN;
		_VTPLNF_SSPNE =   builder.__VTPLNF_SSPNE;
		_VTPLNF_SAVSI =   builder.__VTPLNF_SAVSI;
		_VTPLNF_SAVSO =   builder.__VTPLNF_SAVSO;
		_VTPLNF_CHSTAT =  builder.__VTPLNF_CHSTAT;
		_VTPLNF_TWISTA =  builder.__VTPLNF_TWISTA;
		_VTPLNF_SSPNDD =  builder.__VTPLNF_SSPNDD;
		_VTPLNF_DHDADI =  builder.__VTPLNF_DHDADI;
		_VTPLNF_DHDADO =  builder.__VTPLNF_DHDADO;
		_VTPLNF_TYPE =    builder.__VTPLNF_TYPE;
		    
		_HTPLNF_CHRDR =   builder.__HTPLNF_CHRDR;
		_HTPLNF_CHRDBP =  builder.__HTPLNF_CHRDBP;
		_HTPLNF_CHRDTP =  builder.__HTPLNF_CHRDTP;
		_HTPLNF_SSPN =    builder.__HTPLNF_SSPN;
		_HTPLNF_SSPNE =   builder.__HTPLNF_SSPNE;
		_HTPLNF_SAVSI =   builder.__HTPLNF_SAVSI;
		_HTPLNF_SAVSO =   builder.__HTPLNF_SAVSO;
		_HTPLNF_CHSTAT =  builder.__HTPLNF_CHSTAT;
		_HTPLNF_TWISTA =  builder.__HTPLNF_TWISTA;
		_HTPLNF_SSPNDD =  builder.__HTPLNF_SSPNDD;
		_HTPLNF_DHDADI =  builder.__HTPLNF_DHDADI;
		_HTPLNF_DHDADO =  builder.__HTPLNF_DHDADO;
		_HTPLNF_TYPE =    builder.__HTPLNF_TYPE;
		    
		_JETPWR_AIETLJ =  builder.__JETPWR_AIETLJ;
		_JETPWR_AMBSTP =  builder.__JETPWR_AMBSTP;
		_JETPWR_AMBTMP =  builder.__JETPWR_AMBTMP;
		_JETPWR_JEALOC =  builder.__JETPWR_JEALOC;
		_JETPWR_JELLOC =  builder.__JETPWR_JELLOC;
		_JETPWR_JERAD =   builder.__JETPWR_JERAD;
		_JETPWR_JEVLOC =  builder.__JETPWR_JEVLOC;
		_JETPWR_JIALOC =  builder.__JETPWR_JIALOC;
		_JETPWR_JINLTA =  builder.__JETPWR_JINLTA;
		_JETPWR_THSTCJ =  builder.__JETPWR_THSTCJ;
		_JETPWR_JEANGL =  builder.__JETPWR_JEANGL;
		_JETPWR_NENGSJ =  builder.__JETPWR_NENGSJ;
		    
		_PROPWR_NENGSP =  builder.__PROPWR_NENGSP;
		_PROPWR_AIETLP =  builder.__PROPWR_AIETLP;
		_PROPWR_THSTCP =  builder.__PROPWR_THSTCP;
		_PROPWR_PHALOC =  builder.__PROPWR_PHALOC;
		_PROPWR_PHVLOC =  builder.__PROPWR_PHVLOC;
		_PROPWR_PRPRAD =  builder.__PROPWR_PRPRAD;
		_PROPWR_ENGFCT =  builder.__PROPWR_ENGFCT;
		_PROPWR_BWAPR3 =  builder.__PROPWR_BWAPR3;
		_PROPWR_BWAPR6 =  builder.__PROPWR_BWAPR6;
		_PROPWR_BWAPR9 =  builder.__PROPWR_BWAPR9;
		_PROPWR_NOPBPE =  builder.__PROPWR_NOPBPE;
		_PROPWR_BAPR75 =  builder.__PROPWR_BAPR75;
		_PROPWR_YP =      builder.__PROPWR_YP;
		_PROPWR_CROT =    builder.__PROPWR_CROT;
		    
		_SYMFLP_FTYPE =   builder.__SYMFLP_FTYPE;
		_SYMFLP_NDELTA =  builder.__SYMFLP_NDELTA;
		_SYMFLP_DELTA =   builder.__SYMFLP_DELTA;
		_SYMFLP_SPANFI =  builder.__SYMFLP_SPANFI;
		_SYMFLP_SPANFO =  builder.__SYMFLP_SPANFO;
		_SYMFLP_CHRDFI =  builder.__SYMFLP_CHRDFI;
		_SYMFLP_CHRDFO =  builder.__SYMFLP_CHRDFO;
		_SYMFLP_NTYPE =   builder.__SYMFLP_NTYPE;
		_SYMFLP_CB =      builder.__SYMFLP_CB;
		_SYMFLP_TC =      builder.__SYMFLP_TC;
		_SYMFLP_PHETE =   builder.__SYMFLP_PHETE;
		_SYMFLP_PHETEP =  builder.__SYMFLP_PHETEP;

	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Engine type: ").append(_engineType).append("\n\n");
		sb.append("**************************************************************************\n");
		sb.append("* List of command card\n");
		sb.append("**************************************************************************\n");
		if (_TRIM)
			sb.append("TRIM\n");
		if (_DAMP)
			sb.append("DAMP\n");
		if (_PART)
			sb.append("PART\n");
		sb.append("DERIV ").append(_DERIV).append("\n");
		
		sb.append(" $FLTCON\n");
		sb.append("    NMACH=").append((double)_FLTCON_NMACH).append(",\n");
		sb.append("    MACH(1)=");
		sb.append(
				_FLTCON_MACH.stream()
				            .map(value -> value.toString())
				            .collect(Collectors.joining(", "))
				).append(",\n");
		sb.append("    NALT=").append((double)_FLTCON_NALT).append(",\n");
		sb.append("    ALT(1)=");
		sb.append(
				_FLTCON_ALT.stream()
				            .map(value -> value.toString())
				            .collect(Collectors.joining(", "))
				).append(",\n");
		sb.append("    NALPHA=").append((double)_FLTCON_NALPHA).append(",\n");
		sb.append("    ALSCHD(1)=");
		sb.append(
				_FLTCON_ALSCHD.stream()
				            .map(value -> value.toString())
				            .collect(Collectors.joining(", "))
				).append(",\n");
		sb.append("    GAMMA=").append(_FLTCON_GAMMA).append("\n");
		sb.append("    RNNUB=").append(_FLTCON_RNNUB).append("\n");
		sb.append(" $\n");
		
		// TODO the rest ...
		
		return sb.toString();

	}
	
	public static void main(String[] args) {
		DatcomInputData0 inputData = new DatcomInputData0Builder(DatcomEngineType.JET)
				.fltconMACH(Arrays.asList( // list of Mach numbers
								0.3, 0.5)
						)
//				.fltconALSCHD(
//						Arrays.asList( // list of AoA
//								-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0,
//									10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0)
//						)
				.build();
		System.out.println("--- Test DatcomInputData ---\n");
		System.out.println(inputData);

	}

}
