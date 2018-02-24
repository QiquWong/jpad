package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;

public class Test21mds {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft ...");
		Aircraft theAircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = theAircraft.getFuselage();
		LiftingSurface wing = theAircraft.getWing();
		LiftingSurface horTail = theAircraft.getHTail();
		LiftingSurface verTail = theAircraft.getVTail();
		LiftingSurface canard = theAircraft.getCanard();
		
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, true, false);	
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
		List<OCCShape> horTailShapes = AircraftUtils.getLiftingSurfaceCAD(horTail, ComponentEnum.HORIZONTAL_TAIL, 1e-3, false, true, false);
		List<OCCShape> verTailShapes = AircraftUtils.getLiftingSurfaceCAD(verTail, ComponentEnum.VERTICAL_TAIL, 1e-3, false, true, false);
		List<OCCShape> canardShapes = AircraftUtils.getLiftingSurfaceCAD(canard, ComponentEnum.CANARD, 1e-3, false, true, false);
		
		// Write to a file
//		String fileName = "test21mds.brep";
//
//		if(OCCUtils.write(fileName, fuselageShapes, wingShapes, horTailShapes, verTailShapes, canardShapes))
//			System.out.println("========== [main] Output written on file: " + fileName);
		
		List<OCCShape> allShapes = new ArrayList<OCCShape>();
		allShapes.addAll(fuselageShapes);
		allShapes.addAll(wingShapes);
		allShapes.addAll(horTailShapes);
		allShapes.addAll(verTailShapes);
		allShapes.addAll(canardShapes);
		AircraftUtils.getAircraftSolidFile(allShapes, "IRON", ".brep");
		AircraftUtils.getAircraftSolidFile(allShapes, "IRON", ".step");
	}

}
