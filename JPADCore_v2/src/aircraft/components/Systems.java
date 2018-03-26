package aircraft.components;

import analyses.systems.SystemsWeightManager;


public class Systems {

	//-------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private ISystems _theSystemsInterface;
	private SystemsWeightManager _theWeightManager;
	
	//------------------------------------------------------------------------------------------
	// BUILDER
	public Systems(ISystems theSystemsInterface) {
		
		this._theSystemsInterface = theSystemsInterface;
		this.setTheWeightManager(new SystemsWeightManager());
	}
	
	//-------------------------------------------------------------------------------------
	// GETTERS AND SETTER
	public ISystems getTheSystemsInterface() {
		return _theSystemsInterface;
	}

	public void setTheSystemsInterface(ISystems _theSystemsInterface) {
		this._theSystemsInterface = _theSystemsInterface;
	}

	public SystemsWeightManager getTheWeightManager() {
		return _theWeightManager;
	}

	public void setTheWeightManager(SystemsWeightManager _theWeightManager) {
		this._theWeightManager = _theWeightManager;
	}

}