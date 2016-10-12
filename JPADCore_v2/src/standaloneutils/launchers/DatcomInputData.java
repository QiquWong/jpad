package standaloneutils.launchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

public class DatcomInputData implements IDatcomInputData {

	private boolean _TRIM = true;
	private boolean _DAMP = true;
	private boolean _PART = true;
	private String _DERIV = "RAD";
	
	private int _FLTCON_NMACH = 0;
	private List<Double> _FLTCON_MACH = new ArrayList<Double>();
	private int _FLTCON_NALT = 0;
	private List<Double> _FLTCON_ALT = new ArrayList<Double>();
	private int _FLTCON_NALPHA = 0;
	private List<Double> _FLTCON_ALSCHD = new ArrayList<Double>();;
	private Double _FLTCON_GAMMA = 0.0;
	private int _FLTCON_LOOP = 1;
	private Double _FLTCON_RNNUB = 1000000.0;

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
	
	// Builder pattern via a nested public static class

	public static class DatcomInputDataBuilder {
		
		// required parameters
		String __engineType;
		
		private int __FLTCON_NMACH;
		private List<Double> __FLTCON_MACH;
		private int __FLTCON_NALT;
		private List<Double> __FLTCON_ALT;
		private int __FLTCON_NALPHA;
		private List<Double> __FLTCON_ALSCHD;
		private Double __FLTCON_GAMMA;
		private int __FLTCON_LOOP;
		private Double __FLTCON_RNNUB;

		// optional parameters ... defaults
		private boolean __TRIM;
		private boolean __DAMP;
		private boolean __PART;
		private String __DERIV;
		
		public DatcomInputDataBuilder(
				String engineType
				){
			this.__engineType = engineType; 
			initializeDefaultVariables();
		}

		public DatcomInputDataBuilder commandTRIM(boolean val) {
			this.__TRIM = val;
			return this;
		}

		public DatcomInputDataBuilder commandDAMP(boolean val) {
			this.__DAMP = val;
			return this;
		}
		
		public DatcomInputDataBuilder commandPART(boolean val) {
			this.__PART = val;
			return this;
		}

		public DatcomInputDataBuilder commandDERIV(String val) {
			this.__DERIV = val;
			return this;
		}
		
		public DatcomInputDataBuilder fltconMACH(List<Double> vec) {
			this.__FLTCON_NMACH = vec.size();
			this.__FLTCON_MACH = vec;
			return this;
		}

		public DatcomInputDataBuilder fltconALT(List<Double> vec) {
			this.__FLTCON_NALT = vec.size();
			this.__FLTCON_ALT = vec;
			return this;
		}

		public DatcomInputDataBuilder fltconALSCHD(List<Double> vec) {
			this.__FLTCON_NALPHA = vec.size();
			this.__FLTCON_ALSCHD = vec;
			return this;
		}

		public DatcomInputDataBuilder fltconGAMMA(Double val) {
			this.__FLTCON_GAMMA = val;
			return this;
		}

		public DatcomInputDataBuilder fltconLOOP(int val) {
			this.__FLTCON_LOOP = val;
			return this;
		}

		public DatcomInputDataBuilder fltconRNNUB(Double val) {
			this.__FLTCON_RNNUB = val;
			return this;
		}
		
		private void initializeDefaultVariables() {
			this.__TRIM = true;
			this.__DAMP = true;
			this.__PART = true;
			this.__DERIV = "RAD";
		}
		
		public DatcomInputData build() {
			return new DatcomInputData(this);
		}
		
	}
	
	private DatcomInputData(DatcomInputDataBuilder builder) {
		_TRIM = builder.__TRIM;
		_DAMP = builder.__DAMP;
		_PART = builder.__PART;
		_DERIV = builder.__DERIV;
		_FLTCON_NMACH = builder.__FLTCON_NMACH;
		_FLTCON_MACH = builder.__FLTCON_MACH;
		_FLTCON_NALT = builder.__FLTCON_NALT;
		_FLTCON_ALT = builder.__FLTCON_ALT;
		_FLTCON_NALPHA = builder.__FLTCON_NALPHA;
		_FLTCON_ALSCHD = builder.__FLTCON_ALSCHD;
		_FLTCON_GAMMA = builder.__FLTCON_GAMMA;
		_FLTCON_LOOP = builder.__FLTCON_LOOP;
		_FLTCON_RNNUB = builder.__FLTCON_RNNUB;

		// TODO the rest

	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
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
		sb.append("    NALPHA=").append(_FLTCON_NALPHA).append(",\n");
		
		sb.append("    ALSCHD(1)=");
		sb.append(
				_FLTCON_ALSCHD.stream()
				            .map(value -> value.toString())
				            .collect(Collectors.joining(", "))
				);
		sb.append("\n");
		sb.append(" $\n");
		
		
		return sb.toString();

	}
	
	public static void main(String[] args) {
		DatcomInputData inputData = new DatcomInputDataBuilder("Propeller")
				.fltconALSCHD(
						Arrays.asList( // list of AoA
								-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0,
									10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0)
						)
				.build();
		System.out.println("--- Test DatcomInputData ---\n");
		System.out.println(inputData);

	}

}
