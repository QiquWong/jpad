package it.unina.daf.jpadcadsandbox;

import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCUtils;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.GeomPlate_BuildPlateSurface;
import opencascade.TopoDS;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Wire;
import opencascade.gp_Pnt;

public class Test17 {

	public static void main(String[] args) {
		
		gp_Pnt p00 = new gp_Pnt(0.0, 0.0, 0.0);
		gp_Pnt p10 = new gp_Pnt(1.0, 0.0, 0.0);
		gp_Pnt p11 = new gp_Pnt(1.0, 1.0, 0.0);
		gp_Pnt p01 = new gp_Pnt(0.0, 1.0, 0.0);
		
		gp_Pnt p001 = new gp_Pnt(0.0, 0.0, 1.0);
		
		BRepBuilderAPI_MakeEdge em1 = new BRepBuilderAPI_MakeEdge(p00, p10);
		em1.Build();
		System.out.println("Edge 1, done? " + em1.IsDone());

		BRepBuilderAPI_MakeEdge em2 = new BRepBuilderAPI_MakeEdge(p10, p11);
		em2.Build();
		System.out.println("Edge 2, done? " + em2.IsDone());

		BRepBuilderAPI_MakeEdge em3 = new BRepBuilderAPI_MakeEdge(p11, p01);
		em3.Build();
		System.out.println("Edge 3, done? " + em3.IsDone());
		
		BRepBuilderAPI_MakeEdge em4 = new BRepBuilderAPI_MakeEdge(p01, p00);
		em4.Build();
		System.out.println("Edge 4, done? " + em4.IsDone());

		BRepBuilderAPI_MakeEdge em5 = new BRepBuilderAPI_MakeEdge(p00, p001);
		em5.Build();
		System.out.println("Edge 5, done? " + em5.IsDone());

		BRepBuilderAPI_MakeEdge em6 = new BRepBuilderAPI_MakeEdge(p10, p001);
		em6.Build();
		System.out.println("Edge 6, done? " + em6.IsDone());

		BRepBuilderAPI_MakeEdge em7 = new BRepBuilderAPI_MakeEdge(p11, p001);
		em7.Build();
		System.out.println("Edge 7, done? " + em7.IsDone());

		BRepBuilderAPI_MakeEdge em8 = new BRepBuilderAPI_MakeEdge(p01, p001);
		em8.Build();
		System.out.println("Edge 8, done? " + em8.IsDone());
		
		BRepBuilderAPI_MakeWire wm0 = new BRepBuilderAPI_MakeWire();
		wm0.Add(em1.Edge());
		wm0.Add(em2.Edge());
		wm0.Add(em3.Edge());
		wm0.Add(em4.Edge());
		wm0.Build();
		System.out.println("Wire 0, done? " + wm0.IsDone());

		OCCUtils.initCADShapeFactory(); // theFactory now non-null

		CADEdge e1 = (CADEdge) OCCUtils.theFactory.newShape(em1.Edge());
		CADGeomCurve3D c1 = OCCUtils.theFactory.newCurve3D(e1);
		
//		TopoDS_Face f0 = new BRepBuilderAPI_MakeFace(wm0.Wire()).Face();
//		System.out.println("Face 0, closed? " + f0.Closed());
//		
//		GeomPlate_BuildPlateSurface geomPlateBuilder = 
//				// new GeomPlate_BuildPlateSurface(3, 2, 10000, 1.e-4, 1.e-5, 1.e-2, 1.e-1, 0); 
//				new GeomPlate_BuildPlateSurface();
//		geomPlateBuilder.Add(em1.Edge().);

	
		
		
		
	}

}
