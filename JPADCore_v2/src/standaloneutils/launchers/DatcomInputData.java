package standaloneutils.launchers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface DatcomInputData {
	
	String getDescription();
	
	DatcomEngineType getEngineType();
	
	String getCommand_DIM();
	boolean getCommand_TRIM();
	boolean getCommand_DAMP();
	boolean getCommand_PART();
	String getCommand_DERIV();
	
//	List<Double> getFltcon_MACH();
	// int getFltcon_NMACH();
	Double getFltcon_MACH(); // only one Mach number permitted
	
//	List<Double> getFltcon_ALT();
	// int getFltcon_NALT();
	Double getFltcon_ALT(); // Only one Altitude permitted 
	
	List<Double> getFltcon_ALSCHD();
	// int getFltcon_NALPHA();
	Double getFltcon_GAMMA();
	int getFltcon_LOOP();
	Double getFltcon_RNNUB();

	Double getOptins_BLREF();
	Double getOptins_SREF();
	Double getOptins_CBARR();

	Double getSynths_XW();
	Double getSynths_ZW();
	Double getSynths_ALIW();
	Double getSynths_XCG();
	Double getSynths_ZCG();
	Double getSynths_XH();
	Double getSynths_ZH();
	Double getSynths_XV();
	Double getSynths_ZV();
	Double getSynths_XVF();
	Double getSynths_ZVF();
	boolean getSynths_VERTUP();

	Double getBody_BNOSE();
	Double getBody_BTAIL();
	Double getBody_BLA();
	List<Double> getBody_X();
	List<Double> getBody_ZU();
	List<Double> getBody_ZL();
	List<Double> getBody_S();

	Double getWgplnf_CHRDR();
	Optional<Double> getWgplnf_CHRDBP();
	Double getWgplnf_CHRDTP();
	Double getWgplnf_SSPN();
	Double getWgplnf_SSPNE();
	Optional<Double> getWgplnf_SSPNOP();
	Double getWgplnf_SAVSI();
	Optional<Double> getWgplnf_SAVSO();
	Double getWgplnf_CHSTAT();
	Double getWgplnf_TWISTA();
	Optional<Double> getWgplnf_SSPNDD();
	Double getWgplnf_DHDADI();
	Optional<Double> getWgplnf_DHDADO();
	int getWgplnf_TYPE();

	Double getVtplnf_CHRDR();
	Optional<Double> getVtplnf_CHRDBP();
	Double getVtplnf_CHRDTP();
	Double getVtplnf_SSPN();
	Double getVtplnf_SSPNE();
	Optional<Double> getVtplnf_SSPNOP();
	Double getVtplnf_SAVSI();
	Optional<Double> getVtplnf_SAVSO();
	Double getVtplnf_CHSTAT();
	Double getVtplnf_TWISTA();
	Optional<Double> getVtplnf_SSPNDD();
	Double getVtplnf_DHDADI();
	Optional<Double> getVtplnf_DHDADO();
	int getVtplnf_TYPE();

	Double getHtplnf_CHRDR();
	Optional<Double> getHtplnf_CHRDBP();
	Double getHtplnf_CHRDTP();
	Double getHtplnf_SSPN();
	Double getHtplnf_SSPNE();
	Optional<Double> getHtplnf_SSPNOP();
	Double getHtplnf_SAVSI();
	Optional<Double> getHtplnf_SAVSO();
	Double getHtplnf_CHSTAT();
	Double getHtplnf_TWISTA();
	Optional<Double> getHtplnf_SSPNDD();
	Double getHtplnf_DHDADI();
	Optional<Double> getHtplnf_DHDADO();
	int getHtplnf_TYPE();

	Optional<Double> getJetpwr_AIETLJ();
	Optional<Double> getJetpwr_AMBSTP();
	Optional<Double> getJetpwr_AMBTMP();
	Optional<List<Double>> getJetpwr_JEALOC();
	Optional<Double> getJetpwr_JELLOC();
	Optional<Double> getJetpwr_JERAD();
	Optional<Double> getJetpwr_JEVLOC();
	Optional<Double> getJetpwr_JIALOC();
	Optional<Double> getJetpwr_JINLTA();
	Optional<Double> getJetpwr_THSTCJ();
	Optional<Double> getJetpwr_JEANGL();
	Optional<Integer> getJetpwr_NENGSJ();

	Optional<Integer> getPropwr_NENGSP();
	Optional<Double> getPropwr_AIETLP();
	Optional<Double> getPropwr_THSTCP();
	Optional<Double> getPropwr_PHALOC();
	Optional<Double> getPropwr_PHVLOC();
	Optional<Double> getPropwr_PRPRAD();
	Optional<Double> getPropwr_ENGFCT();
	Optional<Double> getPropwr_BWAPR3();
	Optional<Double> getPropwr_BWAPR6();
	Optional<Double> getPropwr_BWAPR9();
	Optional<Integer> getPropwr_NOPBPE();
	Optional<Double> getPropwr_BAPR75();
	Optional<Double> getPropwr_YP();
	Optional<Boolean> getPropwr_CROT();

	Optional<Integer> getSymflp_FTYPE();
	//Optional<Integer> getSymflp_NDELTA();
	Optional<List<Double>> getSymflp_DELTA();
	Optional<Double> getSymflp_SPANFI();
	Optional<Double> getSymflp_SPANFO();
	Optional<Double> getSymflp_CHRDFI();
	Optional<Double> getSymflp_CHRDFO();
	Optional<Integer> getSymflp_NTYPE();
	Optional<Double> getSymflp_CB();
	Optional<Double> getSymflp_TC();
	Optional<Double> getSymflp_PHETE();
	Optional<Double> getSymflp_PHETEP();
	
	/** Builder of IDatcomInputData instances. */
	class Builder extends DatcomInputData_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("(c) Agostino De Marco - X-airplane");
			setEngineType(DatcomEngineType.JET);
			
			setCommand_DIM("FT");
			setCommand_TRIM(true);
			setCommand_DAMP(true);
			setCommand_PART(true);
			setCommand_DERIV("RAD");

//			addAllFltcon_MACH(Arrays.asList( // list of Mach numbers
//					0.3));
			setFltcon_MACH(0.3);
			
//			addAllFltcon_ALT(Arrays.asList( // list of Altitudes
//					1500.0));
			setFltcon_ALT(1500.0);
			
			addAllFltcon_ALSCHD(Arrays.asList( // list of Altitudes
					-16.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 8.0, 9.0,
						10.0, 12.0, 14.0, 16.0, 18.0, 19.0, 20.0, 21.0, 22.0, 24.0));
			setFltcon_GAMMA(0.0);
			setFltcon_RNNUB(20120887.0);
			setFltcon_LOOP(2);
			
			setOptins_BLREF(93.0);
			setOptins_SREF(1329.0);
			setOptins_CBARR(14.3);

			setSynths_XW(28.3);
			setSynths_ZW(-1.4);
			setSynths_ALIW(1.0);
			setSynths_XCG(41.3);
			setSynths_ZCG(0.0);
			setSynths_XH(76.6);
			setSynths_ZH(6.2);
			setSynths_XV(71.1);
			setSynths_ZV(7.6);
			setSynths_XVF(66.2);
			setSynths_ZVF(13.1);
			setSynths_VERTUP(true);

			setBody_BNOSE(2.0);
			setBody_BTAIL(2.0);
			setBody_BLA(2.0);
			addAllBody_X(Arrays.asList( // list of X
					0., 1.38, 4.83, 6.90, 8.97, 13.8, 27.6, 55.2, 65.6, 69.0, 75.9, 82.8, 89.7, 90.4));
			addAllBody_ZU(Arrays.asList( // list of ZU
					0.69, 2.07, 3.45, 4.38, 5.87, 6.90, 8.28, 8.28, 8.28, 8.28, 7.94, 7.59, 7.50, 6.9));
			addAllBody_ZL(Arrays.asList( // list of ZL
					-0.35, -1.73, -3.45, -3.80, -4.14, -4.49, -4.83, -4.83, -3.45, -2.76, -0.81, 1.04, 4.14, 6.21));
			addAllBody_S(Arrays.asList( // list of S
					0.55, 8.23, 28.89, 44.31, 65.06, 92.63, 127.81, 127.81, 108.11, 95.68, 56.88, 28.39, 3.64, 0.11));
			
			setWgplnf_CHRDR(23.8);
			setWgplnf_CHRDBP(12.4);
			setWgplnf_CHRDTP(4.8);
			setWgplnf_SSPN(46.9);
			setWgplnf_SSPNOP(31.1);
			setWgplnf_SSPNE(40.0);
			setWgplnf_CHSTAT(0.25);
			setWgplnf_TWISTA(0.0);
			setWgplnf_TYPE(1);
			setWgplnf_SAVSI(29.0);
			setWgplnf_SAVSO(26.0);
			setWgplnf_DHDADI(0.0);
			setWgplnf_DHDADO(4.0);

			setVtplnf_CHRDR(15.9);
			setVtplnf_CHRDBP(4.8);
			setVtplnf_CHRDTP(4.8);
			setVtplnf_SSPN(27.6);
			setVtplnf_SSPNOP(0.0);
			setVtplnf_SSPNE(20.7);
			setVtplnf_CHSTAT(0.25);
			setVtplnf_TWISTA(0.0);
			setVtplnf_TYPE(1);
			setVtplnf_SAVSI(33.0);
			setVtplnf_SAVSO(33.0);
			setVtplnf_DHDADI(0.0);
			setVtplnf_DHDADO(0.0);

			setHtplnf_CHRDR(12.4);
			setHtplnf_CHRDBP(4.1);
			setHtplnf_CHRDTP(4.1);
			setHtplnf_SSPN(17.6);
			setHtplnf_SSPNOP(0.0);
			setHtplnf_SSPNE(15.87);
			setHtplnf_CHSTAT(0.25);
			setHtplnf_TWISTA(0.0);
			setHtplnf_TYPE(1);
			setHtplnf_SAVSI(31.0);
			setHtplnf_SAVSO(31.0);
			setHtplnf_DHDADI(9.0);
			setHtplnf_DHDADO(9.0);
			
			setJetpwr_AIETLJ(-2.0);
			setJetpwr_AMBSTP(2116.8);
			setJetpwr_AMBTMP(59.7);
			clearJetpwr_JEALOC();
			setJetpwr_JEALOC(Arrays.asList( // list of JEALOC
					42.25, 58.0));
			setJetpwr_JELLOC(15.9);
			setJetpwr_JERAD(2.065);
			setJetpwr_JEVLOC(-5.2);
			setJetpwr_JIALOC(34.5);
			setJetpwr_JINLTA(13.4);
			setJetpwr_THSTCJ(0.0);
			setJetpwr_JEANGL(-2.0);
			setJetpwr_NENGSJ(2);
			
			setSymflp_FTYPE(1);
			setSymflp_DELTA(Arrays.asList( // list of JEALOC
					-40.0, -30.0, -20.0, -10.0, 0.0, 10.0, 20.0, 30.0, 40.0));
			setSymflp_SPANFI(0.0);
			setSymflp_SPANFO(14.0);
			setSymflp_CHRDFI(1.72);
			setSymflp_CHRDFO(1.72);
			setSymflp_NTYPE(1);
			setSymflp_CB(0.50);
			setSymflp_TC(0.44);
			setSymflp_PHETE(0.003);
			setSymflp_PHETEP(0.002);
		}
		
		//--------------------------------------------------------
		// CONSTRAINTS
		// 
		// some of them are for convenience and might be removed
		// at some point in time
		//
		//--------------------------------------------------------
		
		@Override
		public Builder setFltcon_MACH(Double val) {
			// Check single-field (argument) constraints in the setter method.
			checkArgument((val > 0) && (val <= 0.85));
			return super.setFltcon_MACH(val);
		}
		@Override
		public Builder setFltcon_ALT(Double val) {
			// Check single-field (argument) constraints in the setter method.
			checkArgument(val > 0);
			return super.setFltcon_ALT(val);
		}
		@Override
		public Builder setFltcon_LOOP(int val) {
			// Check single-field (argument) constraints in the setter method.
			checkArgument(val == 2);
			return super.setFltcon_LOOP(val);
		}
		@Override
		public Builder setSynths_XW(Double val) {
			// Check single-field (argument) constraints in the setter method.
			checkArgument(val >= 0);
			return super.setSynths_XW(val);
		}
		@Override 
		public DatcomInputData build() {
			DatcomInputData data = super.build();
			
			// Check cross-field (state) constraints in the build method.
			//checkState(data.getDescription().contains("Agostino De Marco"));
			
			return data;
		}
	}
}
