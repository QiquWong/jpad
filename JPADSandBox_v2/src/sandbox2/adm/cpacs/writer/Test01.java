package sandbox2.adm.cpacs.writer;

import javax.xml.parsers.ParserConfigurationException;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import standaloneutils.cpacs.CPACSWriter;

public class Test01 {

	public static void main(String[] args) {

		System.out.println("-------------------");
		System.out.println("CPACSWriter Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft and the fuselage ...");
		Aircraft theAircraft = AircraftUtils.importAircraft(args);

		Fuselage fuselage = theAircraft.getFuselage();
		LiftingSurface wing = theAircraft.getWing();
		LiftingSurface horTail = theAircraft.getHTail();
		LiftingSurface verTail = theAircraft.getVTail();
		
		
		try {
			CPACSWriter writer = new CPACSWriter(AircraftUtils.va.getOutputFile());			
			writer.export(fuselage);
		} catch (Exception e) {
			System.err.println("========== [main] Unable to export to AircraftUtils.va.getOutputFile() in CPACS format.");
			e.printStackTrace();
		}
	}
}
