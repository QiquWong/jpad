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
import opencascade.IFSelect_ReturnStatus;
import opencascade.STEPControl_StepModelType;
import opencascade.STEPControl_Writer;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Shape;

public class Test19mds {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft and the fuselage ...");
		Aircraft theAircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = theAircraft.getFuselage();
		LiftingSurface wing = theAircraft.getWing();
		LiftingSurface horTail = theAircraft.getHTail();
		LiftingSurface verTail = theAircraft.getVTail();
		
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, true, true);
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-2, true, true);
		List<OCCShape> horTailShapes = AircraftUtils.getLiftingSurfaceCAD(horTail, ComponentEnum.HORIZONTAL_TAIL, 1e-3, true, true);
		List<OCCShape> verTailShapes = AircraftUtils.getLiftingSurfaceCAD(verTail, ComponentEnum.VERTICAL_TAIL, 1e-2, true, true);
		
		// Write to a file
		String fileName = "test19mds.brep";

		if(OCCUtils.write(fileName, fuselageShapes, wingShapes, horTailShapes, verTailShapes))
			System.out.println("========== [main] Output written on file: " + fileName);		
	}	
}	