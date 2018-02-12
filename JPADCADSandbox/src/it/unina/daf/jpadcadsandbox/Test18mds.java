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
		boolean exportSolids = true;
		boolean exportSupportShapes = true;	
		
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING,1e-2, exportLofts, exportSolids, exportSupportShapes);
//		List<OCCShape> horTailShapes = AircraftUtils.getLiftingSurfaceCAD(horTail, ComponentEnum.HORIZONTAL_TAIL, exportLofts, exportSupportShapes);
//		List<OCCShape> verTailShapes = AircraftUtils.getLiftingSurfaceCAD(verTail, ComponentEnum.VERTICAL_TAIL, exportLofts, exportSupportShapes);
		
		Fuselage fuselage = theAircraft.getFuselage();
		fuselage.getFuselageCreator().calculateGeometry(40, 3, 40, 20, 20);
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(
				fuselage, 
				0.15, 1.0, 3, 13, 7, 1.0, 0.10, 3, 
				exportLofts, exportSupportShapes);

		
		
		// Write to a file
		String fileName = "test18mds.brep";
		
		if (OCCUtils.write(fileName, wingShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
		
		
		// ====================
		// write on step file, only solids
		String fileNameSolids = "test18mds_solids.step";
		List<TopoDS_Shape> tds_shapes = new ArrayList<>();
		List<OCCShape> allShapes = wingShapes;
		allShapes.addAll(fuselageShapes);
		
		allShapes.stream()
				  .forEach( sh -> {
					  TopoDS_Shape tds_shape1 = sh.getShape();
					  // filter solids
					  TopExp_Explorer exp1 = new TopExp_Explorer(tds_shape1, TopAbs_ShapeEnum.TopAbs_SOLID);
					  while (exp1.More() > 0) {
						  System.out.println(">> [main] solid!");
						  tds_shapes.add(exp1.Current());
						  exp1.Next();
					  }
				  });
		STEPControl_Writer stepWriter = new STEPControl_Writer();
		tds_shapes.stream()
			.forEach(tds -> stepWriter.Transfer(tds, STEPControl_StepModelType.STEPControl_AsIs));

		IFSelect_ReturnStatus statusStep = stepWriter.Write(fileNameSolids);
		System.out.println("========== [main] STEP output status: " + statusStep);
		
		
		
	}
	
}
