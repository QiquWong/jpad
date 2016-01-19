package sandbox.adm;

import java.util.ArrayList;

import org.jcae.opencascade.jni.BRepBuilderAPI_MakeEdge;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeFace;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeVertex;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeWire;
import org.jcae.opencascade.jni.BRepFill_Filling;
import org.jcae.opencascade.jni.BRepGProp;
import org.jcae.opencascade.jni.BRepOffsetAPI_MakeFilling;
import org.jcae.opencascade.jni.BRepOffsetAPI_ThruSections;
import org.jcae.opencascade.jni.BRepTools;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.BRep_Tool;
import org.jcae.opencascade.jni.GProp_GProps;
import org.jcae.opencascade.jni.GeomAPI_Interpolate;
import org.jcae.opencascade.jni.GeomAPI_ProjectPointOnCurve;
import org.jcae.opencascade.jni.GeomAbs_Shape;
import org.jcae.opencascade.jni.Geom_BSplineCurve;
import org.jcae.opencascade.jni.TopAbs_ShapeEnum;
import org.jcae.opencascade.jni.TopExp_Explorer;
import org.jcae.opencascade.jni.TopoDS_Compound;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Face;
import org.jcae.opencascade.jni.TopoDS_Shape;
import org.jcae.opencascade.jni.TopoDS_Vertex;
import org.jcae.opencascade.jni.TopoDS_Wire;


/**
 * Try to build a complex non-trivial spline/nurbs
 * 
 * see: OccJava : TopoJunction.java, MyTest_ADM02
 */

public class MyTest_ADM_03aSplines {

