package it.unina.daf.jpadcadsandbox;

import java.util.List;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;

public class Test16 {

	public static Aircraft theAircraft;
	
	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft and the fuselage ...");
		Test16.theAircraft = AircraftUtils.importAircraft(args);

		Fuselage fuselage = theAircraft.getFuselage();
		
		// System.out.println(theAircraft);
		
		System.out.println("========== [main] Initialize CAD shape factory");
		OCCUtils.initCADShapeFactory();
		
		boolean supportShapes = true;
		OCCShell.setDefaultMakeSolid(true);
		System.out.println(">>>>>> OCCShell default-make-solid: " + OCCShell.isDefaultMakeSolid());

		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, supportShapes);

		// Write to a file
		String fileName = "test16.brep";
		
		if (OCCUtils.write(fileName, fuselageShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
		
	}

}
