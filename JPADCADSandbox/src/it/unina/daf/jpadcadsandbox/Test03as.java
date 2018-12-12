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

public class Test03as {

	public static void main(String[] args) {

		gp_Pnt p1 = new gp_Pnt(-13.45,0,0); 
		gp_Pnt p2 = new gp_Pnt(-13.45,-31.5,0);
		gp_Pnt p3 = new gp_Pnt(-22,-31.5,0);
		gp_Pnt p4 = new gp_Pnt(-24,-6,0);
		gp_Pnt p5 = new gp_Pnt(-33,-6,0);
		gp_Pnt p6 = new gp_Pnt(-35,-33,0);
		gp_Pnt p7 = new gp_Pnt(-41,-33,0);
		gp_Pnt p8 = new gp_Pnt(-41,0,0);


		TopoDS_Edge edge1 = new BRepBuilderAPI_MakeEdge(p1,p2).Edge();
		TopoDS_Edge edge2 = new BRepBuilderAPI_MakeEdge(p2,p3).Edge();
		TopoDS_Edge edge3 = new BRepBuilderAPI_MakeEdge(p3,p4).Edge();
		TopoDS_Edge edge4 = new BRepBuilderAPI_MakeEdge(p4,p5).Edge();
		TopoDS_Edge edge5 = new BRepBuilderAPI_MakeEdge(p5,p6).Edge();
		TopoDS_Edge edge6 = new BRepBuilderAPI_MakeEdge(p6,p7).Edge();
		TopoDS_Edge edge7 = new BRepBuilderAPI_MakeEdge(p7,p8).Edge();

		BRepBuilderAPI_MakeWire mWire1 = new BRepBuilderAPI_MakeWire(edge1,edge2,edge3,edge4);
		mWire1.Add(edge5);
		mWire1.Add(edge6);
		mWire1.Add(edge7);

		TopoDS_Wire wire1 = TopoDS.ToWire(mWire1.Shape());

		// Mirror transformation
		gp_Pnt aPoint = new gp_Pnt(0,0,0);
		gp_Dir aDir = new gp_Dir(1,0,0);
		gp_Ax1 axis = new gp_Ax1(aPoint,aDir);
		gp_Trsf mirror = new gp_Trsf();
		mirror.SetMirror(axis);

		TopoDS_Wire wireMirr = TopoDS.ToWire(new BRepBuilderAPI_Transform(wire1,mirror).Shape());
		BRepBuilderAPI_MakeWire mWire2 = new BRepBuilderAPI_MakeWire();
		mWire2.Add(wire1);
		mWire2.Add(wireMirr);
		TopoDS_Wire finalWire = TopoDS.ToWire(mWire2.Shape());
				
		TopoDS_Face face = new BRepBuilderAPI_MakeFace(finalWire).Face();
		
		// Rotation		
		gp_Ax1 ax1 = new gp_Ax1(new gp_Pnt(0,0,0),new gp_Dir(0,1,0));
		TopoDS_Shape solid1 = new BRepPrimAPI_MakeRevol(face,ax1).Shape();
		
		
		BRepFilletAPI_MakeFillet fillets = new BRepFilletAPI_MakeFillet(solid1);
		 TopExp_Explorer anEdgeExplorer = new TopExp_Explorer(solid1,TopAbs_ShapeEnum.TopAbs_EDGE);

		 while(anEdgeExplorer.More()>0) {			
			 TopoDS_Edge anEdge = TopoDS.ToEdge(anEdgeExplorer.Current());
			 fillets.Add(3, anEdge);
			 anEdgeExplorer.Next();			
		 }

		 TopoDS_Shape solid2 = fillets.Shape();
		 
		
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		builder.Add(compound,solid2);

		 //Write to a file
		 String fileName = "test03as.brep";
		 BRepTools.Write(compound, fileName);

		 System.out.println("Output written on file: " + fileName);


	}

}
