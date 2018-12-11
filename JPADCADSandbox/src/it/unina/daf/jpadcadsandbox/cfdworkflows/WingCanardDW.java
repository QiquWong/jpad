package it.unina.daf.jpadcadsandbox.cfdworkflows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import aircraft.Aircraft;
import it.unina.daf.jpadcadsandbox.Test26mds.AeroComponents;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;

public class WingCanardDW {
	
	public static final String workingFolderPath = "C:\\Users\\Mario\\Documents\\Tesi_Magistrale\\Test_Macro_Star";
	public static final String jpadCADFolder = "C:\\Users\\Mario\\JPAD_PROJECT\\jpad\\JPADCADSandbox";
	public static final String macroPath = "Users\\Mario\\eclipse-workspace\\STARCCM\\src\\test";
	public static final String macroName = "Test_MultipleExecutes.java";
	public static final String starExePath = "C:\\Program Files\\CD-adapco\\13.04.010-R8\\STAR-CCM+13.04.010-R8\\star\\bin\\starccm+.exe";
	public static final String starOptions = "-cpubind -np 4 -rsh ssh";

	public static void main(String[] args) throws Exception {
		System.out.println("--------------------------------------------------");
		System.out.println("JPAD/STAR-CCM+ Workflow: WC downwash investigation");
		System.out.println("--------------------------------------------------");
		
		// Import the aircraft object from the xml file
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		// Initialize the aircraft components map
		ConcurrentHashMap<AeroComponents, List<Object>> aeroMap = new ConcurrentHashMap<AeroComponents, List<Object>>();
		aeroMap.put(AeroComponents.FUSELAGE, new ArrayList<Object>());
		aeroMap.put(AeroComponents.WING, new ArrayList<Object>());
		aeroMap.put(AeroComponents.CANARD, new ArrayList<Object>());
		aeroMap.put(AeroComponents.HORIZONTAL, new ArrayList<Object>());
		aeroMap.put(AeroComponents.VERTICAL, new ArrayList<Object>());
		
		// Populate the map with all the aircraft components
		for(Iterator<AeroComponents> comp = aeroMap.keySet().iterator(); comp.hasNext(); ) {
			
			AeroComponents component = comp.next();
			switch(component) {
			
			case FUSELAGE:
				if(!(aircraft.getFuselage() == null))
					aeroMap.get(AeroComponents.FUSELAGE).add(aircraft.getFuselage());
				else {
					System.out.println("There's no FUSELAGE component for the selected aircraft!");
					aeroMap.remove(component);
				}
				
				break;
				
			case WING:
				if(!(aircraft.getWing() == null))
					aeroMap.get(AeroComponents.WING).add(aircraft.getWing());
				else {
					System.out.println("There's no WING component for the selected aircraft!");
					aeroMap.remove(component);
				}
				
				break;
				
			case HORIZONTAL:
				if(!(aircraft.getHTail() == null))
					aeroMap.get(AeroComponents.HORIZONTAL).add(aircraft.getHTail());
				else {
					System.out.println("There's no HORIZONTAL component for the selected aircraft!");
					aeroMap.remove(component);
				}
				
				break;
				
			case VERTICAL:
				if(!(aircraft.getVTail() == null))
					aeroMap.get(AeroComponents.VERTICAL).add(aircraft.getVTail());
				else {
					System.out.println("There's no VERTICAL component for the selected aircraft!");
					aeroMap.remove(component);
				}
				
				break;	
				
			case CANARD:
				if(!(aircraft.getCanard() == null))
					aeroMap.get(AeroComponents.CANARD).add(aircraft.getCanard());
				else {
					System.out.println("There's no CANARD component for the selected aircraft!");
					aeroMap.remove(component);
				}
				
				break;
				
			default:
				
				break;
			}
		}
		
		// Remove undesired components
		List<AeroComponents> necessComponents = new ArrayList<>();
		
		necessComponents.add(AeroComponents.WING);
		necessComponents.add(AeroComponents.CANARD);
		
		for (Iterator<AeroComponents> iter = necessComponents.iterator(); iter.hasNext(); ) {
			AeroComponents component = iter.next();
			
			if (!aeroMap.keySet().contains(component)) {
				throw new Exception("Warning: some of the components selected for the current simulation are not"
						+ "provided by the template aircraft " + aircraft.getId());
			}
		}
		
		for (Iterator<AeroComponents> iter = aeroMap.keySet().iterator(); iter.hasNext(); ) {		
			AeroComponents component = iter.next();
			
			if (!necessComponents.contains(component)) {
				aeroMap.remove(component);
			}
		}
		
		System.out.println("Selected components for the current simulation: " + aeroMap.keySet().toString());
		
		// Modify selected lifting surfaces
		AeroComponents modComponentEnum = AeroComponents.CANARD;

	}

}
