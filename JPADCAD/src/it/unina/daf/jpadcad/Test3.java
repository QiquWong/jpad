package it.unina.daf.jpadcad;

import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeVertex;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_Interpolate;
import opencascade.GeomAbs_Shape;
import opencascade.Geom_BSplineCurve;
import opencascade.Precision;
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Vertex;
import opencascade.TopoDS_Wire;
import opencascade.gp_Pnt;

public class Test3 {

	/** Easy creation of vertices */
	static TopoDS_Vertex createVertex(double x, double y, double z)
	{
		return (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(new gp_Pnt(x, y, z)).Vertex();
	}
	/** Easy creation of vertices */
	static TopoDS_Vertex createVertex(double[] coords)
	{
		if (coords.length == 3)
			return (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
					new gp_Pnt(coords[0], coords[1], coords[2])).Vertex();
		else
			return null;
	}	
	/** Easy creation of vertices */
	static TopoDS_Vertex createVertex(gp_Pnt p)
	{
		return (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(p).Vertex();
	}
	/** Easy creation of edges */
	static TopoDS_Edge createEdge(TopoDS_Vertex v1, TopoDS_Vertex v2)
	{
		return (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(v1, v2).Edge();
	}
	/** Easy creation of faces */
	static TopoDS_Face createFace(TopoDS_Edge e1, TopoDS_Edge e2, TopoDS_Edge e3, TopoDS_Edge e4)
	{
		TopoDS_Wire wirePlate =
			(TopoDS_Wire) new BRepBuilderAPI_MakeWire(e1, e2, e3, e4).Shape();
		return (TopoDS_Face) new BRepBuilderAPI_MakeFace(wirePlate).Face();
	}
	/** Easy creation of faces */
	static TopoDS_Face createFace(TopoDS_Shape wire)
	{
		return (TopoDS_Face) new BRepBuilderAPI_MakeFace((TopoDS_Wire)wire).Face();
	}
	
	public static void main(String[] args) {
		System.out.println("Testing Java Wrapper of OCCT v7.0.0");

		// curve #1
		TColgp_HArray1OfPnt points1 = new TColgp_HArray1OfPnt(1, 3);
		points1.SetValue(1, new gp_Pnt( 0,  0, 0));
		points1.SetValue(2, new gp_Pnt( 0, 10, 5));
		points1.SetValue(3, new gp_Pnt( 0, 20, 0));
		long isPeriodic = 0;
		GeomAPI_Interpolate aNoPeriodInterpolate1 = 
				new GeomAPI_Interpolate(points1, isPeriodic, Precision.Confusion());
		aNoPeriodInterpolate1.Perform();
		Geom_BSplineCurve anInterpolationCurve1 = aNoPeriodInterpolate1.Curve();
		TopoDS_Edge spline1 = new BRepBuilderAPI_MakeEdge(anInterpolationCurve1).Edge();

		// curve #2
		TColgp_HArray1OfPnt points2 = new TColgp_HArray1OfPnt(1, 3);
		points2.SetValue(1, new gp_Pnt(15,  0, 0));
		points2.SetValue(2, new gp_Pnt(15, 10, 5));
		points2.SetValue(3, new gp_Pnt(15, 20, 0));
		isPeriodic = 0;
		GeomAPI_Interpolate aNoPeriodInterpolate2 = 
				new GeomAPI_Interpolate(points2, isPeriodic, Precision.Confusion());
		aNoPeriodInterpolate2.Perform();
		Geom_BSplineCurve anInterpolationCurve2 = aNoPeriodInterpolate2.Curve();
		TopoDS_Edge spline2 = new BRepBuilderAPI_MakeEdge(anInterpolationCurve2).Edge();

		// curve #3
		TColgp_HArray1OfPnt points3 = new TColgp_HArray1OfPnt(1, 3);
		points3.SetValue(1, new gp_Pnt(45,  0, 0));
		points3.SetValue(2, new gp_Pnt(45, 10, 5));
		points3.SetValue(3, new gp_Pnt(45, 20, 0));
		isPeriodic = 0;
		GeomAPI_Interpolate aNoPeriodInterpolate3 = 
				new GeomAPI_Interpolate(points3, isPeriodic, Precision.Confusion());
		aNoPeriodInterpolate3.Perform();
		Geom_BSplineCurve anInterpolationCurve3 = aNoPeriodInterpolate3.Curve();
		TopoDS_Edge spline3 = new BRepBuilderAPI_MakeEdge(anInterpolationCurve3).Edge();

		// Display various other properties
		GProp_GProps property = new GProp_GProps(); // store measurements
		
		// If Eps is argument is absent, precision is quite poor
		BRepGProp.LinearProperties(spline1, property);
		System.out.println("spline 1 length = " + property.Mass());
		BRepGProp.LinearProperties(spline2, property);
		System.out.println("spline 2 length = " + property.Mass());
		BRepGProp.LinearProperties(spline3, property);
		System.out.println("spline 3 length = " + property.Mass());
		
		// Loft surface
		BRepOffsetAPI_ThruSections loft = new BRepOffsetAPI_ThruSections();
		
		TopoDS_Vertex vertex1 = Test3.createVertex(10, 15, 0);
		//loft.AddVertex(vertex1);
		
		// make wires
		BRepBuilderAPI_MakeWire wire1 = new BRepBuilderAPI_MakeWire();
		wire1.Add(spline1);
		BRepBuilderAPI_MakeWire wire2 = new BRepBuilderAPI_MakeWire();
		wire2.Add(spline2);
		BRepBuilderAPI_MakeWire wire3 = new BRepBuilderAPI_MakeWire();
		wire3.Add(spline3);
		
		// add wires to loft structure
		loft.AddWire(wire1.Wire());
		loft.AddWire(wire2.Wire());
		loft.AddWire(wire3.Wire());
		
		loft.Build();
		
		BRepGProp.SurfaceProperties(loft.Shape(), property);
		System.out.println("Loft surface area = " + property.Mass());

		// try a filling surface
		BRepOffsetAPI_MakeFilling filled = new BRepOffsetAPI_MakeFilling();
		TopExp_Explorer faceExplorer = new TopExp_Explorer(loft.Shape(), TopAbs_ShapeEnum.TopAbs_FACE);
		TopoDS_Face loftFace = TopoDS.ToFace(faceExplorer.Current());
		System.out.println("loftFace - Shape type: " + loftFace.ShapeType());
		
		// finds Edges in Face
		TopExp_Explorer edgeExplorer = new TopExp_Explorer(loftFace, TopAbs_ShapeEnum.TopAbs_EDGE);
		for ( ; edgeExplorer.More()!=0; edgeExplorer.Next())
		{
			TopoDS_Edge anEdge = TopoDS.ToEdge(edgeExplorer.Current());
			if (BRep_Tool.Degenerated(anEdge)==0)
			{
				System.out.println("REGULAR Edge!");
				filled.Add(anEdge, GeomAbs_Shape.GeomAbs_C0);
			}
			else 
			{
				System.out.println("DEGENERATE Edge!");				
			}
		}

		
		// Constrain to a point off-the initial surface
		gp_Pnt pointConstraint = new gp_Pnt(35, 10, 0);
		TopoDS_Vertex vertexConstraint = Test3.createVertex(pointConstraint);

		filled.Add(pointConstraint); // constraint
		
		filled.LoadInitSurface(loftFace);
		filled.Build();
		System.out.println("Deformed surface is done? = " + filled.IsDone());
		
		BRepGProp.SurfaceProperties(filled.Shape(), property);
		System.out.println("Filled surface area = " + property.Mass());
		
		// Put everything in a compound
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		builder.Add(compound, spline1);
		builder.Add(compound, spline2);
		builder.Add(compound, spline3);
		builder.Add(compound, vertex1);
		builder.Add(compound, loft.Shape());
		builder.Add(compound, vertexConstraint);
		builder.Add(compound, filled.Shape());
		
		
		//Write to a file
		String fileName = "test03.brep";
		BRepTools.Write(compound, fileName);
		
		System.out.println("Output written on file: " + fileName);

	}

}
