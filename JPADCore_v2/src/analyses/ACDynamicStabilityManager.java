package analyses;

import java.io.IOException;

import aircraft.Aircraft;
import configuration.enumerations.ConditionEnum;

public class ACDynamicStabilityManager {
	
	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private IACDynamicStabilityManager _theDynamicStabilityManagerInterface;
	
	
	//------------------------------------------------------------------------------
	// OUTPUT DATA
	//..............................................................................

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	private void initializeData() {
		// TODO
		
	}
	
	public static ACDynamicStabilityManager importFromXML (
			String pathToXML,
			Aircraft theAircraft,
			OperatingConditions theOperatingConditions,
			ConditionEnum condition
			) throws IOException {
		

		// TODO @agodemar
		
		//===========================================================================================
		// BUILDING THE CALCULATOR ...
		IACDynamicStabilityManager theDynamicsManagerInterface = new IACDynamicStabilityManager.Builder()
				.setId("TODO...")
				.setTheAircraft(theAircraft)
				// TODO: add the rest...
				.build();
				
		ACDynamicStabilityManager theDynamicsManager = new ACDynamicStabilityManager();
		theDynamicsManager.setTheDynamicsInterface(theDynamicsManagerInterface);
		
		return theDynamicsManager;
	}

	/**
	 * This method initializes the related calculators inner classes and 
	 * performs the required calculation
	 */
	public void calculate(String resultsFolderPath) {

		initializeData();

	}
	
	
	
	// GETTERS/SETTERS
	private void setTheDynamicsInterface(IACDynamicStabilityManager theDynamicsManagerInterface) {
		this._theDynamicStabilityManagerInterface = theDynamicsManagerInterface;
	}
}
