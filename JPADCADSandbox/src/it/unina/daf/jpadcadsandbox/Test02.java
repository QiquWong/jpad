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
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Vertex;
import opencascade.TopoDS_Wire;
import opencascade.gp_Pnt;
import opencascade.gp_Vec;

public class Test02 {

	/** Easy creation of vertices */
	static TopoDS_Vertex createVertex(double x, double y, double z)
	{
		return (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(new gp_Pnt(x, y, z)).Shape();
	}
	/** Easy creation of vertices */
	static TopoDS_Vertex createVertex(double[] coords)
	{
		if (coords.length == 3)
			return (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
					new gp_Pnt(coords[0], coords[1], coords[2])).Shape();
		else
			return null;
	}	
	/** Easy creation of edges */
	static TopoDS_Edge createEdge(TopoDS_Vertex v1, TopoDS_Vertex v2)
	{
		return (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(v1, v2).Shape();
	}
	/** Easy creation of faces */
	static TopoDS_Face createFace(TopoDS_Edge e1, TopoDS_Edge e2, TopoDS_Edge e3, TopoDS_Edge e4)
	{
		TopoDS_Wire wirePlate =
			(TopoDS_Wire) new BRepBuilderAPI_MakeWire(e1, e2, e3, e4).Shape();
		return (TopoDS_Face) new BRepBuilderAPI_MakeFace(wirePlate).Shape();
	}
	/** Easy creation of faces */
	static TopoDS_Face createFace(TopoDS_Shape wire)
	{
		return (TopoDS_Face) new BRepBuilderAPI_MakeFace((TopoDS_Wire)wire).Shape();
	}
	
	public static void main(String[] args) {
		System.out.println("Testing Java Wrapper of OCCT v7.0.0");
		
		TColgp_HArray1OfPnt points = new TColgp_HArray1OfPnt(1, 4);
		points.SetValue(1, new gp_Pnt( 0,  0, 0));
		points.SetValue(2, new gp_Pnt( 0, 10, 0));
		points.SetValue(3, new gp_Pnt(10, 10, 0));
		points.SetValue(4, new gp_Pnt(15, 10, 0));
		
		//=====================================
		// Creating a non periodic interpolation curve without constraints 
		// (type of resulting curve is BSpline)
		long isPeriodic = 0;
		GeomAPI_Interpolate aNoPeriodInterpolate = 
				new GeomAPI_Interpolate(points, isPeriodic, Precision.Confusion());
		aNoPeriodInterpolate.Perform();
		// check results
		Geom_BSplineCurve anInterpolationCurve = aNoPeriodInterpolate.Curve();
		TopoDS_Shape spline = new BRepBuilderAPI_MakeEdge(anInterpolationCurve).Shape();

		//=====================================
		// Make a face by extruding the spline
		TopoDS_Shape face = new BRepPrimAPI_MakePrism(
				spline, new gp_Vec(0,0,15)).Shape();
		
		
		//Put everything in a compound
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		builder.Add(compound, spline);
		builder.Add(compound, face);
		
		//Write to a file
		String fileName = "test02.brep";
		BRepTools.Write(compound, fileName);
		
		System.out.println("Output written on file: " + fileName);
		
		
	}

}
