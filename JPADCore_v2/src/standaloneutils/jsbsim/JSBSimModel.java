package standaloneutils.jsbsim;

import java.io.File;

import standaloneutils.CPACSReader;

public class JSBSimModel {
	
	CPACSReader _cpacsReader;
	
	public JSBSimModel(CPACSReader reader) {
		_cpacsReader = reader;
		
	}

	public CPACSReader getCpacsReader() {
		return _cpacsReader;
	}

	public void appendToCPACSFile(File file) {
		
		// TODO implement CPACS file export function
		
		System.out.println("[JSBSimModel.appendToCPACSFile] --> not yet implemented.");
		
	}
	public void exportToJSBSimFile(File file) {
		
		// TODO implement JSBSim file export function
		
		System.out.println("[JSBSimModel.exportToJSBSimFile] --> not yet implemented.");
		
	}
}