	/** Easy creation of vertices */
	static TopoDS_Vertex createVertex(double x, double y, double z)
	{
		return (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(new double[]{x, y, z}).shape();
	}

	/** Easy creation of vertices */
	static TopoDS_Vertex createVertex(double[] coords)
	{
		return (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(coords).shape();
	}

	/** Easy creation of edges */
	static TopoDS_Edge createEdge(TopoDS_Vertex v1, TopoDS_Vertex v2)
	{
		return (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(v1, v2).shape();
	}
	
	/** Easy creation of faces */
	static TopoDS_Face createFace(TopoDS_Edge e1, TopoDS_Edge e2, TopoDS_Edge e3, TopoDS_Edge e4)
	{
		TopoDS_Wire wirePlate=
			(TopoDS_Wire) new BRepBuilderAPI_MakeWire(e1, e2, e3, e4).shape();
		return (TopoDS_Face) new BRepBuilderAPI_MakeFace(wirePlate, true).shape();
	}
	
	/** Easy creation of faces */
	static TopoDS_Face createFace(TopoDS_Shape wire)
	{
		return (TopoDS_Face) new BRepBuilderAPI_MakeFace((TopoDS_Wire)wire, true).shape();
	}

	////////////////////////////////////////////////////////////////////
	
	public MyTest_ADM_03aSplines() {
		
		
		////////////////////////////////////////////////////////////////////////////

		// spline 1
		
		//test with spline
				//		double[] p1 = new double[]{0,0,0};
				//		double[] p2 = new double[]{0,10,0};
				//		double[] p3 = new double[]{10,10,0};

		double[] points_1 = new double[]{
//				0,0,0,
//				0,10,0,
//				10,10,0,
//				15,10,0
				0,0,0,
				0,10,5,
				0,20,0
				};

		GeomAPI_Interpolate repSpline_1 = new GeomAPI_Interpolate(
				points_1, // list of points
				false, // periodic or non-periodic 
				1E-7 // tolerance
				);
		repSpline_1.Perform();
		Geom_BSplineCurve s_1 = repSpline_1.Curve();
		TopoDS_Edge spline_1 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(s_1).shape();
		
//		// Extrude
//		TopoDS_Face face = (TopoDS_Face) new BRepPrimAPI_MakePrism(
//				spline, new double[]{0, 0, 15}).shape();

		// spline 2
		// build from a list of points
		
		ArrayList<double[]> pointList = new ArrayList<double[]>();

//		pointList.add(new double[]{  7,   0,  5 });
//		pointList.add(new double[]{  0,  10,  10 });
//		pointList.add(new double[]{ 15,  10,   5 });
//		pointList.add(new double[]{ 30,  12,  7 });

		pointList.add(new double[]{  15,   0,  0 });
		pointList.add(new double[]{  15,  10,  5 });
		pointList.add(new double[]{  15,  20,  0 });
		
		double[] points_2 = new double[3*pointList.size()];
		int k = 0;
		for (int i=0; i < pointList.size(); i++)
		{
			points_2[k] = pointList.get(i)[0];
			k++;
			points_2[k] = pointList.get(i)[1];
			k++;
			points_2[k] = pointList.get(i)[2];
			k++;
		}
		
//		double[] points_2 = new double[]{
//				0,0,0,
//				0,10,0,
//				10,10,0,
//				15,10,0
//				};

		GeomAPI_Interpolate repSpline_2 = new GeomAPI_Interpolate(
				points_2, // list of points
				false, // periodic or non-periodic 
				1E-7 // tolerance
				);
		repSpline_2.Perform();
		Geom_BSplineCurve s_2 = repSpline_2.Curve();
		TopoDS_Edge spline_2 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(s_2).shape();
		
		// spline 3
		// build from a list of points
		
		ArrayList<double[]> pointList_3 = new ArrayList<double[]>();

//		pointList_3.add(new double[]{  -7,  0,  15 });
//		pointList_3.add(new double[]{  0,  12,  20 });
//		pointList_3.add(new double[]{ 15,   8,  15 });
//		pointList_3.add(new double[]{ 20,  12,  17 });

		pointList_3.add(new double[]{  45,   0,  0 });
		pointList_3.add(new double[]{  45,  10,  5 });
		pointList_3.add(new double[]{  45,  20,  0 });
		
		double[] points_3 = new double[3*pointList_3.size()];
		k = 0;
		for (int i=0; i < pointList_3.size(); i++)
		{
			points_3[k] = pointList_3.get(i)[0];
			k++;
			points_3[k] = pointList_3.get(i)[1];
			k++;
			points_3[k] = pointList_3.get(i)[2];
			k++;
		}
		
//		double[] points_2 = new double[]{
//				0,0,0,
//				0,10,0,
//				10,10,0,
//				15,10,0
//				};

		GeomAPI_Interpolate repSpline_3 = new GeomAPI_Interpolate(
				points_3, // list of points
				false, // periodic or non-periodic 
				1E-7 // tolerance
				);
		repSpline_3.Perform();
		Geom_BSplineCurve s_3 = repSpline_3.Curve();
		TopoDS_Edge spline_3 = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(s_3).shape();
		
		// Display various other properties
		GProp_GProps property = new GProp_GProps(); // store measurements
		
		// If Eps is argument is absent, precision is quite poor
		BRepGProp.linearProperties(spline_1, property);
		System.out.println("spline 1 length = " + property.mass());
		BRepGProp.linearProperties(spline_2, property);
		System.out.println("spline 2 length = " + property.mass());
		BRepGProp.linearProperties(spline_3, property);
		System.out.println("spline 3 length = " + property.mass());

		// Loft
		
		
		BRepOffsetAPI_ThruSections loft = new BRepOffsetAPI_ThruSections();
		
		
		TopoDS_Vertex vertex1 = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
				new double[]{ -10, 15, 0 }
				).shape();
		
//		loft.addVertex(vertex1);
		
		// make wires
		BRepBuilderAPI_MakeWire wire_1 = new BRepBuilderAPI_MakeWire();
		wire_1.add(new TopoDS_Shape[]{spline_1});
		BRepBuilderAPI_MakeWire wire_2 = new BRepBuilderAPI_MakeWire();
		wire_2.add(new TopoDS_Shape[]{spline_2});
		BRepBuilderAPI_MakeWire wire_3 = new BRepBuilderAPI_MakeWire();
		wire_3.add(new TopoDS_Shape[]{spline_3});
		
		// add wires to loft structure
		loft.addWire((TopoDS_Wire)wire_1.shape());
		loft.addWire((TopoDS_Wire)wire_2.shape());
		loft.addWire((TopoDS_Wire)wire_3.shape());
		
		loft.build();

		BRepGProp.surfaceProperties(loft.shape(), property);
		System.out.println("Loft surface area = " + property.mass());
		
		//Put everything in a compound
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound=new TopoDS_Compound();
		builder.makeCompound(compound);
		builder.add(compound, spline_1);
		builder.add(compound, spline_2);
		builder.add(compound, spline_3);
//		builder.add(compound, loft.shape());
		
		BRepOffsetAPI_MakeFilling filled = new BRepOffsetAPI_MakeFilling();
		
		TopExp_Explorer faceExplorer = new TopExp_Explorer(loft.shape(), TopAbs_ShapeEnum.FACE);
		TopoDS_Face loftFace = (TopoDS_Face) faceExplorer.current();
		
		// finds Edges in Face
		TopExp_Explorer edgeExplorer = new TopExp_Explorer(loftFace, TopAbs_ShapeEnum.EDGE);
		for ( ; edgeExplorer.more(); edgeExplorer.next())
		{
			TopoDS_Edge anEdge = (TopoDS_Edge) edgeExplorer.current();
			if (!BRep_Tool.degenerated(anEdge))
			{
				System.out.println("REGULAR Edge!");
				filled.add(anEdge, GeomAbs_Shape.C0);
			}
			else 
			{
				System.out.println("DEGENERATE Edge!");				
			}
		}
		
		// Constrain to a point off-the initial surface
		double[] pointConstraint = new double[]{ 35, 10, 0 };
		
		TopoDS_Vertex vertexConstraint = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
				pointConstraint
				).shape();
		builder.add(compound, vertexConstraint);

		filled.add(pointConstraint); // constraint
		
		filled.loadInitSurface(loftFace);

//		// get u, v coords of loftFace
//		double[] 
//				uMin = new double[]{ -1 }, 
//				uMax = new double[]{ -1 }, 
//				vMin = new double[]{ -1 },
//				vMax = new double[]{ -1 };
//		
//		BRepTools.uvBounds(
//				loftFace, 
//				uMin, uMax, 
//				vMin, vMax
//		); 
//		System.out.println("uMin = " + uMin[0] + "\tuMax = " + uMax[0]);
//		System.out.println("vMin = " + vMin[0] + "\tvMax = " + vMax[0]);
//
//		for (int i = 1; i < 10; i++)
//		{
//			for (int j = 1; j < 10; j++) {
////				filled.add(
////						uMin[0] + i*(uMax[0]-uMin[0])/10,
////						vMin[0] + j*(vMax[0]-vMin[0])/10,
////						loftFace, GeomAbs_Shape.C1);
//			}		
//		}
		
		filled.build();
		System.out.println("Deformed surface is done? = " + filled.isDone());
		
//		TopoDS_Shape[] tds = filled.Generated(loftFace);
//		System.out.println("tds size = " + tds.length);
	
		BRepGProp.surfaceProperties(filled.shape(), property);
		System.out.println("Deformed Loft surface area = " + property.mass());
		
		
		// finds Edges in new Face
		TopExp_Explorer edgeExplorer2 = new TopExp_Explorer(filled.shape(), TopAbs_ShapeEnum.EDGE);
		for ( ; edgeExplorer2.more(); edgeExplorer2.next())
		{
			TopoDS_Edge anEdge2 = (TopoDS_Edge) edgeExplorer2.current();
			if (!BRep_Tool.degenerated(anEdge2))
			{
				System.out.println("... REGULAR Edge!");
				BRepBuilderAPI_MakeWire wireEdge2 = new BRepBuilderAPI_MakeWire();
				wireEdge2.add(new TopoDS_Shape[]{anEdge2});
				builder.add(compound, (TopoDS_Wire)wireEdge2.shape());
			}
			else 
			{
				System.out.println("DEGENERATE Edge!");				
			}
		}

//		builder.add(compound, filled.shape());
		
//		// get points on the deformed surface
//		BRepTools.uvBounds(
//				(TopoDS_Face) filled.shape(), 
//				uMin, uMax, 
//				vMin, vMax
//				); 
//		System.out.println("[Deformed surface]");
//		System.out.println("uMin = " + uMin[0] + "\tuMax = " + uMax[0]);
//		System.out.println("vMin = " + vMin[0] + "\tvMax = " + vMax[0]);

		TopoDS_Face filledFace = (TopoDS_Face) filled.shape();


		// Try some plating, http://opencascade.blogspot.it/2010/03/surface-modeling-part6.html
		System.out.println("Plating ...");
		
		BRepFill_Filling plate = new BRepFill_Filling();
		plate.loadInitSurface(loftFace);

		// finds Edges in supporting Face borders and add to the plate
		TopExp_Explorer edgeExplorer3 = new TopExp_Explorer(loft.shape(), TopAbs_ShapeEnum.EDGE);
		for ( ; edgeExplorer3.more(); edgeExplorer3.next())
		{
			TopoDS_Edge anEdge3 = (TopoDS_Edge) edgeExplorer3.current();
			if (!BRep_Tool.degenerated(anEdge3))
			{
				System.out.println("... REGULAR Edge!");
				// BRepBuilderAPI_MakeWire wireEdge3 = new BRepBuilderAPI_MakeWire();
				
				// add edge as a constraint to the plate of type BRepFill_Filling
				plate.add(anEdge3, GeomAbs_Shape.C0);
			}
			else 
			{
				System.out.println("DEGENERATE Edge!");				
			}
		}
		
		// point/vertex as a new constraint
		double[] pointConstraint2 = new double[]{ 35, 10, -9 };
		TopoDS_Vertex vertexConstraint2 = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
				pointConstraint2
				).shape();
		builder.add(compound, vertexConstraint2);

		// constrain to pass through the 2nd spline
//		plate.add(spline_2, loftFace, GeomAbs_Shape.C0); // not working

		double[] pOnCurv1 = new double[]{15, 10, 0};
		GeomAPI_ProjectPointOnCurve pointOnCurve1 = new GeomAPI_ProjectPointOnCurve();
		pointOnCurve1.init(pOnCurv1, s_2);
		double[] pointConstraint2a = pointOnCurve1.nearestPoint(); 
		TopoDS_Vertex vertexConstraint2a = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
				pointConstraint2a
				).shape();
		builder.add(compound, vertexConstraint2a);

