package standaloneutils.launchers;

public class DatcomInputDataTest {

	public static void main(String[] args) {
		try {
			
			DatcomInputData inputData = new DatcomInputData
					.Builder()
					//.setDescription("Pippo Agostino De Marco")
					.build(); // validate for all fields to be set; Optional fields are empty	

			System.out.println("--- Test DatcomInputData ---\n");
			//System.out.println(inputData);

			System.out.println(report(inputData));
			
			
		} catch (Exception e) {
			System.out.println("[Unacceptable input data]: " + e.getMessage());
		}

	}

	public static String report(DatcomInputData inputData) {

		StringBuilder sb = new StringBuilder();
		sb.append("Description: " + inputData.getDescription()).append("\n\n");
		sb.append("Engine type: ").append(inputData.getEngineType()).append("\n\n");
		sb.append("*************************************\n");
		sb.append("* List of command card\n");
		sb.append("*************************************\n");
		if(inputData.getCommand_TRIM())
			sb.append("TRIM\n");
		if(inputData.getCommand_DAMP())
			sb.append("DAMP\n");
		if(inputData.getCommand_PART())
			sb.append("PART\n");
		sb.append("DERIV ").append(inputData.getCommand_DERIV()).append("\n");
		sb.append("\n");

		sb.append("*************************************\n");
		sb.append("* Flight Conditions\n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockFLTCON(
						inputData.getFltcon_MACH(), 
						inputData.getFltcon_ALT(), 
						inputData.getFltcon_ALSCHD(), 
						inputData.getFltcon_GAMMA(), 
						inputData.getFltcon_LOOP(), 
						inputData.getFltcon_RNNUB()
						)
				);
		sb.append("\n");
		sb.append("\n");

