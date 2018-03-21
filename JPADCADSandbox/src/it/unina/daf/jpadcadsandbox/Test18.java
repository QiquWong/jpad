package it.unina.daf.jpadcadsandbox;

import java.util.List;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;

public class Test18 {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft and the fuselage ...");
		Aircraft theAircraft = AircraftUtils.importAircraft(args);

		LiftingSurface wing = theAircraft.getWing();
		
		boolean exportLofts = true;
		boolean exportSolids = true;
		boolean exportSupportShapes = true;

		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-2, exportLofts, exportSolids, exportSupportShapes);

		// Write to a file
		String fileName = "test18.brep";
		
		if (OCCUtils.write(fileName, wingShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
		
		
	}

}
