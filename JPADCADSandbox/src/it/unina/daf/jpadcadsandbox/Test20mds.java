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
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.StlAPI_ErrorStatus;
import opencascade.StlAPI_Writer;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS_Shape;

public class Test20mds {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft and the fuselage ...");
		Aircraft theAircraft = AircraftUtils.importAircraft(args);
		
//		Fuselage fuselage = theAircraft.getFuselage();
		LiftingSurface wing = theAircraft.getWing();
		
//		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, true, true);	
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
		
		// Write to a file
		String fileName = "test20mds.brep";

		if(OCCUtils.write(fileName, wingShapes))
			System.out.println("========== [main] Output written on file: " + fileName);	
		
		// write on stl file, only solids
		String fileNameSTL = "Test20mds_solids.stl";
		List<TopoDS_Shape> tdsSolid = new ArrayList<>();
		wingShapes.forEach(s -> {
			TopoDS_Shape tdsShape = s.getShape();
			TopExp_Explorer exp = new TopExp_Explorer(tdsShape, TopAbs_ShapeEnum.TopAbs_SOLID);
			while(exp.More() > 0) {
				tdsSolid.add(exp.Current());
				exp.Next();
			}
		});
		
		BRepMesh_IncrementalMesh solidMesh = new BRepMesh_IncrementalMesh();
		tdsSolid.forEach(s -> solidMesh.SetShape(s));
		solidMesh.Perform();
		TopoDS_Shape tdsWingSolidMeshed = solidMesh.Shape();
		tdsWingSolidMeshed.Reverse();
		
		StlAPI_Writer stlWriter = new StlAPI_Writer();
		stlWriter.Write(tdsWingSolidMeshed, fileNameSTL);
	}
}