		sb.append("*************************************\n");
		sb.append("* Reference Parameters\n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockOPTINS(
						inputData.getOptins_BLREF(), 
						inputData.getOptins_SREF(), 
						inputData.getOptins_CBARR()
						)
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Group II Synthesis Parameters\n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockSYNTHS(
						inputData.getSynths_XW(), inputData.getSynths_ZW(), 
						inputData.getSynths_ALIW(), 
						inputData.getSynths_XCG(), inputData.getSynths_ZCG(), 
						inputData.getSynths_XH(), inputData.getSynths_ZH(), 
						inputData.getSynths_XV(), inputData.getSynths_ZV(), 
						inputData.getSynths_XVF(), inputData.getSynths_ZVF(), 
						inputData.getSynths_VERTUP())
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Body Configuration Parameters\n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockBODY(
						inputData.getBody_BNOSE(), inputData.getBody_BTAIL(), inputData.getBody_BLA(), 
						inputData.getBody_X(), inputData.getBody_ZU(), inputData.getBody_ZL(), 
						inputData.getBody_S())
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Wing planform variables \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockGenericPLNF(
						"WGPLNF", 
						inputData.getWgplnf_CHRDR(), inputData.getWgplnf_CHRDTP(), 
						inputData.getWgplnf_CHRDBP().isPresent() ? inputData.getWgplnf_CHRDBP().get() : null, 
						inputData.getWgplnf_SSPN(), 
						inputData.getWgplnf_SSPNOP().isPresent() ? inputData.getWgplnf_SSPNOP().get() : null, 
						inputData.getWgplnf_SSPNE(), 
						inputData.getWgplnf_CHSTAT(), inputData.getWgplnf_TWISTA(), 
						inputData.getWgplnf_SAVSI(), 
						inputData.getWgplnf_SAVSO().isPresent() ? inputData.getWgplnf_SAVSO().get() : null,  
						inputData.getWgplnf_DHDADI(), 
						inputData.getWgplnf_DHDADO().isPresent() ? inputData.getWgplnf_DHDADO().get() : null, 
						inputData.getWgplnf_TYPE())
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Jet Power Effects parameters \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockJETPWR(
						inputData.getJetpwr_AIETLJ().get(), 
						inputData.getJetpwr_AMBSTP().get(), 
						inputData.getJetpwr_AMBTMP().get(), 
						inputData.getJetpwr_JEALOC().get(), 
						inputData.getJetpwr_JELLOC().get(), 
						inputData.getJetpwr_JERAD().get(), 
						inputData.getJetpwr_JEVLOC().get(), 
						inputData.getJetpwr_JIALOC().get(), 
						inputData.getJetpwr_JINLTA().get(), 
						inputData.getJetpwr_NENGSJ().get(), 
						inputData.getJetpwr_THSTCJ().get(), 
						inputData.getJetpwr_JEANGL().get()
						)
				);
		sb.append("\n");
		sb.append("\n");
				
		
		sb.append("*************************************\n");
		sb.append("* Vertical Tail planform variables \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockGenericPLNF(
						"VTPLNF", 
						inputData.getVtplnf_CHRDR(), inputData.getVtplnf_CHRDTP(), 
						inputData.getVtplnf_CHRDBP().isPresent() ? inputData.getVtplnf_CHRDBP().get() : null, 
						inputData.getVtplnf_SSPN(), 
						inputData.getVtplnf_SSPNOP().isPresent() ? inputData.getVtplnf_SSPNOP().get() : null, 
						inputData.getVtplnf_SSPNE(), 
						inputData.getVtplnf_CHSTAT(), inputData.getVtplnf_TWISTA(), 
						inputData.getVtplnf_SAVSI(), 
						inputData.getVtplnf_SAVSO().isPresent() ? inputData.getVtplnf_SAVSO().get() : null,  
						inputData.getVtplnf_DHDADI(), 
						inputData.getVtplnf_DHDADO().isPresent() ? inputData.getVtplnf_DHDADO().get() : null, 
						inputData.getVtplnf_TYPE())
				);
		sb.append("\n");
		sb.append("\n");
		
		sb.append("*************************************\n");
		sb.append("* Horizontal Tail planform variables \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockGenericPLNF(
						"HTPLNF", 
						inputData.getHtplnf_CHRDR(), inputData.getHtplnf_CHRDTP(), 
						inputData.getHtplnf_CHRDBP().isPresent() ? inputData.getHtplnf_CHRDBP().get() : null, 
						inputData.getHtplnf_SSPN(), 
						inputData.getHtplnf_SSPNOP().isPresent() ? inputData.getHtplnf_SSPNOP().get() : null, 
						inputData.getHtplnf_SSPNE(), 
						inputData.getHtplnf_CHSTAT(), inputData.getHtplnf_TWISTA(), 
						inputData.getHtplnf_SAVSI(), 
						inputData.getHtplnf_SAVSO().isPresent() ? inputData.getHtplnf_SAVSO().get() : null,  
						inputData.getHtplnf_DHDADI(), 
						inputData.getHtplnf_DHDADO().isPresent() ? inputData.getHtplnf_DHDADO().get() : null, 
						inputData.getHtplnf_TYPE())
				);
		sb.append("\n");
		sb.append("\n");

		sb.append("*************************************\n");
		sb.append("* Symetrical Flap Deflection parameters \n");
		sb.append("*************************************\n");
		sb.append(
				DatcomPlusInputGenerator.generateBlockSYMFLP(
						inputData.getSymflp_FTYPE().get(), 
						inputData.getSymflp_DELTA().get(), 
						inputData.getSymflp_SPANFI().get(), 
						inputData.getSymflp_SPANFO().get(), 
						inputData.getSymflp_CHRDFI().get(), 
						inputData.getSymflp_CHRDFO().get(), 
						inputData.getSymflp_NTYPE().get(), 
						inputData.getSymflp_CB().get(), 
						inputData.getSymflp_TC().get(), 
						inputData.getSymflp_PHETE().get(), 
						inputData.getSymflp_PHETEP().get())
				);
		sb.append("\n");
		sb.append("\n");
		
		return sb.toString();
	}


}
