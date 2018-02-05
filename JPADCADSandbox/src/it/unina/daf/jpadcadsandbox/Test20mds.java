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
import opencascade.BRep_Builder;
import opencascade.StlAPI_Writer;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;

public class Test20mds {

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
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, true, true, true);
		List<OCCShape> horTailShapes = AircraftUtils.getLiftingSurfaceCAD(horTail, ComponentEnum.HORIZONTAL_TAIL, 1e-3, true, true, true);
		List<OCCShape> verTailShapes = AircraftUtils.getLiftingSurfaceCAD(verTail, ComponentEnum.VERTICAL_TAIL, 1e-3, true, true, true);
		
		// Write to a file
//		String fileName = "test20mds.brep";
//
//		if(OCCUtils.write(fileName, fuselageShapes, wingShapes, horTailShapes, verTailShapes))
//			System.out.println("========== [main] Output written on file: " + fileName);	
		
		// write on stl file, only solids
//		List<OCCShape> allShapes = new ArrayList<>();
//		allShapes.addAll(fuselageShapes);
//		allShapes.addAll(wingShapes);
//		allShapes.addAll(horTailShapes);
//		allShapes.addAll(verTailShapes);
//		
//		String fileNameSTL = "Test20mds_solids.stl";
//		List<TopoDS_Shape> tdsSolids = new ArrayList<>();
//		allShapes.forEach(s -> {
//			TopoDS_Shape tdsShape = s.getShape();
//			TopExp_Explorer exp = new TopExp_Explorer(tdsShape, TopAbs_ShapeEnum.TopAbs_SOLID);
//			while(exp.More() > 0) {
//				tdsSolids.add(exp.Current());
//				exp.Next();
//			}
//		});
//		
//		BRep_Builder compoundBuilder = new BRep_Builder();
//		TopoDS_Compound solidsCompound = new TopoDS_Compound();
//		compoundBuilder.MakeCompound(solidsCompound);
//		
//		// meshing each solid separately
//		BRepMesh_IncrementalMesh solidMesh = new BRepMesh_IncrementalMesh();
//		StlAPI_Writer stlWriter = new StlAPI_Writer();
//		tdsSolids.forEach(s -> {
//			solidMesh.SetShape(s);
//			solidMesh.Perform();
//			TopoDS_Shape tdsSolidMeshed = solidMesh.Shape();
//			tdsSolidMeshed.Reverse();
//			compoundBuilder.Add(solidsCompound, tdsSolidMeshed);		
//		});
		
		// meshing all the solids at the same time
//		tdsSolids.forEach(s -> compoundBuilder.Add(solidsCompound, s));
//		solidMesh.SetShape(solidsCompound);
//		solidMesh.Perform();
//		TopoDS_Shape tdsSolidMeshed = solidMesh.Shape();
//		tdsSolidMeshed.Reverse();
//		
//		//stlWriter.Write(solidsCompound, fileNameSTL);
//		stlWriter.Write(tdsSolidMeshed, fileNameSTL);
		
		// write to a file
		List<OCCShape> allShapes = new ArrayList<>();
//		allShapes.addAll(fuselageShapes);
		allShapes.addAll(wingShapes);
//		allShapes.addAll(horTailShapes);
//		allShapes.addAll(verTailShapes);
		
//		AircraftUtils.getAircraftSolidFile(allShapes, "Test20mds", ".brep");
		AircraftUtils.getAircraftSolidFile(allShapes, "Test20mds_solids", ".stl");
	}
}
