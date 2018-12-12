package it.unina.daf.jpadcad;

import java.util.List;

import aircraft.Aircraft;
import it.unina.daf.jpadcad.occ.OCCSolid;
import standaloneutils.JPADXmlReader;

public class CADManager {
	
	public CADManager importFromXML(
			String pathToXML, 
			Aircraft theAircraft) {
		
		// Preliminary operations
		JPADXmlReader reader = new JPADXmlReader(pathToXML);
		
		System.out.println("Reading CAD modeler configuration file ...");
		
		
		
		return new CADManager();
	}
	


}
