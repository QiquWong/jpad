package it.unina.daf.jpadcadsandbox;

import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepAlgoAPI_Fuse;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeVertex;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepFilletAPI_MakeFillet;
import opencascade.BRepOffsetAPI_MakeThickSolid;
import opencascade.BRepPrimAPI_MakeCylinder;
import opencascade.BRepPrimAPI_MakePrism;
import opencascade.BRepPrimAPI_MakeRevol;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GeomAPI_Interpolate;
import opencascade.Geom_BSplineCurve;
import opencascade.Geom_Circle;
import opencascade.Geom_Curve;
import opencascade.Geom_CylindricalSurface;
import opencascade.Geom_Plane;
import opencascade.Geom_Surface;
import opencascade.GC_Root;
import opencascade.Geom2d_BezierCurve;
import opencascade.Geom2d_Curve;
import opencascade.Geom2d_Ellipse;
import opencascade.Geom2d_TrimmedCurve;
import opencascade.Precision;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TColgp_Array1OfPnt2d;
import opencascade.TColgp_Array1OfVec;
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TColgp_HArray1OfPnt2d;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_ListOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Solid;
import opencascade.TopoDS_Vertex;
import opencascade.TopoDS_Wire;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Ax2d;
import opencascade.gp_Ax3;
import opencascade.gp_Dir;
import opencascade.gp_Dir2d;
import opencascade.gp_Pnt;
import opencascade.gp_Pnt2d;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import opencascade.Geom_TrimmedCurve;
import opencascade.Standard_Transient;
import opencascade.Standard_Type;

public class Test04as {

	static double diameter = 30;

