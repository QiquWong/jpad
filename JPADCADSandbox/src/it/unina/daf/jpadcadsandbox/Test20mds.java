package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
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
		
		LiftingSurface wing = theAircraft.getWing();
		
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, true, true, true);
		
		// write on stl file, only solids
		String fileName = "Test20mds_solids.stl";
		List<TopoDS_Shape> tdsWingSolids = new ArrayList<>();
		wingShapes.forEach(s -> {
			TopoDS_Shape tdsShape = s.getShape();
			TopExp_Explorer exp = new TopExp_Explorer(tdsShape, TopAbs_ShapeEnum.TopAbs_SOLID);
			while(exp.More() > 0) {
				tdsWingSolids.add(exp.Current());
				exp.Next();
			}
		});
		BRepMesh_IncrementalMesh solidMesh = new BRepMesh_IncrementalMesh();
		tdsWingSolids.forEach(s -> solidMesh.SetShape(s));
		solidMesh.Perform();
		TopoDS_Shape tdsWingSolidMeshed = solidMesh.Shape();
		StlAPI_Writer stlWriter = new StlAPI_Writer();
		stlWriter.Write(tdsWingSolidMeshed, fileName);
	}
}
