package it.unina.daf.jpadcadsandbox;

import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeVertex;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepPrimAPI_MakePrism;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.GeomAPI_Interpolate;
import opencascade.Geom_BSplineCurve;
import opencascade.Geom_Circle;
import opencascade.Precision;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TColgp_Array1OfVec;
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Vertex;
import opencascade.TopoDS_Wire;
import opencascade.gp_Pnt;
import opencascade.gp_Vec;

public class Test01as {

	public static void main(String[] args) {
		System.out.println("First test in JPADCADSandbox");
		System.out.println("Creation of a closed 3D curve");
		
		// Creating array of points
		TColgp_HArray1OfPnt points = new TColgp_HArray1OfPnt(1, 4);
		points.SetValue(1, new gp_Pnt( 0,  5, 0));
		points.SetValue(2, new gp_Pnt( 5, 0, 0));
		points.SetValue(3, new gp_Pnt(0, -5, 0));
		points.SetValue(4, new gp_Pnt(-5, 0, 0));
		// Creating a periodic 3D curve
		GeomAPI_Interpolate periodicCurve = new GeomAPI_Interpolate(points,1, Precision.Angular());
		
		TColgp_Array1OfVec tangents = new TColgp_Array1OfVec(1,4);
		tangents.SetValue(1, new gp_Vec(new gp_Pnt(0,5,0),new gp_Pnt(1,5,0)));
		tangents.SetValue(2, new gp_Vec(new gp_Pnt(5,0,0),new gp_Pnt(5,1,0)));
		tangents.SetValue(3, new gp_Vec(new gp_Pnt(0,-5,0),new gp_Pnt(1,-5,0)));
		tangents.SetValue(1, new gp_Vec(new gp_Pnt(-5,0,0),new gp_Pnt(-5,1,0)));
		
		periodicCurve.Perform();
		periodicCurve.IsDone();
		// Check result
		if (periodicCurve.IsDone()==1)
			System.out.println("Curve succssfully constructed.");
		else
			System.out.println("Error in curve construction!");
		
		Geom_BSplineCurve interpCurve = periodicCurve.Curve();
		
		// Make a face by extruding the spline
		// TopoDS_Shape spline = new BRepBuilderAPI_MakeEdge(interpCurve).Shape();
		// TopoDS_Shape face = new BRepPrimAPI_MakePrism(spline, new gp_Vec(0,0,15)).Shape();
		
		// Make a solid by extruding the face
		TopoDS_Edge spline = new BRepBuilderAPI_MakeEdge(interpCurve).Edge();
		TopoDS_Wire wire = new BRepBuilderAPI_MakeWire(spline).Wire();
		TopoDS_Face face = new BRepBuilderAPI_MakeFace(wire).Face();
		TopoDS_Shape solid = new BRepPrimAPI_MakePrism(face, new gp_Vec(0,0,15)).Shape();
		
		
		
		//Put everything in a compound
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		builder.Add(compound, spline);
		builder.Add(compound, face);
		builder.Add(compound, solid);
		
		//Write to a file
		String fileName = "test01as.brep";
		BRepTools.Write(compound, fileName);
		
		System.out.println("Output written on file: " + fileName);
		
		
	}

}