	public static void main(String[] args) {

		// Shaft
		gp_Pnt p = new gp_Pnt(0, 0, 0);
		gp_Ax2 ax = new gp_Ax2(p, new gp_Dir(0, -1, 0));
		Geom_Curve c = new Geom_Circle(ax, diameter/2);

		TopoDS_Edge edge_s = new BRepBuilderAPI_MakeEdge(c).Edge();				
		TopoDS_Wire wire_s = new BRepBuilderAPI_MakeWire(edge_s).Wire();
		TopoDS_Shape face_s = new BRepBuilderAPI_MakeFace(wire_s).Face();
		TopoDS_Shape solid_s = new BRepPrimAPI_MakePrism(face_s,new gp_Vec(0,-3*diameter,0)).Shape();

		// Hub

		gp_Pnt p1 = new gp_Pnt(0, 0, 0);
		gp_Ax2 ax1 = new gp_Ax2(p1, new gp_Dir(0, -1, 0));
		Geom_Curve c1 = new Geom_Circle(ax1, diameter*2);

		TopoDS_Edge edge1 = new BRepBuilderAPI_MakeEdge(c1).Edge();				
		TopoDS_Wire wire1 = new BRepBuilderAPI_MakeWire(edge1).Wire();
		TopoDS_Shape face1 = new BRepBuilderAPI_MakeFace(wire1).Face();
		TopoDS_Shape solid1 = new BRepPrimAPI_MakePrism(face1,new gp_Vec(0,-0.5*diameter,0)).Shape();

		gp_Pnt p2 = new gp_Pnt(0, 0, 0);
		gp_Ax2 ax2 = new gp_Ax2(p2, new gp_Dir(0, -1, 0));
		Geom_Curve c2 = new Geom_Circle(ax2, diameter);

		TopoDS_Edge edge2 = new BRepBuilderAPI_MakeEdge(c2).Edge();				
		TopoDS_Wire wire2 = new BRepBuilderAPI_MakeWire(edge2).Wire();
		TopoDS_Shape face2 = new BRepBuilderAPI_MakeFace(wire2).Face();
		TopoDS_Shape solid2 = new BRepPrimAPI_MakePrism(face2,new gp_Vec(0,-1.5*diameter,0)).Shape();

		TopoDS_Shape solid3 = new BRepAlgoAPI_Fuse(solid1,solid2).Shape();
		TopoDS_Shape solid4 = new BRepAlgoAPI_Cut(solid3, solid_s).Shape();
		
		// 4 Holes
		gp_Pnt p3 = new gp_Pnt(0, 0, 1.5*diameter);
		gp_Ax2 ax3 = new gp_Ax2(p3, new gp_Dir(0, -1, 0));
		Geom_Curve c3 = new Geom_Circle(ax3, diameter*0.3);

		TopoDS_Edge edge_h1 = new BRepBuilderAPI_MakeEdge(c3).Edge();				
		TopoDS_Wire wire_h1 = new BRepBuilderAPI_MakeWire(edge_h1).Wire();
		TopoDS_Shape face_h1 = new BRepBuilderAPI_MakeFace(wire_h1).Face();
		TopoDS_Shape solid_h1 = new BRepPrimAPI_MakePrism(face_h1,new gp_Vec(0,-0.5*diameter,0)).Shape();
		
		gp_Pnt aPoint = new gp_Pnt(0,0,0);
		gp_Dir aDir = new gp_Dir(0,-1,0);
		gp_Ax1 axis = new gp_Ax1(aPoint,aDir);
		gp_Trsf rot = new gp_Trsf();
		rot.SetRotation(axis,Math.PI/2);
		
		TopoDS_Solid solid_h2 = TopoDS.ToSolid(new BRepBuilderAPI_Transform(solid_h1,rot).Shape());
		TopoDS_Solid solid_h3 = TopoDS.ToSolid(new BRepBuilderAPI_Transform(solid_h2,rot).Shape());
		TopoDS_Solid solid_h4 = TopoDS.ToSolid(new BRepBuilderAPI_Transform(solid_h3,rot).Shape());

		TopoDS_Shape solid5 = new BRepAlgoAPI_Cut(solid4, solid_h1).Shape();
		TopoDS_Shape solid6 = new BRepAlgoAPI_Cut(solid5, solid_h2).Shape();
		TopoDS_Shape solid7 = new BRepAlgoAPI_Cut(solid6, solid_h3).Shape();
		TopoDS_Shape solid_h = new BRepAlgoAPI_Cut(solid7, solid_h4).Shape();

		
		// Sunk key
		
		gp_Pnt p1_k = new gp_Pnt(3,0,12);
		gp_Pnt p2_k = new gp_Pnt(3,0,16.5);
		gp_Pnt p3_k = new gp_Pnt(-3,0,16.5);
		gp_Pnt p4_k = new gp_Pnt(-3,0,12);
		
		TopoDS_Edge edge_k1 = new BRepBuilderAPI_MakeEdge(p1_k,p2_k).Edge();
		TopoDS_Edge edge_k2 = new BRepBuilderAPI_MakeEdge(p2_k,p3_k).Edge();
		TopoDS_Edge edge_k3 = new BRepBuilderAPI_MakeEdge(p3_k,p4_k).Edge();
		TopoDS_Edge edge_k4 = new BRepBuilderAPI_MakeEdge(p4_k,p1_k).Edge();
		TopoDS_Wire wire_k = new BRepBuilderAPI_MakeWire(edge_k1,edge_k2,edge_k3,edge_k4).Wire();
		TopoDS_Face face_k = new BRepBuilderAPI_MakeFace(wire_k).Face();
		TopoDS_Shape solid_k = new BRepPrimAPI_MakePrism(face_k,new gp_Vec(0,-1.7*diameter,0)).Shape();


		TopoDS_Shape solid_hfinal = new BRepAlgoAPI_Cut(solid_h,solid_k).Shape();
		TopoDS_Shape solid_sfinal = new BRepAlgoAPI_Cut(solid_s,solid_k).Shape();
		
		
		
		
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		builder.Add(compound,solid_hfinal);
		builder.Add(compound, solid_sfinal);
		builder.Add(compound, wire_k);
		//Write to a file
		String fileName = "test04as.brep";
		BRepTools.Write(compound, fileName);

		System.out.println("Output written on file: " + fileName);


	}

}
