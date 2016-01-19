package sandbox.adm;

import org.jcae.opencascade.jni.BRepBuilderAPI_MakeEdge;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeFace;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeVertex;
import org.jcae.opencascade.jni.BRepBuilderAPI_MakeWire;
import org.jcae.opencascade.jni.BRepGProp;
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
 * Try to build a NURB surface
 * 
 * see: OccJava : TopoJunction.java
 */

public class MyTest_ADM_02 {

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
	
	public MyTest_ADM_02() {
		
		// The plate
		double[] p1=new double[]{0, 0, 0};
		double[] p2=new double[]{0, 1, 0};
		double[] p3=new double[]{1, 1, 0};
		double[] p4=new double[]{1, 0, 0};

		double[] p5=new double[]{1, -1, 0.2};
		double[] p6=new double[]{0, -1, 0.2};
		
		TopoDS_Edge edge1=createEdge(createVertex(p1), createVertex(p2)); // <-- see this easy call
		TopoDS_Edge edge2=(TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p2,p3).shape();
		TopoDS_Edge edge3=(TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p3,p4).shape();
		TopoDS_Edge edge4=(TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p4,p1).shape();

		TopoDS_Edge edge5=(TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p4,p5).shape();
		TopoDS_Edge edge6=(TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p5,p6).shape();
		TopoDS_Edge edge7=(TopoDS_Edge) new BRepBuilderAPI_MakeEdge(p6,p1).shape();
		
		BRepBuilderAPI_MakeWire mw1 = new BRepBuilderAPI_MakeWire();
		mw1.add(edge1);
		mw1.add(edge2);
		mw1.add(edge3);
		mw1.add(edge4);
		TopoDS_Wire wirePlate1 = (TopoDS_Wire) mw1.shape();
		
		TopoDS_Face face1=(TopoDS_Face) new BRepBuilderAPI_MakeFace(
				wirePlate1, 
				false // true: OnlyPlane
				).shape();

		BRepBuilderAPI_MakeWire mw2 = new BRepBuilderAPI_MakeWire();
		mw2.add(edge4);
		mw2.add(edge5);
		mw2.add(edge6);
		mw2.add(edge7);
		TopoDS_Wire wirePlate2 = (TopoDS_Wire) mw2.shape();
		
		TopoDS_Face face2=(TopoDS_Face) new BRepBuilderAPI_MakeFace(
				wirePlate2, 
				false // true: OnlyPlane
				).shape();
		
		//Put everything in a compound
		BRep_Builder bb=new BRep_Builder();
		TopoDS_Compound compound=new TopoDS_Compound();
		bb.makeCompound(compound);
		bb.add(compound, face1);
		bb.add(compound, face2);
		
		org.jcae.opencascade.Utilities.dumpTopology(compound, System.out);
		//Write to to a file
		BRepTools.write(compound, "test/test02.brep");
		
		////////////////////////////////////////////////////////////////////////////
		// Display various other properties
		GProp_GProps property=new GProp_GProps();
		// If Eps is argument is absent, precision is quite poor
		BRepGProp.linearProperties(face1, property);
		System.out.println("linear face 1 = "+property.mass());
		// If Eps is argument is absent, precision is quite poor
		BRepGProp.surfaceProperties(face1, property, 1.e-2);
		System.out.println("surface face 1 = "+property.mass());

		// If Eps is argument is absent, precision is quite poor
		BRepGProp.linearProperties(face2, property);
		System.out.println("linear face 2 = "+property.mass());
		// If Eps is argument is absent, precision is quite poor
		BRepGProp.surfaceProperties(face2, property, 1.e-2);
		System.out.println("surface face 2 = "+property.mass());
		
		
		////////////////////////////////////////////////////////////////////////////

		//test with spline
				//		double[] p1 = new double[]{0,0,0};
				//		double[] p2 = new double[]{0,10,0};
				//		double[] p3 = new double[]{10,10,0};

		double[] points = new double[]{0,0,0,0,10,0,10,10,0};

		GeomAPI_Interpolate repSpline = new GeomAPI_Interpolate(points, false, 1E-7);
		repSpline.Perform();
		Geom_BSplineCurve s = repSpline.Curve();
		TopoDS_Edge spline = (TopoDS_Edge) new BRepBuilderAPI_MakeEdge(s).shape();
		
		// Display various other properties
		GProp_GProps property2=new GProp_GProps();
		// If Eps is argument is absent, precision is quite poor
		BRepGProp.linearProperties(spline, property);
		System.out.println("spline length = "+property.mass());
		
		//Put everything in a compound
		BRep_Builder bb2=new BRep_Builder();
		TopoDS_Compound compound2=new TopoDS_Compound();
		bb2.makeCompound(compound2);
		bb2.add(compound2, spline);
		
		org.jcae.opencascade.Utilities.dumpTopology(compound2, System.out);
		//Write to to a file
		BRepTools.write(compound2, "test/test02a.brep");
		
	}
}
