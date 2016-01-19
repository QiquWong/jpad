package sandbox.adm;

import org.jcae.opencascade.jni.BRepBuilderAPI_MakeEdge;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeFace;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeVertex;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeWire;
import org.jcae.opencascade.jni.BRepGProp;
import org.jcae.opencascade.jni.BRepPrimAPI_MakePrism;
import org.jcae.opencascade.jni.BRepTools;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.GProp_GProps;
import org.jcae.opencascade.jni.GeomAPI_Interpolate;
import org.jcae.opencascade.jni.Geom_BSplineCurve;
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


public class MyTest_ADM_03Splines {

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
	
	public MyTest_ADM_03Splines() {
		
		
		////////////////////////////////////////////////////////////////////////////

		//test with spline
				//		double[] p1 = new double[]{0,0,0};
				//		double[] p2 = new double[]{0,10,0};
				//		double[] p3 = new double[]{10,10,0};

		double[] points = new double[]{
				0,0,0,
				0,10,0,
				10,10,0,
				15,10,0
				};

		GeomAPI_Interpolate repSpline = new GeomAPI_Interpolate(
				points, // list of points
				false, // periodic or non-periodic 
				1E-7 // tolerance
				);
		repSpline.Perform();
		Geom_BSplineCurve s = repSpline.Curve();
		TopoDS_Edge spline = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(s).shape();
		
		// Extrude
		
		TopoDS_Face face = (TopoDS_Face) new BRepPrimAPI_MakePrism(
				spline, new double[]{0, 0, 15}).shape();
		
		// Display various other properties
		GProp_GProps property = new GProp_GProps(); // store measurements
		
		// If Eps is argument is absent, precision is quite poor
		BRepGProp.linearProperties(spline, property);
		System.out.println("spline length = " + property.mass());

		BRepGProp.surfaceProperties(face, property);
		System.out.println("face area = " + property.mass());
		
		
		//Put everything in a compound
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound=new TopoDS_Compound();
		builder.makeCompound(compound);
		builder.add(compound, spline);
		builder.add(compound, face);
		
//		org.jcae.opencascade.Utilities.dumpTopology(compound2, System.out);
		//Write to to a file
		BRepTools.write(compound, "test/test03.brep");
		
	} // end of constructor


}
