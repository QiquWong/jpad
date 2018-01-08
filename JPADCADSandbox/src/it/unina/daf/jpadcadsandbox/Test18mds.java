package it.unina.daf.jpadcadsandbox;

import java.util.List;

import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;

public class Test18mds {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft and the fuselage ...");
		Aircraft theAircraft = AircraftUtils.importAircraft(args);

		LiftingSurface wing = theAircraft.getWing();
//		LiftingSurface horTail = theAircraft.getHTail();
//		LiftingSurface verTail = theAircraft.getVTail();
		
		boolean exportLofts = true;
		boolean exportSupportShapes = true;
		
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, exportLofts, exportSupportShapes);
//		List<OCCShape> horTailShapes = AircraftUtils.getLiftingSurfaceCAD(horTail, ComponentEnum.HORIZONTAL_TAIL, exportLofts, exportSupportShapes);
//		List<OCCShape> verTailShapes = AircraftUtils.getLiftingSurfaceCAD(verTail, ComponentEnum.VERTICAL_TAIL, exportLofts, exportSupportShapes);
		
		// Write to a file
		String fileName = "test18mds.brep";
		
		if (OCCUtils.write(fileName, wingShapes))
			System.out.println("========== [main] Output written on file: " + fileName);	
	}
	
}