		double[] pOnCurv2 = new double[]{15, 10+3, 0};
		GeomAPI_ProjectPointOnCurve pointOnCurve2 = new GeomAPI_ProjectPointOnCurve();
		pointOnCurve2.init(pOnCurv2, s_2);
		double[] pointConstraint2b = pointOnCurve2.nearestPoint(); 
		TopoDS_Vertex vertexConstraint2b = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
				pointConstraint2b
				).shape();
		builder.add(compound, vertexConstraint2b);

		
		double[] pOnCurv3 = new double[]{15, 10-3, 0};
		GeomAPI_ProjectPointOnCurve pointOnCurve3 = new GeomAPI_ProjectPointOnCurve();
		pointOnCurve3.init(pOnCurv3, s_2);
		double[] pointConstraint2c = pointOnCurve3.nearestPoint(); 
		TopoDS_Vertex vertexConstraint2c = (TopoDS_Vertex) new BRepBuilderAPI_MakeVertex(
				pointConstraint2c
				).shape();
		builder.add(compound, vertexConstraint2c);
		
		
		plate.add(pointConstraint2);
		plate.add(pointConstraint2a);
		plate.add(pointConstraint2b);
		plate.add(pointConstraint2c);
		
		plate.build();
		System.out.println(plate.isDone());
		
		TopoDS_Face filledFace2 = plate.face();
		System.out.println(
				"face --> " 
				+ filledFace2
				);
		
		BRepGProp.surfaceProperties(filledFace2, property);
		System.out.println("Plated surface = " + property.mass());

		builder.add(compound, filledFace2);
		
		
		
		
//		org.jcae.opencascade.Utilities.dumpTopology(compound2, System.out);
		//Write to to a file
		BRepTools.write(compound, "test/test03a.brep");
		
	}

}
